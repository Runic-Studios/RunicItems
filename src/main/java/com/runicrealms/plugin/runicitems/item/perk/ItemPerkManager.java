package com.runicrealms.plugin.runicitems.item.perk;

import java.util.HashSet;
import java.util.Set;

public class ItemPerkManager {

    private static final Set<ItemPerkType> itemPerks = new HashSet<>();

    public static Set<ItemPerkType> getItemPerks() {
        return itemPerks;
    }

}
