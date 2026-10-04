#!/bin/bash
set -e
cd /workspace/Re-WearBili

# ---- 锚点 1：主题基建 + 前景色清理 ----
git add -A -- \
  ':(exclude)app/src/main/AndroidManifest.xml' \
  ':(exclude)app/src/main/java/cn/spacexc/wearbili/remake/app/MainActivity.kt' \
  ':(exclude)app/src/main/java/cn/spacexc/wearbili/remake/app/login/LoginViewModel.kt' \
  ':(exclude)app/build.gradle.kts'
git commit -m "fix(theme): 前景色系统基建重做——暗色黑字/亮色白字根修

- WearBiliTheme 根部同时提供 M2/M3 两套 LocalContentColor = onSurface：
  此前 M3 MaterialTheme 只经 Surface 提供 M3 Local，M2 Text/Icon 与
  未显式指定颜色的组件回落 Compose 默认 Color.Black，暗色下黑字黑底
- 清理 31 文件共 116 处残留 color/tint = Color.White → onSurface
- 保留彩底/媒体层白字：粉底角标、追番按钮、番剧 badge、私信气泡、
  封面时长标签、播放器/弹幕/图片查看器/直播/歌词/Toast/崩溃页
- 顺带修复 VideoCard 非 Large 版 badge 误用 onSurfaceVariant"

# ---- 锚点 2：窗口层（uiMode + 手机沉浸式）----
git add app/src/main/AndroidManifest.xml \
  app/src/main/java/cn/spacexc/wearbili/remake/app/MainActivity.kt
git commit -m "fix(window): configChanges 补 uiMode，手机布局隐藏状态栏

- Manifest configChanges 追加 uiMode：系统深浅色切换不再触发 Activity
  重建（切的一瞬间崩溃的首要根因），Compose LocalConfiguration 自动
  更新，主题即时重组生效
- MainActivity 按布局规格响应式隐藏状态栏（手机布局沉浸式，下拉可
  临时呼出）：修复顶部工具条图标被状态栏遮挡无法点击；手表布局不受
  影响，切换布局时自动恢复/隐藏"

# ---- 锚点 3：短信凭证 fallback + 版本号 ----
git add app/src/main/java/cn/spacexc/wearbili/remake/app/login/LoginViewModel.kt \
  app/build.gradle.kts
git commit -m "fix(login): 短信登录凭证缺失时回退 url 解析并透传诊断；bump v50

- 通用登录 envelope 的 cookie_info 为空时回退 parseCookiesFromUrl
  （与扫码 poll 同构，TV 响应可能仅带 crossDomain url）
- 仍无凭证时透传 token/refresh/url 三项存在性到错误信息，便于真机
  定位 TV OAuth2 体系下缺什么；refreshToken 非空时先行存档
- versionCode 49 → 50（Rel.5）"

git log --oneline -4
git verify-commit HEAD HEAD~1 HEAD~2 2>/dev/null; git log --show-signature -1 --format="%h %G? %s" HEAD
