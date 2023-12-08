package com.runicrealms.plugin.runicitems.item.perk.handlers;

import com.runicrealms.plugin.runicitems.item.perk.ItemPerkHandler;
import org.bukkit.Bukkit;

public class TestItemPerkHandler extends ItemPerkHandler {

    public TestItemPerkHandler() {
        super("test-speed");
    }

    @Override
    public void onChange(int stacks) {
        Bukkit.broadcastMessage("CHANGE TEST-SPEED, stacks: " + stacks);
    }
}
