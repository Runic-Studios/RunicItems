package com.runicrealms.plugin.runicitems.item.perk;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * Called when a player's active item perks are changed through equipping new items/changing weapons.
 */
public class ActiveItemPerksChangeEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private final Player player;
    private final Set<ItemPerk> oldItemPerks;
    private final Set<ItemPerk> newItemPerks;

    public ActiveItemPerksChangeEvent(Player player, Set<ItemPerk> oldItemPerks, Set<ItemPerk> newItemPerks) {
        this.player = player;
        this.oldItemPerks = oldItemPerks;
        this.newItemPerks = newItemPerks;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public Player getPlayer() {
        return this.player;
    }

    /**
     * The perks the player used to have equipped
     */
    public Set<ItemPerk> getOldItemPerks() {
        return this.oldItemPerks;
    }

    /**
     * The perks the player now has equipped
     */
    public Set<ItemPerk> getNewItemPerks() {
        return this.newItemPerks;
    }

    @NotNull
    public HandlerList getHandlers() {
        return handlers;
    }

}