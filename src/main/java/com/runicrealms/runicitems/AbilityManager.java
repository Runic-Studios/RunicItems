package com.runicrealms.runicitems;

import com.runicrealms.runicitems.item.stats.RunicArtifactAbility;

import java.util.HashMap;
import java.util.Map;

public class AbilityManager {

    private static Map<String, RunicArtifactAbility> abilities = new HashMap<>();

    public static void setAbilities(Map<String, RunicArtifactAbility> abilities) {
        AbilityManager.abilities = abilities;
    }

    public static RunicArtifactAbility getAbilityFromId(String id) {
        return abilities.get(id);
    }

}
