package com.runicrealms.runicitems;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers;
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
import net.minecraft.server.v1_16_R3.PacketPlayOutCollect;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerAttemptPickupItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;


public class ItemManager implements Listener {

    private static final String INVENTORY_PATH = "inventory";

    private static int tickCounter = 0;

    public ItemManager() {
        Bukkit.getScheduler().runTaskTimer(RunicItems.getInstance(), () -> {
            tickCounter++;
            if (tickCounter >= 20) {
                tickCounter = 0;
            }
        }, 0L, 1L);

        /*
        Note to developers:
        We use this packet receiver instead of using PlayerDropItemEvent because
        dropping events are split into multiple categories: hitting q with an item in your hand,
        using ctrl+q, opening inventory and hitting q or moving items outside the inventory, etc.

        PlayerDropItemEvent fires on all of these, InventoryClickEvent (while checking event.getAction)
        fires only when you have the inventory open, and this packet only fires when you don't have your
        inventory open and hit q.

        We use this packet + InventoryClickEvent to handle all of the events without overlap.

        In addition, this receiver should fire before the bukkit events, allowing us to handle it before bukkit.
         */
        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(RunicItems.getInstance(), PacketType.Play.Client.BLOCK_DIG) {
            @Override
            public void onPacketReceiving(PacketEvent event) {
                PacketContainer container = event.getPacket();
                if (container.getPlayerDigTypes().getValues().get(0).equals(EnumWrappers.PlayerDigType.DROP_ITEM) // Just Q
                        || container.getPlayerDigTypes().getValues().get(0).equals(EnumWrappers.PlayerDigType.DROP_ALL_ITEMS)) { // CTRL+Q
                    DupeManager.checkInventoryForDupes(event.getPlayer().getInventory(), event.getPlayer());
                }
            }
        });
    }

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
        if (event.getHand() == EquipmentSlot.HAND
                && (event.getAction() == Action.LEFT_CLICK_AIR
                || event.getAction() == Action.LEFT_CLICK_BLOCK
                || event.getAction() == Action.RIGHT_CLICK_AIR
                || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
            PlayerInventory inventory = event.getPlayer().getInventory();
            if (event.getHand() == null) return;
            ItemStack itemStack = inventory.getItem(event.getHand());
            if (itemStack.getType() == Material.AIR) return;
            RunicItem item = getRunicItemFromItemStack(itemStack);
            if (!(item instanceof RunicItemGeneric)) return;
            RunicItemGeneric generic = (RunicItemGeneric) item;
            ClickTrigger clickTrigger = ClickTrigger.getFromInteractAction(event.getAction(), event.getPlayer());
            if (generic.getTriggers().containsKey(clickTrigger)) {
                boolean isDuped = DupeManager.checkInventoryForDupes(inventory, event.getItem(), event, event.getPlayer(), event.getPlayer().getInventory().getHeldItemSlot());
                if (!isDuped) Bukkit.getPluginManager().callEvent(new RunicItemGenericTriggerEvent(event.getPlayer(), generic, itemStack, clickTrigger, generic.getTriggers().get(clickTrigger)));
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onSwapWithCursor(InventoryClickEvent event) {
        if (event.isCancelled()) return;
        if (!(event.getWhoClicked() instanceof Player)) return;

        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;
        if (event.getCursor() == null || event.getCursor().getType() == Material.AIR) return;

        if (event.getAction() == InventoryAction.SWAP_WITH_CURSOR) {
            if (NBTUtil.isNBTSimilar(event.getCurrentItem(), event.getCursor(), false, false)) {
                if (event.isLeftClick()) {
                    if (event.getCurrentItem().getAmount() + event.getCursor().getAmount() == event.getCurrentItem().getMaxStackSize()) {
                        ItemStack item = event.getCurrentItem();
                        item.setAmount(event.getCurrentItem().getMaxStackSize());
                        event.setCurrentItem(item);
                        event.setCursor(null);
                        event.setCancelled(true);
                    } else if (event.getCurrentItem().getAmount() + event.getCursor().getAmount() > event.getCurrentItem().getMaxStackSize()) {
                        ItemStack cursorItem = event.getCursor();
                        cursorItem.setAmount(event.getCursor().getAmount() - (event.getCurrentItem().getMaxStackSize() - event.getCurrentItem().getAmount()));
                        ItemStack currentItem = event.getCurrentItem();
                        currentItem.setAmount(currentItem.getMaxStackSize());
                        event.setCursor(cursorItem);
                        event.setCurrentItem(currentItem);
                        event.setCancelled(true);
                    } else if (event.getCurrentItem().getAmount() + event.getCursor().getAmount() < event.getCurrentItem().getMaxStackSize()) {
                        ItemStack item = event.getCurrentItem();
                        item.setAmount(item.getAmount() + event.getCursor().getAmount());
                        event.setCurrentItem(item);
                        event.setCursor(null);
                        event.setCancelled(true);
                    }
                } else if (event.isRightClick() && !event.isShiftClick()) {
                    if (event.getCurrentItem().getAmount() != event.getCurrentItem().getMaxStackSize()) {
                        ItemStack currentItem = event.getCurrentItem();
                        currentItem.setAmount(currentItem.getAmount() + 1);
                        event.setCurrentItem(currentItem);
                        if (event.getCursor().getAmount() > 1) {
                            ItemStack cursor = event.getCursor();
                            cursor.setAmount(cursor.getAmount() - 1);
                            event.setCursor(cursor);
                        } else {
                            event.setCursor(null);
                        }
                        event.setCancelled(true);
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCollectToCursor(InventoryClickEvent event) {
        if (event.isCancelled()) return;
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();

        if (event.getAction() == InventoryAction.COLLECT_TO_CURSOR) {
            ItemStack cursor = event.getCursor();
            if (cursor != null && cursor.getType() != Material.AIR) {
                int cursorAmount = cursor.getAmount();
                if (cursorAmount != cursor.getMaxStackSize()) {
                    if (event.getClickedInventory() != null) {
                        for (ItemStack item : event.getClickedInventory()) {
                            if (item == null || item.getType() == Material.AIR) continue;
                            if (item.getAmount() == item.getMaxStackSize()) continue;
                            if (!NBTUtil.isNBTSimilar(item, cursor, false, false)) continue;
                            if (cursorAmount + item.getAmount() <= cursor.getMaxStackSize()) {
                                cursorAmount += item.getAmount();
                                player.getInventory().remove(item);
                            } else {
                                item.setAmount(item.getAmount() - cursor.getMaxStackSize() + cursorAmount);
                                cursorAmount = cursor.getMaxStackSize();
                                break;
                            }
                        }
                    }
                    if (event.getWhoClicked().getInventory() != event.getClickedInventory()) {
                        for (ItemStack item : event.getWhoClicked().getInventory()) {
                            if (item == null || item.getType() == Material.AIR) continue;
                            if (item.getAmount() == item.getMaxStackSize()) continue;
                            if (!NBTUtil.isNBTSimilar(item, cursor, false, false)) continue;
                            if (cursorAmount + item.getAmount() <= cursor.getMaxStackSize()) {
                                cursorAmount += item.getAmount();
                                player.getInventory().remove(item);
                            } else {
                                item.setAmount(item.getAmount() - cursor.getMaxStackSize() + cursorAmount);
                                cursorAmount = cursor.getMaxStackSize();
                                break;
                            }
                        }
                    }
                }
                cursor.setAmount(cursorAmount);
                event.setCursor(cursor); // only deprecated because it can create server-client desync, don't care lol
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onMoveToOtherInventory(InventoryClickEvent event) {

        Inventory clickedInventory = event.getClickedInventory();
        Inventory targetInventory = event.getClickedInventory() instanceof PlayerInventory ? event.getView().getTopInventory() : event.getWhoClicked().getInventory();

        String inventoryTitle = ChatColor.stripColor(event.getView().getTitle());
        if (inventoryTitle.equalsIgnoreCase("Guild Bank")) return;

        if (event.isCancelled()) return;
        if (!(event.getWhoClicked() instanceof Player)) return;

        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;

        if (event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
            int amountLeft = event.getCurrentItem().getAmount();
            if (clickedInventory != null) {
                for (ItemStack item : targetInventory) {
                    if (item == null || item.getType() == Material.AIR) continue;
                    if (item.getAmount() == item.getMaxStackSize()) continue;
                    if (!NBTUtil.isNBTSimilar(item, event.getCurrentItem(), false, false)) continue;
                    if (item.getAmount() + amountLeft <= item.getMaxStackSize()) {
                        item.setAmount(item.getAmount() + amountLeft);
                        clickedInventory.remove(event.getCurrentItem());
                        event.setCurrentItem(null);
                        event.setCancelled(true);
                        amountLeft = 0;
                        break;
                    } else {
                        amountLeft -= item.getMaxStackSize() - item.getAmount();
                        item.setAmount(item.getMaxStackSize());
                    }
                }
            }
            if (amountLeft > 0) {
                event.getCurrentItem().setAmount(amountLeft);
            }
            ((Player) event.getWhoClicked()).updateInventory();
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPickup(PlayerAttemptPickupItemEvent event) {

        if (!event.getFlyAtPlayer()) return;

        if (tickCounter != 0) return;

        if (event.isCancelled()) return;

        event.setCancelled(true);

        ItemStack droppedItem = event.getItem().getItemStack().clone();
        DupeManager.checkMissingDupeNBT(droppedItem);

        int initialAmount = event.getItem().getItemStack().getAmount();

        if (droppedItem.getType() == Material.AIR) return;

        int amountLeft = droppedItem.getAmount();
        ItemStack[] contents = event.getPlayer().getInventory().getContents();

        List<Integer> slotsReceivingNewIds = new LinkedList<>();

        for (int i = 0; i < contents.length; i++) {

            ItemStack item = contents[i];

            if (amountLeft > 0) {

                if (item == null || item.getType() == Material.AIR) continue;
                if (item.getAmount() == item.getMaxStackSize()) continue;

                if (NBTUtil.isNBTSimilar(droppedItem, item, false, false)) {

                    ItemStack itemToAdd = item.clone();

                    if (item.getAmount() + amountLeft <= item.getMaxStackSize()) {

                        itemToAdd.setAmount(amountLeft);
                        amountLeft = 0;

                    } else if (item.getAmount() + amountLeft > item.getMaxStackSize()) {

                        int amountAdded = item.getMaxStackSize() - item.getAmount();
                        itemToAdd.setAmount(amountAdded);
                        amountLeft -= amountAdded;

                    }

                    slotsReceivingNewIds.add(i);

                    event.getPlayer().getInventory().addItem(itemToAdd);

                }

            } else break;

        }

        for (int slot : slotsReceivingNewIds) {
            ItemStack item = event.getPlayer().getInventory().getItem(slot);
            if (item != null && item.getType() != Material.AIR) {
                DupeManager.assignNewDupeId(item);
            }
        }

        HashMap<Integer, ItemStack> overflow = null;

        if (amountLeft > 0) {
            droppedItem.setAmount(amountLeft);
            overflow = event.getPlayer().getInventory().addItem(droppedItem);
        }

        boolean pickedUp = true;
        boolean remove = false;
        int pickupItemCount = 1;

        if (overflow != null && !overflow.isEmpty()) {
            AtomicInteger totalOverflow = new AtomicInteger();
            overflow.forEach((slot, leftOver) -> totalOverflow.addAndGet(leftOver.getAmount()));
            if (totalOverflow.get() != initialAmount) {
                ItemStack ground = event.getItem().getItemStack();
                ground.setAmount(totalOverflow.get());
                event.getItem().setItemStack(ground);
                pickupItemCount = totalOverflow.get();
            } else pickedUp = false;
        } else {
            remove = true;
            pickupItemCount = event.getItem().getItemStack().getAmount();
        }

        if (pickedUp) {
            PacketPlayOutCollect packet = new PacketPlayOutCollect(event.getItem().getEntityId(), event.getPlayer().getEntityId(), pickupItemCount);
            ((CraftPlayer) event.getPlayer()).getHandle().playerConnection.sendPacket(packet);
            event.getPlayer().updateInventory();
            event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.ENTITY_ITEM_PICKUP, 1, 1);
        }

        if (remove) event.getItem().remove();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDrop(PlayerDropItemEvent event) {
        if (event.isCancelled()) return;
        ItemStack item = event.getItemDrop().getItemStack().clone();
        NBTItem nbtItem = new NBTItem(item, true);
        if (!nbtItem.hasNBTData()) return;
        if (nbtItem.hasKey("last-count")) nbtItem.removeKey("last-count");
        if (nbtItem.hasKey("id")) nbtItem.removeKey("id");
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
        } else if (template instanceof RunicItemDynamicTemplate) {
            return RunicItemDynamic.getFromItemStack(itemStack);
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