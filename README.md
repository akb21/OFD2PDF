# OFD 转 PDF

基于 `ofdrw + JavaFX` 的桌面版 `OFD -> PDF` 转换工具。

当前特性：

- 支持选择一个或多个 `.ofd` 文件
- 支持选择文件夹导入 `.ofd` 文件
- 支持递归扫描文件夹中的 `.ofd` 文件
- 支持拖拽 `.ofd` 文件和文件夹到界面中
- 默认在源文件同目录生成同名 `.pdf`
- 支持自定义输出目录
- 支持 Windows 和 Linux 打包

## 运行环境

- OpenJDK 25 或更高版本

## 本地运行

```bash
./gradlew run
```

Windows:

```bat
gradlew.bat run
```

## 运行测试

```bash
./gradlew test
```

## 打包

Windows 安装包：

```bash
./gradlew packageWindows
```

Linux `.deb`：

```bash
./gradlew packageDeb
```

说明：
- 安装包版本使用 `jpackage`，自带 Java 运行时，体积更大但开箱即用。


## GitHub Actions

仓库中的 GitHub Actions 会在推送 `v*` 标签时自动构建并发布 Release 资源：

- Windows x64 `.exe`
- Linux x64 `.deb`
- Linux arm64 `.deb`

产物文件名格式：

- `ofd2pdf-<version>-windows-x64-<yyyymmdd>.exe`
- `ofd2pdf-<version>-linux-x64-<yyyymmdd>.deb`
- `ofd2pdf-<version>-linux-arm64-<yyyymmdd>.deb`

输出目录：

```text
build/jpackage/package/
```

## 图标资源

当前已接入：

- `packaging/assets/app.ico`
- `packaging/assets/app.png`
- `packaging/assets/app.jpeg` 作为图标源文件

窗口图标资源位于：

```text
src/main/resources/icons/app.png
```

## 发布标识

- 应用名：`ofd2pdf`
- 显示名称：`OFD 转 PDF`
- Gradle Group：`info.akb21`
- 主包名：`info.akb21.ofd2pdf`

## 许可证

本项目源码采用 Apache License 2.0，见根目录：

[LICENSE](LICENSE)

附加免责声明见：

[DISCLAIMER.md](DISCLAIMER.md)

第三方依赖 `ofdrw` 的协议文本单独存放于：

[packaging/assets/ofdrw_LICENSE](packaging/assets/ofdrw_LICENSE)
