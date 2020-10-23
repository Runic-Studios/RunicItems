package com.runicrealms.runicitems.item.util;

import com.runicrealms.runicitems.item.stats.RunicSpell;

public class DefaultSpell {

    private ClickTrigger trigger;
    private RunicSpell spell;

    public DefaultSpell(ClickTrigger trigger, RunicSpell spell) {
        this.trigger = trigger;
        this.spell = spell;
    }

    public ClickTrigger getTrigger() {
        return this.trigger;
    }

    public RunicSpell getSpell() {
        return this.spell;
    }

}
