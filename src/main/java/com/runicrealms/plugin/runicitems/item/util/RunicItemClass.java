package com.runicrealms.plugin.runicitems.item.util;

import com.runicrealms.plugin.common.CharacterClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

    @Nullable
    public static RunicItemClass getFromIdentifier(@Nullable String identifier) {
        if (identifier == null) {
            return null;
        }

        for (RunicItemClass itemClass : RunicItemClass.values()) {
            if (itemClass.getIdentifier().equalsIgnoreCase(identifier)) {
                return itemClass;
            }
        }
        return null;
    }

    @NotNull
    public String getDisplay() {
        return this.display;
    }

    @NotNull
    public String getIdentifier() {
        return this.identifier;
    }

    public CharacterClass toCharacterClass() {
        return CharacterClass.getFromName(this.identifier);
    }

}
