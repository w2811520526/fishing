package fishing;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Random;
import java.util.List;
import java.util.ArrayList;

public class FishingGame extends JFrame {

    // ============== 数据 ==============
    private Player player;
    private Random random = new Random();
    private int totalFishSpecies; // 鱼种总数

    // ============== 组件 - 状态栏 ==============
    private JLabel goldLabel;
    private JLabel rodLabel;
    private JComboBox<String> spotCombo;

    // ============== 组件 - 左侧按钮 ==============
    private JButton fishBtn;
    private JButton inventoryBtn;
    private JButton collectionBtn;
    private JButton statsBtn;
    private JButton upgradeBtn;
    private JButton adminBtn;

    // ============== 组件 - CardLayout ==============
    private JPanel centerPanel;
    private CardLayout cardLayout;
    private final String CARD_FISH      = "fish";
    private final String CARD_INVENTORY = "inventory";
    private final String CARD_COLLECTION= "collection";
    private final String CARD_STATS     = "stats";
    private final String CARD_UPGRADE   = "upgrade";

    // ============== 钓鱼面板 ==============
    private FishingScenePanel scenePanel;
    private JLabel fishStatusLabel;
    private JTextArea fishLogArea;
    private JButton castBtn;
    private JButton hookBtn;               // "提竿"按钮
    private Timer biteTimer;               // 咬钩超时计时器
    private boolean fishBiting;            // 鱼正在咬钩
    private double castingPower = 0;       // 上次抛竿力度 (0~1)

    // ============== 背包面板 ==============
    private JTable inventoryTable;
    private DefaultTableModel tableModel;
    private JButton sellBtn;
    private JLabel inventoryInfoLabel;

    // ============== 图鉴面板 ==============
    private JTable collectionTable;
    private DefaultTableModel collectionTableModel;

    // ============== 统计面板 ==============
    private JLabel statTotalCaught;
    private JLabel statTotalGold;
    private JLabel statRareCaught;
    private JLabel statLegendaryCaught;
    private JLabel statCollected;
    private JLabel statRodLevel;

    // ============== 升级面板 ==============
    private JLabel currentRodLabel;
    private JLabel upgradeEffectLabel;
    private JLabel upgradeCostLabel;
    private JButton upgradeConfirmBtn;
    private JLabel upgradeInfoLabel;

    // ============== 全局鱼库 ==============
    // 各水域鱼种: {名称, 等级(1~5), 最小重量, 最大重量, 基数价格, 图标, 描述, 难度, 水域索引}
    private String[][][] fishDatabase = {
        // ---- 湖泊 ----
        {
            {"白条鱼", "1", "0.5", "2", "4", "\uD83D\uDC1F", "练手小鱼", "5"},
            {"鲫鱼",   "2", "1",   "3", "6", "\uD83D\uDC1F", "最常见的淡水鱼", "10"},
            {"鲤鱼",   "2", "2",   "5", "10","\uD83D\uDC1F", "力气大，手感不错", "15"},
            {"草鱼",   "3", "3",   "7", "12","\uD83D\uDC1F", "草食性鱼类", "18"},
            {"鲶鱼",   "3", "4",   "8", "15","\uD83D\uDC1F", "滑溜溜的不好抓", "25"},
            {"鳜鱼",   "4", "2",   "4", "30","\uD83D\uDC20", "淡水美味珍品", "45"},
            {"鲈鱼",   "4", "3",   "6", "35","\uD83D\uDC20", "清蒸鲈鱼想想就香", "50"},
            {"黑鱼",   "4", "4",   "7", "40","\uD83D\uDC20", "生性凶猛拉力强", "55"},
            {"锦鲤",   "5", "4",   "8", "100","\uD83D\uDC1F", "带来好运的吉祥之鱼", "75"},
            {"金龙鱼", "5", "6",   "12","120","\uD83D\uDC6F", "传说中的风水鱼！", "80"},
        },
        // ---- 河流 ----
        {
            {"白条鱼", "1", "0.5", "1.5", "3", "\uD83D\uDC1F", "河里到处都是", "5"},
            {"马口鱼", "1", "0.3", "1",   "5", "\uD83D\uDC1F", "嘴巴特别大", "8"},
            {"鲫鱼",   "2", "1",   "3",   "6", "\uD83D\uDC1F", "河产鲫鱼肉质紧实", "10"},
            {"鲤鱼",   "2", "2",   "5",   "10","\uD83D\uDC1F", "河鲤活力十足", "15"},
            {"虹鳟鱼", "4", "2",   "4",   "32","\uD83D\uDC20", "色彩斑斓的冷水鱼", "48"},
            {"鳜鱼",   "4", "2",   "5",   "35","\uD83D\uDC20", "河中珍品", "50"},
            {"鲶鱼",   "4", "6",   "12",  "45","\uD83D\uDC20", "大河鲶鱼体型巨大", "60"},
            {"金龙鱼", "5", "5",   "10",  "130","\uD83D\uDC6F", "河神化身？", "85"},
            {"中华鲟", "5", "12",  "25",  "200","\uD83D\uDC33", "水中大熊猫！极其珍贵", "90"},
        },
        // ---- 海洋 ----
        {
            {"沙丁鱼", "1", "0.2", "0.5", "4", "\uD83D\uDC1F", "小鱼小虾", "3"},
            {"小黄鱼", "2", "0.3", "0.8", "8", "\uD83D\uDC1F", "味道鲜美的经济鱼类", "12"},
            {"带鱼",   "2", "1",   "3",   "12","\uD83D\uDC1F", "长条形的海鱼", "20"},
            {"鲷鱼",   "4", "2",   "5",   "40","\uD83D\uDC20", "日料中的常客", "45"},
            {"石斑鱼", "4", "3",   "7",   "50","\uD83D\uDC20", "名贵海鱼肉质极品", "55"},
            {"金枪鱼", "4", "20",  "60",  "80","\uD83D\uDC20", "海洋中的战斗机", "65"},
            {"蓝鳍金枪鱼","5","50","100","300","\uD83D\uDC33", "鱼中贵族一条价值连城！", "95"},
            {"美人鱼", "5", "3",   "8",   "888","\uD83E\uDDDC", "你钓到了美人鱼？！不可能！", "100"},
        }
    };

