package com.runicrealms.runicitems.item.event;

import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.runicitems.item.RunicItemArtifact;
import com.runicrealms.runicitems.item.util.RunicArtifactAbilityTrigger;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public class RunicArtifactOnCastEvent extends RunicItemArtifactTriggerEvent {

    private final Spell spellTrigger;

    /**
     * Used to handle passives for artifacts
     *
     * @param player              who triggered the event
     * @param runicItemArtifact   the artifact associated with the trigger
     * @param itemStack           the itemStack of the artifact in the event
     * @param abilityTrigger      the type of trigger (cast, hit, kill, etc.)
     * @param artifactSpellToCast nullable parameter to specify which spell artifact spell to cast (for managing cooldowns)
     * @param spellTrigger        the spell which triggered the on-cast
     */
    public RunicArtifactOnCastEvent(Player player, RunicItemArtifact runicItemArtifact, ItemStack itemStack,
                                    RunicArtifactAbilityTrigger abilityTrigger,
                                    @Nullable Spell artifactSpellToCast, Spell spellTrigger) {
        super(player, runicItemArtifact, itemStack, abilityTrigger, artifactSpellToCast);
        this.spellTrigger = spellTrigger;
    }

    public Spell getSpellTrigger() {
        return spellTrigger;
    }
}
