package com.runicrealms.runicitems.listeners;

import com.runicrealms.plugin.events.WeaponDamageEvent;
import com.runicrealms.runicitems.item.util.RunicArtifactAbilityTrigger;
import com.runicrealms.runicitems.util.ArtifactUtil;
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
        ArtifactUtil.checkForArtifactTrigger(e.getPlayer(), itemStack, RunicArtifactAbilityTrigger.ON_HIT, e.getVictim());
    }
}
