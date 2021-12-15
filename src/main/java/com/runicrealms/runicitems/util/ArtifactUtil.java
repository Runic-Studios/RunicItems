package com.runicrealms.runicitems.util;

import com.runicrealms.runicitems.RunicItemsAPI;
import com.runicrealms.runicitems.item.RunicItem;
import com.runicrealms.runicitems.item.RunicItemArtifact;
import com.runicrealms.runicitems.item.event.RunicItemArtifactTriggerEvent;
import com.runicrealms.runicitems.item.util.RunicArtifactAbilityTrigger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ArtifactUtil {

    /**
     * Checks to see if the server should fire off a RunicArtifactTriggerEvent
     *
     * @param player      the player who triggered the check
     * @param itemStack   the itemStack in the player's hand
     * @param triggerType the type of trigger to be fired
     * @param victim      optional parameter of entity involved (on-hit, on-kill)
     */
    public static void checkForArtifactTrigger(Player player, ItemStack itemStack,
                                               RunicArtifactAbilityTrigger triggerType, Entity... victim) {
        RunicItem runicItem = RunicItemsAPI.getRunicItemFromItemStack(itemStack);
        if (runicItem == null) return;
        if (!(runicItem instanceof RunicItemArtifact)) return;
        RunicItemArtifact artifact = (RunicItemArtifact) runicItem;
        RunicArtifactAbilityTrigger abilityTrigger = artifact.getAbility().getTrigger();
        if (abilityTrigger != triggerType) return;
        Bukkit.getPluginManager().callEvent(new RunicItemArtifactTriggerEvent
                (
                        player,
                        artifact,
                        itemStack,
                        abilityTrigger,
                        victim
                )
        );
    }
}
