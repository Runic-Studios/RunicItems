package com.runicrealms.runicitems.item.stats;

import com.runicrealms.runicitems.item.util.RunicArtifactAbilityTrigger;

public class RunicArtifactAbility {

    private String identifier;
    private String name;
    private String description;
    private RunicArtifactAbilityTrigger trigger;

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
