package com.runicrealms.runicitems.item.util;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;

public enum ClickTrigger {

    RIGHT_CLICK("right", ChatColor.GOLD + "" + ChatColor.BOLD + "RIGHT CLICK"),
    SHIFT_RIGHT_CLICK("shift-right", ChatColor.GOLD + "" + ChatColor.BOLD + "SNEAK + RIGHT CLICK"),
    LEFT_CLICK("left", ChatColor.GOLD + "" + ChatColor.BOLD + "LEFT CLICK"),
    SHIFT_LEFT_CLICK("shift-left", ChatColor.GOLD + "" + ChatColor.BOLD + "SNEAK + LEFT CLICK");

    private final String identifier;
    private final String display;

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

    public static ClickTrigger getFromInteractAction(Action action, Player player) {
        if (action == Action.RIGHT_CLICK_AIR
                || action == Action.RIGHT_CLICK_BLOCK) {
            if (player.isSneaking()) {
                return ClickTrigger.SHIFT_RIGHT_CLICK;
            }
            return ClickTrigger.RIGHT_CLICK;
        } else if (action == Action.LEFT_CLICK_AIR
                || action == Action.LEFT_CLICK_BLOCK) {
            if (player.isSneaking()) {
                return ClickTrigger.SHIFT_LEFT_CLICK;
            }
            return ClickTrigger.LEFT_CLICK;
        }
        return null;
    }

}
