package com.runicrealms.plugin.runicitems.dynamic;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.runicrealms.plugin.runicitems.RunicItems;
import com.runicrealms.plugin.runicitems.TemplateManager;
import com.runicrealms.plugin.runicitems.item.template.RunicItemTemplate;
import de.tr7zw.nbtapi.NBTItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class DynamicItemHandler extends PacketAdapter {

    private final Map<String, DynamicItemTextPlaceholder> textPlaceholders = new HashMap<>(); // map between identifier and placeholder

    public DynamicItemHandler() {
        super(RunicItems.getInstance(), ListenerPriority.HIGHEST, PacketType.Play.Server.SET_SLOT, PacketType.Play.Server.WINDOW_ITEMS);

        ProtocolLibrary.getProtocolManager().getAsynchronousManager().registerAsyncHandler(this).start();
    }

    /**
     * Finds all instances of text wrapped in <> and returns a mapping between the indices of the start of the text
     * inside the brackets, and the text without the brackets itself
     * <p>
     * array deque so that we can reverse the order when replacing so that indices don't get messed up
     */
    private static ArrayDeque<BracketedTextInfo> findBracketedTextWithIndices(String input) {
        ArrayDeque<BracketedTextInfo> results = new ArrayDeque<>();
        int startIndex = -1;
        int endIndex;

        for (int i = 0; i < input.length(); i++) {
            if (input.charAt(i) == '<' && startIndex == -1) {
                startIndex = i;
            } else if (input.charAt(i) == '>' && startIndex != -1) {
                endIndex = i;
                String text = input.substring(startIndex + 1, endIndex);
                results.add(new BracketedTextInfo(startIndex, endIndex, text));
                startIndex = -1;
            }
        }
        return results;
    }

    /**
     * Replaces a segment of the string starting from begin index to end index with a given replacement, then returns that string.
     * Begin index is inclusive, endIndex is not inclusive.
     */
    private static String replaceStringSegment(String original, int beginIndex, int endIndex, String replacement) {
        if (beginIndex < 0 || endIndex > original.length() || beginIndex > endIndex)
            throw new IllegalArgumentException("Invalid indices");
        String before = original.substring(0, beginIndex);
        String after = original.substring(endIndex + 1);
        return before + replacement + after;
    }

    public void registerTextPlaceholder(DynamicItemTextPlaceholder placeholder) {
        textPlaceholders.put(placeholder.getIdentifier(), placeholder);
    }

    @Override
    public void onPacketSending(PacketEvent event) {
        if (event.getPacketType() == PacketType.Play.Server.SET_SLOT) {

            for (ItemStack target : event.getPacket().getItemModifier().getValues()) {
                if (target == null || target.getType() == Material.AIR) continue;
                processItem(event.getPlayer(), target);
            }

        } else if (event.getPacketType() == PacketType.Play.Server.WINDOW_ITEMS) {

            for (List<ItemStack> items : event.getPacket().getItemListModifier().getValues()) {
                for (ItemStack target : items) {
                    if (target == null || target.getType() == Material.AIR) continue;
                    processItem(event.getPlayer(), target);
                }
            }

            for (ItemStack target : event.getPacket().getItemModifier().getValues()) {
                if (target == null || target.getType() == Material.AIR) continue;
                processItem(event.getPlayer(), target);
            }

        }
    }

    public void processItem(Player viewer, ItemStack item) { // Try and be as efficient as we can!
        NBTItem nbtItem = new NBTItem(item); // sadly not efficient D:
        if (!nbtItem.hasNBTData()) return;
        String templateID = nbtItem.getString("template-id");
        if (templateID == null || templateID.isEmpty()) return;
        RunicItemTemplate template = TemplateManager.getTemplateFromId(templateID);
        if (template == null) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        Component displayNameComponent = meta.displayName();
        if (displayNameComponent != null) {
            String displayName = LegacyComponentSerializer.legacySection().serialize(displayNameComponent);
            @Nullable String replacement = generateReplacement(displayName, viewer, item, nbtItem, template);
            if (replacement != null) {
                Component newComponent = LegacyComponentSerializer.legacySection().deserialize(replacement);
                // So this is a really really really annoying workaround because the adventure API is really hard to work with
                // and i kind of sometimes hate it but maybe one day i will find a better way to do this
                Component noItalics = Component.text().decoration(TextDecoration.ITALIC, false).append(newComponent).build();
                meta.displayName(noItalics);
            }
        }

        if (meta.hasLore()) {
            List<Component> lore = meta.lore();
            List<Component> newLore = new LinkedList<>();
            boolean changed = false;
            for (Component component : lore) {
                String line = LegacyComponentSerializer.legacySection().serialize(component);
                @Nullable String replacement = generateReplacement(line, viewer, item, nbtItem, template);
                if (replacement != null) {
                    changed = true;
                    Component newComponent = LegacyComponentSerializer.legacySection().deserialize(replacement);
                    // So this is a really really really annoying workaround because the adventure API is really hard to work with
                    // and i kind of sometimes hate it but maybe one day i will find a better way to do this
                    Component noItalics = Component.text().decoration(TextDecoration.ITALIC, false).append(newComponent).build();
                    newLore.add(noItalics);
                } else {
                    newLore.add(component);
                }
            }
            if (changed) meta.lore(newLore);
        }
        item.setItemMeta(meta);
    }

    /**
     * Attempts to generate a replacement of all the placeholders in a given string.
     * If succeeds, returns the new string.
     * If fails (no placeholders), returns null.
     */
    private @Nullable String generateReplacement(String target, Player viewer, ItemStack item, NBTItem nbtItem, RunicItemTemplate template) {
        ArrayDeque<BracketedTextInfo> results = findBracketedTextWithIndices(target);
        boolean changed = false;
        while (!results.isEmpty()) {
            BracketedTextInfo brackets = results.removeLast(); // Reverse order so that indices don't get messed up
            DynamicItemTextPlaceholder placeholder = textPlaceholders.get(brackets.bracketedText);
            if (placeholder != null) {
                String replacement = placeholder.generateReplacement(viewer, item, nbtItem, template);
                if (replacement == null) continue;
                target = replaceStringSegment(target, brackets.beginIndex, brackets.endIndex, replacement);
                changed = true;
            }
        }
        if (changed) return target;
        return null;
    }

    private record BracketedTextInfo(int beginIndex, int endIndex, String bracketedText) {
    }

}