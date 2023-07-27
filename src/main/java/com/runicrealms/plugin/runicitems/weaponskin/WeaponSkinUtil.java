package com.runicrealms.plugin.runicitems.weaponskin;

import com.runicrealms.plugin.runicitems.RunicItemsAPI;
import com.runicrealms.plugin.runicitems.item.template.RunicItemTemplate;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class WeaponSkinUtil {

    public static final Map<String, String> WEAPON_SKIN_ITEMS = new HashMap<>(); // Item-id -> weapon skin permission

    static {
        WEAPON_SKIN_ITEMS.put("sebaths-cave-archer-artifact", "runic.weapon.skin.sebaths-cave.archer");
        WEAPON_SKIN_ITEMS.put("crystal-cavern-archer-artifact", "runic.weapon.skin.crystal-cavern.archer");
        WEAPON_SKIN_ITEMS.put("jorundr-keep-archer-artifact", "runic.weapon.skin.jorundr-keep.archer");
        WEAPON_SKIN_ITEMS.put("sunken-library-archer-artifact", "runic.weapon.skin.sunken-library.archer");
        WEAPON_SKIN_ITEMS.put("crypts-archer-artifact", "runic.weapon.skin.crypts.archer");
        WEAPON_SKIN_ITEMS.put("frozen-fortress-archer-artifact", "runic.weapon.skin.frozen-fortress.archer");

        WEAPON_SKIN_ITEMS.put("sebaths-cave-rogue-artifact", "runic.weapon.skin.sebaths-cave.rogue");
        WEAPON_SKIN_ITEMS.put("crystal-cavern-rogue-artifact", "runic.weapon.skin.crystal-cavern.rogue");
        WEAPON_SKIN_ITEMS.put("jorundr-keep-rogue-artifact", "runic.weapon.skin.jorundr-keep.rogue");
        WEAPON_SKIN_ITEMS.put("sunken-library-rogue-artifact", "runic.weapon.skin.sunken-library.rogue");
        WEAPON_SKIN_ITEMS.put("crypts-rogue-artifact", "runic.weapon.skin.crypts.rogue");
        WEAPON_SKIN_ITEMS.put("frozen-fortress-rogue-artifact", "runic.weapon.skin.frozen-fortress.rogue");

        WEAPON_SKIN_ITEMS.put("sebaths-cave-mage-artifact", "runic.weapon.skin.sebaths-cave.mage");
        WEAPON_SKIN_ITEMS.put("crystal-cavern-mage-artifact", "runic.weapon.skin.crystal-cavern.mage");
        WEAPON_SKIN_ITEMS.put("jorundr-keep-mage-artifact", "runic.weapon.skin.jorundr-keep.mage");
        WEAPON_SKIN_ITEMS.put("sunken-library-mage-artifact", "runic.weapon.skin.sunken-library.mage");
        WEAPON_SKIN_ITEMS.put("crypts-mage-artifact", "runic.weapon.skin.crypts.mage");
        WEAPON_SKIN_ITEMS.put("frozen-fortress-mage-artifact", "runic.weapon.skin.frozen-fortress.mage");

        WEAPON_SKIN_ITEMS.put("sebaths-cave-warrior-artifact", "runic.weapon.skin.sebaths-cave.warrior");
        WEAPON_SKIN_ITEMS.put("crystal-cavern-warrior-artifact", "runic.weapon.skin.crystal-cavern.warrior");
        WEAPON_SKIN_ITEMS.put("jorundr-keep-warrior-artifact", "runic.weapon.skin.jorundr-keep.warrior");
        WEAPON_SKIN_ITEMS.put("sunken-library-warrior-artifact", "runic.weapon.skin.sunken-library.warrior");
        WEAPON_SKIN_ITEMS.put("crypts-warrior-artifact", "runic.weapon.skin.crypts.warrior");
        WEAPON_SKIN_ITEMS.put("frozen-fortress-warrior-artifact", "runic.weapon.skin.frozen-fortress.warrior");

        WEAPON_SKIN_ITEMS.put("sebaths-cave-cleric-artifact", "runic.weapon.skin.sebaths-cave.cleric");
        WEAPON_SKIN_ITEMS.put("crystal-cavern-cleric-artifact", "runic.weapon.skin.crystal-cavern.cleric");
        WEAPON_SKIN_ITEMS.put("jorundr-keep-cleric-artifact", "runic.weapon.skin.jorundr-keep.cleric");
        WEAPON_SKIN_ITEMS.put("sunken-library-cleric-artifact", "runic.weapon.skin.sunken-library.cleric");
        WEAPON_SKIN_ITEMS.put("crypts-cleric-artifact", "runic.weapon.skin.crypts.cleric");
        WEAPON_SKIN_ITEMS.put("frozen-fortress-cleric-artifact", "runic.weapon.skin.frozen-fortress.cleric");
    }

    /**
     * If the given item stack:
     * 1) is a runic item
     * 2) has a runic item id that is one that corresponds to an unlock-able weapon skin
     * returns a weapon skin obtain event for that item
     * else null
     */
    public static @Nullable WeaponSkinObtainEvent createObtainEvent(Player player, ItemStack item) {
        RunicItemTemplate template = RunicItemsAPI.getItemStackTemplate(item);
        if (template == null) return null;
        String templateId = template.getId();
        String permission = WEAPON_SKIN_ITEMS.get(templateId);
        if (permission == null) return null;
        return new WeaponSkinObtainEvent(player, permission);
    }

}
