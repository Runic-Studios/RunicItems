package com.runicrealms.plugin.runicitems.item.perk.handlers;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;

public class TestItemPerkHandler extends ItemPerkHandler implements Listener {

    public TestItemPerkHandler() {
        super("test-speed", 5);
    }

    @Override
    protected void onChange(int stacks) {
        Bukkit.broadcastMessage("CHANGE TEST-SPEED, stacks: " + stacks);
    }
}
