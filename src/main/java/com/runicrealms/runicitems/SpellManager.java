package com.runicrealms.runicitems;

import com.runicrealms.runicitems.item.stats.RunicSpell;

import java.util.HashMap;
import java.util.Map;

public class SpellManager {

    private static Map<String, RunicSpell> spells = new HashMap<String, RunicSpell>();

    public static void setSpells(Map<String, RunicSpell> spells) {
        SpellManager.spells = spells;
    }

    public static RunicSpell getSpellFromId(String id) {
        return spells.get(id);
    }

}
