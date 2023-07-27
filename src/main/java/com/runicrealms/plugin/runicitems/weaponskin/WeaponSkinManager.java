package com.runicrealms.plugin.runicitems.weaponskin;

import com.runicrealms.plugin.common.DonorRank;
import com.runicrealms.plugin.common.RunicCommon;
import com.runicrealms.plugin.runicitems.RunicItems;
import com.runicrealms.plugin.runicitems.RunicItemsAPI;
import com.runicrealms.plugin.runicitems.api.WeaponSkinAPI;
import de.tr7zw.nbtapi.NBTItem;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.node.Node;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class WeaponSkinManager implements WeaponSkinAPI, Listener {

    private final List<WeaponSkin> weaponSkins = WeaponSkinConfigLoader.loadFromConfig(new File(RunicItems.getInstance().getDataFolder(), "weapon-skins.yml"));
    private final Map<Material, Set<WeaponSkin>> materialWeaponSkins = new HashMap<>();
    private final Map<String, WeaponSkin> idWeaponSkins = new HashMap<>();

    public WeaponSkinManager() {
        Bukkit.getPluginManager().registerEvents(this, RunicItems.getInstance());
        RunicItems.getCommandManager().getCommandCompletions().registerCompletion("weaponskins", (context) ->
                weaponSkins.stream()
                        .map(WeaponSkin::id)
                        .collect(Collectors.toSet()));
        for (WeaponSkin skin : weaponSkins) {
            if (!materialWeaponSkins.containsKey(skin.material()))
                materialWeaponSkins.put(skin.material(), new HashSet<>());
            materialWeaponSkins.get(skin.material()).add(skin);
        }
        for (WeaponSkin skin : weaponSkins) {
            idWeaponSkins.put(skin.id(), skin);
        }
    }

    @Override
    public boolean canActivateSkin(Player player, WeaponSkin skin) {
//        if (skin.hasRank() && !player.hasPermission("runic.rank." + skin.rank())) return false;
        if (skin.hasRank()) {
            DonorRank playersRank = DonorRank.getDonorRank(player);
            boolean hasRank = false;
            for (DonorRank rank : skin.rank()) {
                if (playersRank.getIdentifier().equalsIgnoreCase(rank.getIdentifier())) {
                    hasRank = true;
                    break;
                }
            }
            if (!hasRank) return false;
        }
        if (skin.hasAchievementID() && !RunicCommon.getAchievementsAPI().hasAchievement(player, skin.achievementID()))
            return false;
        return !skin.hasPermission() || player.hasPermission(skin.permission());
    }

    @Override
    public void activateSkin(Player player, WeaponSkin skin) {
        if (!canActivateSkin(player, skin))
            throw new IllegalStateException("Player " + player.getName() + " cannot equip skin " + skin.id());

        for (ItemStack item : player.getInventory()) {
            if (item == null) continue;
            if (item.getType() == skin.material()) {
                skin.apply(item);
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
    public List<WeaponSkin> getAllSkins() {
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

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onWeaponSkinObtainHandler(WeaponSkinObtainEvent event) {
        if (event.isCancelled()) return;
        LuckPermsProvider.get().getUserManager().loadUser(event.getPlayer().getUniqueId()).thenAcceptAsync(user -> {
            user.data().add(Node.builder(event.getPermission()).build());
            LuckPermsProvider.get().getUserManager().saveUser(user);
        });
    }

}
