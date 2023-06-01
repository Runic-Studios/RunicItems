package com.runicrealms.runicitems.weaponskin;

import com.runicrealms.plugin.common.RunicCommon;
import com.runicrealms.runicitems.RunicItems;
import com.runicrealms.runicitems.RunicItemsAPI;
import com.runicrealms.runicitems.api.WeaponSkinAPI;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class WeaponSkinManager implements WeaponSkinAPI {

    private final Set<WeaponSkin> weaponSkins = WeaponSkinConfigLoader.loadFromConfig(new File(RunicItems.getInstance().getDataFolder(), "weapon-skins.yml"));
    private final Map<Material, Set<WeaponSkin>> materialWeaponSkins = new HashMap<>();
    private final Map<String, WeaponSkin> idWeaponSkins = new HashMap<>();

    public WeaponSkinManager() {
        RunicItems.getCommandManager().getCommandCompletions().registerCompletion("weaponskins", (context) ->
                weaponSkins.stream()
                        .map(WeaponSkin::customName)
                        .collect(Collectors.toSet()));
        for (WeaponSkin skin : weaponSkins) {
            if (!materialWeaponSkins.containsKey(skin.material()))
                materialWeaponSkins.put(skin.material(), new HashSet<>());
            materialWeaponSkins.get(skin.material()).add(skin);
        }
        for (WeaponSkin skin : weaponSkins) {
            idWeaponSkins.put(skin.customName(), skin);
        }
    }

    @Override
    public boolean canActivateSkin(Player player, WeaponSkin skin) {
        if (skin.hasRank() && !player.hasPermission("runic.rank." + skin.rank())) return false;
        if (skin.hasAchievementID() && !RunicCommon.getAchievementsAPI().hasAchievement(player, skin.achievementID()))
            return false;
        return !skin.hasPermission() || player.hasPermission(skin.permission());
    }

    @Override
    public void activateSkin(Player player, WeaponSkin skin) {
        if (!canActivateSkin(player, skin))
            throw new IllegalStateException("Player " + player.getName() + " cannot equip skin " + skin.customName());

        for (ItemStack item : player.getInventory()) {
            if (item == null) continue;
            if (item.getType() == skin.material()) {
                skin.apply(item);
                NBTItem nbtItem = new NBTItem(item, true);
                nbtItem.setString("weapon-skin", skin.customName());
            }
        }
    }

    @Override
    public void deactivateSkin(Player player, WeaponSkin skin) {
        for (ItemStack item : player.getInventory()) {
            if (item == null) continue;
            if (item.getType() == skin.material()) {
                disableSkin(item);
            }
        }
    }

    @Override
    public Set<WeaponSkin> getAllSkins() {
        return weaponSkins;
    }

    @Override
    public ItemStack disableSkin(ItemStack itemStack) {
        if (itemStack == null) return null;
        if (!itemStack.hasItemMeta()) return itemStack;
        if (!(itemStack.getItemMeta() instanceof Damageable)) return itemStack;
        NBTItem nbtItem = new NBTItem(itemStack, true);
        if (!nbtItem.hasNBTData()) return itemStack;
        if (!nbtItem.hasKey("weapon-skin")) return itemStack;
        return forceDisableSkin(itemStack);
    }

    @Override
    public ItemStack disableDisallowedSkin(Player owner, ItemStack itemStack) {
        if (itemStack == null) return null;
        if (!itemStack.hasItemMeta()) return itemStack;
        if (!(itemStack.getItemMeta() instanceof Damageable)) return itemStack;
        NBTItem nbtItem = new NBTItem(itemStack, true);
        if (!nbtItem.hasNBTData()) return itemStack;
        if (!nbtItem.hasKey("weapon-skin")) return itemStack;
        WeaponSkin skin = getWeaponSkin(nbtItem.getString("weapon-skin"));
        if (skin == null) return itemStack;
        if (!canActivateSkin(owner, skin)) return forceDisableSkin(itemStack);
        return itemStack;
    }

    @Override
    public Collection<WeaponSkin> getMaterialSkins(Material material) {
        return materialWeaponSkins.get(material);
    }

    @Override
    public WeaponSkin getWeaponSkin(String weaponSkinID) {
        return idWeaponSkins.get(weaponSkinID);
    }

    private ItemStack forceDisableSkin(ItemStack item) {
        Damageable meta = (Damageable) item.getItemMeta();
        meta.setDamage(RunicItemsAPI.getItemStackTemplate(item).getDisplayableItem().getDamage());
        item.setItemMeta((ItemMeta) meta);
        NBTItem nbtItem = new NBTItem(item);
        nbtItem.removeKey("weapon-skin");
        return nbtItem.getItem();
    }

}