    // ============== 构造器 ==============
    public FishingGame() {
        Player loaded = Player.load();
        player = loaded != null ? loaded : new Player();
        for (String[][] spot : fishDatabase) totalFishSpecies += spot.length;

        setTitle("\uD83C\uDFA3 钓鱼大师");
        setSize(820, 580);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        add(createStatusBar(), BorderLayout.NORTH);
        add(createLeftPanel(), BorderLayout.WEST);
        add(createCenterPanel(), BorderLayout.CENTER);

        updateStatusBar();
            player.save();
    }

    // =========================================================
    //  状态栏（调亮配色 + 鱼饵 + 水域）
    // =========================================================
    private JPanel createStatusBar() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 7));
        panel.setBackground(new Color(65, 120, 200));
        panel.setBorder(BorderFactory.createEmptyBorder(5, 12, 5, 12));

        goldLabel = new JLabel("\uD83D\uDCB0 金币：20");
        goldLabel.setFont(FontUtil.bold(16));
        goldLabel.setForeground(new Color(255, 215, 0));

        rodLabel = new JLabel("\uD83C\uDFA3 鱼竿 Lv.1");
        rodLabel.setFont(FontUtil.bold(16));
        rodLabel.setForeground(Color.WHITE);

        // 钓点
        JLabel spotLabel = new JLabel("\uD83C\uDF0A 钓点：");
        spotLabel.setFont(FontUtil.bold(14));
        spotLabel.setForeground(Color.WHITE);
        spotCombo = new JComboBox<>(Player.SPOTS);
        spotCombo.setFont(FontUtil.plain(13));
        spotCombo.setBackground(new Color(80, 150, 220));
        spotCombo.setForeground(Color.WHITE);
        spotCombo.addActionListener(e -> {
            player.setCurrentSpot((String) spotCombo.getSelectedItem());
            scenePanel.setCurrentSpot(player.getCurrentSpot());
            fishLogArea.append("切换到 " + player.getCurrentSpot() + " 垂钓\n");
        });

        panel.add(goldLabel);
        panel.add(rodLabel);
        panel.add(Box.createHorizontalStrut(5));
        panel.add(spotLabel);
        panel.add(spotCombo);

        return panel;
    }

    private void updateStatusBar() {
        goldLabel.setText("\uD83D\uDCB0 金币：" + player.getGold());
        rodLabel.setText("\uD83C\uDFA3 鱼竿 Lv." + player.getRodLevel());
    }

    // =========================================================
    //  左侧导航按钮
    // =========================================================
    private JPanel createLeftPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));
        panel.setBackground(new Color(75, 135, 215));

        fishBtn       = createNavButton("\uD83C\uDFA3 钓鱼");
        inventoryBtn  = createNavButton("\uD83C\uDF92 背包");
        collectionBtn = createNavButton("\uD83D\uDCD6 图鉴");
        statsBtn      = createNavButton("\uD83D\uDCCA 统计");
        upgradeBtn    = createNavButton("\u2B06 升级");
        adminBtn      = createNavButton("\u2699\uFE0F 管理");
        adminBtn.setBackground(new Color(200, 100, 60));

        panel.add(fishBtn);
        panel.add(Box.createRigidArea(new Dimension(0, 12)));
        panel.add(inventoryBtn);
        panel.add(Box.createRigidArea(new Dimension(0, 12)));
        panel.add(collectionBtn);
        panel.add(Box.createRigidArea(new Dimension(0, 12)));
        panel.add(statsBtn);
        panel.add(Box.createRigidArea(new Dimension(0, 12)));
        panel.add(upgradeBtn);
        panel.add(Box.createRigidArea(new Dimension(0, 12)));
        panel.add(adminBtn);

        fishBtn.addActionListener(e -> switchToFish());
        inventoryBtn.addActionListener(e -> switchToInventory());
        collectionBtn.addActionListener(e -> switchToCollection());
        statsBtn.addActionListener(e -> switchToStats());
        upgradeBtn.addActionListener(e -> switchToUpgrade());
        adminBtn.addActionListener(e -> openAdminPanel());

        return panel;
    }

    private JButton createNavButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FontUtil.bold(14));
        btn.setForeground(Color.WHITE);
        btn.setBackground(new Color(80, 140, 220));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 25));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setMaximumSize(new Dimension(130, 42));
        btn.setPreferredSize(new Dimension(130, 42));
        return btn;
    }

    // =========================================================
    //  中心区域 (CardLayout)
    // =========================================================
    private JPanel createCenterPanel() {
        cardLayout = new CardLayout();
        centerPanel = new JPanel(cardLayout);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        centerPanel.setBackground(new Color(235, 242, 255));

        centerPanel.add(createFishPanel(),      CARD_FISH);
        centerPanel.add(createInventoryPanel(), CARD_INVENTORY);
        centerPanel.add(createCollectionPanel(),CARD_COLLECTION);
        centerPanel.add(createStatsPanel(),     CARD_STATS);
        centerPanel.add(createUpgradePanel(),   CARD_UPGRADE);
        cardLayout.show(centerPanel, CARD_FISH);
        return centerPanel;
    }

    // =========================================================
    //  钓鱼面板 (含进度条动画)
    // =========================================================
    private JPanel createFishPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(50, 120, 200), 2),
                "\uD83C\uDFA3 钓鱼区", 0, 0,
                FontUtil.bold(16), new Color(50, 110, 180)));

        // 顶部控制栏（抛竿 + 提竿按钮）
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 8));
        topPanel.setBackground(new Color(220, 235, 250));

        castBtn = new JButton("\uD83C\uDFA3 抛竿！");
        castBtn.setFont(FontUtil.bold(15));
        castBtn.setPreferredSize(new Dimension(120, 38));
        castBtn.setBackground(new Color(50, 140, 240));
        castBtn.setForeground(Color.WHITE);
        castBtn.setFocusPainted(false);
        topPanel.add(castBtn);

        hookBtn = new JButton("\uD83C\uDFAF 提竿！");
        hookBtn.setFont(FontUtil.bold(15));
        hookBtn.setPreferredSize(new Dimension(120, 38));
        hookBtn.setBackground(new Color(220, 60, 60));
        hookBtn.setForeground(Color.WHITE);
        hookBtn.setFocusPainted(false);
        hookBtn.setVisible(false);
        topPanel.add(hookBtn);

        fishStatusLabel = new JLabel("点击\u300C抛竿\u300D开始钓鱼...");
        fishStatusLabel.setFont(FontUtil.plain(14));
        topPanel.add(fishStatusLabel);

        panel.add(topPanel, BorderLayout.NORTH);

        // 场景面板（核心视觉）
        scenePanel = new FishingScenePanel();
        scenePanel.setPreferredSize(new Dimension(0, 260));
        panel.add(scenePanel, BorderLayout.CENTER);

        // 蓄力释放 → 继续流程
        scenePanel.setOnPowerRelease(() -> {
            double power = scenePanel.getPowerValue();
            castingPower = power;  // 保存力度用于选鱼
            fishLogArea.append("\uD83D\uDCA8 抛竿！（力度 " + (int)(power * 100) + "%）\n");
            afterCast();
        });

        // 日志区
        fishLogArea = new JTextArea();
        fishLogArea.setEditable(false);
        fishLogArea.setFont(FontUtil.plain(12));
        fishLogArea.setLineWrap(true);
        fishLogArea.setWrapStyleWord(true);
        fishLogArea.setBackground(new Color(248, 252, 255));
        JScrollPane scrollPane = new JScrollPane(fishLogArea);
        scrollPane.setPreferredSize(new Dimension(0, 110));
        panel.add(scrollPane, BorderLayout.SOUTH);

        castBtn.addActionListener(e -> startCasting());
        hookBtn.addActionListener(e -> doHook());

        return panel;
    }

    // =========================================================
    //  钓鱼流程：蓄力抛竿 → 等鱼 → 咬钩提竿 → 小游戏
    // =========================================================

    /** 点击抛竿 → 进入蓄力模式 */
    private void startCasting() {
        castBtn.setEnabled(false);
        castBtn.setVisible(false);
        spotCombo.setEnabled(false);
        fishStatusLabel.setText("\uD83C\uDFA3 按住画面蓄力，松开抛竿！");
        fishStatusLabel.setForeground(new Color(180, 100, 0));
        scenePanel.startCasting();
    }

    /** 蓄力释放 → 进入等待阶段 */
    private void afterCast() {
        scenePanel.startWaiting();
        fishStatusLabel.setText("\uD83D\uDC1F 等待鱼儿上钩...");
        fishStatusLabel.setForeground(Color.DARK_GRAY);

        // 随机时间后鱼靠近（缩短等待）
        int nearDelay = 800 + random.nextInt(1200);
        new Timer(nearDelay, e -> {
            scenePanel.showFishNear();
            fishStatusLabel.setText("\uD83D\uDC1F 有鱼在附近游动...");
            fishStatusLabel.setForeground(new Color(0, 130, 0));

            // 再等一会儿咬钩（缩短等待）
            int biteDelay = 500 + random.nextInt(1000);
            new Timer(biteDelay, e2 -> {
                enterBiting();
            }) {{ setRepeats(false); }}.start();
        }) {{ setRepeats(false); }}.start();
    }

    /** 鱼咬钩 → 显示提竿按钮闪烁，限时点击 */
    private void enterBiting() {
        scenePanel.showBiting();
        fishBiting = true;
        hookBtn.setVisible(true);
        fishStatusLabel.setText("\uD83C\uDFAF 咬钩了！快提竿！");
        fishStatusLabel.setForeground(new Color(200, 40, 0));

        // 提竿按钮闪烁（改背景色，不改变 visible 避免布局抖动）
        hookBtn.setBackground(new Color(255, 80, 80));
        Timer flash = new Timer(400, null);
        flash.addActionListener(e -> {
            Color c = hookBtn.getBackground();
            hookBtn.setBackground(c.equals(new Color(255, 80, 80))
                    ? new Color(200, 40, 40) : new Color(255, 80, 80));
        });
        flash.start();

        // 超时未提竿
        biteTimer = new Timer(3500, e -> {
            flash.stop();
            hookBtn.setVisible(false);
            fishBiting = false;
            fishStatusLabel.setText("\u274C 鱼跑了...下次快点提竿！");
            fishStatusLabel.setForeground(Color.RED);
            fishLogArea.append("\u274C 反应太慢，鱼跑了\n");
            resetAfterFishing();
            scenePanel.setIdle();
        });
        biteTimer.setRepeats(false);
        biteTimer.start();
    }

    /** 点击提竿 → 弹出小游戏 */
    private void doHook() {
        if (!fishBiting) return;
        biteTimer.stop();
        hookBtn.setVisible(false);
        fishBiting = false;
        fishStatusLabel.setText("\uD83C\uDFAF 提竿成功！");
        fishStatusLabel.setForeground(new Color(0, 130, 0));
        fishLogArea.append("\uD83C\uDFAF 提竿！鱼上钩了！\n");
        scenePanel.setIdle();
        launchMiniGame();
    }

    /** 确定鱼种并弹出小游戏 */
    private void launchMiniGame() {
        int rodLv = player.getRodLevel();

        // 按等级权重选择鱼（受抛竿力度影响：力度越大鱼越好）
        int[] weights = new int[6]; // index 1~5
        double targetLv = 1 + castingPower * 4; // 力度0~1映射到等级1~5
        for (int lv = 1; lv <= 5; lv++) {
            int baseWeight = Math.max(1, 6 - Math.abs(lv - rodLv));
            double distFromTarget = Math.abs(lv - targetLv);
            int powerBonus = Math.max(0, 10 - (int)(distFromTarget * 4));
            weights[lv] = baseWeight + powerBonus;
        }
        int totalW = 0;
        for (int w : weights) totalW += w;
        int roll = random.nextInt(totalW);
        int chosenLevel = 1;
        for (int lv = 1; lv <= 5; lv++) {
            roll -= weights[lv];
            if (roll < 0) { chosenLevel = lv; break; }
        }

        String lvStr = String.valueOf(chosenLevel);

        // 从对应水域选出该等级的鱼
        String spot = player.getCurrentSpot();
        int si = 0;
        for (int i = 0; i < Player.SPOTS.length; i++)
            if (Player.SPOTS[i].equals(spot)) { si = i; break; }

        List<String[]> cand = new ArrayList<>();
        for (String[] f : fishDatabase[si])
            if (f[1].equals(lvStr)) cand.add(f);
        if (cand.isEmpty()) {
            // 兜底：取该水域第一条
            cand.add(fishDatabase[si][0]);
        }

        String[] d = cand.get(random.nextInt(cand.size()));
        String name = d[0];
        int fishLv = Integer.parseInt(d[1]);
        double minW = Double.parseDouble(d[2]), maxW = Double.parseDouble(d[3]);
        double baseP = Double.parseDouble(d[4]);
        String emoji = d[5], desc = d[6];
        int difficulty = Integer.parseInt(d[7]);

        // 难度公式：同级=适中，竿高=简单，竿低=难，传说鱼额外+10
        int adjDiff = difficulty - rodLv * 10 + (fishLv - 1) * 5;
        if (fishLv >= 5) adjDiff += 30; // 传说鱼额外挑战
        adjDiff = Math.max(5, adjDiff);

        String rarity = fishLv >= 5 ? "传说" : fishLv >= 3 ? "稀有" : "普通";

        FishingMiniGame miniGame = new FishingMiniGame(this, adjDiff, emoji, name, spot,
                player.getRodLevel(), fishLv, new FishingMiniGame.Callback() {
            @Override
            public void onResult(boolean success, boolean perfect, int accuracy) {
                if (success) {
                    double w = minW + random.nextDouble() * (maxW - minW);
                    w = Math.round(w * 10) / 10.0;
                    int price = (int)(baseP * (0.8 + random.nextDouble() * 0.4));
                    price = Math.max(1, price);
                    if (perfect) price = (int)(price * 1.5);
                    Fish fish = new Fish(name, fishLv, w, price, emoji, desc, spot);
                    player.addFish(fish);

                    String pt = perfect ? "\u2728完美！" : "";
                    String stars = fish.getLevelStars();
                    fishStatusLabel.setText(fish.getRarityEmoji() + " " + pt
                            + fish.getEmoji() + fish.getName() + " (" + stars + ") " + w + "\u65A4");
                    Color c;
                    if (fishLv >= 5) c = new Color(255,180,0);
                    else if (fishLv >= 3) c = new Color(50,80,200);
                    else c = new Color(0,120,0);
                    fishStatusLabel.setForeground(c);
                    fishLogArea.append("\uD83C\uDFA3 " + fish);
                    if (perfect) fishLogArea.append(" \u2728\u5B8C\u7F8E\u9493\u9C7C\uFF01\u5956\u52B1x1.5");
                    fishLogArea.append("\n");
                    if (fishLv >= 5) showLegendaryEffect(fish);
                } else {
                    fishStatusLabel.setText("\u274C \u9C7C\u631F\u8131\u4E86...");
                    fishStatusLabel.setForeground(Color.RED);
                    fishLogArea.append("\u274C \u9C7C\u631F\u626E\u9003\u8131\n");
                }
                resetAfterFishing();
                updateStatusBar();
                player.save();
            }
        });
        miniGame.start();
    }

    /** 钓鱼结束后恢复界面 */
    private void resetAfterFishing() {
        castBtn.setVisible(true);
        castBtn.setEnabled(true);
        castBtn.setText("\uD83C\uDFA3 抛竿！");
        spotCombo.setEnabled(true);
    }

    /** 传说鱼特效弹窗 */
    private void showLegendaryEffect(Fish fish) {
        // 闪烁状态栏
        Color saved = fishStatusLabel.getForeground();
        fishStatusLabel.setForeground(new Color(255, 180, 0));
        fishStatusLabel.setFont(FontUtil.bold(18));

        // 弹出华丽对话框
        JDialog dialog = new JDialog(this, "🌟 传说之鱼！", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(350, 200);
        dialog.setLocationRelativeTo(this);

        JPanel content = new JPanel(new GridBagLayout());
        content.setBackground(new Color(255, 250, 220));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);

        gbc.gridx = 0; gbc.gridy = 0;
        gbc.gridwidth = 2;
        JLabel titleLabel = new JLabel("🌟🌟🌟 传说之鱼！ 🌟🌟🌟");
        titleLabel.setFont(FontUtil.bold(20));
        titleLabel.setForeground(new Color(200, 120, 0));
        content.add(titleLabel, gbc);

        gbc.gridy = 1; gbc.gridwidth = 1;
        JLabel emojiLabel = new JLabel(fish.getEmoji());
        emojiLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
        content.add(emojiLabel, gbc);

        gbc.gridx = 1;
        JLabel infoLabel = new JLabel("<html><b>" + fish.getName() + "</b><br>"
                + fish.getWeight() + "斤  " + fish.getPrice() + " 金币</html>");
        infoLabel.setFont(FontUtil.plain(16));
        content.add(infoLabel, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        JLabel descLabel = new JLabel(fish.getDesc());
        descLabel.setFont(FontUtil.plain(14));
        content.add(descLabel, gbc);

        JButton okBtn = new JButton("太棒了！");
        okBtn.addActionListener(e -> dialog.dispose());
        dialog.add(content, BorderLayout.CENTER);
        dialog.add(okBtn, BorderLayout.SOUTH);
        dialog.setVisible(true);

        fishStatusLabel.setFont(FontUtil.plain(14));
    }

    // =========================================================
    //  背包面板 (带颜色渲染)
    // =========================================================
    private JPanel createInventoryPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("\uD83C\uDF92 背包 - 已钓到的鱼"));

        String[] columnNames = {"名称", "稀有度", "重量(斤)", "价格(金币)"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        inventoryTable = new JTable(tableModel);
        inventoryTable.setRowHeight(26);
        inventoryTable.getTableHeader().setReorderingAllowed(false);
        inventoryTable.getTableHeader().setFont(FontUtil.bold(13));

        // 自定义颜色渲染
        inventoryTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                if (!isSelected && row < player.getFishCount()) {
                    Fish f = player.getInventory().get(row);
                    c.setForeground(f.getRarityColor());
                    setFont(FontUtil.get(f.getRarity().equals("传说") ? Font.BOLD : Font.PLAIN, 13));
                }
                return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(inventoryTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        // 底部
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        sellBtn = new JButton("\uD83D\uDCB0 卖出选中");
        sellBtn.setFont(FontUtil.bold(13));
        sellBtn.setBackground(new Color(255, 180, 80));
        sellBtn.setFocusPainted(false);
        inventoryInfoLabel = new JLabel("共 0 条鱼 | 总价值：0 金币");
        inventoryInfoLabel.setFont(FontUtil.plain(13));
        bottomPanel.add(sellBtn);
        bottomPanel.add(inventoryInfoLabel);
        panel.add(bottomPanel, BorderLayout.SOUTH);

        sellBtn.addActionListener(e -> sellSelectedFish());
        return panel;
    }

    private void refreshInventoryTable() {
        tableModel.setRowCount(0);
        for (Fish f : player.getInventory()) {
            tableModel.addRow(new Object[]{f.getEmoji() + " " + f.getName(), f.getRarity(),
                    f.getWeight(), f.getPrice()});
        }
        inventoryInfoLabel.setText("共 " + player.getFishCount() + " 条鱼 | 总价值：" + player.getTotalInventoryValue() + " 金币");
    }

    private void sellSelectedFish() {
        int row = inventoryTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "请先选中要卖出的鱼！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Fish fish = player.getInventory().get(row);
        int price = fish.getPrice();
        int result = JOptionPane.showConfirmDialog(this,
                "确定卖出「" + fish.getEmoji() + " " + fish.getName() + "」获得 " + price + " 金币？",
                "卖出确认", JOptionPane.YES_NO_OPTION);
        if (result == JOptionPane.YES_OPTION) {
            player.addGold(price);
            player.recordGoldEarned(price);
            player.removeFish(row);
            refreshInventoryTable();
            updateStatusBar();
            player.save();
            fishLogArea.append("💰 卖出 " + fish.getName() + "，获得 " + price + " 金币\n");
        }
    }

    // =========================================================
    //  图鉴面板
    // =========================================================
    private JPanel createCollectionPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("\uD83D\uDCD6 鱼类图鉴"));

        String[] colNames = {"状态", "图标", "名称", "等级", "出没水域", "描述"};
        collectionTableModel = new DefaultTableModel(colNames, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        collectionTable = new JTable(collectionTableModel);
        collectionTable.setRowHeight(26);
        collectionTable.getTableHeader().setReorderingAllowed(false);
        collectionTable.getTableHeader().setFont(FontUtil.bold(13));

        // 自定义渲染
        collectionTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                if (!isSelected) {
                    // 判断是否已收集：第一列是状态
                    String status = (String) table.getValueAt(row, 0);
                    if (status.equals("\u2705")) {
                        c.setForeground(new Color(0, 130, 0));
                        setFont(FontUtil.plain(13));
                    } else {
                        c.setForeground(Color.LIGHT_GRAY);
                        setFont(FontUtil.get(Font.ITALIC, 13));
                    }
                }
                return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(collectionTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        // 底部统计
        JLabel tip = new JLabel("未钓到的鱼显示为灰色");
        tip.setFont(FontUtil.plain(12));
        tip.setForeground(Color.GRAY);
        panel.add(tip, BorderLayout.SOUTH);

        return panel;
    }

    private void refreshCollectionPanel() {
        collectionTableModel.setRowCount(0);
        for (String[][] spotFish : fishDatabase) {
            for (String[] f : spotFish) {
                boolean collected = player.hasCollected(f[0]);
                String status = collected ? "\u2705" : "\u274C";
                int fLevel = Integer.parseInt(f[1]);
                StringBuilder stars = new StringBuilder();
                for (int i = 0; i < fLevel; i++) stars.append('\u2605');
                for (int i = fLevel; i < 5; i++) stars.append('\u2606');
                collectionTableModel.addRow(new Object[]{
                        status, f[5], f[0], stars.toString(), f[6], collected ? f[6] : "???"});
            }
        }
    }

    // =========================================================
    //  统计面板
    // =========================================================
    private JPanel createStatsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("\uD83D\uDCCA 钓鱼统计"));

        JPanel gridPanel = new JPanel(new GridLayout(6, 1, 10, 15));
        gridPanel.setBorder(BorderFactory.createEmptyBorder(25, 40, 25, 40));

        Font labelFont = FontUtil.plain(18);
        Font valFont   = FontUtil.bold(20);

        statTotalCaught = createStatRow("\uD83C\uDFA3 总钓鱼数", "0", gridPanel, labelFont, valFont);
        statTotalGold   = createStatRow("\uD83D\uDCB0 总赚金币", "0", gridPanel, labelFont, valFont);
        statRareCaught  = createStatRow("\u2728 稀有鱼数", "0", gridPanel, labelFont, valFont);
        statLegendaryCaught = createStatRow("\uD83C\uDF1F 传说鱼数", "0", gridPanel, labelFont, valFont);
        statCollected   = createStatRow("\uD83D\uDCD6 图鉴收集", "0/0", gridPanel, labelFont, valFont);
        statRodLevel    = createStatRow("\uD83C\uDFA3 鱼竿等级", "Lv.1", gridPanel, labelFont, valFont);

        panel.add(gridPanel, BorderLayout.CENTER);
        return panel;
    }

    private JLabel createStatRow(String label, String val, JPanel parent, Font lf, Font vf) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));
        JLabel lbl = new JLabel(label);
        lbl.setFont(lf);
        JLabel vlb = new JLabel(val);
        vlb.setFont(vf);
        vlb.setForeground(new Color(40, 80, 160));
        row.add(lbl);
        row.add(vlb);
        parent.add(row);
        return vlb;
    }

    private void refreshStatsPanel() {
        statTotalCaught.setText(String.valueOf(player.getTotalCaught()));
        statTotalGold.setText(String.valueOf(player.getTotalGoldEarned()));
        statRareCaught.setText(String.valueOf(player.getRareCaught()));
        statLegendaryCaught.setText(String.valueOf(player.getLegendaryCaught()));
        statCollected.setText(player.getCollectedCount() + "/27");
        statRodLevel.setText("Lv." + player.getRodLevel());
    }

    // =========================================================
    //  升级面板
    // =========================================================
    private JPanel createUpgradePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("\u2B06 鱼竿升级"));

        JPanel center = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 10, 12, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        currentRodLabel = new JLabel("\uD83C\uDFA3 当前鱼竿等级：Lv.1 (最大Lv.5)");
        currentRodLabel.setFont(FontUtil.bold(18));
        center.add(currentRodLabel, gbc);

        gbc.gridy = 1;
        upgradeEffectLabel = new JLabel("控制条：10% | 1\u2605鱼简单");
        upgradeEffectLabel.setFont(FontUtil.plain(15));
        center.add(upgradeEffectLabel, gbc);

        gbc.gridy = 2;
        upgradeCostLabel = new JLabel("升级费用：30 金币");
        upgradeCostLabel.setFont(FontUtil.plain(15));
        center.add(upgradeCostLabel, gbc);

        gbc.gridy = 3;
        upgradeConfirmBtn = new JButton("\u2B06 升级鱼竿");
        upgradeConfirmBtn.setFont(FontUtil.bold(15));
        upgradeConfirmBtn.setPreferredSize(new Dimension(160, 45));
        upgradeConfirmBtn.setBackground(new Color(255, 200, 80));
        upgradeConfirmBtn.setFocusPainted(false);
        center.add(upgradeConfirmBtn, gbc);

        gbc.gridy = 4;
        upgradeInfoLabel = new JLabel("");
        upgradeInfoLabel.setFont(FontUtil.plain(14));
        center.add(upgradeInfoLabel, gbc);

        panel.add(center, BorderLayout.CENTER);

        upgradeConfirmBtn.addActionListener(e -> doUpgrade());
        return panel;
    }

    private void refreshUpgradePanel() {
        currentRodLabel.setText("\uD83C\uDFA3 当前鱼竿等级：Lv." + player.getRodLevel() + " (最大Lv.5)");
        int bh = (int)(player.getBarHalf() * 100);
        upgradeEffectLabel.setText("控制条：" + bh + "% | Lv." + player.getRodLevel() + "\u2605鱼正合适");
        if (player.isMaxLevel()) {
            upgradeCostLabel.setText("\uD83C\uDFC6 已满级！");
            upgradeConfirmBtn.setEnabled(false);
            upgradeInfoLabel.setText("鱼竿已达到最高等级！5\u2605传说鱼等你挑战！");
            return;
        }
        int cost = player.getUpgradeCost();
        int nextBh = (int)((player.getRodLevel() + 2) * 5);
        upgradeCostLabel.setText("升级费用：" + cost + " 金币 (控制条 ->" + nextBh + "%)");
        upgradeConfirmBtn.setEnabled(true);
    }

    private void doUpgrade() {
        if (player.isMaxLevel()) {
            upgradeInfoLabel.setText("鱼竿已达到最高等级！");
            return;
        }
        int cost = player.getUpgradeCost();
        if (player.spendGold(cost)) {
            player.upgradeRod();
            refreshUpgradePanel();
            updateStatusBar();
            player.save();
            upgradeInfoLabel.setText("\u2705 升级成功！当前鱼竿等级 Lv." + player.getRodLevel());
            upgradeInfoLabel.setForeground(new Color(0, 130, 0));
            fishLogArea.append("\u2B06 鱼竿升级到 Lv." + player.getRodLevel() + "！\n");
        } else {
            upgradeInfoLabel.setText("\u274C 金币不足！需要 " + cost + " 金币");
            upgradeInfoLabel.setForeground(Color.RED);
        }
    }

    // =========================================================
    //  面板切换
    // =========================================================

    private void switchToFish()       { cardLayout.show(centerPanel, CARD_FISH); }
    private void switchToInventory() {
        refreshInventoryTable();
        cardLayout.show(centerPanel, CARD_INVENTORY);
    }
    private void switchToCollection() {
        refreshCollectionPanel();
        cardLayout.show(centerPanel, CARD_COLLECTION);
    }
    private void switchToStats() {
        refreshStatsPanel();
        cardLayout.show(centerPanel, CARD_STATS);
    }
    private void switchToUpgrade() {
        refreshUpgradePanel();
        cardLayout.show(centerPanel, CARD_UPGRADE);
    }

    // =========================================================
    //  管理员面板
    // =========================================================
    private void openAdminPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("\u2699\uFE0F 管理员功能"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        gbc.gridwidth = 2;
        JLabel tip = new JLabel("管理员专属功能（调试用）");
        tip.setFont(FontUtil.bold(16));
        panel.add(tip, gbc);

        gbc.gridwidth = 1;
        gbc.gridy = 1; gbc.gridx = 0;
        JButton addGold100 = new JButton("\uD83D\uDCB0 +100 金币");
        addGold100.setFont(FontUtil.bold(14));
        addGold100.setBackground(new Color(255, 200, 50));
        addGold100.addActionListener(e -> {
            player.addGold(100);
            updateStatusBar();
            player.save();
            fishLogArea.append("\uD83D\uDCB0 管理员发放了 100 金币\n");
            JOptionPane.showMessageDialog(this, "已增加 100 金币！当前金币：" + player.getGold());
        });
        panel.add(addGold100, gbc);

        gbc.gridx = 1;
        JButton addGold500 = new JButton("\uD83D\uDCB5 +500 金币");
        addGold500.setFont(FontUtil.bold(14));
        addGold500.setBackground(new Color(255, 180, 50));
        addGold500.addActionListener(e -> {
            player.addGold(500);
            updateStatusBar();
            player.save();
            fishLogArea.append("\uD83D\uDCB5 管理员发放了 500 金币\n");
            JOptionPane.showMessageDialog(this, "已增加 500 金币！当前金币：" + player.getGold());
        });
        panel.add(addGold500, gbc);

        gbc.gridy = 2; gbc.gridx = 0;
        JButton setRodBtn = new JButton("\uD83C\uDFA3 满级鱼竿");
        setRodBtn.setFont(FontUtil.bold(14));
        setRodBtn.setBackground(new Color(100, 200, 100));
        setRodBtn.addActionListener(e -> {
            while (!player.isMaxLevel()) player.upgradeRod();
            updateStatusBar();
            player.save();
            fishLogArea.append("\uD83C\uDFA3 管理员将鱼竿升到满级\n");
            JOptionPane.showMessageDialog(this, "鱼竿已满级 Lv.5！");
        });
        panel.add(setRodBtn, gbc);

        gbc.gridx = 1;
        JButton maxGold = new JButton("\uD83D\uDCB8 满金币");
        maxGold.setFont(FontUtil.bold(14));
        maxGold.setBackground(new Color(180, 255, 100));
        maxGold.addActionListener(e -> {
            int amount = 9999 - player.getGold();
            if (amount > 0) player.addGold(amount);
            updateStatusBar();
            player.save();
            fishLogArea.append("\uD83D\uDCB8 管理员已将金币拉满\n");
            JOptionPane.showMessageDialog(this, "金币已满！");
        });
        panel.add(maxGold, gbc);


        gbc.gridy = 3; gbc.gridx = 0;
        gbc.gridwidth = 2;
        JButton resetBtn = new JButton("🔄 重新开始");
        resetBtn.setFont(FontUtil.bold(14));
        resetBtn.setBackground(new Color(200, 60, 60));
        resetBtn.setForeground(Color.WHITE);
        resetBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, "确定重新开始？所有进度将被清空！", "确认", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                Player.deleteSave();
                player.resetState();
                updateStatusBar();
                fishLogArea.append("🔄 游戏已重置\n");
                JOptionPane.showMessageDialog(this, "游戏已重置！");
            }
        });
        panel.add(resetBtn, gbc);
        cardLayout.show(centerPanel, CARD_FISH);

        JOptionPane.showMessageDialog(this, panel, "\u2699\uFE0F 管理员控制台",
                JOptionPane.PLAIN_MESSAGE);
    }

    // =========================================================
    //  入口
    // =========================================================
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}
        SwingUtilities.invokeLater(() -> new FishingGame().setVisible(true));
    }
}
