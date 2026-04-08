# OFD 转 PDF

一个面向日常使用的桌面工具，用来把 `.ofd` 文件批量转换成 `.pdf`。

项目基于 [`ofdrw`](https://github.com/ofdrw/ofdrw) + JavaFX，提供已经打包好的安装程序，普通用户安装后即可直接使用，不需要自己配置 Java 环境。

![应用截图](screenshot.png)

## 适合谁用

- 需要把 OFD 文件转成 PDF 再发给别人
- 手里有一批 OFD 文件，想一次性处理
- 不想折腾命令行，只想点开就能用

## 主要功能

- 支持选择一个或多个 `.ofd` 文件
- 支持导入整个文件夹
- 支持递归扫描文件夹中的 `.ofd` 文件
- 支持拖拽文件或文件夹到窗口中
- 默认在原文件旁边生成同名 `.pdf`
- 支持指定统一的输出目录
- 支持批量转换

## 下载安装

请到 [GitHub Releases](https://github.com/akb21/ofd2pdf/releases) 页面下载与你系统对应的安装包。

当前提供的打包程序：

- Windows x64：`.exe`
- Linux x64：`.deb`
- Linux arm64：`.deb`

常见文件名示例：

- `ofd2pdf-0.1.5-windows-x64-20260406.exe`
- `ofd2pdf-0.1.5-linux-x64-20260406.deb`
- `ofd2pdf-0.1.5-linux-arm64-20260406.deb`

说明：

- 打包程序内置运行时，安装后可直接启动
- 一般不需要额外安装 Java

## 怎么使用

1. 安装并启动 `OFD 转 PDF`
2. 通过下面任一方式加入文件：
   - 点击界面按钮选择 `.ofd` 文件
   - 选择一个目录批量导入
   - 直接把文件或文件夹拖进窗口
3. 选择是否使用默认输出位置
4. 开始转换
5. 在输出目录中查看生成的 `.pdf`

默认输出规则：

- 不改输出目录时，PDF 会生成在原 `.ofd` 文件所在目录
- 生成文件名通常与原文件同名，只是扩展名变成 `.pdf`

## 平台说明

### Windows

- 下载 `.exe` 安装包
- 安装完成后可从开始菜单启动

### Linux

- Debian / Ubuntu 及兼容发行版可直接安装 `.deb`
- 安装后可从应用菜单启动

## 常见问题

### 1. 安装包里为什么这么大？

因为打包程序带了运行时环境。好处是开箱即用，不要求用户自己安装 Java。

### 2. 转换后的 PDF 会放到哪里？

默认放在原 OFD 文件旁边；如果你在界面里设置了输出目录，就会统一输出到你指定的位置。

### 3. 能一次处理很多文件吗？

可以。这个工具支持多文件和整目录导入，也支持递归扫描。

## 许可证

本项目源码采用 Apache License 2.0：

[LICENSE](LICENSE)

附加免责声明：

[DISCLAIMER.md](DISCLAIMER.md)

第三方依赖 [`ofdrw`](https://github.com/ofdrw/ofdrw) 的许可证文本：

[packaging/assets/ofdrw_LICENSE](packaging/assets/ofdrw_LICENSE)

## 开发与构建

如果你只是想使用软件，可以跳过这一节。

### 本地运行

需要：

- OpenJDK 25 或更高版本

运行：

```bash
./gradlew run
```

Windows：

```bat
gradlew.bat run
```

### 测试

```bash
./gradlew test
```

### 打包

Windows 安装包：

```bash
./gradlew packageWindows
```

Linux `.deb`：

```bash
./gradlew packageDeb
```

打包输出目录：

```text
build/jpackage/package/
```
