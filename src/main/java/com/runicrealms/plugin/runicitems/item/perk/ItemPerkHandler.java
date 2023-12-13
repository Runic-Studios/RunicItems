package com.runicrealms.plugin.runicitems.item.perk;

import com.runicrealms.plugin.runicitems.RunicItems;
import com.runicrealms.plugin.runicitems.RunicItemsAPI;
import com.runicrealms.plugin.runicitems.player.PlayerStatHolder;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class ItemPerkHandler {

    protected final ItemPerkType type;
    protected final Map<String, Object> config;
    private final DynamicItemPerkStacksTextPlaceholder dynamicItemPerksStacksTextPlaceholder;

    private final String configName;
    private final @Nullable List<String> configLore;

    @SuppressWarnings("unchecked")
    protected ItemPerkHandler(String identifier) {
        this.config = this.loadConfig(identifier);
        int maxStacks = (Integer) this.config.getOrDefault("max-stacks", 1);
        this.type = new ItemPerkType(identifier, maxStacks);

        this.configName = (String) this.config.getOrDefault("name", this.type.getIdentifier());
        this.configLore = (List<String>) this.config.getOrDefault("lore", null);

        dynamicItemPerksStacksTextPlaceholder = new DynamicItemPerkStacksTextPlaceholder(this); // used to handle lore
        dynamicItemPerksStacksTextPlaceholder.register();
    }

    public ItemPerkType getType() {
        return this.type;
    }

    /**
     * Called when the number of stacks of this item perk changes.
     * Fires async.
     *
     * @param stacks Number of stacks, 0 indicates no stacks (item perk deactivated).
     */
    public abstract void onChange(Player player, int stacks);

    /**
     * Gets the lore for this item perk.
     * This is the portion of the lore that appears on the item for the item perk, NOT INCLUDING the header
     * <p>
     * Header:
     * [?/4] +X Perk Name
     * <p>
     * Lore:
     * explanation lorum ipsum 50% dolor
     * sit amet etc etc etc
     * <p>
     * Null indicates no lore
     */
    public @Nullable List<String> getLoreSection() {
        return configLore;
    }

    /**
     * Returns the display name for this perk to be used on item lore
     */
    public @NotNull String getName() {
        return configName;
    }

    /**
     * Returns the current amount of stacks of this perk that a player has equipped.
     * This is automatically capped by the maximum.
     */
    public int getCurrentStacks(Player player) {
        PlayerStatHolder cache = RunicItemsAPI.getCachedPlayerItems(player.getUniqueId());
        if (cache == null) return 0;
        Set<ItemPerk> activePerks = cache.getTotalStats().getItemPerks();
        for (ItemPerk perk : activePerks) {
            if (perk.getType() == this.type) {
                return perk.getStacks();
            }
        }
        return 0;
    }

    /**
     * Returns the current amount of stacks of this perk that a player has equipped;
     * This is not capped by the maximum number of stacks for this perk.
     */
    public int getCurrentUncappedStacks(Player player) {
        PlayerStatHolder cache = RunicItemsAPI.getCachedPlayerItems(player.getUniqueId());
        if (cache == null) return 0;
        Map<ItemPerkType, Integer> activePerks = cache.getItemPerksExceedingMax();
        int uncappedStacks = activePerks.getOrDefault(this.type, 0);
        if (uncappedStacks != 0) return uncappedStacks;
        return getCurrentStacks(player);
    }

    private Map<String, Object> loadConfig(String identifier) {
        File filePath = new File(RunicItems.getInstance().getDataFolder(), "itemperks/" + identifier + ".yml");
        if (filePath.exists()) {
            try {
                Yaml yaml = new Yaml();
                FileInputStream fileInputStream = new FileInputStream(filePath);
                return yaml.load(fileInputStream);
            } catch (FileNotFoundException exception) {
                throw new IllegalStateException("Missing RunicItems/itemperks/" + identifier + ".yml");
            }
        }
        throw new IllegalStateException("Missing RunicItems/itemperks/" + identifier + ".yml");
    }

    public DynamicItemPerkStacksTextPlaceholder getDynamicItemPerksStacksTextPlaceholder() {
        return this.dynamicItemPerksStacksTextPlaceholder;
    }

}
