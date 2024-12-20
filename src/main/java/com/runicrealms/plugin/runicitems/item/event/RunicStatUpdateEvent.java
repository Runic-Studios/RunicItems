package com.runicrealms.plugin.runicitems.item.event;

import com.runicrealms.plugin.runicitems.player.PlayerEquipmentCache;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class RunicStatUpdateEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final PlayerEquipmentCache statHolder;

    public RunicStatUpdateEvent(Player player, PlayerEquipmentCache statHolder) {
        super(true);
        this.player = player;
        this.statHolder = statHolder;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public Player getPlayer() {
        return this.player;
    }

    public PlayerEquipmentCache getStats() {
        return this.statHolder;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

}
