# Publishing a release

A release is built, signed, checked and published by GitHub Actions (`.github/workflows/release.yml`) when a version tag
such as `v1.0.0` is pushed. The result is a public page under **Releases** with the APK attached.

## One-time setup

### 1. Create your signing key (on your own computer, never in the repository)
The key proves that updates really come from you. Keep the file **outside** the project folder.

    & "C:\Program Files\Android\Android Studio\jbr\bin\keytool.exe" -genkeypair -v -keystore "$env:USERPROFILE\dayflow-release.jks" -alias dayflow -keyalg RSA -keysize 2048 -validity 10000

It asks for a password (twice), your name and a few details, then "Is this correct?" (type `yes`), then a key password
(press Enter to reuse the same password). Nothing appears while you type a password; that is normal.

**Back up `dayflow-release.jks` and its password in two separate safe places** (for example a USB stick and your own
cloud storage). If the key or password is lost, no future update can be installed over existing copies of the app: every
user would have to uninstall first and lose their reminders. Never email it, never commit it, never paste it in a chat.
`.gitignore` already blocks `*.jks`, `*.keystore` and `keystore.properties`.

### 2. Give the key to GitHub as four encrypted secrets
Repository on GitHub: **Settings > Secrets and variables > Actions > New repository secret**. Create these four:

| Secret name | Value |
|---|---|
| `KEYSTORE_BASE64` | The key file converted to text. Run `[Convert]::ToBase64String([IO.File]::ReadAllBytes("$env:USERPROFILE\dayflow-release.jks")) \| Set-Clipboard`, then paste. |
| `KEYSTORE_PASSWORD` | The keystore password you chose |
| `KEY_ALIAS` | `dayflow` |
| `KEY_PASSWORD` | The key password (the same one, if you pressed Enter) |

GitHub never shows a secret again after saving it, and does not print secrets in build logs.

## Publishing a version
1. In `app/build.gradle.kts`, set `versionName` (for example `"1.0.0"`). For every later release also raise `versionCode`
   by one, because Android only installs a build with a higher `versionCode`.
2. Commit and push to `main`, and wait for the normal build to turn green.
3. Create and push the tag (it must be `v` plus the exact `versionName`):

        git tag v1.0.0
        git push origin v1.0.0

4. Open the **Actions** tab. The run "Publish release APK" runs the tests, builds a signed release APK and then checks
   it before publishing: the file exists, it is signed, the package id is `com.aggreykibe.dayflow`, the version matches
   the tag, and it is not a debug build. If any check fails nothing is published.
5. Open the **Releases** page. The release contains `AggreyDayFlow.apk` and `AggreyDayFlow.apk.sha256`.

Always-newest direct link (works once a release exists): `https://github.com/Aggrey-kibe/AggreyDayFlow/releases/latest/download/AggreyDayFlow.apk`

## Debug and release builds
The **debug** APK from the normal build is signed with a throw-away key that changes on every GitHub build, so Android will
not update one over another. The **release** APK is signed with your permanent key: later releases install over earlier
ones and keep the user's reminders. A phone that has a debug build must uninstall it once before installing the release.

## Mistakes
- Wrong tag: delete it with `git push --delete origin v1.0.0` and `git tag -d v1.0.0`, fix, and tag again. If a release
  was already created, delete it on the Releases page first.
- Code shrinking (R8) is off in release builds for now. Turn it on only after testing that exact build on a phone.
