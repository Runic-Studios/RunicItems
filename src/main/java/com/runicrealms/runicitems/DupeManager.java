package com.runicrealms.runicitems;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.api.RunicBankAPI;
import com.runicrealms.plugin.api.RunicCoreAPI;
import com.runicrealms.runicguilds.gui.GuildBankUtil;
import com.runicrealms.runicitems.command.RunicItemCommand;
import com.runicrealms.runicitems.util.NBTUtil;
import de.tr7zw.nbtapi.NBTItem;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.ServerOperator;

import java.awt.*;
import java.text.SimpleDateFormat;

public class DupeManager implements Listener {

    public static final String TEXT_CHANNEL_ID = "813580198133628928";
    public static final Color EMBED_COLOR = new Color(204, 35, 184);

    private static TextChannel channel;

    private static long nextId = Long.MIN_VALUE;

    public static void setupJda() {
        channel = RunicItems.getJda().getTextChannelById(TEXT_CHANNEL_ID);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player) {
            final Player player = (Player) event.getWhoClicked();
            if (!RunicCoreAPI.getLoadedCharacters().contains(player.getUniqueId())) return;
            final ItemStack currentItem;
            final CurrentItemType type;
            if (event.getClickedInventory() == event.getWhoClicked().getInventory()) {
                if (event.getSlot() == 0 || event.getSlot() == 7 || event.getSlot() == 8) return;
            }
            if (event.getAction() == InventoryAction.PICKUP_HALF
                    && (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR)
                    && event.getCursor() != null && event.getCursor().getType() != Material.AIR) {
                assignNewDupeId(event.getCursor());
            }

            if (event.getAction() == InventoryAction.DROP_ALL_CURSOR
                    || event.getAction() == InventoryAction.DROP_ONE_CURSOR) {
                if (player.getGameMode() != GameMode.CREATIVE && checkInventoryForDupes(event.getWhoClicked().getInventory(), event.getCursor(), CurrentItemType.CURSOR, event, player)) return;
            } else if (event.getAction() == InventoryAction.DROP_ALL_SLOT
                    || event.getAction() == InventoryAction.DROP_ONE_SLOT) {
                if (player.getGameMode() != GameMode.CREATIVE && checkInventoryForDupes(event.getClickedInventory(), event.getCurrentItem(), CurrentItemType.CURRENT, event, player)) return;
            }

            if (event.getCurrentItem() != null && event.getCurrentItem().getType() != Material.AIR) {
                currentItem = event.getCurrentItem();
                type = CurrentItemType.CURRENT;
            } else if (event.getCursor() != null && event.getCursor().getType() != Material.AIR) {
                currentItem = event.getCursor();
                type = CurrentItemType.CURSOR;
            } else return;
            if (GuildBankUtil.isViewingBank(player.getUniqueId())) {
                if (player.getGameMode() != GameMode.CREATIVE && checkInventoryForDupes(player.getOpenInventory().getTopInventory(), currentItem, type, event, player)) return;
            }
            if (RunicBankAPI.isViewingBank(player)) {
                if (player.getGameMode() != GameMode.CREATIVE && checkInventoryForDupes(player.getOpenInventory().getTopInventory(), currentItem, type, event, player)) return;
            }
            if (player.getGameMode() != GameMode.CREATIVE) checkInventoryForDupes(player.getInventory(), currentItem, type, event, player);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.isCancelled()) return;
        Bukkit.getScheduler().runTaskLater(RunicItems.getInstance(), () -> {
            event.getNewItems().keySet().forEach(slot -> {
                ItemStack item = event.getWhoClicked().getInventory().getItem(slot);
                if (item == null || item.getType() == Material.AIR) return;
                assignNewDupeId(item);
            });
        }, 1L);
        ItemStack item = event.getCursor();
        if (item == null || item.getType() == Material.AIR) return;
        assignNewDupeId(item);
        event.setCursor(item);
    }

    public static void checkMissingDupeNBT(ItemStack item) {
        NBTItem nbtItem = new NBTItem(item, true);
        if (!nbtItem.hasNBTData() || !nbtItem.hasKey("template-id")) return;
        if (!nbtItem.hasKey("id")) nbtItem.setLong("id", getNextItemId());
        if (!nbtItem.hasKey("last-count")
                || nbtItem.getInteger("last-count") != item.getAmount())
            nbtItem.setInteger("last-count", item.getAmount());
    }

    public static void assignNewDupeId(ItemStack item) {
        NBTItem nbtItem = new NBTItem(item, true);
        if (nbtItem.hasNBTData() && nbtItem.hasKey("isRI") && !nbtItem.getBoolean("isRI")) return;
        nbtItem.setLong("id", getNextItemId());
    }

    public static boolean checkItemsDuped(ItemStack itemOne, ItemStack itemTwo) {
        NBTItem nbtItemOne = new NBTItem(itemOne, true);
        NBTItem nbtItemTwo = new NBTItem(itemTwo, true);
        if (!nbtItemOne.hasNBTData()) return false;
        if (!nbtItemOne.hasKey("id")) {
            if (nbtItemOne.hasKey("template-id")) {
                nbtItemOne.setLong("id", getNextItemId());
            }
            return false;
        }
        if (!nbtItemOne.hasKey("last-count")) return false;
        if (nbtItemOne.getInteger("last-count") != itemOne.getAmount()) {
            nbtItemOne.setInteger("last-count", itemOne.getAmount());
            nbtItemOne.setLong("id", getNextItemId());
            return false;
        }
        if (!nbtItemTwo.hasNBTData()) return false;
        if (!nbtItemTwo.hasKey("id")) {
            if (nbtItemTwo.hasKey("template-id")) {
                nbtItemTwo.setLong("id", getNextItemId());
            }
            return false;
        }
        if (!nbtItemTwo.hasKey("last-count")) return false;
        if (nbtItemTwo.getInteger("last-count") != itemTwo.getAmount()) {
            nbtItemTwo.setInteger("last-count", itemTwo.getAmount());
            nbtItemTwo.setLong("id", getNextItemId());
            return false;
        }
        return nbtItemOne.getLong("id").equals(nbtItemTwo.getLong("id"));
    }

    public static long getNextItemId() {
        return nextId++;
    }

    private static String getItemName(ItemStack item) {
        if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            return ChatColor.stripColor(item.getItemMeta().getDisplayName());
        }
        return item.getType().toString().toLowerCase();
    }

    // For inventory click events
    public static boolean checkInventoryForDupes(Inventory inventory, ItemStack currentItem, CurrentItemType type, InventoryClickEvent event, Player player) {
        if (currentItem == null) return false;
        int ignoreSlot = -1;
        if (type != CurrentItemType.CURSOR) {
            for (int i = 0; i < inventory.getSize(); i++) {
                ItemStack item = inventory.getItem(i);
                if (item == null) continue;
                if (item.getType() != Material.AIR && item.getAmount() == currentItem.getAmount()) {
                    if (NBTUtil.isNBTSimilar(item, currentItem, true, true)) {
                        ignoreSlot = i;
                        break; // We only want to remove once!!!
                    }
                }
            }
        }

        boolean hasDuped = checkInventoryForDupesNoDelete(inventory, currentItem, player, ignoreSlot);
        if (hasDuped) type.deleteItem(event);
        return hasDuped;
    }

    // For player right/left clicking with generic item
    public static boolean checkInventoryForDupes(Inventory inventory, ItemStack currentItem, PlayerInteractEvent event, Player player, int ignoreSlot) {
        boolean hasDuped = checkInventoryForDupesNoDelete(inventory, currentItem, player, ignoreSlot);
        if (hasDuped) {
            event.getPlayer().getInventory().setItemInMainHand(null);
        }
        return hasDuped;
    }

    // For using the item the player is holding
    public static boolean checkInventoryForDupes(Inventory inventory, Player player) {
        boolean hasDuped = checkInventoryForDupesNoDelete(inventory, player.getInventory().getItemInMainHand(), player, player.getInventory().getHeldItemSlot());
        if (hasDuped) {
            player.getInventory().setItemInMainHand(null);
        }
        return hasDuped;
    }

    private static boolean checkInventoryForDupesNoDelete(Inventory inventory, ItemStack currentItem, Player player, int ignoreSlot) {
        if (currentItem == null) return false;
        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack item = inventory.getItem(i);
            if (item == null) continue;
            if (item.getType() != Material.AIR && item != currentItem && i != ignoreSlot) {
                if (checkItemsDuped(item, currentItem)) {
                    if (channel != null) {
                        channel.sendMessage(new EmbedBuilder()
                                .setColor(EMBED_COLOR)
                                .setTitle("Dupe Notification")
                                .setDescription("Player `"
                                        + player.getName()
                                        + "` has attempted to dupe `"
                                        + currentItem.getAmount()
                                        + "x "
                                        + getItemName(currentItem)
                                        + "` at "
                                        + new SimpleDateFormat("MM/dd/yy HH:mm:ss").format(System.currentTimeMillis())
                                        + "(db: " + RunicCore.getInstance().getConfig().getString("database") + ")"
                                ).build()).queue();
                        Bukkit.getOnlinePlayers().stream().filter(ServerOperator::isOp).forEach(target -> {
                            target.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                    RunicItemCommand.PREFIX + "Player " + player.getName() + " has attempted dupe. Check discord for more info."
                                    ));
                        });
                    }
                    return true;
                }
            }
        }
        return false;
    }



    private enum CurrentItemType {
        CURRENT, CURSOR;

        void deleteItem(InventoryClickEvent event) {
            switch (this) {
                case CURSOR: event.setCursor(null);
                case CURRENT: event.setCurrentItem(null);
            }
        }
    }

}