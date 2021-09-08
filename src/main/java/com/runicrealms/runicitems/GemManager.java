package com.runicrealms.runicitems;

import com.runicrealms.runicitems.item.RunicItemArmor;
import com.runicrealms.runicitems.item.stats.Gem;
import com.runicrealms.runicitems.item.template.RunicItemArmorTemplate;
import com.runicrealms.runicitems.player.PlayerStatHolder;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
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

        RunicItemArmor armor = (RunicItemArmor) RunicItemsAPI.getRunicItemFromItemStack(event.getCurrentItem());
        armor.getGems().add(bonuses);

        ItemStack generatedItem = armor.generateItem();

        event.setCurrentItem(generatedItem);
        event.setCursor(null);

        Bukkit.getScheduler().runTaskAsynchronously(RunicItems.getInstance(), () -> {
            PlayerStatHolder holder = PlayerManager.getCachedPlayerStats().get(event.getWhoClicked().getUniqueId());
            holder.updateHelmet();
            holder.updateChestplate();
            holder.updateLeggings();
            holder.updateBoots();
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
