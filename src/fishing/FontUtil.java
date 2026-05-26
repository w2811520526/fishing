package fishing;

import java.awt.Font;
import java.awt.GraphicsEnvironment;

/**
 * 字体工具 - 自动选择系统中支持中文的字体
 * 避免乱码和方框问题
 */
public class FontUtil {

    private static String chineseFontName = null;

    static {
        String[] preferred = {"微软雅黑", "Microsoft YaHei", "宋体", "SimSun",
                "SimHei", "黑体", "Noto Sans CJK SC", "Source Han Sans",
                "PingFang SC", "WenQuanYi Micro Hei", "Dialog"};
        String[] fonts = GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getAvailableFontFamilyNames();
        for (String p : preferred) {
            for (String f : fonts) {
                if (f.equalsIgnoreCase(p) || f.contains(p)) {
                    chineseFontName = f;
                    break;
                }
            }
            if (chineseFontName != null) break;
        }
        if (chineseFontName == null) {
            chineseFontName = "Dialog"; // 兜底
        }
    }

    public static Font get(int style, int size) {
        return new Font(chineseFontName, style, size);
    }

    public static Font bold(int size) {
        return new Font(chineseFontName, Font.BOLD, size);
    }

    public static Font plain(int size) {
        return new Font(chineseFontName, Font.PLAIN, size);
    }
}
