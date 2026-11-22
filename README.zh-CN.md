# Chest Rig（胸挂）

[English](README.md) | [简体中文](README.zh-CN.md)

Minecraft JAVA版 客户端/服务端模组，在胸甲上添加**胸挂**。胸挂可存放武器、箭矢和食物，其箭矢槽供每把弓/弩的弹药选择器使用。更多 Minecraft 版本与 Fabric 版本的支持已在计划中，将尽快推进。

## 胸挂

胸甲自带独立的胸挂内容物。取下或丢出胸甲不会单独掉出内容物，内容物会保留在物品内部。由钻石胸甲升级而来的下界合金胸甲会保留内容物、自定义名称和附魔，行为与原版一致。

胸挂容量取决于胸甲材质：

| 胸甲 | 武器槽 | 箭矢槽 | 食物槽 | 通用槽 |
| --- | ---: | ---: | ---: | ---: |
| 皮革 | 1 | 1 | 0 | 0 |
| 锁链 | 1 | 2 | 1 | 0 |
| 铁 | 2 | 3 | 1 | 0 |
| 金 | 2 | 3 | 4 | 0 |
| 钻石 | 2 | 4 | 3 | 0 |
| 下界合金 | 2 | 4 | 2 | 1 |

装备胸甲后，生存模式背包会在快捷栏下方显示一行九个胸挂槽位。原版快捷栏本身保持原位。只有当前胸甲解锁的槽位可用。空的武器、箭矢和食物槽会显示空心的剑、箭和面包图标；通用槽没有图标。

武器槽接受弓、弩、剑、斧和三叉戟。箭矢槽接受箭矢（包括模组添加的箭矢）。食物槽接受带有食物组件的物品。通用槽接受任何物品。

## 弹药选择

- 手持弓或弩并真正处于潜行状态时，会立即显示 HUD。
- HUD 只列出当前装有箭矢类物品的槽位：先是胸挂箭矢槽，然后是副手、快捷栏 1–9 和主背包。
- 选中的槽位带有红色边框。胸挂、副手和快捷栏槽位带有标签。
- 滚动滚轮可循环切换选择。只有真正潜行时才会响应滚动；鞘翅飞行、创造模式飞行和游泳不会打开 HUD，也不会切换弹药。
- 中键点击会将武器重置到默认槽位；创造模式下禁用。
- 停止潜行时 HUD 关闭。没有两秒的潜行超时。
- 快速切换键无需潜行即可使用，并会显示 HUD 两秒。默认前一槽为 XBUTTON2，后一槽为 XBUTTON1，两者均可重新绑定。已有的 options.txt 如果使用旧的相反默认值，会执行一次迁移。
- 切换潜行模式下，手持弓/弩时会临时表现得像按住潜行。在切换潜行状态下将主手切换到弓/弩会退出潜行。这些规则不适用于按住潜行模式。
- 武器只要仍在玩家背包中，就会保留其选择。丢出武器、放入容器、在未开启 keepInventory 时死亡，或结束游戏会话，都会丢失这个临时选择。无效选择会在下一次射击时回退到默认值。

## 快捷栏右侧的胸挂武器槽

- 原版快捷栏右侧新增一个槽位，显示当前激活的胸挂武器。
- 两个按键可选择它：数字键 0（单独的可重绑按键，类似快捷栏第 10 格）和 G（可重绑的切换键）。
- G 会选择胸挂武器，并记住之前的快捷栏物品。再次按 G 会恢复之前的快捷栏槽位，并把胸挂武器放回。
- 使用 G 选中期间，滚动滚轮或副手交换键会在胸挂的武器槽之间循环。滚动会先检查弹药 HUD，因此箭矢切换保持优先。
- 通过数字键 0 或滚动到新槽位选中时，滚动和副手交换使用原版行为；当快捷栏选择变化时，控制器会恢复保存的物品。
- 槽位右侧，上方数字是胸挂箭矢槽中的箭矢总数。下方数字在安装 AppleSkin 时为食物总营养值，否则为食物物品数量。空状态使用空心箭、面包或鸡腿图标。

## 零耐久装备

被追踪的装备（盔甲、盾牌、分级武器/工具、弓、弩和三叉戟）耐久归零后仍留在世界中，不会消失。

- 损坏的盔甲和盾牌会保留附魔条目，但不再提供护甲值或非负面附魔效果，也无法格挡攻击。诅咒和经验修补仍然有效。
- 损坏的装备被取下后无法再次装备。它可以通过铁砧、工作台或经验修补修复。
- 损坏的盾牌在下一次尝试格挡攻击时会被摧毁。
- 当饥饿值低于三个图标时，胸挂会自动进食，直到饥饿值高于五个图标。只有在安装 AppleSkin 时才会启用低生命值自动进食。
- 拾取箭矢时，如果胸挂中存在对应且未满的箭矢槽，箭矢会自动进入该槽位。普通掉落物在快捷栏和副手都没有合适空间时会进入通用槽；空的通用槽可以接受不死图腾。
- 右键点击箭矢会将其存入胸挂。右键点击剑或斧会将其存入胸挂；当两个武器槽都已满时，则与当前显示的武器槽交换。
- 在背包中悬停胸甲时，物品提示框会像潜影盒一样显示其中存放的胸挂内容物。这是原版提示框组件，不需要 Jade。
- 安装 **Jade Addons** 后，Jade 物品标签还会显示两个计数器：左侧为胸挂箭矢总数，右侧为食物总营养值（未安装 AppleSkin 时为食物数量）。空心图标表示该类别为空或不存在。未安装 Jade Addons 时，整行计数器都不显示。
- 关闭 keepInventory 时，损坏的盔甲会在死亡时销毁。损坏的胸甲不会掉落胸甲本身，而是掉落其中存放的内容物。
- 开启 keepInventory 时，损坏的盔甲会保留，并在重生后立即获得持续 60 秒的绑定诅咒。
- 在铁砧上用两个相同部件修复损坏装备时，经验消耗翻倍。用钻石或下界合金锭修复会消耗三倍材料，但经验消耗保持正常。
- 在工作台中合并两个相同的被追踪物品时，会将其修复到两者剩余耐久之和的一半（向上取整）。
- 有损的钻石物品升级为下界合金后，会立即恢复满耐久。

