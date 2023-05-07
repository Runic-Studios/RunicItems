package com.runicrealms.runicitems;

import org.bukkit.ChatColor;

@SuppressWarnings("unused")
public enum Stat {

    DEXTERITY
            (
                    "dexterity", "Dexterity", "DEX", ChatColor.YELLOW, "✦",
                    "Gain increased attack speed and ability haste!"
            ),
    INTELLIGENCE
            (
                    "intelligence", "Intelligence", "INT", ChatColor.DARK_AQUA, "ʔ",
                    "Deal more magic damage and gain more max mana!"
            ),
    STRENGTH
            (
                    "strength", "Strength", "STR", ChatColor.RED, "⚔",
                    "Deal more physical damage!"
            ),
    VITALITY
            (
                    "vitality", "Vitality", "VIT", ChatColor.WHITE, "■",
                    "Gain damage reduction and health regen!"
            ),
    WISDOM
            (
                    "wisdom", "Wisdom", "WIS", ChatColor.GREEN, "✸",
                    "Gain more spell healing, shielding, mana regen and experience!"
            );

    public static final Stat[] PLAYER_STATS = new Stat[]{DEXTERITY, INTELLIGENCE, STRENGTH, VITALITY, WISDOM};

    public static final String HEALTH_ICON = "❤";
    public static final String EMPTY_GEM_ICON = "◇";

    private static final double CRITICAL_DAMAGE_MULTIPLIER = 1.5; // 150%
    /*
    Combat multipliers
     */
    // Dexterity
    private static final double RANGED_ATTACK_SPEED = 0.005; // .5%
    private static final double ABILITY_HASTE = 0.0025; // .25%
    // Intelligence
    private static final double MANA_REGEN_MULT = 0.01; // 1%
    private static final double MAGIC_DMG_MULT = 0.01; // 1%
    // Strength
    private static final double PHYSICAL_DMG_MULT = 0.0075; // 0.75%
    // Wisdom
    private static final double MAX_MANA_MULT = 0.01; // 1%
    private static final double SPELL_HEALING_MULT = 0.006; // .6%
    private static final double SPELL_SHIELDING_MULT = 0.006; // .6%
    private static final double EXP_MULT = .0025; // 0.25%

    // Vitality
    private static final double DAMAGE_REDUCTION_MULT = 0.004; // 0.4%
    private static final double HEALTH_REGEN_MULT = 0.02; // 2%

    /*
    Capped values
     */
    private static final double DAMAGE_REDUCTION_CAP = 40; // %

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

    public static double getCriticalDamageMultiplier() {
        return CRITICAL_DAMAGE_MULTIPLIER;
    }

    public static double getAbilityHaste() {
        return ABILITY_HASTE;
    }

    public static double getRangedAttackSpeed() {
        return RANGED_ATTACK_SPEED;
    }

    public static double getExpMult() {
        return EXP_MULT;
    }

    public static double getMagicDmgMult() {
        return MAGIC_DMG_MULT;
    }

    public static double getMaxManaMult() {
        return MAX_MANA_MULT;
    }

    public static double getPhysicalDmgMult() {
        return PHYSICAL_DMG_MULT;
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

    public static double getSpellShieldingMult() {
        return SPELL_SHIELDING_MULT;
    }

    public static double getManaRegenMult() {
        return MANA_REGEN_MULT;
    }

    public static double getDamageReductionCap() {
        return DAMAGE_REDUCTION_CAP;
    }

    /**
     * Returns the enum value of a stat from its string
     *
     * @param identifier of stat (not case-sensitive)
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

    public ChatColor getChatColor() {
        return chatColor;
    }

    public String getDescription() {
        return description;
    }

    public String getIcon() {
        return icon;
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

}
