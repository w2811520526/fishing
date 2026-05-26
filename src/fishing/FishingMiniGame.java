package fishing;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Random;

/**
 * 星露谷风格钓鱼小游戏
 * - 垂直界面，鱼上下游动
 * - 玩家控制的绿色控制条（有惯性物理）
 * - 右侧进度条，根据水域显示不同水面风格
 */
public class FishingMiniGame extends JDialog {

    public interface Callback {
        void onResult(boolean success, boolean perfect, int accuracyPercent);
    }

    // ============== 常量 ==============
    private static final int GAME_WIDTH = 230;
    private static final int GAME_HEIGHT = 400;
    private static final int MARGIN = 25;
    private static final int PLAY_AREA_WIDTH = 120;
    private static final int PROGRESS_WIDTH = 22;
    private static final int FISH_SIZE = 14;

    // ============== 物理参数 ==============
    private double fishY, fishTargetY, fishSpeed, barY, barVelocity, progress;
    private boolean isHolding, perfect, fishEscaped, finished, isLegendary;
    private int difficulty, fishChangeTimer, missedFrames, rodLevel, fishLevel;
    private Random random = new Random();
    private String fishEmoji, fishName, spot;
    private int dashCooldown;     // 传说鱼冲刺冷却
    private int dashActive;       // 冲刺进行中的帧数
    private double dashSpeedMul;  // 冲刺速度倍率

    private GamePanel gamePanel;
    private Timer gameLoop;
    private Callback callback;

    // 各水域配色
    private static final Color[][] WATER_COLORS = {
        // 湖泊
        {new Color(100, 180, 240), new Color(60, 140, 210)},
        // 河流
        {new Color(80, 200, 160), new Color(40, 150, 120)},
        // 海洋
        {new Color(30, 120, 200), new Color(10, 60, 140)},
    };

