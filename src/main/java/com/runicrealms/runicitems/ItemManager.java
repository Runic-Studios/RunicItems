package com.runicrealms.runicitems;

import com.runicrealms.plugin.character.api.CharacterLoadEvent;
import com.runicrealms.plugin.database.Data;
import com.runicrealms.plugin.database.MongoDataSection;
import com.runicrealms.plugin.database.event.CacheSaveEvent;
import com.runicrealms.runicitems.config.ItemLoader;
import com.runicrealms.runicitems.item.*;
import com.runicrealms.runicitems.item.event.RunicItemGenericTriggerEvent;
import com.runicrealms.runicitems.item.template.*;
import com.runicrealms.runicitems.item.util.ClickTrigger;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;


public class ItemManager implements Listener {

    @EventHandler
    public void onCharacterJoin(CharacterLoadEvent event) {
        if (RunicItems.isDatabaseLoadingEnabled()) {
            if (event.getPlayerCache().getMongoData().has("character." + event.getSlot() + ".inventory")) {
                Data data = event.getPlayerCache().getMongoData().getSection("character." + event.getSlot() + ".inventory");
                for (String key : data.getKeys()) {
                    if (!key.equalsIgnoreCase("type")) {
                        event.getPlayer().getInventory().setItem(Integer.parseInt(key), ItemLoader.loadItem(data.getSection(key), DupeManager.getNextItemId()).generateItem());
                    }
                }
            }
        }
    }

    @EventHandler
    public void onCacheSave(CacheSaveEvent event) {
        if (RunicItems.isDatabaseLoadingEnabled()) {
            if (!event.getMongoDataSection().has("inventory")) {
                event.getMongoDataSection().set("inventory.type", "runicitems");
                event.getMongoDataSection().save();
            }
            MongoDataSection inventorySection = event.getMongoDataSection().getSection("inventory");
            for (String key : inventorySection.getKeys()) {
                inventorySection.remove(key);
            }
            event.getMongoDataSection().save();
            ItemStack[] contents = event.getPlayer().getInventory().getContents();
            for (int i = 0; i < contents.length; i++) {
                if (contents[i] != null) {
                    RunicItem runicItem = getRunicItemFromItemStack(contents[i]);
                    if (runicItem != null) {
                        runicItem.addToData(inventorySection, i + "");
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!event.isCancelled()) {
            if (event.getAction() == Action.LEFT_CLICK_AIR
                    || event.getAction() == Action.LEFT_CLICK_BLOCK
                    || event.getAction() == Action.RIGHT_CLICK_AIR
                    || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                if (event.getPlayer().getInventory().getItemInMainHand() != null
                        && event.getPlayer().getInventory().getItemInMainHand().getType() != Material.AIR) {
                    Bukkit.getScheduler().runTaskAsynchronously(RunicItems.getInstance(), () -> {
                        RunicItem item = getRunicItemFromItemStack(event.getPlayer().getInventory().getItemInMainHand());
                        if (item == null) return;
                        if (!(item instanceof RunicItemGeneric)) return;
                        RunicItemGeneric generic = (RunicItemGeneric) item;
                        ClickTrigger clickTrigger = ClickTrigger.getFromInteractAction(event.getAction(), event.getPlayer());
                        if (generic.getTriggers().containsKey(clickTrigger)) {
                            Bukkit.getScheduler().runTask(RunicItems.getInstance(), () -> {
                                Bukkit.getPluginManager().callEvent(new RunicItemGenericTriggerEvent(event.getPlayer(), generic, clickTrigger, generic.getTriggers().get(clickTrigger)));
                            });
                        }
                    });
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent event) {
//        if (!event.isCancelled()) {
//            for ()
//        }
    }

    public static RunicItem getRunicItemFromItemStack(ItemStack itemStack) {
        NBTItem nbtItem = new NBTItem(itemStack);
        if ((!nbtItem.hasNBTData()) || (!nbtItem.hasKey("template-id"))) return null;
        RunicItemTemplate template = TemplateManager.getTemplateFromId(nbtItem.getString("template-id"));
        if (template == null) return null;
        if (template instanceof RunicItemArmorTemplate) {
            return RunicItemArmor.getFromItemStack(itemStack);
        } else if (template instanceof RunicItemArtifactTemplate) {
            return RunicItemArtifact.getFromItemStack(itemStack);
        } else if (template instanceof RunicItemBookTemplate) {
            return RunicItemBook.getFromItemStack(itemStack);
        } else if (template instanceof RunicItemGenericTemplate) {
            return RunicItemGeneric.getFromItemStack(itemStack);
        } else if (template instanceof RunicItemOffhandTemplate) {
            return RunicItemOffhand.getFromItemStack(itemStack);
        } else if (template instanceof RunicItemWeaponTemplate) {
            return RunicItemWeapon.getFromItemStack(itemStack);
        }
        return null;
    }

}