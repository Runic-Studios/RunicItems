package com.runicrealms.plugin.runicitems.api;

import com.runicrealms.plugin.runicitems.weaponskin.WeaponSkin;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

public interface WeaponSkinAPI {

    /**
     * Checks if a player has permission to activate a specific weapon skin
     */
    boolean canActivateSkin(Player player, WeaponSkin skin);

    /**
     * Activates a weapon skin for a player. Throw illegal state if player does not have permission.
     */
    void activateSkin(Player player, WeaponSkin skin);

    /**
     * Deactivates a weapon skin for a player.
     */
    void deactivateSkin(Player player, WeaponSkin skin);

    /**
     * Gets a list of all skins loaded from config
     */
    List<WeaponSkin> getAllSkins();

    /**
     * Reverts an itemstack's skin to the default defined by runic items.
     * Performs nothing if not applicable.
     */
    ItemStack disableSkin(ItemStack itemStack);

    /**
     * Reverts an itemstack's skin to the default defined by runic items,
     * ...IF AND ONLY IF the owner does not have permission to be using this skin.
     * Performs nothing if not applicable.
     */
    ItemStack disableDisallowedSkin(Player owner, ItemStack itemStack);

    /**
     * Gets all existing weapon skins for a given material
     */
    Collection<WeaponSkin> getMaterialSkins(Material material);

    /**
     * Gets a weapon skin by its custom ID in the weapon skins file
     */
    @Nullable
    WeaponSkin getWeaponSkin(String weaponSkinID);

}
