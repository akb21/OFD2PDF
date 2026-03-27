# 打包说明

## 环境要求

- OpenJDK 25 或更高版本
- `jpackage` 已加入 `PATH`
- 必须在目标操作系统上执行对应打包任务

说明：
- Windows 安装包需要在 Windows 上构建
- Linux `.deb` 需要在 Debian 或 Ubuntu 类发行版上构建

## Gradle 任务

准备 `jpackage` 输入目录：

```bash
./gradlew prepareJpackageInput
```

构建 Windows 安装包：

```bash
./gradlew packageWindows
```

构建 Linux `.deb`：

```bash
./gradlew packageDeb
```

构建 Linux app image（`packageDeb` 复用它来打包）：

```bash
./gradlew packageAppImage
```

构建不带 Java 运行时的轻量版压缩包：

```bash
./gradlew packageNoRuntimeZip
```

## 输出目录

生成后的安装包位于：

```text
build/jpackage/package/
```

轻量版压缩包位于：

```text
build/package-no-runtime/
```

`jpackage` 中间输入文件位于：

```text
build/jpackage/input/
```

## 当前打包类型

Windows：
- `.exe`

Linux：
- `.deb`

轻量版：
- `.zip`

- `packageDeb` 现在会先运行 `packageAppImage` 生成 `app-image`、再复用它构建 `.deb`，同时用兼容形式写入 `Depends`（`libasound2 | libasound2t64` 等），避免旧版 Debian/Ubuntu 报缺失包的问题。
- `jpackage` 产物默认自带 Java 运行时。
- 轻量版不使用 `jpackage`，而是使用 Gradle `application` 分发包，因此要求目标机器已安装 `OpenJDK 25` 并可通过 `java` 命令启动。

## CI 发布

GitHub Actions 会在推送 `v*` 标签时自动构建并上传 Release 资源：

- Windows x64 `.exe`
- Linux x64 `.deb`
- Linux arm64 `.deb`

产物文件名格式：

- `ofd2pdf-<version>-windows-x64-<yyyymmdd>.exe`
- `ofd2pdf-<version>-linux-x64-<yyyymmdd>.deb`
- `ofd2pdf-<version>-linux-arm64-<yyyymmdd>.deb`

## 当前体积优化

打包任务在生成安装包前会先执行 `calculateRuntimeModules`，调用 `jdeps` 自动分析应用实际依赖的 JDK 模块，并将结果写入：

```text
build/jpackage/runtime-modules.txt
```

随后 `jpackage` 会基于这份最小模块清单构建运行时，并叠加 `jlink` 压缩参数继续裁剪无关内容：

- `--strip-debug`
- `--no-header-files`
- `--no-man-pages`
- `--strip-native-commands`
- `--compress=zip-6`

这两层优化主要减少随安装包分发的 Java 运行时体积，对业务功能没有影响。

## 当前安装包元数据

- 显示名称：`OFD 转 PDF`
- 厂商：`AKB21`
- Windows 菜单组：`AKB21`
- Linux 包名：`ofd2pdf`
- Linux 分类：`Office`

## 预期输出

Windows：
- `build/jpackage/package/ofd2pdf-<version>-windows-x64-<yyyymmdd>.exe`

Linux：
- `build/jpackage/package/ofd2pdf-<version>-linux-x64-<yyyymmdd>.deb`
- `build/jpackage/package/ofd2pdf-<version>-linux-arm64-<yyyymmdd>.deb`

轻量版：
- `build/package-no-runtime/ofd2pdf-<version>-no-runtime-<yyyymmdd>.zip`

## 建议验证项

- 启动打包后的应用
- 打开一个 `.ofd` 文件
- 验证是否在同目录生成 PDF
- 验证拖拽功能是否正常

## 图标资源

图标资源占位说明见：

```text
packaging/assets/README.md
```

## 第三方协议

`ofdrw` 的协议文本存放于：

```text
packaging/assets/ofdrw_LICENSE
```

打包时会将该文件复制到：

```text
build/jpackage/input/third-party-licenses/ofdrw_LICENSE
```

这样 `jpackage` 产物会一并包含这份协议文本，界面中不额外提供查看入口。