    public FishingMiniGame(JFrame owner, int difficulty, String fishEmoji,
                           String fishName, String spot, int rodLevel, int fishLevel, Callback callback) {
        super(owner, "\uD83C\uDFA3 钓鱼！", true);
        this.difficulty = difficulty;
        this.fishEmoji = fishEmoji;
        this.fishName = fishName;
        this.spot = spot;
        this.rodLevel = rodLevel;
        this.fishLevel = fishLevel;
        this.isLegendary = fishLevel >= 5;
        this.callback = callback;

        fishY = 0.3 + random.nextDouble() * 0.4;
        fishTargetY = fishY;
        fishSpeed = 0.0015 + difficulty * 0.0003;
        barY = 0.5;
        barVelocity = 0;
        progress = 0;
        perfect = true;
        fishEscaped = false;
        finished = false;
        missedFrames = 0;
        dashCooldown = 0;
        dashActive = 0;
        dashSpeedMul = 1.0;

        gamePanel = new GamePanel();
        gamePanel.setPreferredSize(new Dimension(GAME_WIDTH, GAME_HEIGHT));
        gamePanel.setBackground(new Color(30, 60, 110));
        gamePanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        gamePanel.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) { isHolding = true; }
            public void mouseReleased(MouseEvent e) { isHolding = false; }
        });

        setLayout(new BorderLayout());
        add(gamePanel, BorderLayout.CENTER);
        pack();
        setLocationRelativeTo(owner);
        setResizable(false);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        gameLoop = new Timer(16, e -> { update(); gamePanel.repaint(); });
    }

    public void start() { gameLoop.start(); setVisible(true); }

    private void update() {
        if (finished) return;

        // 1. 鱼移动
        fishChangeTimer--;
        if (fishChangeTimer <= 0) {
            double range = 0.15 + difficulty * 0.003;
            if (isLegendary) range += 0.05; // 传说鱼跳更远
            fishTargetY = fishY + (random.nextDouble() - 0.5) * range;
            fishTargetY = Math.max(0.05, Math.min(0.95, fishTargetY));
            fishChangeTimer = isLegendary
                ? Math.max(8, 40 - difficulty / 3)  // 传说鱼换向更快
                : Math.max(15, 60 - difficulty / 3);
        }
        double diff = fishTargetY - fishY;
        double moveSpeed = (fishSpeed + difficulty * 0.0002) * dashSpeedMul;
        if (Math.abs(diff) > 0.01) fishY += Math.signum(diff) * moveSpeed;
        fishY = Math.max(0.02, Math.min(0.98, fishY));

        // 1b. 传说鱼特殊机制：平滑冲刺（不瞬移，高速游向目标）
        if (isLegendary) {
            if (dashActive > 0) {
                // 冲刺中：高速游向目标
                dashActive--;
                dashSpeedMul = 6.0;
            } else {
                dashSpeedMul = isLegendary ? 1.3 : 1.0;
                dashCooldown--;
                if (dashCooldown <= 0) {
                    // 设定一个远端目标开始冲刺
                    fishTargetY = 0.05 + random.nextDouble() * 0.9;
                    dashActive = 15 + random.nextInt(15); // 0.25~0.5秒冲刺
                    dashCooldown = 25 + random.nextInt(35); // 0.4~1.0秒冷却
                    // 偶尔连冲
                    if (random.nextInt(3) == 0) {
                        dashCooldown = 5;
                    }
                }
            }
        }

        // 2. 控制条物理（降低难度）
        double gravity = 0.010;                  // 更轻的重力
        double raiseForce = 0.040;               // 更大的抬升力
        barVelocity += isHolding ? -raiseForce : gravity;
        barVelocity *= 0.94;                     // 更小的阻尼
        double maxSpeed = 0.025;                 // 更低的最大速度
        if (barVelocity > maxSpeed) barVelocity = maxSpeed;
        if (barVelocity < -maxSpeed) barVelocity = -maxSpeed;
        barY += barVelocity;
        double barHalf = getBarHalf();
        if (barY < barHalf) { barY = barHalf; barVelocity = 0; }
        if (barY > 1 - barHalf) { barY = 1 - barHalf; barVelocity = 0; }

        // 3. 进度（更友好）
        boolean inBar = Math.abs(fishY - barY) < (barHalf + 0.02);
        if (inBar) {
            progress += 0.003 + (1.0 / Math.max(1, difficulty)) * 0.0005;
            missedFrames = 0;
        } else {
            progress -= 0.004;                   // 减少丢失进度
            missedFrames++;
            perfect = false;
            if (missedFrames > 80) {             // 更多容错时间
                fishEscaped = true;
                finished = true;
                gameLoop.stop();
                repaint();
                new Timer(800, e -> { callback.onResult(false, false, 0); dispose(); })
                    {{ setRepeats(false); }}.start();
                return;
            }
        }
        progress = Math.max(0, Math.min(1, progress));

        if (progress >= 1) {
            finished = true;
            gameLoop.stop();
            repaint();
            new Timer(800, e -> { callback.onResult(true, perfect, (int)(progress*100)); dispose(); })
                {{ setRepeats(false); }}.start();
        }
    }

    private int getSpotIndex() {
        for (int i = 0; i < Player.SPOTS.length; i++)
            if (Player.SPOTS[i].equals(spot)) return i;
        return 0;
    }

    /** 根据鱼竿等级计算控制条半宽: Lv.1=1cm, Lv.5=半个条 */
    private double getBarHalf() {
        return 0.05 * rodLevel;
    }

    // ============== 绘制 ==============
    private class GamePanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int areaLeft = MARGIN;
            int areaTop = 18;
            int areaWidth = PLAY_AREA_WIDTH;
            int areaHeight = GAME_HEIGHT - 38;

            // ---- 水域背景 ----
            int si = getSpotIndex();
            Color c1 = WATER_COLORS[si][0], c2 = WATER_COLORS[si][1];

            // 水面渐变
            GradientPaint waterGrad = new GradientPaint(0, areaTop, c1, 0, areaTop + areaHeight, c2);
            g2d.setPaint(waterGrad);
            g2d.fillRect(areaLeft, areaTop, areaWidth, areaHeight);

            // 水纹波浪效果（半透明波浪线）
            g2d.setStroke(new BasicStroke(1.2f));
            for (int w = 0; w < 6; w++) {
                g2d.setColor(new Color(255, 255, 255, 20 + w * 8));
                int baseY = areaTop + areaHeight * w / 6;
                int[] xs = new int[areaWidth];
                int[] ys = new int[areaWidth];
                for (int x = 0; x < areaWidth; x++) {
                    xs[x] = areaLeft + x;
                    ys[x] = baseY + (int)(Math.sin((x + System.currentTimeMillis()*0.0008) * 0.12) * 4);
                }
                g2d.drawPolyline(xs, ys, areaWidth);
            }

            // ---- 水草装饰 ----
            g2d.setColor(new Color(40, 180, 60, 80));
            for (int gx = 0; gx < 3; gx++) {
                int gbx = areaLeft + 10 + gx * 35;
                int gby = areaTop + areaHeight - 10;
                g2d.setStroke(new BasicStroke(3));
                g2d.drawArc(gbx, gby - 25, 15, 30, 0, 180);
                g2d.drawArc(gbx + 5, gby - 18, 12, 24, 0, 180);
            }

            // ---- 气泡装饰 ----
            g2d.setColor(new Color(255, 255, 255, 30));
            int[] bubbleX = {areaLeft + 25, areaLeft + 80, areaLeft + 50, areaLeft + 95};
            int[] bubbleY = {areaTop + areaHeight - 30, areaTop + areaHeight - 50,
                             areaTop + areaHeight - 80, areaTop + areaHeight - 20};
            for (int b = 0; b < 4; b++) {
                int bs = 3 + (b * 2);
                g2d.fillOval(bubbleX[b], bubbleY[b], bs, bs);
            }

            // ---- 边框 ----
            g2d.setColor(WATER_COLORS[si][0].brighter());
            g2d.setStroke(new BasicStroke(2));
            g2d.drawRect(areaLeft, areaTop, areaWidth, areaHeight);

            // ---- 刻度线 ----
            g2d.setColor(new Color(255, 255, 255, 40));
            for (int i = 1; i < 10; i++) {
                int y = areaTop + areaHeight * i / 10;
                g2d.drawLine(areaLeft, y, areaLeft + areaWidth, y);
            }

            // ---- 绿色控制条 ----
            double bh = getBarHalf();
            int barTop = (int)(areaTop + (barY - bh) * areaHeight);
            int barBot = (int)(areaTop + (barY + bh) * areaHeight);
            int barH = barBot - barTop;

            // 外发光
            g2d.setColor(new Color(100, 255, 120, 50));
            g2d.fillRoundRect(areaLeft - 3, barTop - 3, areaWidth + 6, barH + 6, 10, 10);

            // 渐变主体
            GradientPaint bg = new GradientPaint(0, barTop, new Color(60, 255, 100),
                    0, barBot, new Color(0, 200, 60));
            g2d.setPaint(bg);
            g2d.fillRoundRect(areaLeft + 2, barTop, areaWidth - 4, barH, 8, 8);
            g2d.setColor(new Color(30, 220, 70));
            g2d.setStroke(new BasicStroke(1.5f));
            g2d.drawRoundRect(areaLeft + 2, barTop, areaWidth - 4, barH, 8, 8);
            // 高光
            g2d.setColor(new Color(255, 255, 255, 60));
            g2d.fillRoundRect(areaLeft + 6, barTop + 2, areaWidth - 12, barH/3, 5, 5);

            // ---- 鱼 ----
            int fishPx = areaLeft + areaWidth / 2;
            int fishPy = (int)(areaTop + fishY * areaHeight);
            boolean inBar = Math.abs(fishY - barY) < (bh + 0.02);
            Color fishCol = inBar ? new Color(255, 230, 60) : new Color(255, 90, 90);

            g2d.setColor(new Color(fishCol.getRed(), fishCol.getGreen(), fishCol.getBlue(), 40));
            g2d.fillOval(fishPx - 10, fishPy - 9, 24, 18);
            g2d.setColor(fishCol);
            g2d.fillOval(fishPx - 6, fishPy - 7, 14, 14);
            g2d.setColor(Color.WHITE);
            g2d.fillOval(fishPx + 2, fishPy - 4, 5, 5);
            g2d.setColor(Color.BLACK);
            g2d.fillOval(fishPx + 3, fishPy - 3, 3, 3);
            int[] tx = {fishPx - 6, fishPx - 14, fishPx - 6};
            int[] ty = {fishPy - 4, fishPy, fishPy + 4};
            g2d.setColor(fishCol);
            g2d.fillPolygon(tx, ty, 3);

            // 传说鱼冲刺特效
            if (isLegendary && dashCooldown > 40) {
                g2d.setStroke(new BasicStroke(1.5f));
                g2d.setColor(new Color(255, 200, 50, 150));
                for (int s = 0; s < 3; s++) {
                    int sx = fishPx - 18 - s * 8;
                    int sy = fishPy - 4 + random.nextInt(9);
                    g2d.drawLine(sx, sy, sx + 6, sy);
                }
            }

            // 鱼名标签
            g2d.setFont(FontUtil.bold(10));
            g2d.setColor(Color.WHITE);
            g2d.drawString(fishEmoji + fishName, fishPx - 16, fishPy - 12);

            // ---- 进度条 ----
            int pl = areaLeft + areaWidth + 12, pw = PROGRESS_WIDTH;
            g2d.setColor(new Color(50, 50, 70));
            g2d.fillRoundRect(pl, areaTop, pw, areaHeight, 7, 7);

            int fh = (int)(areaHeight * progress);
            int ft = areaTop + areaHeight - fh;
            GradientPaint pg = new GradientPaint(0, ft, new Color(80, 220, 255),
                    0, areaTop + areaHeight, new Color(0, 120, 220));
            g2d.setPaint(pg);
            g2d.fillRoundRect(pl + 1, ft, pw - 2, fh, 5, 5);
            g2d.setColor(new Color(120, 180, 240));
            g2d.setStroke(new BasicStroke(1));
            g2d.drawRoundRect(pl, areaTop, pw, areaHeight, 7, 7);

            g2d.setFont(FontUtil.bold(10));
            g2d.setColor(Color.WHITE);
            String pct = (int)(progress*100) + "%";
            g2d.drawString(pct, pl + (pw - g2d.getFontMetrics().stringWidth(pct))/2, areaTop + areaHeight/2 + 3);

            // ---- 底部状态文字 ----
            g2d.setFont(FontUtil.plain(12));
            if (finished && progress >= 1) {
                g2d.setColor(new Color(255, 230, 80));
                String msg = perfect ? "\u2728 完美钓鱼！" : "\u2705 成功！";
                g2d.drawString(msg, areaLeft + (areaWidth - g2d.getFontMetrics().stringWidth(msg))/2, areaTop + areaHeight + 20);
            } else if (finished) {
                g2d.setColor(new Color(255, 100, 100));
                String msg = "\u274C 鱼跑了...";
                g2d.drawString(msg, areaLeft + (areaWidth - g2d.getFontMetrics().stringWidth(msg))/2, areaTop + areaHeight + 20);
            } else {
                g2d.setColor(new Color(200, 220, 240));
                g2d.drawString("\uD83D\uDC46按住鼠标上提，松开下降", areaLeft - 5, areaTop + areaHeight + 20);
            }

            // ---- 传说鱼标记 ----
            if (isLegendary) {
                g2d.setFont(FontUtil.bold(10));
                g2d.setColor(new Color(255, 180, 0));
                String lg = "\uD83C\uDF1F 传说鱼 \u26A1 冲刺中";
                g2d.drawString(lg, areaLeft + areaWidth - g2d.getFontMetrics().stringWidth(lg) - 4, areaTop - 6);
            }

            // ---- 难度 ----
            g2d.setFont(FontUtil.bold(11));
            if (difficulty < 20) { g2d.setColor(new Color(130, 255, 130));
                g2d.drawString("\u2605 简单", areaLeft, areaTop - 6); }
            else if (difficulty < 50) { g2d.setColor(new Color(255, 210, 60));
                g2d.drawString("\u2605\u2605 中等", areaLeft, areaTop - 6); }
            else if (difficulty < 80) { g2d.setColor(new Color(255, 120, 60));
                g2d.drawString("\u2605\u2605\u2605 困难", areaLeft, areaTop - 6); }
            else { g2d.setColor(new Color(255, 60, 60));
                g2d.drawString("\u2605\u2605\u2605\u2605 传说", areaLeft, areaTop - 6); }
        }
    }
}
