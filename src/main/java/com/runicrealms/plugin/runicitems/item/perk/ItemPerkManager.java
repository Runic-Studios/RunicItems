package com.runicrealms.plugin.runicitems.item.perk;

import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

public class ItemPerkManager {

    private static final Set<ItemPerkType> itemPerks = new HashSet<>();
    private static ItemPerkHandlerManager handlerManager;

    public static Set<ItemPerkType> getItemPerks() {
        return itemPerks;
    }

    public static void registerItemPerk(ItemPerkHandler handler) {
        itemPerks.add(handler.getType());
    }

    public static void initializeItemPerks() {
        handlerManager = new ItemPerkHandlerManager();
    }

    public static boolean isValidItemPerkIdentifier(String identifier) {
        for (ItemPerkType type : itemPerks) {
            if (type.getIdentifier().equalsIgnoreCase(identifier)) return true;
        }
        return false;
    }

    public static @Nullable ItemPerkType getItemPerkFromIdentifier(String identifier) {
        for (ItemPerkType type : itemPerks) {
            if (type.getIdentifier().equalsIgnoreCase(identifier)) return type;
        }
        return null;
    }

}
