package com.runicrealms.runicitems.item.stats;

import com.runicrealms.runicitems.config.SpellLoader;

public enum RunicSpellType {

    TEST_SPELL("test-spell", "Test Spell");

    private String identifier;
    private String name;

    RunicSpellType(String identifier, String name) {
        this.identifier = identifier;
        this.name = name;
    }

    public String getDescription() {
        return SpellLoader.getSpellDescription(this.identifier);
    }

    public String getSpellName() {
        return this.name;
    }

}
