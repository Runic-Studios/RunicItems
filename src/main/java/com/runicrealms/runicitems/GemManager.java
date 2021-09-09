package com.runicrealms.runicitems;

import com.codingforcookies.armorequip.ArmorEquipEvent;
import com.codingforcookies.armorequip.ArmorType;
import com.runicrealms.runicitems.item.RunicItemArmor;
import com.runicrealms.runicitems.item.stats.Gem;
import com.runicrealms.runicitems.item.template.RunicItemArmorTemplate;
import com.runicrealms.runicitems.player.PlayerStatHolder;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

import java.util.LinkedHashMap;

public class GemManager implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onGemApply(InventoryClickEvent event) {

        if (event.isCancelled()) return;
        if (event.getAction() != InventoryAction.SWAP_WITH_CURSOR) return;
        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;
        if (event.getCursor() == null || event.getCursor().getType() == Material.AIR) return;
        if (!(RunicItemsAPI.getItemStackTemplate(event.getCurrentItem()) instanceof RunicItemArmorTemplate)) return;

        NBTItem gemNbt = new NBTItem(event.getCursor(), true);
        if (!gemNbt.hasNBTData()) return;

        Gem bonuses = new Gem(new LinkedHashMap<>(), 0);
        for (String key : gemNbt.getKeys()) {
            if (!key.startsWith("data-gem-")) continue;
            String type = key.substring("data-gem-".length());
            Integer value = parseInt(gemNbt.getString(key));
            if (value == null) continue;
            if (type.equalsIgnoreCase("health")) {
                bonuses.setHealth(value);
            } else {
                Stat stat = Stat.getFromIdentifier(type);
                if (stat != null) bonuses.getStats().put(stat, value);
            }
        }
        if (bonuses.getStats().size() == 0 && !bonuses.hasHealth()) return;

        if (event.getCursor().getAmount() != 1) {
            event.getWhoClicked().sendMessage(ChatColor.RED + "You can only apply gems one at a time!");
            return;
        }

        RunicItemArmor armor = (RunicItemArmor) RunicItemsAPI.getRunicItemFromItemStack(event.getCurrentItem());

        if (armor.getGems().size() >= armor.getMaxGemSlots()) {
            event.getWhoClicked().sendMessage(ChatColor.RED + "This item doesn't have any free gem slots!");
            return;
        }

        armor.getGems().add(bonuses);

        ItemStack generatedItem = armor.generateItem();

        if (event.getSlotType() == InventoryType.SlotType.ARMOR) {
            ArmorEquipEvent armorEvent = new ArmorEquipEvent(
                    (Player) event.getWhoClicked(),
                    ArmorEquipEvent.EquipMethod.DRAG,
                    ArmorType.matchType(generatedItem),
                    event.getCurrentItem(),
                    generatedItem);
            Bukkit.getPluginManager().callEvent(armorEvent);
        }

        event.getWhoClicked().sendMessage(ChatColor.GREEN + "Applied " +
                event.getCursor().getItemMeta().getDisplayName() +
                ChatColor.GREEN + " to " +
                armor.getDisplayableItem().getDisplayName() +
                ChatColor.GREEN + ".");

        event.setCurrentItem(generatedItem);
        event.setCursor(null);

        if (event.getSlotType() == InventoryType.SlotType.ARMOR) Bukkit.getScheduler().runTaskAsynchronously(RunicItems.getInstance(), () -> {
            PlayerStatHolder holder = PlayerManager.getCachedPlayerStats().get(event.getWhoClicked().getUniqueId());
            holder.updateItems(ArmorType.HELMET, ArmorType.CHESTPLATE, ArmorType.LEGGINGS, ArmorType.BOOTS);
        });

        event.setCancelled(true);
    }

    private static Integer parseInt(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            return null;
        }
    }

}
