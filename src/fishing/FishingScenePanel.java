package fishing;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * 钓鱼场景面板 - 绘制水面、浮漂、鱼影、蓄力条
 */
public class FishingScenePanel extends JPanel {

    public enum State { IDLE, CASTING, WAITING, FISH_NEAR, BITING }

    private State state = State.IDLE;
    private double powerValue = 0;       // 蓄力值 0~1
    private double bobberY = 0.40f;      // 浮漂上下位置
    private double fishX = -0.2f;        // 鱼影水平位置
    private double fishAlpha = 0;        // 鱼影透明度
    private float bobPhase = 0;          // 动画相位
    private boolean powerHolding = false; // 是否在蓄力
    private int areaLeft, areaWidth, waterLine; // 绘制区域缓存

    private Timer animTimer;
    private Runnable onPowerRelease;     // 蓄力释放回调
    private Runnable onBiteTimeout;      // 咬钩超时回调

    public FishingScenePanel() {
        setPreferredSize(new Dimension(400, 280));
        setBackground(new Color(135, 190, 235));

        // 鼠标蓄力控制
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (state == State.CASTING) {
                    powerHolding = true;
                }
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                if (state == State.CASTING && powerHolding) {
                    powerHolding = false;
                    if (onPowerRelease != null) onPowerRelease.run();
                }
            }
        });

        // 动画循环（40ms => 25fps，够用）
        animTimer = new Timer(40, e -> {
            animate();
            repaint();
        });
        animTimer.start();
    }

    public void setOnPowerRelease(Runnable r) { this.onPowerRelease = r; }
    public void setOnBiteTimeout(Runnable r) { this.onBiteTimeout = r; }

    public double getPowerValue() { return powerValue; }
    public State getState() { return state; }

    public void startCasting() {
        state = State.CASTING;
        powerValue = 0;
        powerHolding = false;
    }

    public void startWaiting() {
        state = State.WAITING;
        bobberY = 0.40;
        fishX = -0.2;
        fishAlpha = 0;
    }

    public void showFishNear() {
        state = State.FISH_NEAR;
        fishX = -0.2;
        fishAlpha = 0;
    }

    public void showBiting() {
        state = State.BITING;
        // 浮漂下沉 + 水花
    }

    public void setIdle() {
        state = State.IDLE;
        powerValue = 0;
    }

    private void animate() {
        bobPhase += 0.06f;
        switch (state) {
            case CASTING:
                if (powerHolding) {
                    powerValue += 0.025;
                    if (powerValue > 1) powerValue = 1;
                }
                break;
            case WAITING:
                // 浮漂轻微上下浮动
                bobberY = 0.40 + Math.sin(bobPhase) * 0.008;
                break;
            case FISH_NEAR:
                // 鱼影从右向左游
                if (fishX < 1.3) fishX += 0.012;
                if (fishAlpha < 0.5) fishAlpha += 0.02;
                // 浮漂抖动
                bobberY = 0.40 + Math.sin(bobPhase * 2) * 0.012;
                break;
            case BITING:
                // 浮漂下沉
                bobberY += 0.008;
                if (bobberY > 0.55) bobberY = 0.55;
                break;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth(), h = getHeight();
        waterLine = h * 55 / 100;
        areaLeft = w / 2 - 50;
        areaWidth = 100;

        // ---- 天空 ----
        GradientPaint sky = new GradientPaint(0, 0, new Color(100, 180, 240),
                0, waterLine, new Color(200, 230, 255));
        g2d.setPaint(sky);
        g2d.fillRect(0, 0, w, waterLine);

        // 远处山丘
        g2d.setColor(new Color(80, 150, 90, 60));
        for (int i = 0; i < 3; i++) {
            int cx = w * i / 4, cy = waterLine - 20;
            g2d.fillArc(cx - 60, cy - 40, 120, 80, 0, 180);
        }

        // ---- 水面 ----
        Color waterTop = new Color(50, 150, 210);
        Color waterBot = new Color(20, 80, 140);
        int si = 0; // simplified - just use lake
        try { String spot = UIManager.getString("spot"); } catch(Exception e) {}
        GradientPaint water = new GradientPaint(0, waterLine, waterTop, 0, h, waterBot);
        g2d.setPaint(water);
        g2d.fillRect(0, waterLine, w, h - waterLine);

        // 水纹
        g2d.setStroke(new BasicStroke(1));
        for (int i = 0; i < 8; i++) {
            g2d.setColor(new Color(255, 255, 255, 15 + i * 5));
            int by = waterLine + (h - waterLine) * i / 8;
            int[] xs = new int[w];
            int[] ys = new int[w];
            for (int x = 0; x < w; x++) {
                xs[x] = x;
                ys[x] = by + (int)(Math.sin((x + bobPhase * 30 + i * 20) * 0.05) * 3);
            }
            g2d.drawPolyline(xs, ys, w);
        }

        // ---- 浮漂主杆线 ----
        int bobberCX = w / 2;
        int bobberCY = (int)(waterLine + (h - waterLine) * bobberY);

        if (state != State.IDLE && state != State.CASTING) {
            // 鱼线
            g2d.setColor(new Color(200, 200, 200, 150));
            g2d.setStroke(new BasicStroke(1));
            g2d.drawLine(bobberCX, 10, bobberCX, bobberCY);
        }

        // ---- 浮漂 ----
        if (state == State.WAITING || state == State.FISH_NEAR) {
            int r = 7;
            // 发光
            g2d.setColor(new Color(255, 255, 200, 40));
            g2d.fillOval(bobberCX - r - 3, bobberCY - r - 3, (r + 3) * 2, (r + 3) * 2);
            // 浮漂主体（上红下白）
            g2d.setColor(new Color(220, 50, 50));
            g2d.fillOval(bobberCX - r, bobberCY - r, r * 2, r * 2);
            g2d.setColor(new Color(255, 255, 255));
            g2d.fillOval(bobberCX - r / 2, bobberCY - r / 2, r, r);

            // 涟漪
            g2d.setStroke(new BasicStroke(1));
            for (int ri = 0; ri < 3; ri++) {
                int s = ri * 4 + 10;
                g2d.setColor(new Color(255, 255, 255, 40 - ri * 10));
                g2d.drawOval(bobberCX - s / 2, bobberCY - s / 4, s, s / 2);
            }
        }

        // ---- 鱼影（FISH_NEAR / BITING） ----
        if (state == State.FISH_NEAR || state == State.BITING) {
            int fx = (int)(fishX * w / 2);
            int fy = waterLine + (h - waterLine) * 60 / 100;

            g2d.setColor(new Color(60, 60, 60, (int)(fishAlpha * 80)));
            g2d.fillOval(fx - 25, fy - 8, 50, 16);
            g2d.fillOval(fx - 20, fy - 12, 30, 8);

            // 鱼鳍露出一半
            if (state == State.FISH_NEAR) {
                g2d.setColor(new Color(40, 40, 40, (int)(fishAlpha * 60)));
                g2d.fillOval(fx, fy - 18, 12, 10);
            }
        }

        // ---- 咬钩水花 ----
        if (state == State.BITING) {
            g2d.setColor(new Color(255, 255, 255, 80));
            for (int s = 0; s < 5; s++) {
                int sx = bobberCX + (int)(Math.sin(bobPhase + s * 1.3) * (s * 3 + 5));
                int sy = bobberCY - 5 + s * 2;
                g2d.fillOval(sx - 2, sy - 2, 4, 4);
            }
        }

        // ---- 蓄力条（CASTING） ----
        if (state == State.CASTING) {
            int barLeft = 20;
            int barWidth = 20;
            int barHeight = h - 60;
            int barTop = 30;

            // 背景
            g2d.setColor(new Color(30, 30, 50, 180));
            g2d.fillRoundRect(barLeft, barTop, barWidth, barHeight, 8, 8);

            // 蓄力填充
            int fillH = (int)(barHeight * powerValue);
            int fillTop = barTop + barHeight - fillH;
            Color powerColor;
            if (powerValue < 0.4) powerColor = new Color(50, 255, 80);
            else if (powerValue < 0.7) powerColor = new Color(255, 200, 50);
            else powerColor = new Color(255, 60, 60);
            g2d.setColor(powerColor);
            g2d.fillRoundRect(barLeft + 2, fillTop, barWidth - 4, fillH, 6, 6);

            // 边框
            g2d.setColor(new Color(200, 200, 220, 180));
            g2d.setStroke(new BasicStroke(1.5f));
            g2d.drawRoundRect(barLeft, barTop, barWidth, barHeight, 8, 8);

            // 文字
            g2d.setFont(FontUtil.bold(12));
            g2d.setColor(Color.WHITE);
            g2d.drawString("蓄力", barLeft - 2, barTop - 8);
            String powStr = (int)(powerValue * 100) + "%";
            FontMetrics fm = g2d.getFontMetrics();
            g2d.drawString(powStr, barLeft + (barWidth - fm.stringWidth(powStr)) / 2, barTop + barHeight + 18);

            // 提示
            if (!powerHolding) {
                g2d.setFont(FontUtil.plain(13));
                g2d.setColor(new Color(255, 255, 200));
                String tip = "按住鼠标蓄力，松开抛竿";
                fm = g2d.getFontMetrics();
                g2d.drawString(tip, (w - fm.stringWidth(tip)) / 2, h - 12);
            }
        }

        // ---- IDLE 提示 ----
        if (state == State.IDLE) {
            g2d.setFont(FontUtil.plain(15));
            g2d.setColor(new Color(255, 255, 255, 180));
            String tip = "点击上方「抛竿」开始钓鱼";
            FontMetrics fm = g2d.getFontMetrics();
            g2d.drawString(tip, (w - fm.stringWidth(tip)) / 2, h / 2 + 30);
        }
    }
}
