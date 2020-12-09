package com.runicrealms.runicitems.item.util;

import org.bukkit.ChatColor;

public enum RunicArtifactAbilityTrigger {

    ON_CLICK("on-click", ChatColor.GOLD + "" + ChatColor.BOLD + "ON CLICK"),
    ON_KILL("on-kill", ChatColor.GOLD + "" + ChatColor.BOLD + "ON KILL");

    private String identifier;
    private String display;

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
