package com.runicrealms.runicitems;

import org.bukkit.ChatColor;

public enum Stat {

    DEXTERITY("dexterity", "Dexterity", "DEX", ChatColor.YELLOW, "✦", "Deal more ranged damage and gain movement speed!"),
    INTELLIGENCE("intelligence", "Intelligence", "INT", ChatColor.DARK_AQUA, "ʔ", "Deal more spell damage and gain more max mana!"),
    STRENGTH("strength", "Strength", "STR", ChatColor.RED, "⚔", "Deal more melee weapon damage!"),
    VITALITY("vitality", "Vitality", "VIT", ChatColor.WHITE, "■", "Gain damage reduction and health regen!"),
    WISDOM("wisdom", "Wisdom", "WIS", ChatColor.GREEN, "✸", "Gain more spell healing and mana regen!");

    public static final Stat[] PLAYER_STATS = new Stat[]{DEXTERITY, INTELLIGENCE, STRENGTH, VITALITY, WISDOM};

    public static final String HEALTH_ICON = "❤";
    public static final String EMPTY_GEM_ICON = "◇";

    private static final double CRITICAL_DAMAGE_MULTIPLIER = 1.5; // 150%
    /*
    Combat multipliers
     */
    // dexterity
    private static final double DODGE_CHANCE = 0.0025; // .25%
    private static final double MOVEMENT_SPEED_MULT = 0.0035; // .35%
    private static final double RANGED_DMG_MULT = 0.01; // 1%
    // intelligence
    private static final double MANA_REGEN_MULT = 0.04; // 4%
    private static final double MAGIC_DMG_MULT = 0.012; // 1.2%
    // strength
    private static final double CRITICAL_CHANCE = 0.004; // .4%
    private static final double MELEE_DMG_MULT = 0.008; // .8%
    // wisdom
    private static final double MAX_MANA_MULT = 0.01; // 1%
    private static final double SPELL_HEALING_MULT = 0.006; // .6%
    // vitality
    private static final double DAMAGE_REDUCTION_MULT = 0.004; // 0.4%
    private static final double HEALTH_REGEN_MULT = 0.02; // 2%

    /*
    Damage caps
     */
    private static final double DAMAGE_REDUCTION_CAP = 40; // %
    private static final double MOVEMENT_SPEED_CAP = 25; // %

    /*
    Enum fields
     */
    private final String identifier;
    private final String name;
    private final String prefix;
    private final ChatColor chatColor;
    private final String icon;
    private final String description;

    Stat(String identifier, String name, String prefix, ChatColor chatColor, String icon, String description) {
        this.identifier = identifier;
        this.name = name;
        this.prefix = prefix;
        this.chatColor = chatColor;
        this.icon = icon;
        this.description = description;
    }

    public String getIdentifier() {
        return this.identifier;
    }

    public String getName() {
        return name;
    }

    public String getPrefix() {
        return prefix;
    }

    public ChatColor getChatColor() {
        return chatColor;
    }

    public String getIcon() {
        return icon;
    }

    public String getDescription() {
        return description;
    }

    public static double getCriticalDamageMultiplier() {
        return CRITICAL_DAMAGE_MULTIPLIER;
    }

    public static double getDodgeChance() {
        return DODGE_CHANCE;
    }

    public static double getRangedDmgMult() {
        return RANGED_DMG_MULT;
    }

    public static double getMovementSpeedMult() {
        return MOVEMENT_SPEED_MULT;
    }

    public static double getMagicDmgMult() {
        return MAGIC_DMG_MULT;
    }

    public static double getMaxManaMult() {
        return MAX_MANA_MULT;
    }

    public static double getMeleeDmgMult() {
        return MELEE_DMG_MULT;
    }

    public static double getCriticalChance() {
        return CRITICAL_CHANCE;
    }

    public static double getDamageReductionMult() {
        return DAMAGE_REDUCTION_MULT;
    }

    public static double getHealthRegenMult() {
        return HEALTH_REGEN_MULT;
    }

    public static double getSpellHealingMult() {
        return SPELL_HEALING_MULT;
    }

    public static double getManaRegenMult() {
        return MANA_REGEN_MULT;
    }

    public static double getDamageReductionCap() {
        return DAMAGE_REDUCTION_CAP;
    }

    public static double getMovementSpeedCap() {
        return MOVEMENT_SPEED_CAP;
    }

    /**
     * Returns the enum value of a stat from its string
     *
     * @param identifier of stat (not case sensitive)
     * @return enum of stat
     */
    public static Stat getFromIdentifier(String identifier) {
        for (Stat stat : Stat.values()) {
            if (stat.getIdentifier().equalsIgnoreCase(identifier)) {
                return stat;
            }
        }
        return null;
    }

    public static Stat getFromName(String name) {
        for (Stat stat : Stat.values()) {
            if (stat.getName().equalsIgnoreCase(name)) {
                return stat;
            }
        }
        return null;
    }

}
