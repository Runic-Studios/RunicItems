package com.runicrealms.plugin.runicitems.item;

import com.runicrealms.plugin.runicitems.item.util.ClickTrigger;
import com.runicrealms.plugin.runicitems.item.util.DisplayableItem;
import com.runicrealms.plugin.runicitems.item.util.ItemLoreSection;
import com.runicrealms.plugin.runicitems.TemplateManager;
import com.runicrealms.plugin.runicitems.item.stats.RunicItemTag;
import com.runicrealms.plugin.runicitems.item.template.RunicItemGenericTemplate;
import com.runicrealms.plugin.runicitems.item.template.RunicItemTemplate;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Document(collection = "items")
@TypeAlias("generic")
public class RunicItemGeneric extends RunicItem {
    private List<String> lore = new ArrayList<>();
    private Map<ClickTrigger, String> triggers = new HashMap<>();

    @SuppressWarnings("unused")
    public RunicItemGeneric() {
        // Default constructor for Spring
    }

    @Autowired
    public RunicItemGeneric(String templateId, DisplayableItem displayableItem, List<RunicItemTag> tags, Map<String, String> data, int count, long id,
                            Map<ClickTrigger, String> triggers, List<String> lore) {
        super(templateId, displayableItem, tags, data, count, id);
        this.lore = lore;
        this.triggers = triggers;
    }

    public RunicItemGeneric(RunicItemGenericTemplate template, int count, long id) {
        this(
                template.getId(), template.getDisplayableItem(), template.getTags(), template.getData(), count, id,
                template.getTriggers(), template.getLore()
        );
    }

    public static RunicItemGeneric getFromItemStack(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return null;
        NBTItem nbtItem = new NBTItem(item);
        RunicItemTemplate uncastedTemplate = TemplateManager.getTemplateFromId(nbtItem.getString("template-id"));
        if (!(uncastedTemplate instanceof RunicItemGenericTemplate template))
            throw new IllegalArgumentException("ItemStack is not a generic item!");
        return new RunicItemGeneric(template, item.getAmount(), nbtItem.getInteger("id"));
    }

    @Override
    protected ItemLoreSection[] generateLore() {
        return new ItemLoreSection[]{ItemLoreSection.generateTranslateColorCodes(lore)};
    }

    public List<String> getLore() {
        return this.lore;
    }

    public void setLore(List<String> lore) {
        this.lore = lore;
    }

    public Map<ClickTrigger, String> getTriggers() {
        return this.triggers;
    }

    public void setTriggers(Map<ClickTrigger, String> triggers) {
        this.triggers = triggers;
    }

}