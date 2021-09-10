package com.runicrealms.runicitems.item;

import com.runicrealms.runicitems.TemplateManager;
import com.runicrealms.runicitems.item.stats.RunicItemTag;
import com.runicrealms.runicitems.item.template.RunicItemGenericTemplate;
import com.runicrealms.runicitems.item.template.RunicItemTemplate;
import com.runicrealms.runicitems.item.util.ClickTrigger;
import com.runicrealms.runicitems.item.util.DisplayableItem;
import com.runicrealms.runicitems.item.util.ItemLoreSection;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

public class RunicItemGeneric extends RunicItem {

    private final List<String> lore;
    private final Map<ClickTrigger, String> triggers;

    public RunicItemGeneric(String templateId, DisplayableItem displayableItem, List<RunicItemTag> tags, Map<String, String> data, int count, long id,
                            Map<ClickTrigger, String> triggers, List<String> lore) {
        super(templateId, displayableItem, tags, data, count, id, () -> new ItemLoreSection[] {ItemLoreSection.generateTranslateColorCodes(lore)});
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
        NBTItem nbtItem = new NBTItem(item);
        RunicItemTemplate uncastedTemplate = TemplateManager.getTemplateFromId(nbtItem.getString("template-id"));
        if (!(uncastedTemplate instanceof RunicItemGenericTemplate)) throw new IllegalArgumentException("ItemStack is not a generic item!");
        RunicItemGenericTemplate template = (RunicItemGenericTemplate) uncastedTemplate;
        return new RunicItemGeneric(template, item.getAmount(), nbtItem.getInteger("id"));
    }

}