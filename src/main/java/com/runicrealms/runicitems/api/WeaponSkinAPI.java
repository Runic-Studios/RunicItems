package com.runicrealms.runicitems.api;

import com.runicrealms.runicitems.weaponskin.WeaponSkin;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

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
    Set<WeaponSkin> getAllSkins();

    /**
     * Checks if a player owns a weapon skin (has permission to use it)
     */
    boolean hasWeaponSkin(Player player, WeaponSkin skin);

    /**
     * Checks if a player has a given weapon skin activated currently
     */
    boolean weaponSkinActive(Player player, WeaponSkin skin);

    /**
     * Checks if a player has a weapon skin currently activated for a given material
     */
    boolean weaponSkinActive(Player player, Material material);

    /**
     * Gets the player's current weapon skin activated for a material, or null
     */
    @Nullable
    WeaponSkin getWeaponSkin(Player player, Material material);

    /**
     * Reverts an itemstack's skin to the default defined by runic items
     */
    ItemStack disableSkin(ItemStack itemStack);


}
