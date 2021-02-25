package com.runicrealms.runicitems.item.event;

import com.runicrealms.runicitems.item.RunicItemGeneric;
import com.runicrealms.runicitems.item.util.ClickTrigger;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class RunicItemGenericTriggerEvent extends Event {

    private final Player player;
    private final RunicItemGeneric item;
    private final ClickTrigger trigger;
    private final String action;

    private static final HandlerList handlers = new HandlerList();

    public RunicItemGenericTriggerEvent(Player player, RunicItemGeneric item, ClickTrigger trigger, String action) {
        this.player = player;
        this.item = item;
        this.trigger = trigger;
        this.action = action;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public Player getPlayer() {
        return this.player;
    }

    public RunicItemGeneric getItem() {
        return this.item;
    }

    public ClickTrigger getTrigger() {
        return this.trigger;
    }

    public String getAction() {
        return this.action;
    }

}
