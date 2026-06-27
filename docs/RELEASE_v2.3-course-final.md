# Growth Hub v2.3-course-final Release Notes

## Release Positioning

`v2.3-course-final` 是 Growth Hub 的课程设计最终稳定版本，也是当前仓库唯一推荐的课程交付版本。

该版本冻结于 Weekly Report 功能完成后，覆盖课程设计所需的需求分析、功能闭环、数据库持久化、UI 展示、测试验证和答辩演示能力。

## Git Reference

```text
Tag: v2.3-course-final
Commit: fe3a336 Polish localization and evaluation report
Branch at freeze: feature/v2-development
Current maintenance branch: feature/v2.4-development
```

## Included Features

- 首页 Premium Dashboard
- 专注计时：倒计时、正计时、暂停、继续、结束、取消
- 分类、任务、标签、倒数日、语录管理
- 专注历史时间线
- 统计分析 Dashboard
- 成就中心
- Growth Profile
- Settings Center
- Data Export
- Local Backup / Restore
- Weekly Report 本地周报系统
- 简体中文 UI 文案统一

## Technical Scope

- Android Java
- XML Layouts
- Material Components
- SQLiteOpenHelper
- DAO / Repository 分层
- Foreground Service
- BroadcastReceiver
- AlarmManager
- ContentProvider
- MPAndroidChart

## Release Notes

本版本重点体现：

1. 完整的本地单机产品闭环。
2. 深色 Premium Dashboard 风格的一致 UI。
3. 基于本地数据的统计、成就和周报分析。
4. 面向课程答辩的可演示、可运行、可解释工程结构。
5. 不依赖云端服务，适合离线场景与个人长期记录。

## Version Relationship

```text
v1.0-course
↓
Historical RC1 / Early Course Milestone

v2-ui-complete
↓
Premium UI Milestone

v2.3-course-final
↓
★★★★★ Latest Stable Release

feature/v2-development
↓
Frozen Development Branch

feature/v2.4-development
↓
Current Development Branch
```

## Recommended GitHub Release Body

```markdown
# Growth Hub v2.3-course-final

This is the official course final release of Growth Hub.

Growth Hub is a local-first Android productivity and growth tracking application for Chinese college students. It includes focus sessions, task/category/tag management, focus history, statistics, achievements, weekly reports, data export, backup and restore.

## Highlights

- Local SQLite data persistence
- Premium Material-style dark dashboard UI
- Focus timer with foreground service
- Statistics, achievements and weekly report generated from local data
- CSV export, TXT weekly report export, local database backup and restore
- Fully usable offline, no cloud dependency

## Version Positioning

- `v1.0-course`: Historical RC1 / Early Course Milestone
- `v2-ui-complete`: Premium UI Milestone
- `v2.3-course-final`: Latest Stable Release
- `feature/v2-development`: Frozen Development Branch
- `feature/v2.4-development`: Current Development Branch
```
