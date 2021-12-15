package com.runicrealms.runicitems.item.util;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ItemLoreSection {

    private List<String> lore = new ArrayList<>();

    public ItemLoreSection(String[] lore) {
        Collections.addAll(this.lore, lore);
    }

    public ItemLoreSection(List<String> lore) {
        this.lore = lore;
    }

    public ItemLoreSection() {
    }

    public List<String> getLore() {
        return this.lore;
    }

    public void addLine(String line) {
        this.lore.add(line);
    }

    public boolean isEmpty() {
        return this.lore.size() == 0;
    }

    public static ItemLoreSection combine(String separator, ItemLoreSection... loreSections) {
        List<String> newLore = new ArrayList<>();
        for (ItemLoreSection section : loreSections) {
            for (String s : section.getLore()) {
                Bukkit.broadcastMessage(s);
            }
            if (!section.getLore().contains(""))
                newLore.add(separator);
            newLore.addAll(section.getLore());
        }
        return new ItemLoreSection(newLore);
    }

    public static ItemLoreSection combine(ItemLoreSection... loreSections) {
        return combine("", loreSections);
    }

    public static ItemLoreSection generateTranslateColorCodes(List<String> lore) {
        List<String> colored = new ArrayList<>();
        for (String line : lore) {
            colored.add(ChatColor.translateAlternateColorCodes('&', line));
        }
        return new ItemLoreSection(colored);
    }

}
