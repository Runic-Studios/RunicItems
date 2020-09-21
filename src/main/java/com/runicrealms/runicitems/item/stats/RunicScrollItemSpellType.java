package com.runicrealms.runicitems.item.stats;

public enum RunicScrollItemSpellType {

    TEST_SPELL("test-spell");

    private String identifier;

    RunicScrollItemSpellType(String identifier) {
        this.identifier = identifier;
    }

    public String getIdentifier() {
        return this.identifier;
    }

}
