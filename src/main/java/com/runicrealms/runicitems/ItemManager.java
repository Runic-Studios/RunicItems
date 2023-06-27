package com.runicrealms.runicitems;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.runicrealms.runicitems.api.InventoryAPI;
import com.runicrealms.runicitems.item.RunicItem;
import com.runicrealms.runicitems.item.RunicItemArmor;
import com.runicrealms.runicitems.item.RunicItemArtifact;
import com.runicrealms.runicitems.item.RunicItemBook;
import com.runicrealms.runicitems.item.RunicItemDynamic;
import com.runicrealms.runicitems.item.RunicItemGem;
import com.runicrealms.runicitems.item.RunicItemGeneric;
import com.runicrealms.runicitems.item.RunicItemOffhand;
import com.runicrealms.runicitems.item.RunicItemWeapon;
import com.runicrealms.runicitems.item.event.RunicItemGenericTriggerEvent;
import com.runicrealms.runicitems.item.template.RunicItemArmorTemplate;
import com.runicrealms.runicitems.item.template.RunicItemArtifactTemplate;
import com.runicrealms.runicitems.item.template.RunicItemBookTemplate;
import com.runicrealms.runicitems.item.template.RunicItemDynamicTemplate;
import com.runicrealms.runicitems.item.template.RunicItemGemTemplate;
import com.runicrealms.runicitems.item.template.RunicItemGenericTemplate;
import com.runicrealms.runicitems.item.template.RunicItemOffhandTemplate;
import com.runicrealms.runicitems.item.template.RunicItemTemplate;
import com.runicrealms.runicitems.item.template.RunicItemWeaponTemplate;
import com.runicrealms.runicitems.item.util.ClickTrigger;
import com.runicrealms.runicitems.util.NBTUtil;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerAttemptPickupItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ItemManager implements InventoryAPI, Listener {
    private static int TICK_COUNTER = 0;

    public ItemManager() {
        Bukkit.getPluginManager().registerEvents(this, RunicItems.getInstance());
        Bukkit.getScheduler().runTaskTimer(RunicItems.getInstance(), () -> {
            TICK_COUNTER++;
            if (TICK_COUNTER >= 10) {
                TICK_COUNTER = 0;
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

        We use this packet + InventoryClickEvent to handle all the events without overlap.

        In addition, this receiver should fire before the bukkit events, allowing us to handle it before bukkit.
         */
        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(RunicItems.getInstance(), PacketType.Play.Client.BLOCK_DIG) {
            @Override
            public void onPacketReceiving(PacketEvent event) {
                PacketContainer container = event.getPacket();
                if (container.getPlayerDigTypes().getValues().get(0).equals(EnumWrappers.PlayerDigType.DROP_ITEM) // Just Q
                        || container.getPlayerDigTypes().getValues().get(0).equals(EnumWrappers.PlayerDigType.DROP_ALL_ITEMS)) { // CTRL+Q
                    if (event.getPlayer().getGameMode() == GameMode.CREATIVE) return;
                    DupeManager.checkInventoryForDupes(event.getPlayer().getInventory(), event.getPlayer());
                }
            }
        });
    }

    public static RunicItem getRunicItemFromItemStack(ItemStack itemStack) {
        if (itemStack == null || itemStack.getType() == Material.AIR) return null;
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
        } else if (template instanceof RunicItemGemTemplate) {
            return RunicItemGem.getFromItemStack(itemStack);
        }
        return null;
    }

//    @EventHandler(priority = EventPriority.HIGHEST)
//    public void onCollectToCursor(InventoryClickEvent event) {
//        if (event.isCancelled()) return;
//        if (!(event.getWhoClicked() instanceof Player player)) return;
//
//        if (event.getAction() == InventoryAction.COLLECT_TO_CURSOR) {
//            ItemStack cursor = event.getCursor();
//            if (cursor != null && cursor.getType() != Material.AIR) {
//                int cursorAmount = cursor.getAmount();
//                if (cursorAmount != cursor.getMaxStackSize()) {
//                    if (event.getClickedInventory() != null) {
//                        for (ItemStack item : event.getClickedInventory()) {
//                            if (item == null || item.getType() == Material.AIR) continue;
//                            if (item.getAmount() == item.getMaxStackSize()) continue;
//                            if (!NBTUtil.isNBTSimilar(item, cursor, false, false)) continue;
//                            if (cursorAmount + item.getAmount() <= cursor.getMaxStackSize()) {
//                                cursorAmount += item.getAmount();
//                                ItemRemover.takeItem(player, item, item.getAmount());
//                            } else {
//                                item.setAmount(item.getAmount() - cursor.getMaxStackSize() + cursorAmount);
//                                cursorAmount = cursor.getMaxStackSize();
//                                break;
//                            }
//                        }
//                    }
//                    if (event.getWhoClicked().getInventory() != event.getClickedInventory()) {
//                        for (ItemStack item : event.getWhoClicked().getInventory()) {
//                            if (item == null || item.getType() == Material.AIR) continue;
//                            if (item.getAmount() == item.getMaxStackSize()) continue;
//                            if (!NBTUtil.isNBTSimilar(item, cursor, false, false)) continue;
//                            if (cursorAmount + item.getAmount() <= cursor.getMaxStackSize()) {
//                                cursorAmount += item.getAmount();
//                                ItemRemover.takeItem(player, item, item.getAmount());
//                            } else {
//                                item.setAmount(item.getAmount() - cursor.getMaxStackSize() + cursorAmount);
//                                cursorAmount = cursor.getMaxStackSize();
//                                break;
//                            }
//                        }
//                    }
//                }
//                cursor.setAmount(cursorAmount);
//                event.setCursor(cursor); // only deprecated because it can create server-client de-sync, don't care lol
//            }
//        }
//    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDrop(PlayerDropItemEvent event) {
        if (event.isCancelled()) return;
        ItemStack item = event.getItemDrop().getItemStack().clone();
        NBTItem nbtItem = new NBTItem(item, true);
        if (!nbtItem.hasNBTData()) return;
        if (nbtItem.hasKey("last-count")) nbtItem.removeKey("last-count");
        if (nbtItem.hasKey("id")) nbtItem.removeKey("id");
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPickup(PlayerAttemptPickupItemEvent event) {
        if (event.isCancelled()) return;
        if (TICK_COUNTER != 0) {
            event.setCancelled(true);
            return;
        }

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

                    RunicItemsAPI.addItem(event.getPlayer().getInventory(), itemToAdd);

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
            overflow = RunicItemsAPI.addItem(event.getPlayer().getInventory(), droppedItem);
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
            PacketContainer pickupItemPacket = new PacketContainer(PacketType.Play.Server.COLLECT);

            pickupItemPacket.getIntegers().write(0, event.getItem().getEntityId());
            pickupItemPacket.getIntegers().write(1, event.getPlayer().getEntityId());
            pickupItemPacket.getIntegers().write(2, pickupItemCount);

            ProtocolLibrary.getProtocolManager().sendServerPacket(event.getPlayer(), pickupItemPacket);

            event.getPlayer().updateInventory();
            event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.ENTITY_ITEM_PICKUP, 1, 1);
        }

        if (remove) event.getItem().remove();
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
            if (itemStack == null || itemStack.getType() == Material.AIR) return;
            RunicItem item = getRunicItemFromItemStack(itemStack);
            if (!(item instanceof RunicItemGeneric generic)) return;
            ClickTrigger clickTrigger = ClickTrigger.getFromInteractAction(event.getAction(), event.getPlayer());
            if (generic.getTriggers().containsKey(clickTrigger)) {
                boolean isDuped = event.getPlayer().getGameMode() != GameMode.CREATIVE && DupeManager.checkInventoryForDupes(inventory, event.getItem(), event, event.getPlayer(), event.getPlayer().getInventory().getHeldItemSlot());
                if (!isDuped)
                    Bukkit.getPluginManager().callEvent(new RunicItemGenericTriggerEvent(event.getPlayer(), generic, itemStack, clickTrigger, generic.getTriggers().get(clickTrigger)));
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onSwapWithCursor(InventoryClickEvent event) {
        if (event.isCancelled()) return;
        if (!(event.getWhoClicked() instanceof Player)) return;

        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR)
            return;
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

}