package com.runicrealms.runicitems.item.stats;

import org.bukkit.ChatColor;

public enum RunicItemTag {

    SOULBOUND("soulbound"), UNTRADEABLE("untradeable"), QUEST_ITEM("quest-item");

    private String identifier;

    RunicItemTag(String identifier) {
        this.identifier = identifier;
    }

    public String getIdentifier() {
        return identifier;
    }

}
