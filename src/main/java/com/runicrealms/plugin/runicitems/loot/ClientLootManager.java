package com.runicrealms.plugin.runicitems.loot;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.MovingObjectPositionBlock;
import com.runicrealms.plugin.common.util.ColorUtil;
import com.runicrealms.plugin.common.util.grid.GridBounds;
import com.runicrealms.plugin.common.util.grid.MultiWorldGrid;
import com.runicrealms.plugin.rdb.event.CharacterLoadedEvent;
import com.runicrealms.plugin.rdb.event.CharacterQuitEvent;
import com.runicrealms.plugin.runicitems.RunicItems;
import com.runicrealms.plugin.runicitems.RunicItemsAPI;
import com.runicrealms.plugin.runicitems.item.RunicItem;
import com.runicrealms.plugin.runicitems.loot.chest.LootChest;
import com.runicrealms.plugin.runicitems.loot.chest.LootChestInventory;
import com.runicrealms.plugin.runicitems.loot.chest.RegenerativeLootChest;
import com.runicrealms.plugin.runicitems.loot.chest.TimedLootChest;
import com.runicrealms.plugin.runicrestart.RunicRestart;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handles displaying the client sided loot chest to players
 * and opening/closing chest inventories
 */
public class ClientLootManager implements Listener {

    private final MultiWorldGrid<LootChest> chestGrid = new MultiWorldGrid<>(new GridBounds(-4096, -4096, 4096, 4096), (short) 32); // Load chests efficiently
    private final Map<Player, Map<Location, ClientLootChest>> loadedChests = new HashMap<>(); // Chests that each player can see in the world
    private final Map<UUID, ConcurrentHashMap<RegenerativeLootChest, Long>> lastOpened = new HashMap<>(); // For cooldowns

    public ClientLootManager(@NotNull Collection<? extends LootChest> lootChests) {
        for (LootChest chest : lootChests) {
            chestGrid.insertElement(chest.getPosition().getLocation(), chest);
        }
        Bukkit.getPluginManager().registerEvents(this, RunicItems.getInstance());
        Bukkit.getScheduler().runTaskTimerAsynchronously(RunicItems.getInstance(), () -> updateClientChests(), 0, 20 * 4);

        ProtocolLibrary.getProtocolManager().getAsynchronousManager().registerAsyncHandler(new PacketAdapter(RunicItems.getInstance(), ListenerPriority.HIGH, PacketType.Play.Client.USE_ITEM) {
            @Override
            public void onPacketReceiving(PacketEvent event) {
                if (event.getPacketType() == PacketType.Play.Client.USE_ITEM && !event.isCancelled() && event.getPacket().getHands().read(0) == EnumWrappers.Hand.MAIN_HAND) {
                    onUseItemPacket(event);
                }
            }
        }).start();
    }

    /**
     * A method that hides and reveals nearby chests
     *
     * @param player the player to update chests for
     */
    public void updateClientChests(@NotNull Player player) {
        Map<Location, ClientLootChest> chests = loadedChests.computeIfAbsent(player, key -> {
            Map<Location, ClientLootChest> emptyChests = new HashMap<>();
            for (LootChest chest : RunicItems.getLootAPI().getRegenerativeLootChests()) {
                emptyChests.put(chest.getPosition().getLocation(), new ClientLootChest(chest, false));
            }
            return emptyChests;
        });

        Set<LootChest> surrounding = chestGrid.getSurroundingElements(player.getLocation(), (short) 2);
        for (ClientLootChest clientChest : chests.values()) {
            LootChest chest = clientChest.lootChest;
            boolean displayed = clientChest.displayed;

            if (displayed) {
                Location location = chest.getPosition().getLocation();
                player.spawnParticle(Particle.END_ROD, location.getX() + 0.5, location.getY(), location.getZ() + 0.5, 7, Math.random(), Math.random(), Math.random(), 0);
            }

            if (!chest.shouldUpdateDisplay()) {
                continue;
            }

            if ((displayed && !surrounding.contains(chest)) || player.getLevel() < chest.getMinLevel() || (chest instanceof RegenerativeLootChest regenChest && (this.isTooCloseToServerRestart(regenChest) || this.isOnCooldown(player, regenChest)))) {
                chest.hideFromPlayer(player, false);
                clientChest.displayed = false;
                continue;
            }

            if (displayed || !surrounding.contains(chest)) {
                continue;
            }

            chest.showToPlayer(player);
            clientChest.displayed = true;
        }
    }

    public boolean isOnCooldown(@NotNull Player player, @NotNull RegenerativeLootChest lootChest) {
        Map<RegenerativeLootChest, Long> playerOpened = lastOpened.get(player.getUniqueId());
        if (playerOpened == null) {
            return false;
        }

        long lastOpenedTime = playerOpened.get(lootChest);
        int timeLeft = (int) ((lastOpenedTime + lootChest.getRegenerationTime() * 1000L - System.currentTimeMillis()) / 1000);

        return timeLeft > 0;
    }

    public boolean isTooCloseToServerRestart(@NotNull RegenerativeLootChest lootChest) {
        int timeBeforeRestart = RunicRestart.getRestartManager().getMinutesBeforeRestart() * 60; //convert to seconds

        //if the time it takes to regen is longer than the time left, when the server restarts they get to skip time, so make sure that they cant open until after server restart
        return timeBeforeRestart <= lootChest.getRegenerationTime();
    }

