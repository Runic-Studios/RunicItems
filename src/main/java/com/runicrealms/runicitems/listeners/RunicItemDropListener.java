package com.runicrealms.runicitems.listeners;

import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;

import java.lang.reflect.Field;

public class RunicItemDropListener implements Listener {

    /**
     * Custom implementation of the setAge method in newer API
     *
     * @param duration in seconds
     * @param item
     */
    private static void setAge(int duration, Item item) {
        try {
            Field itemField = item.getClass().getDeclaredField("item");
            Field ageField;
            Object entityItem;

            itemField.setAccessible(true);
            entityItem = itemField.get(item);

            ageField = entityItem.getClass().getDeclaredField("age");
            ageField.setAccessible(true);
            ageField.set(entityItem, 6000 - (20 * duration));
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private static boolean isRunicItem(Item item) {
        // Logic to decide whether item is runicItem or not
        return true;
    }

    /* TODO
        - Check if item is instance of Runic item
        - Set the age of the item to 0
     */
    @EventHandler
    public void itemDrop(PlayerDropItemEvent e) {
        Item item = e.getItemDrop();
        if (isRunicItem(item)) {
            // Set age of dropped item to 0
            setAge(0, item);
        }
    }
}
