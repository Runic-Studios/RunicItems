package com.runicrealms.plugin.runicitems;

import com.runicrealms.plugin.common.event.ArmorEquipEvent;
import com.runicrealms.plugin.common.util.ArmorType;
import com.runicrealms.plugin.runicitems.item.RunicItemArmor;
import com.runicrealms.plugin.runicitems.item.RunicItemGem;
import com.runicrealms.plugin.runicitems.item.stats.GemBonus;
import com.runicrealms.plugin.runicitems.item.template.RunicItemArmorTemplate;
import com.runicrealms.plugin.runicitems.item.template.RunicItemGemTemplate;
import com.runicrealms.plugin.runicitems.player.PlayerStatHolder;
import com.runicrealms.plugin.runicitems.util.StatUtil;
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

public class GemManager implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onGemApply(InventoryClickEvent event) {

        if (event.isCancelled()) return;
        // Nothing inventory action is when you click on armor in armor slots
        if (!(event.getAction() == InventoryAction.SWAP_WITH_CURSOR || event.getAction() == InventoryAction.NOTHING))
            return;
        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;
        if (event.getCursor() == null || event.getCursor().getType() == Material.AIR) return;
        if (!(RunicItemsAPI.getItemStackTemplate(event.getCurrentItem()) instanceof RunicItemArmorTemplate)) return;
        if (!(RunicItemsAPI.getItemStackTemplate(event.getCursor()) instanceof RunicItemGemTemplate)) return;

        NBTItem gemNbt = new NBTItem(event.getCursor(), true);
        if (!gemNbt.hasNBTData()) return;

        if (event.getCursor().getAmount() != 1) {
            event.getWhoClicked().sendMessage(ChatColor.RED + "You can only apply gems one at a time!");
            return;
        }

        RunicItemArmor armor = (RunicItemArmor) RunicItemsAPI.getRunicItemFromItemStack(event.getCurrentItem());
        RunicItemGem gemItem = (RunicItemGem) RunicItemsAPI.getRunicItemFromItemStack(event.getCursor());

        int gemSlotsUsed = 0;
        for (GemBonus gem : armor.getGems()) gemSlotsUsed += StatUtil.getGemSlots(gem.getTier());
        if (gemSlotsUsed + StatUtil.getGemSlots(gemItem.getBonus().getTier()) > armor.getMaxGemSlots()) {
            event.getWhoClicked().sendMessage(ChatColor.RED + "This item doesn't have enough free gem slots!");
            return;
        }

        armor.getGems().add(gemItem.getBonus());

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

        if (event.getSlotType() == InventoryType.SlotType.ARMOR)
            Bukkit.getScheduler().runTaskAsynchronously(RunicItems.getInstance(), () -> {
                PlayerStatHolder holder = PlayerManager.getCachedPlayerStats().get(event.getWhoClicked().getUniqueId());
                holder.updateItems(false,
                        PlayerStatHolder.StatHolderType.HELMET,
                        PlayerStatHolder.StatHolderType.CHESTPLATE,
                        PlayerStatHolder.StatHolderType.LEGGINGS,
                        PlayerStatHolder.StatHolderType.BOOTS
                );
            });

        event.setCancelled(true);
    }

}
