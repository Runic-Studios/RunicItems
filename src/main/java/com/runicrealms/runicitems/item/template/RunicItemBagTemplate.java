package com.runicrealms.runicitems.item.template;

import com.runicrealms.runicitems.item.stats.RunicItemTag;
import com.runicrealms.runicitems.item.util.ClickTrigger;
import com.runicrealms.runicitems.item.util.DisplayableItem;

import java.util.List;
import java.util.Map;

/**
 * This is for caching / storing YAML data
 */
public class RunicItemBagTemplate extends RunicItemGenericTemplate {

    private final int coins;

    public RunicItemBagTemplate(String id, DisplayableItem displayableItem, List<RunicItemTag> tags,
                                Map<String, String> data, Map<ClickTrigger, String> triggers, List<String> lore,
                                int coins) {
        super(id, displayableItem, tags, data, triggers, lore);
        this.coins = coins;
    }

    public int getCoins() {
        return coins;
    }
}
