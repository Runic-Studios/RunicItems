package com.runicrealms.runicitems.item.util;

import com.runicrealms.runicitems.item.stats.RunicSpellType;

public class DefaultSpell {

    private SpellClickTrigger trigger;
    private RunicSpellType spellType;

    public DefaultSpell(SpellClickTrigger trigger, RunicSpellType spellType) {
        this.trigger = trigger;
        this.spellType = spellType;
    }

    public SpellClickTrigger getTrigger() {
        return this.trigger;
    }

    public RunicSpellType getSpellType() {
        return this.spellType;
    }

}
