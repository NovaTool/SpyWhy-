# SpyWhy v1.0.0 Release

## Reassemble APK

The APK was split into parts to fit GitHub's 100MB file limit.

### On Linux/Mac:
```bash
cat SpyWhy-v1.0.0-release.apk.part_* > SpyWhy-v1.0.0-release.apk
```

### On Windows (PowerShell):
```powershell
Get-Content SpyWhy-v1.0.0-release.apk.part_aa, SpyWhy-v1.0.0-release.apk.part_ab -Encoding Byte -ReadCount 0 | Set-Content SpyWhy-v1.0.0-release.apk -Encoding Byte
```

Then install on your device:
```bash
adb install SpyWhy-v1.0.0-release.apk
```
