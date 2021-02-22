package com.runicrealms.runicitems.item;

import com.runicrealms.runicitems.TemplateManager;
import com.runicrealms.runicitems.item.stats.RunicItemTag;
import com.runicrealms.runicitems.item.template.RunicItemGenericTemplate;
import com.runicrealms.runicitems.item.template.RunicItemTemplate;
import com.runicrealms.runicitems.item.util.ClickTrigger;
import com.runicrealms.runicitems.item.util.DisplayableItem;
import com.runicrealms.runicitems.item.util.ItemLoreSection;
import com.runicrealms.runicitems.item.util.ItemNbtUtils;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;

public class RunicItemGeneric extends RunicItem {

    private final List<String> lore;
    private final Map<ClickTrigger, String> triggers;

    public RunicItemGeneric(String templateId, DisplayableItem displayableItem, List<RunicItemTag> tags, Map<String, String> data, int count, long id,
                            Map<ClickTrigger, String> triggers, List<String> lore) {
        super(templateId, displayableItem, tags, data, count, id, () -> new ItemLoreSection[] {new ItemLoreSection(lore)});
        this.lore = lore;
        this.triggers = triggers;
    }

    public RunicItemGeneric(RunicItemGenericTemplate template, int count, long id) {
        this(
                template.getId(), template.getDisplayableItem(), template.getTags(), template.getData(), count, id,
                template.getTriggers(), template.getLore()
        );
    }

    public List<String> getLore() {
        return this.lore;
    }

    public Map<ClickTrigger, String> getTriggers() {
        return this.triggers;
    }

    public static RunicItemGeneric getFromItemStack(ItemStack item) {
        RunicItemTemplate uncastedTemplate = TemplateManager.getTemplateFromId(ItemNbtUtils.getNbtString(item, "template-id"));
        if (!(uncastedTemplate instanceof RunicItemGenericTemplate)) throw new IllegalArgumentException("ItemStack is not a generic item!");
        RunicItemGenericTemplate template = (RunicItemGenericTemplate) uncastedTemplate;
        return new RunicItemGeneric(template, item.getAmount(), ItemNbtUtils.getNbtInteger(item, "id"));
    }

    // TODO on click check for generic item then check for triggers

}