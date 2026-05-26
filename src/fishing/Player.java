package fishing;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;

public class Player implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final String SAVE_FILE = "fishing_save.dat";
    private int gold = 20;                 // 初始金币
    private int rodLevel = 1;              // 鱼竿等级 1~5
    private String currentSpot = "湖泊";    // 当前钓鱼点
    private ArrayList<Fish> inventory;     // 背包
    private HashSet<String> collection;    // 图鉴（已钓过的鱼种）
    private int totalCaught = 0;           // 总钓鱼数
    private int totalGoldEarned = 0;       // 总赚金币
    private int rareCaught = 0;            // 稀有鱼数
    private int legendaryCaught = 0;       // 传说鱼数

    public static final String[] SPOTS = {"湖泊", "河流", "海洋"};

    public Player() {
        inventory = new ArrayList<>();
        collection = new HashSet<>();
    }

    // ===== 金币 =====
    public int getGold() { return gold; }
    public void addGold(int amount) { gold += amount; }
    public boolean spendGold(int amount) {
        if (gold >= amount) {
            gold -= amount;
            return true;
        }
        return false;
    }

    // ===== 鱼竿 =====
    public int getRodLevel() { return rodLevel; }
    public void upgradeRod() { rodLevel++; }
    public int getUpgradeCost() { return rodLevel * 30; }
    public boolean isMaxLevel() { return rodLevel >= 5; }

    /** 钓鱼成功率 */
    public double getSuccessRate() { return 0.5 + rodLevel * 0.1; }

    /** 控制条大小（0~1），Lv.1=10%, Lv.5=30% */
    public double getBarHalf() { return 0.05 * rodLevel + 0.05; }

    // ===== 钓鱼点 =====
    public String getCurrentSpot() { return currentSpot; }
    public void setCurrentSpot(String spot) { currentSpot = spot; }

    // ===== 背包 =====
    public ArrayList<Fish> getInventory() { return inventory; }

    public void addFish(Fish fish) {
        inventory.add(fish);
        collection.add(fish.getName());
        totalCaught++;
        switch (fish.getRarity()) {
            case "传说": legendaryCaught++; break;
            case "稀有": rareCaught++; break;
        }
    }

    public void removeFish(int index) {
        if (index >= 0 && index < inventory.size()) {
            inventory.remove(index);
        }
    }

    public int getFishCount() { return inventory.size(); }

    public int getTotalInventoryValue() {
        int total = 0;
        for (Fish f : inventory) {
            total += f.getPrice();
        }
        return total;
    }

    public void recordGoldEarned(int amount) { totalGoldEarned += amount; }

    // ===== 图鉴 =====
    public boolean hasCollected(String fishName) { return collection.contains(fishName); }
    public int getCollectedCount() { return collection.size(); }

    // ===== 统计 =====
    public int getTotalCaught() { return totalCaught; }
    public int getTotalGoldEarned() { return totalGoldEarned; }
    public int getRareCaught() { return rareCaught; }
    public int getLegendaryCaught() { return legendaryCaught; }

    // ===== 存档 =====
    public void save() {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(SAVE_FILE))) {
            out.writeObject(this);
        } catch (IOException e) {
            System.err.println("保存失败: " + e.getMessage());
        }
    }

    public static Player load() {
        File f = new File(SAVE_FILE);
        if (!f.exists()) return null;
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(f))) {
            return (Player) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("读取存档失败: " + e.getMessage());
            return null;
        }
    }

    public void resetState() {
        gold = 20;
        rodLevel = 1;
        currentSpot = "湖泊";
        inventory.clear();
        collection.clear();
        totalCaught = 0;
        totalGoldEarned = 0;
        rareCaught = 0;
        legendaryCaught = 0;
    }

    public static void deleteSave() {
        new File(SAVE_FILE).delete();
    }
}
