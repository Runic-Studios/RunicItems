package com.runicrealms.runicitems.util;

import com.runicrealms.runicitems.RunicItemsAPI;
import com.runicrealms.runicitems.item.RunicItem;
import com.runicrealms.runicitems.item.RunicItemArtifact;
import com.runicrealms.runicitems.item.util.RunicArtifactAbilityTrigger;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public class ArtifactUtil {

    /**
     * Checks to see if the server should fire off a RunicArtifactTriggerEvent
     *
     * @param itemStack   the itemStack in the player's hand
     * @param triggerType the type of trigger to be fired
     * @return a wrapper that contains the artifact reference and a boolean to prevent needing to get the artifact again later
     */
    public static ArtifactAndBooleanWrapper checkForArtifactTrigger(ItemStack itemStack, RunicArtifactAbilityTrigger triggerType) {
        RunicItem runicItem = RunicItemsAPI.getRunicItemFromItemStack(itemStack);
        if (runicItem == null) return new ArtifactAndBooleanWrapper(null, false);
        if (!(runicItem instanceof RunicItemArtifact)) return new ArtifactAndBooleanWrapper(null, false);
        RunicItemArtifact artifact = (RunicItemArtifact) runicItem;
        RunicArtifactAbilityTrigger abilityTrigger = artifact.getAbility().getTrigger();
        if (abilityTrigger != triggerType) return new ArtifactAndBooleanWrapper(artifact, false);
        return new ArtifactAndBooleanWrapper(artifact, true);
    }

    public static class ArtifactAndBooleanWrapper {

        private final RunicItemArtifact runicItemArtifact;
        private final boolean trigger;

        ArtifactAndBooleanWrapper(@Nullable RunicItemArtifact runicItemArtifact, boolean trigger) {
            this.runicItemArtifact = runicItemArtifact;
            this.trigger = trigger;
        }

        public RunicItemArtifact getRunicItemArtifact() {
            return runicItemArtifact;
        }

        public boolean isTrigger() {
            return trigger;
        }
    }
}
