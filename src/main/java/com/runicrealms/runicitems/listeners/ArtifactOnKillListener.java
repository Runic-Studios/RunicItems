package com.runicrealms.runicitems.listeners;

import com.runicrealms.plugin.events.RunicDeathEvent;
import com.runicrealms.runicitems.item.event.RunicArtifactOnKillEvent;
import com.runicrealms.runicitems.item.util.RunicArtifactAbilityTrigger;
import com.runicrealms.runicitems.util.ArtifactUtil;
import io.lumine.xikage.mythicmobs.api.bukkit.events.MythicMobDeathEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

public class ArtifactOnKillListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST) // last
    public void onMobKill(MythicMobDeathEvent e) {
        if (e.getKiller() == null) return;
        if (!(e.getKiller() instanceof Player)) return;
        Player killer = (Player) e.getKiller();
        ItemStack itemStack = killer.getInventory().getItemInMainHand();
        checkForKillTrigger(killer, itemStack, e.getEntity());
    }

    @EventHandler(priority = EventPriority.HIGHEST) // last
    public void onPlayerKill(RunicDeathEvent e) {
        if (e.isCancelled()) return;
        if (e.getKiller() == null) return;
        if (!(e.getKiller()[0] instanceof Player)) return;
        Player killer = (Player) e.getKiller()[0];
        ItemStack itemStack = killer.getInventory().getItemInMainHand();
        checkForKillTrigger(killer, itemStack, e.getVictim());
    }

    /**
     * Checks to see if an artifact ON-KILL event should be called
     *
     * @param player    who used the artifact
     * @param itemStack the itemStack representing the artifact
     * @param entity    the entity that was killed (player or mob)
     */
    private void checkForKillTrigger(Player player, ItemStack itemStack, Entity entity) {
        ArtifactUtil.ArtifactAndBooleanWrapper artifactAndBooleanWrapper = ArtifactUtil.checkForArtifactTrigger(itemStack, RunicArtifactAbilityTrigger.ON_KILL);
        if (!artifactAndBooleanWrapper.isTrigger()) return;
        Bukkit.getPluginManager().callEvent(new RunicArtifactOnKillEvent
                (
                        player,
                        artifactAndBooleanWrapper.getRunicItemArtifact(),
                        itemStack,
                        RunicArtifactAbilityTrigger.ON_KILL,
                        null,
                        entity
                )
        );
    }
}
