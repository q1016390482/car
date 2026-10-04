package com.carwith383.persist;

import io.github.libxposed.api.XposedInterface;
import io.github.libxposed.api.XposedModule;
import io.github.libxposed.api.XposedModuleInterface;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Keeps the installed original CarWith 3.8.3 system update from being
 * rejected as a version downgrade during PackageManager boot reconciliation.
 *
 * Android 16 AOSP performs this check in:
 * com.android.server.pm.PackageManagerServiceUtils#checkDowngrade(...)
 *
 * We only bypass the check when the package involved is com.miui.carlink.
 * The APK itself is not modified.
 */
public class Hook extends XposedModule {
    private static final String TARGET_PACKAGE = "com.miui.carlink";
    private static final String PMS_UTILS = "com.android.server.pm.PackageManagerServiceUtils";

    @Override
    public void onModuleLoaded(XposedModuleInterface.ModuleLoadedParam param) {
        if (!param.isSystemServer()) {
            return;
        }
        log("CarWith383Persist API102: system_server loaded");
    }

    @Override
    public void onSystemServerStarting(XposedModuleInterface.SystemServerStartingParam param) {
        try {
            ClassLoader cl = param.getClassLoader();
            Class<?> cls = Class.forName(PMS_UTILS, false, cl);

            int count = 0;
            for (Method m : cls.getDeclaredMethods()) {
                if (!"checkDowngrade".equals(m.getName())) {
                    continue;
                }

                try {
                    deoptimize(m);
                } catch (Throwable t) {
                    log("deoptimize skipped: " + m);
                }

                try {
                    hook(m)
                        .setExceptionMode(XposedInterface.ExceptionMode.PROTECTIVE)
                        .intercept(new DowngradeHooker());
                    count++;
                    log("Hooked " + m);
                } catch (Throwable t) {
                    log("Hook failed " + m + " : " + t);
                }
            }

            log("CarWith383Persist: checkDowngrade hooks installed=" + count);
        } catch (Throwable t) {
            log("CarWith383Persist: system_server hook setup failed: " + t);
        }
    }

    private static final class DowngradeHooker implements XposedInterface.Hooker {
        @Override
        public Object intercept(XposedInterface.Chain chain) throws Throwable {
            try {
                List<Object> args = chain.getArgs();

                // Android 16's checkDowngrade has the package being replaced
                // as the first argument. Depending on the ROM branch it is
                // PackageSetting or AndroidPackage.
                if (!args.isEmpty() && isCarWithPackage(args.get(0))) {
                    return null; // void method: skip the downgrade exception
                }
            } catch (Throwable ignored) {
                // Fail open: if we cannot positively identify CarWith,
                // execute the original PackageManager check.
            }

            return chain.proceed();
        }
    }

    private static boolean isCarWithPackage(Object pkg) {
        if (pkg == null) return false;

        try {
            Method m = findNoArg(pkg.getClass(), "getPackageName");
            if (m != null) {
                Object name = m.invoke(pkg);
                return TARGET_PACKAGE.equals(String.valueOf(name));
            }
        } catch (Throwable ignored) {}

        try {
            Method m = findNoArg(pkg.getClass(), "getPkg");
            if (m != null) {
                Object inner = m.invoke(pkg);
                if (inner != null) {
                    Method pn = findNoArg(inner.getClass(), "getPackageName");
                    if (pn != null) {
                        return TARGET_PACKAGE.equals(String.valueOf(pn.invoke(inner)));
                    }
                }
            }
        } catch (Throwable ignored) {}

        return false;
    }

    private static Method findNoArg(Class<?> cls, String name) {
        for (Class<?> c = cls; c != null; c = c.getSuperclass()) {
            try {
                Method m = c.getDeclaredMethod(name);
                m.setAccessible(true);
                return m;
            } catch (Throwable ignored) {}
        }

        try {
            Method m = cls.getMethod(name);
            m.setAccessible(true);
            return m;
        } catch (Throwable ignored) {}

        return null;
    }
}
