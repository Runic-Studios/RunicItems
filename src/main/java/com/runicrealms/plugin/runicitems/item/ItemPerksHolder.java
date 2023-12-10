package com.runicrealms.plugin.runicitems.item;

import com.runicrealms.plugin.runicitems.item.perk.ItemPerk;

import java.util.LinkedHashSet;

public interface ItemPerksHolder {

    LinkedHashSet<ItemPerk> getItemPerks();

    boolean hasItemPerks();

}
