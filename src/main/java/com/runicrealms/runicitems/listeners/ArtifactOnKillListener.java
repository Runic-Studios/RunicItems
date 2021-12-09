package com.runicrealms.runicitems.listeners;

import com.runicrealms.plugin.events.RunicDeathEvent;
import com.runicrealms.runicitems.item.util.RunicArtifactAbilityTrigger;
import com.runicrealms.runicitems.util.ArtifactUtil;
import io.lumine.xikage.mythicmobs.api.bukkit.events.MythicMobDeathEvent;
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
        ArtifactUtil.checkForArtifactTrigger(killer, itemStack, RunicArtifactAbilityTrigger.ON_KILL);
    }

    @EventHandler(priority = EventPriority.HIGHEST) // last
    public void onPlayerKill(RunicDeathEvent e) {
        if (e.isCancelled()) return;
        if (e.getKiller() == null) return;
        if (!(e.getKiller()[0] instanceof Player)) return;
        Player killer = (Player) e.getKiller()[0];
        ItemStack itemStack = killer.getInventory().getItemInMainHand();
        ArtifactUtil.checkForArtifactTrigger(killer, itemStack, RunicArtifactAbilityTrigger.ON_KILL);
    }
}
