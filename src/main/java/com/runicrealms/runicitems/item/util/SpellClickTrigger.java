package com.runicrealms.runicitems.item.util;

import org.bukkit.ChatColor;

public enum SpellClickTrigger {

    RIGHT_CLICK(ChatColor.GOLD + "" + ChatColor.BOLD + "RIGHT CLICK"),
    SHIFT_RIGHT_CLICK(ChatColor.GOLD + "" + ChatColor.BOLD + "SNEAK + RIGHT CLICK"),
    LEFT_CLICK(ChatColor.GOLD + "" + ChatColor.BOLD + "LEFT CLICK"),
    SHIFT_LEFT_CLICK(ChatColor.GOLD + "" + ChatColor.BOLD + "SNEAK + LEFT CLICK");

    private String display;

    SpellClickTrigger(String display) {
        this.display = display;
    }

    public String getDisplay() {
        return this.display;
    }

}
