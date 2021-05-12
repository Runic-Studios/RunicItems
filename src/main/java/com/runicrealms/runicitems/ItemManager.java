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
import com.runicrealms.runicitems.util.NBTUtil;
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

    private static final String INVENTORY_PATH = "inventory";

    @EventHandler
    public void onCharacterJoin(CharacterLoadEvent event) {
        //if (RunicItems.isDatabaseLoadingEnabled()) {
            if (event.getPlayerCache().getMongoData().has("character." + event.getSlot() + ".inventory")) {
                Data data = event.getPlayerCache().getMongoData().getSection("character." + event.getSlot() + ".inventory");
                for (String key : data.getKeys()) {
                    if (!key.equalsIgnoreCase("type")) {
                        RunicItem item = ItemLoader.loadItem(data.getSection(key), DupeManager.getNextItemId());
                        if (item != null) event.getPlayer().getInventory().setItem(Integer.parseInt(key), item.generateItem());
                    }
                }
            }
        //}
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onCacheSave(CacheSaveEvent event) {
        //if (RunicItems.isDatabaseLoadingEnabled()) {
        ItemStack[] contents = event.getPlayer().getInventory().getContents();
        MongoDataSection character = event.getMongoDataSection();
        character.remove(INVENTORY_PATH); // removes all stored inventory stuffs
        character.set(INVENTORY_PATH + ".type", "runicitems");
        character.save();
        for (int i = 0; i < contents.length; i++) {
            if (contents[i] != null) {
                RunicItem runicItem = getRunicItemFromItemStack(contents[i]);
                if (runicItem != null) {
                    runicItem.addToData(character, i + "");
                }
            }
        }
        character.save();
        //}
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
        if (!event.isCancelled()) {
            if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;
            if (event.getCursor() == null || event.getCursor().getType() == Material.AIR) return;
            if (NBTUtil.isNBTSimilar(event.getCurrentItem(), event.getCursor(), false, false)) {
                if (event.getCurrentItem().getAmount() == event.getCurrentItem().getMaxStackSize()) {
                    //Bukkit.broadcastMessage("1");
                    ItemStack currentItem = event.getCurrentItem();
                    event.setCurrentItem(event.getCursor());
                    event.setCursor(currentItem);
                } else if (event.getCurrentItem().getAmount() + event.getCursor().getAmount() == event.getCurrentItem().getMaxStackSize()) {
                    //Bukkit.broadcastMessage("2");
                    ItemStack item = event.getCurrentItem();
                    item.setAmount(event.getCurrentItem().getMaxStackSize());
                    event.setCurrentItem(item);
                    event.setCursor(null);
                } else if (event.getCurrentItem().getAmount() + event.getCursor().getAmount() > event.getCurrentItem().getMaxStackSize()) {
                    //Bukkit.broadcastMessage("3");
                    ItemStack cursorItem = event.getCursor();
                    cursorItem.setAmount(event.getCursor().getAmount() - (event.getCurrentItem().getMaxStackSize() - event.getCurrentItem().getAmount()));
                    ItemStack currentItem = event.getCurrentItem();
                    currentItem.setAmount(currentItem.getMaxStackSize());
                    event.setCursor(cursorItem);
                    event.setCurrentItem(currentItem);
                } else if (event.getCurrentItem().getAmount() + event.getCursor().getAmount() < event.getCurrentItem().getMaxStackSize()) {
                    //Bukkit.broadcastMessage("4");
                    ItemStack item = event.getCurrentItem();
                    item.setAmount(item.getAmount() + event.getCursor().getAmount());
                    event.setCurrentItem(item);
                    event.setCursor(null);
                }
            }
        }
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

    public static String getInventoryPath() {
        return INVENTORY_PATH;
    }
}