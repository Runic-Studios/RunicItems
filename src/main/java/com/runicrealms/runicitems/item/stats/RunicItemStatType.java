package com.runicrealms.runicitems.item.stats;

public enum RunicItemStatType {

    HEALTH("health"),
    MANA("mana"),
    SPELL_DAMAGE("spell-damage"),
    WEAPON_DAMAGE("weapon-damage"),
    HEALTH_REGEN("health-regen"),
    HEALING("healing"),
    SHIELD("shield");

    private String identifier;

    RunicItemStatType(String identifier) {
        this.identifier = identifier;
    }

    public String getIdentifier() {
        return identifier;
    }

}
