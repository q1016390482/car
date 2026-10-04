# CarWith383Persist — API 102

这是一个实验性的 Modern LSPosed / libxposed API 102 模块。

## 目的

针对 `com.miui.carlink`：

- 使用原始、未修改的 CarWith 3.8.3 APK
- 不修改 APK 的 versionCode、versionName 或签名
- 在 system_server 启动时 Hook Android PackageManager 的 `checkDowngrade`
- 只对 `com.miui.carlink` 跳过 downgrade 检查
- 目标是让已安装的 3.8.3 system update 在重启后的 PackageManager reconciliation 中继续保留

## Modern API 102

模块使用：

- `io.github.libxposed:api:102.0.0`
- `META-INF/xposed/java_init.list`
- `META-INF/xposed/scope.list`
- `META-INF/xposed/module.prop`
- system_server scope: `system`

## GitHub Actions

进入：

Actions → Build CarWith383Persist API102 → Run workflow

成功后在 Artifacts 下载 `CarWith383Persist-API102`。

## 重要说明

这个模块针对 Android 16 / HyperOS 的实际行为做了定向 Hook，但 Xiaomi HyperOS 可能对 AOSP PackageManager 做额外修改，因此即使编译成功，也不能保证在所有 HyperOS 3 构建上都有效。

如果模块加载但重启后仍恢复 4.0.1，应根据 LSPosed/system_server 日志进一步定位 HyperOS 的持久化检查点。
