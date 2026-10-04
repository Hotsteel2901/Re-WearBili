<p align="center">
    <img src="https://repository-images.githubusercontent.com/625249285/2ce9fdfe-9ad8-46ec-9442-bfa69f268be1">
</p>


<h3 align="center">一个手表上的第三方Bilibili客户端，但是重制版</center>

## :watch:配置要求

- 最低内存(RAM)：512M
- 最低储存(ROM)：2G
- 最低兼容的系统：Android 5.0 (API Level 21 Lolipop)



- 推荐内存(RAM)：2G+
- 推荐储存：8G+
- 推荐系统：Android 8.0+ (API Level 26 Oreo)



## :rocket:下载

- 加入QQ群组`912493736`后在群文件处下载安装包。加入后请仔细阅读群公告中的内容。
- 你也可以自行clone该仓库并构建此项目。


##  :hammer_and_wrench: 构建

1. clone 本项目, 用你的 terminal 环境打开项目根目录
2. 输入 ```./gradlew build```
3. 在目录下的 ```build/libs/Re-WearBili - $versionName Ver.$releaseNumber Rel.$versionCode.apk``` 找到apk文件
4. 导入IDEA进行二次开发，或者将apk导入到你的手表使用

### :computer:我的构建环境：

```
Computer: Apple Macbook Pro 2021 M1 Pro 14 inch, 16GB RAM and 1TB ROM
Android Studio: Android Studio Iguana | 2023.2.1 Beta 2
AGP version: 8.2.0
Gradle version: 8.0
Java version: openjdk version "17.0.8.1" 2023-08-24
```

## :book:行为准则

见[行为准则](https://github.com/SpaceXC/Re-WearBili/blob/main/.github/files/CodeOfConduct.md)

## :page_facing_up:开源协议

- 这个项目使用GNU General Public License v3.0协议开源，详见[LICENSE.md](https://github.com/SpaceXC/Re-WearBili/blob/main/LICENSE)
- 这个项目的UI设计使用Creative Common 4.0协议共享。将会在日后正式公开。

## :memo:二改声明

> 本项目是 [SpaceXC/Re-WearBili](https://github.com/SpaceXC/Re-WearBili) 的二次开发（二改）版本，**二改作者：hotsteel**（[GitHub @HotSteel2901](https://github.com/HotSteel2901)）。

- **二改仓库**：[HotSteel2901/Re-WearBili](https://github.com/HotSteel2901/Re-WearBili)
- **主要改动**：
  - Material Design 3 Expressive 全面改版（含亮色主题修复）
  - 双端 UI 布局体系（手表端 / 手机端，首启可选、设置可切换）
  - 多渠道登录（扫码 / Cookie / 密码 / 短信）
  - 播放与浏览增强（续播 / 连播 / 倍速 / 跳过片头片尾 / 后台音频 / 广告过滤 / 长按菜单）
  - Haze 液态玻璃、Monet 动态取色与全套自绘图标
- 感谢原作者 [XC-Qan](https://github.com/SpaceXC) 与所有原项目贡献者。原项目版权归原作者所有，本二改版本遵循原项目 GPL-3.0 协议继续开源。
