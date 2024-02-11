package com.runicrealms.plugin.runicitems.loot;

import com.runicrealms.plugin.runicitems.RunicItemsAPI;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Random;

public class LootTable {

    private final String identifier;
    private final List<LootItem> items;
    private final int totalWeight;

    public LootTable(String identifier, List<LootItem> items) {
        this.identifier = identifier;
        this.items = items;
        this.totalWeight = items.stream().mapToInt(item -> item.weight).sum();
    }

    public String getIdentifier() {
        return this.identifier;
    }

    public ItemStack generateLoot(LootHolder lootHolder, Player player) {
        Random rand = new Random();
        int value = rand.nextInt(totalWeight);
        for (LootItem item : items) {
            value -= item.weight;
            if (value < 0) {
                return item.generateItem(lootHolder, player);
            }
        }
        throw new IllegalStateException("Unreachable");
    }

    public List<LootItem> getItems() {
        return this.items;
    }

    public static class LootItem {
        private final String templateID;
        private final int weight;
        private final int minStackSize;
        private final int maxStackSize;

        public LootItem(String templateID, int weight, int minStackSize, int maxStackSize) {
            this.templateID = templateID;
            this.weight = weight;
            if (minStackSize > maxStackSize)
                throw new IllegalArgumentException("LootItem min stack size cannot exceed max stack size!");
            this.minStackSize = minStackSize;
            this.maxStackSize = maxStackSize;
        }

        public ItemStack generateItem(LootHolder lootHolder, Player player) {
            return RunicItemsAPI.generateItemFromTemplate(templateID, new Random().nextInt(maxStackSize - minStackSize + 1) + minStackSize).generateItem();
        }

    }

    public static class LootScriptItem extends LootItem {

        public LootScriptItem(int weight) {
            super("", weight, 1, 1);
        }

        @Override
        public ItemStack generateItem(LootHolder lootHolder, Player player) {
            return RunicItemsAPI.generateItemInRange(lootHolder.getItemMinLevel(player), lootHolder.getItemMaxLevel(player), 1).generateItem();
        }
    }

}