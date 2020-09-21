package com.runicrealms.runicitems.item.stats;

import java.util.HashMap;
import java.util.Map;

public enum RunicGemType {

    HEALTH_GEM(new HashMap<RunicItemStatType, Integer>() {{
        put(RunicItemStatType.HEALING, 10);
    }});

    private Map<RunicItemStatType, Integer> stats;

    RunicGemType(Map<RunicItemStatType, Integer> stats) {
        this.stats = stats;
    }

    public Map<RunicItemStatType, Integer> getStats() {
        return this.stats;
    }

}
