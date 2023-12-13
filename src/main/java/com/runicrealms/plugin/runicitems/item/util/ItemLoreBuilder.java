package com.runicrealms.plugin.runicitems.item.util;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

public class ItemLoreBuilder {

    private final List<String> lore = new LinkedList<>();

    public ItemLoreBuilder() {
    }

    public ItemLoreBuilder(String initial) {
        lore.add(initial);
    }

    public ItemLoreBuilder(String[] initial) {
        lore.addAll(Arrays.asList(initial));
    }

    public ItemLoreBuilder(List<String> initial) {
        lore.addAll(initial);
    }

    public ItemLoreBuilder appendLines(String... lines) {
        lore.addAll(Arrays.asList(lines));
        return this;
    }

    public ItemLoreBuilder appendLines(List<String> lines) {
        lore.addAll(lines);
        return this;
    }

    public ItemLoreBuilder appendLinesIf(boolean condition, String... lines) {
        if (condition) lore.addAll(Arrays.asList(lines));
        return this;
    }

    public ItemLoreBuilder appendLinesIf(boolean condition, List<String> lines) {
        if (condition) lore.addAll(lines);
        return this;
    }

    public ItemLoreBuilder appendLinesIf(boolean condition, Supplier<String[]> lineSupplier) {
        if (condition) lore.addAll(Arrays.asList(lineSupplier.get()));
        return this;
    }

    public ItemLoreBuilder newLine() {
        lore.add("");
        return this;
    }

    public ItemLoreBuilder newLineIf(boolean condition) {
        if (condition) lore.add("");
        return this;
    }

    public List<String> build() {
        return lore;
    }

}
