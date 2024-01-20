package com.runicrealms.plugin.runicitems.item.perk;

import com.runicrealms.plugin.runicitems.RunicItems;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ItemPerkManager implements Listener {

    private final Map<ItemPerkType, ItemPerkHandler> handlers = new HashMap<>();
    private final Map<String, ItemPerkType> types = new HashMap<>();

    public ItemPerkManager() {
        Bukkit.getPluginManager().registerEvents(this, RunicItems.getInstance());
    }

    public boolean isValidItemPerkIdentifier(String identifier) {
        for (ItemPerkType type : handlers.keySet()) {
            if (type.getIdentifier().equalsIgnoreCase(identifier)) return true;
        }
        return false;
    }

    public @Nullable ItemPerkType getItemPerkFromIdentifier(String identifier) {
        for (ItemPerkType type : handlers.keySet()) {
            if (type.getIdentifier().equalsIgnoreCase(identifier)) return type;
        }
        return null;
    }

    public Set<ItemPerkType> getItemPerks() {
        return handlers.keySet();
    }

    public ItemPerkHandler getHandler(ItemPerkType type) {
        return handlers.get(type);
    }

    public @Nullable ItemPerkType getType(String identifier) {
        return types.get(identifier);
    }

    public void registerItemPerk(ItemPerkHandler handler) {
        handlers.put(handler.getType(), handler);
        types.put(handler.getType().getIdentifier(), handler.getType());

        if (handler instanceof Listener listener) {
            Bukkit.getPluginManager().registerEvents(listener, RunicItems.getInstance());
        }
    }

    @EventHandler
    public void onActiveItemPerksChange(ActiveItemPerksChangeEvent event) {
        Map<ItemPerkType, Integer> oldPerks = new HashMap<>();
        for (ItemPerk perk : event.getOldItemPerks()) oldPerks.put(perk.getType(), perk.getStacks());
        Map<ItemPerkType, Integer> newPerks = new HashMap<>();
        for (ItemPerk perk : event.getNewItemPerks()) newPerks.put(perk.getType(), perk.getStacks());

        boolean activated = false;
        boolean deactivated = false;

        for (ItemPerkType handlerType : handlers.keySet()) {
            int oldCount = oldPerks.getOrDefault(handlerType, 0);
            int newCount = newPerks.getOrDefault(handlerType, 0);
            if (oldCount != newCount) {
                handlers.get(handlerType).onChange(event.getPlayer(), newCount);
                if (newCount > oldCount) {
                    activated = true;
                } else {
                    deactivated = true;
                }
            }
        }

        if (event.shouldPlaySounds()) {
            if (activated && !deactivated) {
                event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1.0F, 2.0F);
            } else if (!activated && deactivated) {
                event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.BLOCK_BEACON_DEACTIVATE, 1.0F, 2.0F);
            } else if (activated) { // both activated and deactivated
                event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.BLOCK_BEACON_DEACTIVATE, 1.0F, 2.0F);
                Bukkit.getScheduler().runTaskLater(RunicItems.getInstance(), () ->
                                event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1.0F, 2.0F),
                        10);
            }
        }
    }

}
