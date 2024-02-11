package com.runicrealms.plugin.runicitems.loot.chest;

import com.runicrealms.plugin.runicitems.RunicItemsAPI;
import com.runicrealms.plugin.runicitems.item.template.RunicItemTemplate;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class LootChestConditions {

    private final List<Condition> conditions;

    public LootChestConditions() {
        this.conditions = new LinkedList<>();
    }

    public LootChestConditions(Condition... conditions) {
        this.conditions = List.of(conditions);
    }

    public LootChestConditions(List<Condition> conditions) {
        this.conditions = conditions;
    }

    public static LootChestConditions loadFromConfig(ConfigurationSection section) {
        List<Condition> conditions = new LinkedList<>();
        for (String key : section.getKeys(false)) {
            ConfigurationSection subSection = section.getConfigurationSection(key);
            if (subSection == null) continue;
            Condition condition = Condition.loadFromConfig(subSection);
            if (condition == null) continue;
            conditions.add(condition);
        }
        return new LootChestConditions(conditions);
    }

    public boolean attempt(Player player) {
        for (Condition condition : conditions) {
            if (condition.isFulfilled(player)) {
                condition.onComplete(player);
            } else {
                condition.onDeny(player);
                return false;
            }
        }
        return true;
    }

    public void addToConfig(ConfigurationSection section) {
        if (conditions.size() == 0) return;
        int counter = 1;
        for (Condition condition : this.conditions) {
            ConfigurationSection subSection = section.createSection(String.valueOf(counter));
            condition.addToConfig(subSection);
            counter++;
        }
    }

    public List<Condition> getConditionsList() {
        return this.conditions;
    }

    public interface Condition {

        static @Nullable Condition loadFromConfig(ConfigurationSection section) {
            String type = section.getString("type");
            if (type == null || type.isEmpty()) return null;
            if (type.equalsIgnoreCase("item")) {
                return Item.loadFromConfig(section);
            }
            return null;
        }

        boolean isFulfilled(Player player);

        void onDeny(Player player);

        void onComplete(Player player);

        void addToConfig(ConfigurationSection section);

    }

    public static class Item implements Condition {

        private final RunicItemTemplate template;
        private final int count;
        private final boolean takeItem;

        public Item(RunicItemTemplate template, int count, boolean takeItem) {
            this.template = template;
            this.count = count;
            this.takeItem = takeItem;
        }

        private static boolean hasItems(Player player, RunicItemTemplate template, int count, boolean takeItem) { // Checks that a player has the required quest items
            int amount = 0;
            LinkedList<ItemStack> itemsToRemove = new LinkedList<>();
            for (ItemStack item : player.getInventory().getContents()) {
                if (item == null || item.getType() == Material.AIR) continue;
                RunicItemTemplate targetTemplate = RunicItemsAPI.getItemStackTemplate(item);
                if (targetTemplate == null || targetTemplate != template) continue;
                amount += item.getAmount();
                itemsToRemove.add(item);
                if (amount >= count) break;
            }
            if (amount < count) {
                return false;
            } else if (takeItem) {
                int remaining = count;
                for (ItemStack item : itemsToRemove) {
                    int itemAmount = item.getAmount();
                    if (itemAmount <= remaining) {
                        player.getInventory().removeItem(item);
                        remaining -= itemAmount;
                    } else {
                        item.setAmount(itemAmount - remaining);
                        remaining = 0;
                    }
                    if (remaining == 0) break;
                }
            }
            return true;
        }

        private static Item loadFromConfig(ConfigurationSection section) {
            int count = section.getInt("count");
            if (count == 0) throw new NullPointerException("LootChestConditions.Item config count is null!");
            return new Item(
                    Objects.requireNonNull(RunicItemsAPI.getTemplate(section.getString("template-id")), "LootChestConditions.Item config template-id is null!"),
                    count,
                    section.getBoolean("take-item", true));
        }

        @Override
        public boolean isFulfilled(Player player) {
            return hasItems(player, template, count, false);
        }

        @Override
        public void onDeny(Player player) {
            player.sendMessage(ChatColor.RED + "You cannot open this loot chest: missing " + template.getDisplayableItem().getDisplayName());
        }

        @Override
        public void onComplete(Player player) {
            if (takeItem) {
                boolean hadItems = hasItems(player, template, count, true);
                if (!hadItems)
                    throw new IllegalStateException("Attempted to complete LootChestConditions.Item without player " + player.getName() + " meeting the requirement first!");
            }
        }

        @Override
        public void addToConfig(ConfigurationSection section) {
            section.set("type", "item");
            section.set("template-id", this.template.getId());
            section.set("count", this.count);
            section.set("take-item", this.takeItem);
        }

    }

}
