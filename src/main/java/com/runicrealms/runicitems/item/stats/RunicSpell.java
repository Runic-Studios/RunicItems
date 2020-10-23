package com.runicrealms.runicitems.item.stats;

public class RunicSpell {

    private String identifier;
    private String name;
    private String description;

    public RunicSpell(String identifier, String name, String description) {
        this.identifier = identifier;
        this.name = name;
        this.description = description;
    }

    public String getIdentifier() {
        return this.identifier;
    }

    public String getSpellName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;
    }

}
