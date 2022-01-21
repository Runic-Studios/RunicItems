package com.runicrealms.runicitems.listeners;

import com.runicrealms.plugin.events.WeaponDamageEvent;
import com.runicrealms.runicitems.item.event.RunicArtifactOnHitEvent;
import com.runicrealms.runicitems.item.util.RunicArtifactAbilityTrigger;
import com.runicrealms.runicitems.util.ArtifactUtil;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

public class ArtifactOnHitListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onWeaponDamage(WeaponDamageEvent e) {
        if (e.isCancelled()) return;
        if (!e.isBasicAttack()) return;
        ItemStack itemStack = e.getPlayer().getInventory().getItemInMainHand();
        ArtifactUtil.ArtifactAndBooleanWrapper artifactAndBooleanWrapper = ArtifactUtil.checkForArtifactTrigger(itemStack, RunicArtifactAbilityTrigger.ON_HIT);
        if (!artifactAndBooleanWrapper.isTrigger()) return;
        Bukkit.getPluginManager().callEvent(new RunicArtifactOnHitEvent
                (
                        e.getPlayer(),
                        artifactAndBooleanWrapper.getRunicItemArtifact(),
                        itemStack,
                        RunicArtifactAbilityTrigger.ON_HIT,
                        null,
                        e.getVictim()
                )
        );
    }
}
