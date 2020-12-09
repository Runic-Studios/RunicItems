package com.runicrealms.runicitems.item.util;

public enum RunicItemClass {

    WARRIOR("Warrior", "warrior"),
    MAGE("Mage", "mage"),
    ARCHER("Archer", "archer"),
    CLERIC("Cleric", "cleric"),
    ROGUE("Rogue", "rogue"),
    ANY("Any", "any");

    private final String display;
    private final String identifier;

    RunicItemClass(String display, String identifier) {
        this.display = display;
        this.identifier = identifier;
    }

    public String getDisplay() {
        return this.display;
    }

    public String getIdentifier() {
        return this.identifier;
    }

    public static RunicItemClass getFromIdentifier(String identifier) {
        for (RunicItemClass itemClass : RunicItemClass.values()) {
            if (itemClass.getIdentifier().equalsIgnoreCase(identifier)) {
                return itemClass;
            }
        }
        return null;
    }

}
