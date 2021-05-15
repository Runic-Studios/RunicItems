package com.runicrealms.runicitems;

import com.runicrealms.plugin.api.RunicBankAPI;
import com.runicrealms.plugin.api.RunicCoreAPI;
import com.runicrealms.runicguilds.gui.GuildBankUtil;
import com.runicrealms.runicitems.util.NBTUtil;
import de.tr7zw.nbtapi.NBTItem;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.awt.*;
import java.text.SimpleDateFormat;

public class DupeManager implements Listener {

    public static final int MAX_ITEMS_CLICKED_CACHE_LENGTH = 50;

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
            if (RunicCoreAPI.getPlayerCache(player) == null) return;
            Bukkit.getScheduler().runTaskAsynchronously(RunicItems.getInstance(), () -> {
                final ItemStack currentItem;
                final CurrentItemType type;
                if (event.getCurrentItem() != null && event.getCurrentItem().getType() != Material.AIR) {
                    currentItem = event.getCurrentItem();
                    type = CurrentItemType.CURRENT;
                } else if (event.getCursor() != null && event.getCursor().getType() != Material.AIR) {
                    currentItem = event.getCursor();
                    type = CurrentItemType.CURSOR;
                } else return;
                if (GuildBankUtil.isViewingBank(player.getUniqueId())) {
                    checkInventoryForDupes(player.getOpenInventory().getTopInventory(), currentItem, type, event, player);
                }
                if (RunicBankAPI.isViewingBank(player)) {
                    checkInventoryForDupes(player.getOpenInventory().getTopInventory(), currentItem, type, event, player);
                }
                checkInventoryForDupes(player.getInventory(), currentItem, type, event, player);
            });
        }
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

    private static boolean checkInventoryForDupes(Inventory inventory, ItemStack currentItem, CurrentItemType type, InventoryClickEvent event, Player player) {
        int ignoreSlot = -1;
        if (type != CurrentItemType.CURSOR) {
            for (int i = 0; i < inventory.getSize(); i++) {
                ItemStack item = inventory.getItem(i);
                if (item != null && item.getType() != Material.AIR && item.getAmount() == currentItem.getAmount()) {
                    if (NBTUtil.isNBTSimilar(item, currentItem, true, true)) {
                        ignoreSlot = i;
                        break; // We only want to remove once!!!
                    }
                }
            }
        }
        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack item = inventory.getItem(i);
            if (item != null && item.getType() != Material.AIR && item != currentItem && i != ignoreSlot) {
                if (checkItemsDuped(item, currentItem)) {
                    NBTItem nbtItemOne = new NBTItem(item);
                    NBTItem nbtItemTwo = new NBTItem(currentItem);
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
                                ).build()).queue();
                    }
                    type.deleteItem(event);
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