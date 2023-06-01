package com.runicrealms.runicitems.weaponskin.gui;

import com.runicrealms.plugin.common.util.ColorUtil;
import com.runicrealms.runicitems.RunicItems;
import com.runicrealms.runicitems.RunicItemsAPI;
import com.runicrealms.runicitems.item.template.RunicItemArtifactTemplate;
import com.runicrealms.runicitems.item.template.RunicItemWeaponTemplate;
import com.runicrealms.runicitems.weaponskin.WeaponSkin;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;

public class WeaponryGUIListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getClickedInventory() == null) return;
        if (!(event.getView().getTopInventory().getHolder() instanceof WeaponryGUI)) return;
        if (event.getClickedInventory().getType() == InventoryType.PLAYER) {
            event.setCancelled(true);
            return;
        }
        WeaponryGUI weaponryGUI = (WeaponryGUI) event.getClickedInventory().getHolder();
        if (!event.getWhoClicked().equals(weaponryGUI.getPlayer())) {
            event.setCancelled(true);
            event.getWhoClicked().closeInventory();
            return;
        }

        Player player = (Player) event.getWhoClicked();
        if (event.getCurrentItem() == null) return;
        if (weaponryGUI.getInventory().getItem(event.getRawSlot()) == null) return;

        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
        event.setCancelled(true);

        if (event.getSlot() == 0 && event.getCurrentItem().getType() == Material.GRAY_STAINED_GLASS_PANE) {
            player.openInventory(new WeaponryGUI(player, weaponryGUI.getPageNumber() - 1).getInventory());
        } else if (event.getSlot() == 8 && event.getCurrentItem().getType() == Material.BROWN_STAINED_GLASS_PANE) {
            player.openInventory(new WeaponryGUI(player, weaponryGUI.getPageNumber() + 1).getInventory());
        } else if (event.getSlot() >= 9) {
            if (player.getInventory().getItemInMainHand().getType() == Material.AIR
                    || (!(RunicItemsAPI.getItemStackTemplate(player.getInventory().getItemInMainHand()) instanceof RunicItemWeaponTemplate)
                    && !(RunicItemsAPI.getItemStackTemplate(player.getInventory().getItemInMainHand()) instanceof RunicItemArtifactTemplate))) {
                player.closeInventory();
                player.sendMessage(ColorUtil.format("&cYou must be holding a weapon while activating a weapon skin!"));
                return;
            }

            NBTItem nbtItem = new NBTItem(event.getCurrentItem());
            if (!nbtItem.hasNBTData() || !nbtItem.hasKey("weapon-skin-menu")) return;
            String weaponSkinID = nbtItem.getString("weapon-skin-menu");
            WeaponSkin skin = RunicItems.getWeaponSkinAPI().getWeaponSkin(weaponSkinID);
            if (skin == null) return;
            if (event.getClick() == ClickType.LEFT) {
                if (!RunicItems.getWeaponSkinAPI().canActivateSkin(player, skin)) return;
                skin.apply(player.getInventory().getItemInMainHand());
            } else if (event.getClick() == ClickType.RIGHT) {
                RunicItems.getWeaponSkinAPI().disableSkin(player.getInventory().getItemInMainHand());
            }
        }
    }

}
