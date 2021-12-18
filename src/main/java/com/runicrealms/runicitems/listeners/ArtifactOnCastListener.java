package com.runicrealms.runicitems.listeners;

import com.runicrealms.plugin.events.SpellCastEvent;
import com.runicrealms.runicitems.item.event.RunicArtifactOnCastEvent;
import com.runicrealms.runicitems.item.util.RunicArtifactAbilityTrigger;
import com.runicrealms.runicitems.util.ArtifactUtil;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

public class ArtifactOnCastListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST) // last
    public void onSpellCast(SpellCastEvent e) {
        if (e.isCancelled()) return;
        ItemStack itemStack = e.getCaster().getInventory().getItemInMainHand();
        ArtifactUtil.ArtifactAndBooleanWrapper artifactAndBooleanWrapper = ArtifactUtil.checkForArtifactTrigger(itemStack, RunicArtifactAbilityTrigger.ON_CAST);
        if (!artifactAndBooleanWrapper.isTrigger()) return;
        Bukkit.getPluginManager().callEvent(new RunicArtifactOnCastEvent
                (
                        e.getCaster(),
                        artifactAndBooleanWrapper.getRunicItemArtifact(),
                        itemStack,
                        RunicArtifactAbilityTrigger.ON_CAST,
                        null,
                        e.getSpell()
                )
        );
    }
}
