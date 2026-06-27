# Growth Hub

Growth Hub 是一款面向中国大学生的本地单机成长与专注管理 Android 应用。项目围绕“分类 -> 任务 -> 专注记录 -> 历史 -> 统计 -> 成就 -> 周报 -> 数据导出/备份”形成完整闭环，核心数据保存在本地 SQLite 中，不依赖网络、云服务或第三方后端。

## Latest Stable Release

★★★★★ `v2.3-course-final`

这是课程设计最终交付版本，也是当前仓库唯一推荐的稳定课程发布版本。课程报告、验收评估、演示说明和后续维护均以该版本为基准。

## Project Highlights

- 本地专注计时：支持倒计时、正计时、暂停、继续、结束与取消。
- 成长数据闭环：专注记录驱动历史、统计、成就、周报等模块。
- Premium Dashboard UI：Home、Focus、Statistics、History、Profile、Achievement 保持统一深色视觉风格。
- 本地周报系统：基于本地 SQLite 自动生成每周成长报告，不调用云端 AI。
- 数据中心：支持 CSV 导出、周报 TXT 导出、本地数据库备份与恢复。
- 单机可落地运行：核心功能离线可用，适合课程答辩与个人学习记录。

## Tech Stack

- Language: Java
- Platform: Android
- UI: XML Layouts, Material Components
- Storage: SQLiteOpenHelper
- Background: Foreground Service, BroadcastReceiver, AlarmManager
- Chart: MPAndroidChart
- Build: Gradle Wrapper

## Repository Structure

```text
app/
  src/main/java/com/growthhub/
    database/        SQLite, DAO, Repository, Entity
    service/         Focus foreground service
    receiver/        Focus and reminder receivers
    ui/              Activities and Fragments
    report/          Local weekly report generator
    data/            Export, backup and restore managers
    util/            Shared UI and duration helpers
  src/main/res/
    layout/          XML UI layouts
    drawable/        Premium dashboard backgrounds and icons
    values/          strings, colors, themes

docs/
  GrowthHub_Final_Evaluation.md
  RELEASE_v2.3-course-final.md
  report_assets/
```

## Version History

| Version / Branch | Positioning |
|---|---|
| `v1.0-course` | Historical RC1 / Early Course Milestone |
| `v2-ui-complete` | Premium UI Milestone |
| `v2.3-course-final` | Latest Stable Release |
| `feature/v2-development` | Frozen Development Branch |
| `feature/v2.4-development` | Current Development Branch |

## Build

使用 Android Studio 打开仓库根目录，等待 Gradle Sync 完成后运行 `app`。

也可以在 PowerShell 中执行：

```powershell
cd "D:\Growth Hub"
.\gradlew.bat :app:assembleDebug
```

调试 APK 输出位置：

```text
app/build/outputs/apk/debug/app-debug.apk
```

## Documentation

- [最终验收评估报告](docs/GrowthHub_Final_Evaluation.md)
- [v2.3-course-final Release Notes](docs/RELEASE_v2.3-course-final.md)

## Maintenance Policy

- `v2.3-course-final` 是课程设计最终稳定版本。
- `feature/v2-development` 保留为冻结开发分支，不再继续开发。
- 后续新功能统一在 `feature/v2.4-development` 或新的功能分支中进行。
- 禁止通过 rebase、force push 或删除 commit 的方式重写历史。
