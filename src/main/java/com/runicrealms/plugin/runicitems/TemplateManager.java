package com.runicrealms.plugin.runicitems;

import com.runicrealms.plugin.runicitems.item.RunicItem;
import com.runicrealms.plugin.runicitems.item.template.RunicItemTemplate;

import java.util.HashMap;
import java.util.Map;

public class TemplateManager {

    private static Map<String, RunicItemTemplate> templates = new HashMap<>();

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
        return templates.get(templateId).generateItem(1, DupeManager.getNextItemId(), null, null);
    }

    public static RunicItem generateItemFromTemplateId(String templateId, int count) {
        return templates.get(templateId).generateItem(count, DupeManager.getNextItemId(), null, null);
    }

}
