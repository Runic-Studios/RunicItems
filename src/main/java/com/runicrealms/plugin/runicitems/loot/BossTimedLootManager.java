package com.runicrealms.plugin.runicitems.loot;

import com.runicrealms.plugin.runicitems.RunicItems;
import com.runicrealms.plugin.runicitems.loot.chest.BossTimedLoot;
import io.lumine.mythic.bukkit.events.MythicMobDeathEvent;
import io.lumine.mythic.bukkit.events.MythicMobSpawnEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class BossTimedLootManager implements Listener {

    private final Map<String, BossTimedLoot> bossLoot = new HashMap<>(); // maps mm IDs to boss loot
    private final Map<UUID, HashMap<UUID, Integer>> bossFighters = new HashMap<>(); // a single boss is mapped to many players (damage threshold tracked here)

    public BossTimedLootManager(Collection<BossTimedLoot> bossTimedLoot) {
        Bukkit.getPluginManager().registerEvents(this, RunicItems.getInstance());
        for (BossTimedLoot boss : bossTimedLoot) {
            this.bossLoot.put(boss.getMmBossID(), boss);
        }
    }

    @EventHandler
    public void onBossDeath(MythicMobDeathEvent event) {
        BossTimedLoot loot = bossLoot.get(event.getMob().getMobType());
        if (loot == null) return;
        if (!bossFighters.containsKey(event.getMob().getUniqueId())) return;
        Location location = event.getEntity().getLocation();
        try {
            bossFighters.get(event.getEntity().getUniqueId()).forEach((uuid, damage) -> {
                Player player = Objects.requireNonNull(Bukkit.getPlayer(uuid));
                if (!player.getWorld().equals(location.getWorld())) return;
                // Too far from boss
                if (loot.getLootRange() != -1 && event.getEntity().getLocation().distanceSquared(player.getLocation()) > Math.pow(loot.getLootRange(), 2))
                    return;
                player.sendMessage(ChatColor.YELLOW + "You dealt " + ChatColor.RED + ChatColor.BOLD + damage + ChatColor.YELLOW + " damage to the boss!");
                double percent = damage / event.getMob().getEntity().getMaxHealth();
                if (percent >= loot.getLootDamageThreshold()) {
                    RunicItems.getLootAPI().displayTimedLootChest(player, loot.getLootChest());
                    player.teleport(loot.getComplete(), PlayerTeleportEvent.TeleportCause.PLUGIN);
                } else {
                    player.sendMessage(ChatColor.RED + "You did not deal enough damage to the boss to qualify for boss loot!");
                }
            });
        } finally {
            bossFighters.remove(event.getEntity().getUniqueId()).clear();
        }
    }

    @EventHandler
    public void onBossSpawn(MythicMobSpawnEvent event) {
        if (!bossLoot.containsKey(event.getMob().getMobType())) return;
        bossFighters.put(event.getEntity().getUniqueId(), new HashMap<>());
    }

    public void trackBossDamage(Player player, Entity entity, int amount) {
        if (!bossFighters.containsKey(entity.getUniqueId())) return;
        UUID bossId = entity.getUniqueId();
        bossFighters.get(bossId).put(player.getUniqueId(), bossFighters.get(bossId).getOrDefault(player.getUniqueId(), 0) + amount);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        for (UUID bossId : bossFighters.keySet()) {
            bossFighters.get(bossId).remove(event.getPlayer().getUniqueId());
        }
    }
}
