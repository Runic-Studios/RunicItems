package com.runicrealms.runicitems.item.util;

import org.bukkit.ChatColor;

public enum ClickTrigger {

    RIGHT_CLICK("right", ChatColor.GOLD + "" + ChatColor.BOLD + "RIGHT CLICK"),
    SHIFT_RIGHT_CLICK("shift-right", ChatColor.GOLD + "" + ChatColor.BOLD + "SNEAK + RIGHT CLICK"),
    LEFT_CLICK("left", ChatColor.GOLD + "" + ChatColor.BOLD + "LEFT CLICK"),
    SHIFT_LEFT_CLICK("shift-left", ChatColor.GOLD + "" + ChatColor.BOLD + "SNEAK + LEFT CLICK");

    private String identifier;
    private String display;

    ClickTrigger(String identifier, String display) {
        this.identifier = identifier;
        this.display = display;
    }

    public String getIdentifier() {
        return this.identifier;
    }

    public String getDisplay() {
        return this.display;
    }

    public static ClickTrigger getFromIdentifier(String identifier) {
        for (ClickTrigger trigger : ClickTrigger.values()) {
            if (trigger.getIdentifier().equalsIgnoreCase(identifier)) {
                return trigger;
            }
        }
        return null;
    }

}
