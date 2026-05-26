package fishing;

import java.awt.Color;

public class Fish {
    private String name;
    private int level;       // 1★~5★
    private String rarity;   // 由 level 派生
    private double weight;
    private int price;
    private String emoji;
    private String desc;
    private String spot;

    public Fish(String name, int level, double weight, int price,
                String emoji, String desc, String spot) {
        this.name = name;
        this.level = level;
        this.rarity = level >= 5 ? "传说" : level >= 3 ? "稀有" : "普通";
        this.weight = Math.round(weight * 10) / 10.0;
        this.price = price;
        this.emoji = emoji;
        this.desc = desc;
        this.spot = spot;
    }

    public String getName() { return name; }
    public int getLevel() { return level; }
    public String getRarity() { return rarity; }
    public double getWeight() { return weight; }
    public int getPrice() { return price; }
    public String getEmoji() { return emoji; }
    public String getDesc() { return desc; }
    public String getSpot() { return spot; }

    public Color getRarityColor() {
        switch (rarity) {
            case "传说": return new Color(255, 160, 0);
            case "稀有": return new Color(100, 100, 255);
            default: return new Color(120, 120, 120);
        }
    }

    public String getRarityEmoji() {
        switch (rarity) {
            case "传说": return "🌟";
            case "稀有": return "✨";
            default: return "🐟";
        }
    }

    public String getLevelStars() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < level; i++) sb.append('\u2605'); // ★
        for (int i = level; i < 5; i++) sb.append('\u2606'); // ☆
        return sb.toString();
    }

    @Override
    public String toString() {
        return emoji + " " + name + " (" + getLevelStars() + ") " + weight + "斤 " + price + "金币";
    }
}
