package com.runicrealms.runicitems;

import com.runicrealms.runicitems.item.RunicItem;
import com.runicrealms.runicitems.item.template.RunicItemTemplate;

import java.util.HashMap;
import java.util.Map;

public class TemplateManager {

    private static Map<String, RunicItemTemplate> templates = new HashMap<String, RunicItemTemplate>();

    public static Map<String, RunicItemTemplate> getTemplates() {
        return templates;
    }

    public static void setTemplates(Map<String, RunicItemTemplate> templates) {
        TemplateManager.templates = templates;
    }

    public static RunicItemTemplate getTemplateFromId(String id) {
        return templates.get(id);
    }

    public static RunicItem generateItemFromTemplateId(String templateId) {
        return templates.get(templateId).generateItem(1, ItemManager.getNextItemId(), null, null);
    }

    public static RunicItem generateItemFromTemplateId(String templateId, int count) {
        return templates.get(templateId).generateItem(count, ItemManager.getNextItemId(), null, null);
    }

}
