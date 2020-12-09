package com.runicrealms.runicitems.config;

import com.runicrealms.runicitems.Plugin;
import com.runicrealms.runicitems.AbilityManager;
import com.runicrealms.runicitems.item.stats.RunicArtifactAbility;
import com.runicrealms.runicitems.item.util.RunicArtifactAbilityTrigger;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class AbilityLoader {

    public static void loadAbilities() {
        Map<String, RunicArtifactAbility> abilities = new HashMap<String, RunicArtifactAbility>();
        File file = new File(Plugin.getInstance().getDataFolder(), "abilities.yml");
        if (file.exists()) {
            FileConfiguration abilityConfig = ConfigUtil.getYamlConfigFile(file.getName(), Plugin.getInstance().getDataFolder());
            for (String key : abilityConfig.getKeys(false)) {
                abilities.put(key, new RunicArtifactAbility(
                        key,
                        abilityConfig.getString(key + ".name"),
                        abilityConfig.getString(key + ".description"),
                        RunicArtifactAbilityTrigger.getFromIdentifier(abilityConfig.getString(key + ".trigger"))
                ));
            }
        } else {
            try {
                file.createNewFile();
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
        AbilityManager.setAbilities(abilities);
    }

}
