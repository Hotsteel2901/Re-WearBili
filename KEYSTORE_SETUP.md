# Release 签名与 GitHub Secrets 配置说明

本文件说明 Re-WearBili 的 release 签名机制，以及如何在 GitHub Actions 中配置。

> ⚠️ **安全提醒**：`keystore/` 目录已被 `.gitignore` 忽略。
> 请务必备份 `keystore/rewearbili.jks` —— **一旦丢失，将无法再向已发布的应用推送更新**（签名不匹配，用户必须卸载重装）。

---

## 1. 已生成的密钥库

| 项 | 值 |
|---|---|
| 文件 | `keystore/rewearbili.jks` |
| 类型 | PKCS12 |
| 别名 (alias) | `hotsteel` |
| 密钥库密码 (storePassword) | `hotsteel` |
| 别名密码 (keyPassword) | `hotsteel` |
| 算法 | RSA 2048 |
| 有效期 | 10950 天（至 2056-09-26） |
| SHA-1 | `F6:8C:1E:8F:71:27:D2:48:25:46:9F:A4:4C:CD:0C:2B:04:4F:FA:E0` |
| SHA-256 | `AB:9F:DE:C9:9A:FA:30:2D:C1:C6:04:06:13:EB:2A:3A:D9:BF:06:23:2B:92:5D:38:75:A8:38:63:49:8E:37:97` |

---

## 2. 签名配置的优先级（`app/build.gradle.kts`）

```
环境变量（CI 优先）
    ↓ 未设置则读
keystore/keystore.properties（本地开发）
    ↓ 都没有则
回落到 debug 签名（构建不中断，但产物不可发布）
```

**环境变量名**：

| 变量 | 含义 |
|---|---|
| `KEYSTORE_PATH` | keystore 文件路径（相对项目根目录，如 `keystore/rewearbili.jks`） |
| `KEYSTORE_PASSWORD` | 密钥库密码 |
| `KEY_ALIAS` | 别名 |
| `KEY_PASSWORD` | 别名密码 |

本地开发无需配置环境变量 —— `keystore/keystore.properties` 已写好。

---

## 3. GitHub Secrets 配置步骤

进入仓库 → **Settings** → **Secrets and variables** → **Actions** → **New repository secret**，依次添加以下 4 个：

### 3.1 `KEYSTORE_BASE64`

把 keystore 文件转成 Base64（单行，不要换行）：

```bash
# Linux / macOS
base64 -w0 keystore/rewearbili.jks

# macOS（-w 参数不支持时）
base64 -i keystore/rewearbili.jks | tr -d '\n'

# Windows PowerShell
[Convert]::ToBase64String([IO.File]::ReadAllBytes("keystore\rewearbili.jks"))
```

把输出（`keystore/rewearbili.jks.b64` 的内容）**整行**粘贴为 secret 值。

> 也可以直接复制仓库内已生成的 `keystore/rewearbili.jks.b64` 文件内容。

### 3.2 `KEYSTORE_PASSWORD`

```
hotsteel
```

### 3.3 `KEY_ALIAS`

```
hotsteel
```

### 3.4 `KEY_PASSWORD`

```
hotsteel
```

---

## 4. 工作流中的使用方式

`.github/workflows/android.yml` 的 release 步骤会：

1. 把 `KEYSTORE_BASE64` 解码写回 `keystore/rewearbili.jks`
2. 注入其余 3 个密码为环境变量
3. 执行 `./gradlew assembleRelease`

关键片段：

```yaml
- name: Decode Keystore
  if: env.KEYSTORE_BASE64 != ''
  env:
    KEYSTORE_BASE64: ${{ secrets.KEYSTORE_BASE64 }}
  run: |
    mkdir -p keystore
    echo "$KEYSTORE_BASE64" | base64 -d > keystore/rewearbili.jks
    ls -la keystore/

- name: Build Release APK
  env:
    KEYSTORE_PATH: keystore/rewearbili.jks
    KEYSTORE_PASSWORD: ${{ secrets.KEYSTORE_PASSWORD }}
    KEY_ALIAS: ${{ secrets.KEY_ALIAS }}
    KEY_PASSWORD: ${{ secrets.KEY_PASSWORD }}
  run: ./gradlew assembleRelease --stacktrace
```

---

## 5. 验证签名是否正确

构建完成后，用 `apksigner` 校验产物：

```bash
# 找 apksigner（Android SDK build-tools 目录下）
$ANDROID_HOME/build-tools/36.0.0/apksigner verify --print-certs \
  "app/build/outputs/apk/release/Re-WearBili - Atlas 阿特拉斯 Ver.3 Rel.47.apk"
```

输出中的 `SHA-256 digest` 应与上表一致。

---

## 6. 上传到应用商店时的附加信息

部分平台需要 keystore 证书指纹：

| 指纹类型 | 值 |
|---|---|
| SHA-1 | `F6:8C:1E:8F:71:27:D2:48:25:46:9F:A4:4C:CD:0C:2B:04:4F:FA:E0` |
| SHA-256 | `AB:9F:DE:C9:9A:FA:30:2D:C1:C6:04:06:13:EB:2A:3A:D9:BF:06:23:2B:92:5D:38:75:A8:38:63:49:8E:37:97` |

如需接入需要 SHA-1 指纹的第三方 SDK（如某些地图、推送服务），也使用上表的 SHA-1。
