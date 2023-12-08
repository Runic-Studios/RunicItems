package com.runicrealms.plugin.runicitems.item.perk;

import com.runicrealms.plugin.runicitems.RunicItems;
import com.runicrealms.plugin.runicitems.item.perk.handlers.TestItemPerkHandler;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.Map;

public class ItemPerkHandlerManager implements Listener {

    private final Map<ItemPerkType, ItemPerkHandler> handlers = new HashMap<>();

    public ItemPerkHandlerManager() {
        Bukkit.getPluginManager().registerEvents(this, RunicItems.getInstance());
        register();
    }

    private void register() {
        setHandler(new TestItemPerkHandler());
    }

    private void setHandler(ItemPerkHandler handler) {
        handlers.put(handler.getType(), handler);
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
                handlers.get(handlerType).onChange(newCount);
                if (newCount > oldCount) {
                    activated = true;
                } else {
                    deactivated = true;
                }
            }
        }
        if (activated && !deactivated) {
            event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1.0F, 2.0F);
        } else if (!activated && deactivated) {
            event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.BLOCK_BEACON_DEACTIVATE, 1.0F, 2.0F);
        } else if (activated) { // both activated and deactivated
            event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.ENTITY_SHULKER_BULLET_HURT, 1.0F, 2.0F);
        }
    }

}
