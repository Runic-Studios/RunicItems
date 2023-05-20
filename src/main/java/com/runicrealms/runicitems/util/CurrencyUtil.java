package com.runicrealms.runicitems.util;

import com.runicrealms.runicitems.RunicItemsAPI;
import org.bukkit.inventory.ItemStack;

/**
 * This utility contains all the units of currency.
 */
public class CurrencyUtil {

    private static final ItemStack GOLD_COIN = RunicItemsAPI.generateItemFromTemplate("coin").generateItem();

    /*
     * A single gold coin, the smallest unit of currency
     */
    public static ItemStack goldCoin() {
        return GOLD_COIN;
    }

    public static ItemStack goldCoin(int stackSize) {
        return RunicItemsAPI.generateItemFromTemplate("coin", stackSize).generateItem();
    }
}
