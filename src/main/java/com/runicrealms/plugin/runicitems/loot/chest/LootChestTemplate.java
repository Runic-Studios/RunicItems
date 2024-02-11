package com.runicrealms.plugin.runicitems.loot.chest;

import com.runicrealms.plugin.runicitems.loot.LootTable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class LootChestTemplate {

    private final String identifier;
    private final Table[] tables;
    private final int inventorySize;

    public LootChestTemplate(@NotNull String identifier, int inventorySize, @NotNull Table... tables) {
        this.identifier = identifier;
        this.tables = tables;

        if (inventorySize % 9 != 0 || inventorySize < Arrays.stream(tables).mapToInt(Table::getMaxCount).sum()) {
            throw new IllegalArgumentException("Cannot create LootChestTemplate " + identifier + " with inventory size " + inventorySize);
        }

        this.inventorySize = inventorySize;
    }

    public LootChestInventory generateInventory(@NotNull LootChest lootChest, @NotNull Player player) {
        Set<ItemStack> items = new HashSet<>();

        for (Table lootTable : this.tables) {
            int itemCount = ThreadLocalRandom.current().nextInt(lootTable.getMaxCount() - lootTable.getMinCount() + 1) + lootTable.getMinCount();

            for (int i = 0; i < itemCount; i++) {
                items.add(lootTable.getLootTable().generateLoot(lootChest));
            }
        }

        return new LootChestInventory(lootChest, items, inventorySize, lootChest.getInventoryTitle(), null);
    }

    @NotNull
    public String getIdentifier() {
        return this.identifier;
    }

    public static class Table {
        private final LootTable lootTable;
        private final int minCount;
        private final int maxCount;

        public Table(@NotNull LootTable lootTable, int minCount, int maxCount) {
            this.lootTable = lootTable;
            this.minCount = minCount;
            this.maxCount = maxCount;

            if (minCount > maxCount) {
                throw new IllegalArgumentException("LootChestTemplate " + lootTable.getIdentifier() + " min count cannot exceed max count!");
            }
        }

        @NotNull
        public LootTable getLootTable() {
            return this.lootTable;
        }

        public int getMinCount() {
            return this.minCount;
        }

        public int getMaxCount() {
            return this.maxCount;
        }
    }
}
