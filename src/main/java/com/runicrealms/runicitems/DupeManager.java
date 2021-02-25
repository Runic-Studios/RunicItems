package com.runicrealms.runicitems;

import de.tr7zw.nbtapi.NBTItem;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import org.bukkit.Bukkit;
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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class DupeManager implements Listener {

    public static final int MAX_ITEMS_CLICKED_CACHE_LENGTH = 50;

    public static final long TEXT_CHANNEL_ID = 813580198133628928L;
    public static final Color EMBED_COLOR = new Color(36, 138, 38);

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
            if (event.getCurrentItem() != null) {
                Bukkit.getScheduler().runTaskAsynchronously(RunicItems.getInstance(), () -> {
                    for (ItemStack itemOne : itemsClicked.get(player)) {
                        if (checkItemsDuped(itemOne, event.getCurrentItem())) {
                            player.getInventory().remove(event.getCurrentItem());
                            channel.sendMessage(new EmbedBuilder()
                            .setColor(EMBED_COLOR)
                            .setTitle("Dupe Notification")
                            .setDescription("Player `"
                                    + player.getName()
                                    + "` has attempted to dupe `"
                                    + event.getCurrentItem().getAmount()
                                    + "x "
                                    + getItemName(event.getCurrentItem())
                                    + "` at "
                                    + new SimpleDateFormat("dd:MM:yy:HH:mm:ss").format(System.currentTimeMillis()))
                            .build()).queue();
                        }
                    }
                    if (!itemsClicked.get(player).contains(event.getCurrentItem())) {
                        while (itemsClicked.get(player).size() >= MAX_ITEMS_CLICKED_CACHE_LENGTH) {
                            itemsClicked.get(player).remove();
                        }
                        itemsClicked.get(player).add(event.getCurrentItem());
                    }
                });
            }
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
        NBTItem nbtItemOne = new NBTItem(itemOne);
        NBTItem nbtItemTwo = new NBTItem(itemTwo);
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
            return false;
        }
        return nbtItemOne.getLong("id").equals(nbtItemTwo.getLong("id"));
    }
    public static long getNextItemId() {
        return nextId++;
    }

    private static String getItemName(ItemStack item) {
        if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            return item.getItemMeta().getDisplayName();
        }
        return item.getType().toString().toLowerCase();
    }

}
