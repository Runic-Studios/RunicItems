package com.runicrealms.runicitems.config;

import java.util.HashMap;
import java.util.Map;

public class SpellReader {

    private static Map<String, String> spells = new HashMap<String, String>();

    public static void loadSpells() {

    }

    public static String getSpellDescription(String identifier) {
        return spells.get(identifier);
    }

}