    public void updateClientChests() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            updateClientChests(player);
        }
    }

    public void addRegenerativeLootChest(@NotNull RegenerativeLootChest chest) {
        this.chestGrid.insertElement(chest.getPosition().getLocation(), chest);
        this.loadedChests.forEach((player, map) -> map.put(chest.getPosition().getLocation(), new ClientLootChest(chest, false)));
        this.lastOpened.forEach((uuid, map) -> map.put(chest, 0L));
    }

    public void deleteRegenerativeLootChest(@NotNull RegenerativeLootChest chest) {
        this.chestGrid.removeElement(chest.getPosition().getLocation(), chest);
        this.loadedChests.forEach((player, map) -> map.remove(chest.getPosition().getLocation()));
        this.lastOpened.forEach((uuid, map) -> map.remove(chest));
    }

    public void displayTimedLootChest(@NotNull Player player, @NotNull TimedLootChest chest) {
        Location identifier = chest.getPosition().getLocation();
        loadedChests.get(player).put(identifier, new ClientLootChest(chest, true));
        chest.beginDisplay(player, () -> {
            Map<Location, ClientLootChest> loaded = loadedChests.get(player);
            if (loaded != null) {
                loaded.remove(identifier);
            }
        });
    }

    @EventHandler
    private void onCharacterQuit(CharacterQuitEvent event) {
        this.loadedChests.remove(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    private void onCharacterLoaded(CharacterLoadedEvent event) {
        Bukkit.getScheduler().runTaskLater(RunicItems.getInstance(), () -> {
            this.updateClientChests(event.getPlayer());

            if (this.lastOpened.containsKey(event.getPlayer().getUniqueId())) {
                return;
            }

            ConcurrentHashMap<RegenerativeLootChest, Long> playerOpened = new ConcurrentHashMap<>();

            for (RegenerativeLootChest lootChest : RunicItems.getLootAPI().getRegenerativeLootChests()) {
                playerOpened.put(lootChest, 0L);
            }

            this.lastOpened.put(event.getPlayer().getUniqueId(), playerOpened);
        }, 20); //give it more time to load
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void onPlayerTeleport(PlayerTeleportEvent event) {
        if (!this.loadedChests.containsKey(event.getPlayer())) {
            return;
        }

        Bukkit.getScheduler().runTaskLaterAsynchronously(RunicItems.getInstance(), () -> this.updateClientChests(event.getPlayer()), 20); //give it more time to load
    }

    @EventHandler
    private void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getBottomInventory().equals(event.getClickedInventory())) return;
        if (!(event.getView().getTopInventory().getHolder() instanceof LootChestInventory)) return;
        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;
        RunicItem runicItem = RunicItemsAPI.getRunicItemFromItemStack(event.getCurrentItem());
        if (RunicItemsAPI.containsBlockedTag(runicItem)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    private void onLootChestClose(InventoryCloseEvent event) {
        if (event.getPlayer() instanceof Player player
                && event.getView().getTopInventory().getHolder() instanceof LootChestInventory holder) {
            holder.close(player);
            player.playSound(holder.getLootChest().getPosition().getLocation(), Sound.BLOCK_CHEST_CLOSE, 1.0f, 1.0f);
            holder.getLootChest().hideFromPlayer(player);
        }
    }

    private void onUseItemPacket(@NotNull PacketEvent event) {
        MovingObjectPositionBlock position = event.getPacket().getMovingBlockPositions().readSafely(0);
        if (position == null) {
            return;
        }

        Location location = position.getBlockPosition().toLocation(event.getPlayer().getWorld());

        Map<Location, ClientLootChest> loaded = loadedChests.get(event.getPlayer());
        if (loaded == null) {
            return;
        }

        ClientLootChest chest = loaded.get(location);
        if (chest == null) {
            return;
        }

        Map<RegenerativeLootChest, Long> playerOpened = lastOpened.get(event.getPlayer().getUniqueId());
        if (playerOpened == null && chest.lootChest instanceof RegenerativeLootChest) {
            RunicItems.getInstance().getLogger().severe("loot chest last opened data is not in memory for " + event.getPlayer().getName());
            event.getPlayer().sendMessage(ColorUtil.format("&cThere was an error getting your loot chest data from memory. Please report this to an admin!"));
            return;
        }

        if (chest.lootChest instanceof RegenerativeLootChest regenChest && (this.isTooCloseToServerRestart(regenChest) || this.isOnCooldown(event.getPlayer(), regenChest))) {
            regenChest.hideFromPlayer(event.getPlayer(), false);
            return;
        }

        if (chest.lootChest instanceof RegenerativeLootChest regenChest) {
            playerOpened.put(regenChest, System.currentTimeMillis());
        }

        event.setCancelled(true);
        RunicItems.newChain()
                .sync(() -> {
                    chest.lootChest.playOpenAnimation();
                    event.getPlayer().playSound(chest.lootChest.getPosition().getLocation(), Sound.BLOCK_CHEST_OPEN, .5F, 1);
                })
                .delay(8)
                .sync(() -> {
                    Map<Location, ClientLootChest> chests = loadedChests.get(event.getPlayer());
                    ClientLootChest client = chests.get(chest.lootChest.getPosition().getLocation());

                    if (client == null) {
                        return;
                    }

                    chest.lootChest.openInventory(event.getPlayer());
                    client.displayed = false;
                    chest.lootChest.hideFromPlayer(event.getPlayer(), false);
                    chest.lootChest.onOpen(event.getPlayer());
                })
                .execute();
    }

    private static class ClientLootChest {
        private final LootChest lootChest;
        private boolean displayed;

        private ClientLootChest(@NotNull LootChest lootChest, boolean displayed) {
            this.lootChest = lootChest;
            this.displayed = displayed;
        }
    }
}