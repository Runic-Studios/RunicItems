package com.runicrealms.plugin.runicitems.item.perk;

import com.runicrealms.plugin.runicitems.RunicItems;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Map;

public abstract class ItemPerkHandler implements Listener {

    protected final String identifier;
    protected final ItemPerkType type;
    protected final Map<String, Object> config;

    protected ItemPerkHandler(String identifier) {
        this.identifier = identifier;
        this.config = this.loadConfig();
        int maxStacks = (Integer) this.config.getOrDefault("max-stacks", 1);
        this.type = new ItemPerkType(identifier, maxStacks);
        ItemPerkManager.registerItemPerk(this);
        Bukkit.getPluginManager().registerEvents(this, RunicItems.getInstance());
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
    public abstract void onChange(int stacks);

    private Map<String, Object> loadConfig() {
        File filePath = new File(RunicItems.getInstance().getDataFolder(), "itemperks/" + this.identifier + ".yml");
        if (filePath.exists()) {
            try {
                Yaml yaml = new Yaml();
                FileInputStream fileInputStream = new FileInputStream(filePath);
                return yaml.load(fileInputStream);
            } catch (FileNotFoundException exception) {
                throw new IllegalStateException("Missing RunicItems/itemperks/" + this.identifier + ".yml");
            }
        }
        throw new IllegalStateException("Missing RunicItems/itemperks/" + this.identifier + ".yml");
    }

}
