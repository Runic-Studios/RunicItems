package com.runicrealms.plugin.runicitems.item.perk;

import com.runicrealms.plugin.runicitems.item.perk.handlers.ItemPerkHandler;
import com.runicrealms.plugin.runicitems.item.perk.handlers.TestItemPerkHandler;

import java.util.HashSet;
import java.util.Set;

public class ItemPerkManager {

    private static final Set<ItemPerkType> itemPerks = new HashSet<>();

    public static Set<ItemPerkType> getItemPerks() {
        return itemPerks;
    }

    public static void registerItemPerk(ItemPerkHandler handler) {
        itemPerks.add(handler.getType());
    }

    public static void initializeItemPerks() {
        new TestItemPerkHandler();
    }

}
