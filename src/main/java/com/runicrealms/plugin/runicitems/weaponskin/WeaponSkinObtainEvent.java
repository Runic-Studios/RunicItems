package com.runicrealms.plugin.runicitems.weaponskin;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class WeaponSkinObtainEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final String permission;
    private boolean isCancelled = false;

    /**
     * Fires when a player obtains a weap
     *
     * @param player     player who triggered the item
     * @param permission the permission of the weapon skin they are obtaining
     */
    public WeaponSkinObtainEvent(Player player, String permission) {
        this.player = player;
        this.permission = permission;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public HandlerList getHandlers() {
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

    public String getPermission() {
        return this.permission;
    }

}
