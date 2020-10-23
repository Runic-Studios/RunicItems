package com.runicrealms.runicitems.item.util;

import com.runicrealms.runicitems.item.stats.RunicSpell;

public class DefaultSpell {

    private SpellClickTrigger trigger;
    private RunicSpell spell;

    public DefaultSpell(SpellClickTrigger trigger, RunicSpell spell) {
        this.trigger = trigger;
        this.spell = spell;
    }

    public SpellClickTrigger getTrigger() {
        return this.trigger;
    }

    public RunicSpell getSpell() {
        return this.spell;
    }

}
