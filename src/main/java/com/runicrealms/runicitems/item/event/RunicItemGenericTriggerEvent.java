package com.runicrealms.runicitems.item.event;

import com.runicrealms.runicitems.item.RunicItemGeneric;
import com.runicrealms.runicitems.item.util.ClickTrigger;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

public class RunicItemGenericTriggerEvent extends Event implements Cancellable {

    private boolean isCancelled;
    private final Player player;
    private final RunicItemGeneric item;
    private final ItemStack itemStack;
    private final ClickTrigger trigger;
    private final String action;

    private static final HandlerList handlers = new HandlerList();

    /**
     * Calls a RunicItemGenericTriggerEvent, for use with built-in item tags and data.
     *
     * @param player    player who triggered the item
     * @param item      runic item
     * @param itemStack reference to item stack
     * @param trigger   click of ClickTrigger enum
     * @param action    which action was taken
     */
    public RunicItemGenericTriggerEvent(Player player, RunicItemGeneric item, ItemStack itemStack, ClickTrigger trigger, String action) {
        this.player = player;
        this.item = item;
        this.itemStack = itemStack;
        this.trigger = trigger;
        this.action = action;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public boolean isCancelled() {
        return this.isCancelled;
    }

    @Override
    public void setCancelled(boolean b) {
        this.isCancelled = b;
    }

    public Player getPlayer() {
        return this.player;
    }

    public RunicItemGeneric getItem() {
        return this.item;
    }

    public ItemStack getItemStack() {
        return this.itemStack;
    }

    public ClickTrigger getTrigger() {
        return this.trigger;
    }

    public String getAction() {
        return this.action;
    }

}
