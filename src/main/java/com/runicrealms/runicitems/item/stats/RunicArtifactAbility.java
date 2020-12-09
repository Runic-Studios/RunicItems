package com.runicrealms.runicitems.item.stats;

import com.runicrealms.runicitems.item.util.RunicArtifactAbilityTrigger;

public class RunicArtifactAbility {

    private final String identifier;
    private final String name;
    private final String description;
    private final RunicArtifactAbilityTrigger trigger;

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
