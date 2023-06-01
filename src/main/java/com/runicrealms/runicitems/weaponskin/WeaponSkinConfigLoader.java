package com.runicrealms.runicitems.weaponskin;

import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.common.RunicCommon;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;

public class WeaponSkinConfigLoader {

    public static List<WeaponSkin> loadFromConfig(File file) {
        FileConfiguration config = RunicCommon.getConfigAPI().getYamlConfigFromFile(file);
        List<WeaponSkin> skins = new ArrayList<>();
        for (String id : config.getKeys(false)) {
            try {
                ConfigurationSection section = config.getConfigurationSection(id);
                assert section != null;
                Material material = Material.getMaterial(Objects.requireNonNull(section.getString("material")));
                int damage = section.getInt("skin-damage");
                String name = section.getString("name");
                CharacterClass classType = CharacterClass.getFromName(section.getString("class"));
                String achievement = null;
                String rank = null;
                String permission = null;
                if (section.contains("achievement")) achievement = section.getString("achievement");
                if (section.contains("rank")) rank = section.getString("rank");
                if (section.contains("permission")) permission = section.getString("permission");
                skins.add(new WeaponSkin(id, name, material, damage, classType, achievement, rank, permission));
            } catch (Exception exception) {
                Bukkit.getLogger().log(Level.INFO, "ERROR loading weapon skin named " + id);
                exception.printStackTrace();
            }
        }
        return skins;
    }

}
