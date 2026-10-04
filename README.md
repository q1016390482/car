# CarWith383Persist — Modern libxposed API 102

针对 Xiaomi HyperOS 3.307 / Android 16 的实验性 LSPosed 模块。

目标：
- 不修改原版 CarWith 3.8.3 APK
- 只在 `android` / system_server 作用域工作
- 针对 `com.miui.carlink`
- 尝试避免 `/product/app/CarWith` 中 4.0.1 的版本比较结果覆盖 3.8.3

## GitHub Actions
上传项目全部文件到 GitHub 后：
Actions → Build CarWith383Persist API102 → Run workflow。
编译结果在 Artifacts。

## 重要
这是实验性代码，尚未在用户的 Xiaomi 14 / HyperOS 3.307 真机上验证。
如果目标类或方法不存在，会记录日志并跳过。
如果出现 system_server 异常，应在 LSPosed 中禁用本模块后重启。

Modern API 入口使用 `META-INF/xposed/java_init.list`，scope 使用 `scope.list`，配置使用 `module.prop`。
