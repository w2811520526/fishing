# 🎣 钓鱼大师

一个用 Java Swing 编写的钓鱼模拟游戏，支持中文。

## 快速开始

### 方式一：浏览器版（推荐）

直接双击打开 `fishing-game.html`，无需任何环境配置，支持触屏操作。

### 方式二：Java 桌面版

确保已安装 JDK 11+，然后运行：

```bash
java -jar fishing-game.jar
```

或从源码编译运行：

```bash
javac -encoding UTF-8 -d bin src/fishing/*.java
java -cp bin fishing.FishingGame
```

## 游戏玩法

1. **选择钓点** — 湖泊 / 河流 / 海洋，不同水域有不同鱼种
2. **抛竿蓄力** — 按住鼠标蓄力，力度越大越容易钓到高级鱼
3. **等待咬钩** — 鱼影出现时准备提竿
4. **提竿小游戏** — 按住/松开鼠标控制绿色条追踪鱼的位置，进度满即成功
5. **卖鱼赚钱** — 在背包中卖出鱼获，赚取金币
6. **升级鱼竿** — 提升鱼竿等级可提高成功率和控制条大小

## 鱼种与稀有度

| 稀有度 | 等级 | 颜色 |
|--------|------|------|
| 🐟 普通 | 1★ ~ 2★ | 灰色 |
| ✨ 稀有 | 3★ ~ 4★ | 蓝色 |
| 🌟 传说 | 5★ | 金色 |

共 27 种鱼分布在三个水域，包括锦鲤、金龙鱼、中华鲟、蓝鳍金枪鱼甚至美人鱼等。

## 项目结构

```
fishing/
├── fishing-game.jar       # 可执行 JAR
├── fishing-game.html      # 浏览器版本（完整功能）
├── src/fishing/           # Java 源码
│   ├── FishingGame.java   # 主窗口、UI 面板、游戏逻辑
│   ├── Fish.java          # 鱼的数据模型
│   ├── Player.java        # 玩家状态管理
│   ├── FishingMiniGame.java # 钓鱼小游戏（竖版追踪）
│   ├── FishingScenePanel.java # 钓鱼场景绘制
│   └── FontUtil.java      # 中文字体支持
└── bin/fishing/           # 编译后的 class 文件
```

## 技术要求

- **Java 桌面版**: JDK 11+
- **浏览器版**: 任意现代浏览器（Chrome / Edge / Firefox 等）
