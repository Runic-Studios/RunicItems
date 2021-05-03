package com.runicrealms.runicitems;

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
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class DupeManager implements Listener {

    public static final int MAX_ITEMS_CLICKED_CACHE_LENGTH = 50;

    public static final String TEXT_CHANNEL_ID = "813580198133628928";
    public static final Color EMBED_COLOR = new Color(204, 35, 184);

    private static final Map<Player, ConcurrentLinkedQueue<ItemStack>> itemsClicked = new ConcurrentHashMap<>();

    private static TextChannel channel;

    private static long nextId = Long.MIN_VALUE;

    public static void setupJda() {
        channel = RunicItems.getJda().getTextChannelById(TEXT_CHANNEL_ID);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player) {
            final Player player = (Player) event.getWhoClicked();
            Bukkit.getScheduler().runTaskAsynchronously(RunicItems.getInstance(), () -> {
                final ItemStack currentItem;
                if (event.getCurrentItem() != null && event.getCurrentItem().getType() != Material.AIR) {
                    Bukkit.broadcastMessage("current");
                    currentItem = event.getCurrentItem();
                } else if (event.getCursor() != null && event.getCursor().getType() != Material.AIR) {
                    Bukkit.broadcastMessage("cursor");
                    currentItem = event.getCursor();
                } else {
                    return;
                }
                for (ItemStack itemOne : itemsClicked.get(player)) {
                    if (itemOne != null && itemOne.getType() != Material.AIR) {
                        if (itemOne != currentItem) {
                            if (!NBTUtil.isSimilar(itemOne, currentItem, true, true)) {
                                if (checkItemsDuped(itemOne, currentItem)) {
                                    NBTItem nbtItemOne = new NBTItem(itemOne);
                                    NBTItem nbtItemTwo = new NBTItem(currentItem);
                                    Bukkit.broadcastMessage("1: " + nbtItemOne.getLong("id") + "," + nbtItemOne.getInteger("last-count") + " 2: " + nbtItemTwo.getLong("id") + "," + nbtItemTwo.getInteger("last-count"));
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
                                    player.getInventory().remove(currentItem);
                                    itemsClicked.get(player).clear();
                                    return;
                                }
                            }
                        }
                    }
                }
                NBTItem nbtCurrentItem = new NBTItem(currentItem);
                if (nbtCurrentItem.hasNBTData() && nbtCurrentItem.hasKey("id")) {
                    boolean contains = false;
                    Iterator<ItemStack> iterator = itemsClicked.get(player).iterator();
                    while (iterator.hasNext()) {
                        ItemStack item = iterator.next();
                        if (item != null && item.getType() != Material.AIR) {
                            if ((!contains) && NBTUtil.isSimilar(currentItem, item, true, true)) {
                                contains = true;
                                Bukkit.broadcastMessage("contains");
                            }
                        } else {
                            itemsClicked.get(player).remove(item);
                        }
                    }
//                    boolean contains = false;
//                    Iterator<ItemStack> iterator = itemsClicked.get(player).iterator();
//                    while (iterator.hasNext()) {
//                        ItemStack item = iterator.next();
//                        if (item != null && item.getType() != Material.AIR) {
//                            NBTItem nbtItem = new NBTItem(item);
//                            if (nbtItem.hasNBTData()
//                                    && nbtItem.hasKey("id")
//                                    && nbtItem.getInteger("id").equals(nbtCurrentItem.getInteger("id"))) {
//                                contains = true;
//                                break;
//                            }
//                        } else {
//                            itemsClicked.get(player).remove(item);
//                        }
//                    }
                    if (!contains) {
                        Bukkit.broadcastMessage("doesn't contain");
                        while (itemsClicked.get(player).size() >= MAX_ITEMS_CLICKED_CACHE_LENGTH) {
                            itemsClicked.get(player).remove();
                        }
                        itemsClicked.get(player).add(currentItem);
                    }
                }
            });
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        itemsClicked.put(event.getPlayer(), new ConcurrentLinkedQueue<>());
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerQuit(PlayerQuitEvent event) {
        itemsClicked.remove(event.getPlayer());
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

}