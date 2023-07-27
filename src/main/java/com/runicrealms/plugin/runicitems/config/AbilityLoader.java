package com.runicrealms.plugin.runicitems.config;

import com.runicrealms.plugin.runicitems.item.util.RunicArtifactAbilityTrigger;
import com.runicrealms.plugin.runicitems.RunicItems;
import com.runicrealms.plugin.runicitems.AbilityManager;
import com.runicrealms.plugin.runicitems.item.stats.RunicArtifactAbility;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class AbilityLoader {

    public static void loadAbilities() {
        Map<String, RunicArtifactAbility> abilities = new HashMap<>();
        File file = new File(RunicItems.getInstance().getDataFolder(), "abilities.yml");
        if (file.exists()) {
            FileConfiguration abilityConfig = ConfigUtil.getYamlConfigFile(file.getName(), RunicItems.getInstance().getDataFolder());
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