## 构建

### 环境要求

- **JDK 21**（构建使用 Java 21 工具链）
- **Gradle 9.9.1** 已安装并在 PATH 中；未附带 Wrapper 启动脚本
- 首次构建会下载 Minecraft、NeoForge 和映射，因此需要网络连接，并会花费几分钟

构建目标：**Java 21**、**Minecraft 1.21.1**、**NeoForge 21.1.267**、**ModDevGradle 2.0.148**。

### 命令行

项目保留了 Wrapper 配置（gradle/wrapper/gradle-wrapper.jar 和 gradle-wrapper.properties，固定为 Gradle 9.9.1），但**不包含** gradlew / gradlew.bat 启动脚本。请使用 gradle 可执行文件，其版本必须为 **9.9.1**。在项目根目录执行：

    gradle build

当怀疑存在过期输出时，先执行 clean 清除 build/：

    gradle clean build

产物为 build/libs/chest_rig-1.0.0.jar。将其放入 Minecraft 1.21.1 + NeoForge 21.1.267 的 mods 文件夹即可。

常用变体：

| 命令 | 结果 |
| --- | --- |
| gradle build | 编译、运行检查并打包 jar |
| gradle clean build | 清除 build/ 后执行同样流程 |
| gradle jar | 仅打包 jar，不运行检查任务 |
| gradle --no-daemon build | 一次性构建，不保留 Gradle 守护进程 |

随时可以在项目根目录执行 gradle wrapper --gradle-version 9.9.1 重新生成启动脚本。

### IntelliJ IDEA

1. **File -> Open** 打开项目文件夹。IDEA 会将其作为 Gradle 项目导入，并运行 neoForgeIdeSync 生成元数据模板。
2. 打开 **Gradle** 工具窗口；项目名显示为 ChestRig。
3. **build -> build** 构建并打包 jar 到 build/libs。

默认情况下，IDEA 使用其自带的 Gradle 发行版。要改用 Wrapper 固定的版本，请在 **Settings -> Build, Execution, Deployment -> Build Tools -> Gradle** 下选择 **Use Gradle from -> gradle-wrapper.properties file**。

如果 Gradle 工具窗口显示的任务列表为空或过期，点击 **reload** 按钮（Gradle 窗口左上角的循环箭头）重新同步。

### 在开发环境中运行游戏

ModDevGradle 的运行任务位于 Gradle 工具窗口的 **Tasks -> mod development** 下：

- runClient - 启动加载了模组的开发客户端
- runServer - 启动专用开发服务端（--nogui）

右键任务并选择 **Run After Build**。直接选择 **Run** 只会启动游戏并跳过编译任务，导致 FML 报错
"File ...\build\classes\java\main is not a valid mod file"，因为类和资源输出尚不存在。

命令行同样可以运行：

    gradle runClient
    gradle runServer

运行文件（世界、选项、日志）会写入 run/。

### 故障排查

- **"is not a valid mod file"** - build/classes 或 build/resources 从未生成。通过 Gradle 工具窗口使用 **Run After Build** 运行任务，或在命令行执行 gradle runClient。
- **更新后快捷栏或胸挂槽位位置错乱** - 执行 gradle clean build 并重启游戏，以清除过期的槽位类和资源。
- **"Parameter ... could not open ...clientRunVmArgs.txt"** - 运行是从手工创建的 IntelliJ Application 配置启动的。请改用 ModDevGradle 的 runClient 任务。
- **"Mod file ... not found" / 缺少资源** - 执行 gradle clean build 清除过期输出。

## 备注

- 客户端和服务端都必须安装本模组。
- Jade、Jade Addons 和 AppleSkin 是可选集成。构建仅使用 Jade 的编译期提示框类。开发运行（`gradle runClient`）会将 Jade、Jade Addons 和 AppleSkin 加入运行时类路径，以便测试这三条路径。
- 当前构建面向 NeoForge 1.21.1；更多 Minecraft 版本和 Fabric 版本已在计划中（见“开发计划”）。
- mixin 配置在 META-INF/neoforge.mods.toml 中声明；access transformer 暴露了 AbstractContainerMenu#addSlot、潜行按键的 isDown 字段以及胸挂行使用的背包界面布局字段。

## 开发计划

当前版本面向 NeoForge 1.21.1。更多 Minecraft 版本以及 Fabric 加载器版本的开发已在计划中，将尽快推进。

## 许可证

Copyright (C) 2026 wlsnb.

Chest Rig 采用 **GNU Affero 通用公共许可证第 3 版（仅此版本）**（AGPL-3.0-only）授权。完整许可证文本见 [LICENSE](LICENSE)。

本程序按“原样”分发，希望它能有用，但不提供任何担保；甚至不提供适销性或特定用途适用性的暗示担保。详情请参阅 GNU Affero 通用公共许可证，并以 [LICENSE](LICENSE) 的英文原文为准。

AGPL 也涵盖网络使用：如果你修改了本模组，并让玩家通过计算机网络远程与其交互，你必须向这些用户提供你所修改版本的对应源代码（Corresponding Source）。发布构建由本仓库构建，本仓库即对应源代码：
<https://github.com/wlsnb688/ChestRig>。
