package com.runicrealms.plugin.runicitems.item.stats;

import com.runicrealms.plugin.runicitems.item.util.RunicArtifactAbilityTrigger;

public class RunicArtifactAbility {

    private final String identifier;
    private final String name;
    private final String description;
    private final RunicArtifactAbilityTrigger trigger;

    /**
     * Creates a RunicArtifactAbility which stores its identifier (used for NBT and listeners),
     *
     * @param identifier  the identifier of the ability, e.g., "adrenaline-rush"
     * @param name        a display for the ability, e.g., "Adrenaline Rush"
     * @param description a description of the passive effect
     * @param trigger     the type of trigger for the ability
     */
    public RunicArtifactAbility(String identifier, String name, String description, RunicArtifactAbilityTrigger trigger) {
        this.identifier = identifier;
        this.name = name;
        this.description = description;
        this.trigger = trigger;
    }

    public String getIdentifier() {
        return this.identifier;
    }

    public String getAbilityName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;
    }

    public RunicArtifactAbilityTrigger getTrigger() {
        return this.trigger;
    }

}
