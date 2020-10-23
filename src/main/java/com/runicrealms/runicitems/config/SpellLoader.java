package com.runicrealms.runicitems.config;

import com.runicrealms.runicitems.Plugin;
import com.runicrealms.runicitems.SpellManager;
import com.runicrealms.runicitems.item.stats.RunicSpell;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class SpellLoader {

    public static void loadSpells() {
        Map<String, RunicSpell> spells = new HashMap<String, RunicSpell>();
        File file = new File(Plugin.getInstance().getDataFolder(), "spells.yml");
        if (file.exists()) {
            FileConfiguration spellConfig = ConfigUtil.getYamlConfigFile(file.getName(), Plugin.getInstance().getDataFolder());
            for (String key : spellConfig.getKeys(false)) {
                spells.put(key, new RunicSpell(key, spellConfig.getString(key + ".name"), spellConfig.getString(key + ".description")));
            }
        } else {
            try {
                file.createNewFile();
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
        SpellManager.setSpells(spells);
    }

}
