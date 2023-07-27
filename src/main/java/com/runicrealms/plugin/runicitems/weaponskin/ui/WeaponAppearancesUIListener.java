package com.runicrealms.plugin.runicitems.weaponskin.ui;

import com.runicrealms.plugin.common.util.ColorUtil;
import com.runicrealms.plugin.runicitems.RunicItems;
import com.runicrealms.plugin.runicitems.RunicItemsAPI;
import com.runicrealms.plugin.runicitems.item.template.RunicItemArtifactTemplate;
import com.runicrealms.plugin.runicitems.item.template.RunicItemWeaponTemplate;
import com.runicrealms.plugin.runicitems.weaponskin.WeaponSkin;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;

public class WeaponAppearancesUIListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getClickedInventory() == null) return;
        if (!(event.getView().getTopInventory().getHolder() instanceof WeaponAppearancesUI)) return;
        if (event.getClickedInventory().getType() == InventoryType.PLAYER) {
            event.setCancelled(true);
            return;
        }
        WeaponAppearancesUI weaponAppearancesUI = (WeaponAppearancesUI) event.getClickedInventory().getHolder();
        if (!event.getWhoClicked().equals(weaponAppearancesUI.getPlayer())) {
            event.setCancelled(true);
            event.getWhoClicked().closeInventory();
            return;
        }

        Player player = (Player) event.getWhoClicked();
        if (event.getCurrentItem() == null) return;
        if (weaponAppearancesUI.getInventory().getItem(event.getRawSlot()) == null) return;

        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
        event.setCancelled(true);

        if (event.getSlot() == 0 && event.getCurrentItem().getType() == Material.GRAY_STAINED_GLASS_PANE) {
            player.openInventory(new WeaponAppearancesUI(player, weaponAppearancesUI.getPageNumber() - 1).getInventory());
        } else if (event.getSlot() == 8 && event.getCurrentItem().getType() == Material.BROWN_STAINED_GLASS_PANE) {
            player.openInventory(new WeaponAppearancesUI(player, weaponAppearancesUI.getPageNumber() + 1).getInventory());
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
            if (skin.material() != player.getInventory().getItemInMainHand().getType()) {
                player.closeInventory();
                player.sendMessage(ColorUtil.format("&cYou must be holding a weapon for class type &e" + skin.classType().getName() + "&c to equip the &e" + skin.name() + "&c skin."));
                return;
            }
            if (event.getClick() == ClickType.LEFT) {
                if (!RunicItems.getWeaponSkinAPI().canActivateSkin(player, skin)) return;
                skin.apply(player.getInventory().getItemInMainHand());
            } else if (event.getClick() == ClickType.RIGHT) {
                RunicItems.getWeaponSkinAPI().disableSkin(player.getInventory().getItemInMainHand());
            }
        }
    }

}
