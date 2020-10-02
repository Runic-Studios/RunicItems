package com.runicrealms.runicitems.config;

import com.runicrealms.runicitems.Plugin;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class SpellLoader {

    private static Map<String, String> spells = new HashMap<String, String>();

    public static void loadSpells() {
        File file = new File(Plugin.getInstance().getDataFolder(), "spells.yml");
        if (file.exists()) {
            FileConfiguration spellConfig = ConfigUtil.getYamlConfigFile(file.getName(), Plugin.getInstance().getDataFolder());
            for (String key : spellConfig.getKeys(false)) {
                spells.put(key, spellConfig.getString(key));
            }
        } else {
            try {
                file.createNewFile();
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
    }

    public static String getSpellDescription(String identifier) {
        return spells.get(identifier);
    }

}
