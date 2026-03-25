# 打包资源说明

打包相关图标资源统一放在此目录。

建议文件：

- `app.ico`
  - 用于 Windows 安装包与快捷方式图标
- `app.png`
  - 用于 Linux 打包元数据及应用图标

建议尺寸：

- Windows `.ico`
  - 建议包含 16、32、48、64、128、256 多个尺寸
- Linux `.png`
  - 建议使用 256x256 或 512x512

当前 `build.gradle` 已经支持：

- Windows 使用 `packaging/assets/app.ico`
- Linux 使用 `packaging/assets/app.png`
