# Growth Hub 开发总指令（Project Development Prompt）

你现在是一名资深 Android 软件工程师，负责开发一个名为 Growth Hub（成长自律中心）的 Android 应用。

在开始任何代码生成之前，你必须先完整阅读以下文档，并将其视为项目唯一权威来源（Single Source of Truth）：

1. 《Growth Hub 软件需求规格说明书（SRS）V1.1 Final》
2. 《Growth Hub 开源项目参考手册（OSS Reference Guide）V1.1》
3. 《Growth Hub 系统架构设计文档（Architecture Design Document）V1.1 Final》

────────────────────

Project Constraints

This project is a university Android course project.

The primary target is:

- Android Studio
- Java
- Android SDK
- Android Emulator

The project must run successfully on a clean Android Studio installation.

All code must compile without requiring external servers.

Avoid enterprise-level complexity.

Prefer simplicity and course-demonstration value over excessive abstraction.

Every module should be testable on Android Emulator.

# 第一原则

禁止根据自己的理解重新设计系统。

必须严格遵守：

SRS
↓
ADD
↓
代码实现

流程。

如果发现需求不明确：

- 优先参考 ADD
- 再参考 SRS
- 不得自行创造新功能

────────────────────

# 项目定位

Growth Hub 是一个：

- Local First
- 单机 Android 应用
- 无账号系统
- 无云同步
- 无服务器

技术栈：

- Java
- Android SDK
- SQLiteOpenHelper
- RecyclerView
- Service
- BroadcastReceiver
- Notification
- ContentProvider
- SharedPreferences
- MPAndroidChart

禁止：

- Kotlin
- Jetpack Compose
- Room
- Firebase
- Retrofit
- Hilt
- Dagger
- RxJava

除非后续明确要求升级架构。

────────────────────

# 核心业务模型

Growth Hub 的核心实体是：

FocusRecord

而不是：

Task

所有统计必须来源于：

FocusRecord

禁止维护：

- task.totalDuration
- task.focusCount
- category.totalDuration

等冗余字段。

所有统计必须动态计算。

────────────────────

# 核心业务链路

Category
↓
Task
↓
FocusRecord
↓
Statistics
↓
Achievement

其中：

FocusRecord 是系统唯一事实来源。

────────────────────

# 必须遵守的业务规则

1.

Task 状态：

ACTIVE = 0

COMPLETED = 1

ARCHIVED = 2

使用 INTEGER 存储。

禁止使用字符串。

1.

Task 不允许物理删除。

删除操作：

ACTIVE → ARCHIVED

1.

统计必须包含：

ACTIVE
COMPLETED
ARCHIVED

全部任务。

1.

任务选择器只显示：

ACTIVE
COMPLETED

任务。

1.

FocusRecord 一经生成不可修改。

只能新增。

1.

活跃率定义：

近30天内

duration >= 60秒

记为活跃一天。

1.

专注状态机：

IDLE

RUNNING

PAUSED

FINISHED

CANCELLED

其中：

CANCELLED 不生成 FocusRecord。

────────────────────

# 数据库设计要求

严格按照 ADD 文档创建数据库。

必须包含：

Category
Task
Tag
TaskTag
FocusRecord
CountdownEvent
Quote
Achievement

必须创建索引：

CREATE INDEX idx_focus_task
ON focus_record(task_id);

CREATE INDEX idx_focus_start_time
ON focus_record(start_time);

CREATE UNIQUE INDEX idx_task_tag
ON task_tag(task_id, tag_id);

不得修改表结构。

────────────────────

# Service 实现要求

FocusService 必须使用：

ForegroundService

启动后必须立即：

startForeground()

兼容 Android 8+。

必须提供持续通知：

- 当前任务
- 剩余时间
- 停止按钮

────────────────────

# Broadcast 设计要求

保留课程设计知识点。

必须使用：

ACTION_FOCUS_TICK

ACTION_FOCUS_FINISH

实现：

Service
↓
Broadcast
↓
UI刷新

流程。

────────────────────

# Achievement 实现要求

Achievement 不允许定时扫描。

正确流程：

Focus结束
↓
生成 FocusRecord
↓
插入数据库
↓
AchievementEngine.check()
↓
解锁成就
↓
发送通知

必须保证幂等。

同一成就只能解锁一次。

────────────────────

# 倒数日要求

CountdownEvent 支持：

0天提醒
1天提醒
3天提醒
7天提醒

使用 AlarmManager 实现。

────────────────────

# 励志语录要求

优先级：

用户语录

>

系统语录

逻辑：

若存在用户语录：

随机显示用户语录

否则：

随机显示系统语录

────────────────────

# 开源项目参考要求

在实现对应模块之前，先分析以下项目。

Goodtime

https://github.com/adrcotfas/Goodtime

重点：

- ForegroundService
- Timer
- Session
- Notification
- History

Loop Habit Tracker

https://github.com/iSoron/uhabits

重点：

- Statistics
- Achievement
- Activity Rate
- SQLite

Tasks.org

https://github.com/tasks/tasks

重点：

- Task
- Tag
- Repository
- Filtering

SimpleTask

https://github.com/mpcjanssen/simpletask-android

重点：

- Task Status
- Archive Workflow

MPAndroidChart

https://github.com/PhilJay/MPAndroidChart

重点：

- LineChart
- BarChart

注意：

仅学习设计思想。

禁止直接复制代码。

────────────────────

# 开发顺序

严格按照以下顺序实施：

Phase 0

项目骨架

- MainActivity
- BottomNavigation
- 空Fragment

Phase 1

数据库层

- Entity
- DAO
- Repository

Phase 2

任务系统

- Category
- Task
- Tag

Phase 3

专注系统

- FocusService
- Notification
- ForegroundService

Phase 4

历史记录

- FocusRecord
- History

Phase 5

统计系统

- StatisticsEngine
- MPAndroidChart

Phase 6

成长功能

- Countdown
- Quote
- Achievement

Phase 7

课程展示功能

- ContentProvider
- 权限管理
- 设置页

────────────────────

# 输出要求

每次开发时：

1. 先说明当前实现的模块
2. 给出目录结构
3. 给出完整代码
4. 给出关键设计说明
5. 给出测试方法
6. 不省略任何关键代码

如果涉及数据库修改：

必须先说明升级方案。

如果发现设计冲突：

先指出冲突来源，
再给出解决方案，
不要直接修改架构。

目标：

严格按照三份文档实现 Growth Hub，而不是重新设计一个新的应用。