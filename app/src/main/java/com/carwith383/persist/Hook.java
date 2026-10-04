package com.carwith383.persist;

import io.github.libxposed.api.XposedModule;
import io.github.libxposed.api.XposedModuleInterface;
import io.github.libxposed.api.XposedInterface;
import java.lang.reflect.Method;

public class Hook extends XposedModule {
    private static final String PKG = "com.miui.carlink";
    private static final long TARGET = 103008003L;

    @Override
    public void onModuleLoaded(XposedModuleInterface.ModuleLoadedParam param) {
        if (!"android".equals(param.getPackageName())) return;

        log("CarWith383Persist API102: loaded into android/system_server");

        String[] candidates = {
            "com.android.server.pm.parsing.pkg.PackageImpl",
            "com.android.server.pm.pkg.AndroidPackage"
        };

        for (String cn : candidates) {
            try {
                Class<?> c = Class.forName(cn, false, param.getClassLoader());
                hook(c);
                log("Hook candidate: " + cn);
            } catch (Throwable t) {
                log("Candidate unavailable: " + cn + " : " + t.getClass().getSimpleName());
            }
        }
    }

    private void hook(Class<?> c) {
        for (Method m : c.getDeclaredMethods()) {
            String n = m.getName();
            if (!n.equals("getLongVersionCode") && !n.equals("getVersionCode")) continue;
            if (m.getParameterTypes().length != 0) continue;

            try {
                m.setAccessible(true);
                hook(m, new VersionHooker());
                log("Hooked " + c.getName() + "#" + n);
            } catch (Throwable t) {
                log("Failed hook " + c.getName() + "#" + n + " : " + t);
            }
        }
    }

    private static class VersionHooker implements XposedInterface.Hooker<Method> {
        @Override
        public Object intercept(XposedInterface.Hooker.Chain<Method> chain) throws Throwable {
            Object result = chain.proceed();

            try {
                Object self = chain.getThisObject();
                Class<?> c = self.getClass();

                Method pn = find(c, "getPackageName");
                Method cp = find(c, "getPath");

                String name = pn == null ? null : String.valueOf(pn.invoke(self));
                if (!PKG.equals(name)) return result;

                String path = cp == null ? null : String.valueOf(cp.invoke(self));
                if (path == null || !path.contains("/product/app/CarWith")) return result;

                Method hooked = chain.getMethod();
                if ("getLongVersionCode".equals(hooked.getName())) {
                    return TARGET - 1;
                }
                if ("getVersionCode".equals(hooked.getName())) {
                    return (int)(TARGET - 1);
                }
            } catch (Throwable ignored) {}

            return result;
        }
    }

    private static Method find(Class<?> c, String name) {
        try { return c.getMethod(name); } catch (Throwable ignored) {}
        try {
            Method m = c.getDeclaredMethod(name);
            m.setAccessible(true);
            return m;
        } catch (Throwable ignored) {}
        return null;
    }
}
