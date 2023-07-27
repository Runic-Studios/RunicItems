package com.runicrealms.plugin.runicitems.item.util;

import org.bukkit.ChatColor;

public enum RunicArtifactAbilityTrigger {

    ON_CAST("on-cast", ChatColor.GOLD + "" + ChatColor.BOLD + "ON CAST"),
    ON_HIT("on-hit", ChatColor.GOLD + "" + ChatColor.BOLD + "ON HIT"),
    ON_KILL("on-kill", ChatColor.GOLD + "" + ChatColor.BOLD + "ON KILL");

    private final String identifier;
    private final String display;

    RunicArtifactAbilityTrigger(String identifier, String display) {
        this.identifier = identifier;
        this.display = display;
    }

    public String getIdentifier() {
        return this.identifier;
    }

    public String getDisplay() {
        return this.display;
    }

    public static RunicArtifactAbilityTrigger getFromIdentifier(String identifier) {
        for (RunicArtifactAbilityTrigger trigger : RunicArtifactAbilityTrigger.values()) {
            if (trigger.getIdentifier().equalsIgnoreCase(identifier)) {
                return trigger;
            }
        }
        return null;
    }

}
