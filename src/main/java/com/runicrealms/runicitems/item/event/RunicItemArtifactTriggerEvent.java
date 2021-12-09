package com.runicrealms.runicitems.item.event;

import com.runicrealms.runicitems.item.RunicItemArtifact;
import com.runicrealms.runicitems.item.util.RunicArtifactAbilityTrigger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

public class RunicItemArtifactTriggerEvent extends Event implements Cancellable {

    private boolean isCancelled;
    private final Player player;
    private final RunicItemArtifact runicItemArtifact;
    private final ItemStack itemStack;
    private final RunicArtifactAbilityTrigger abilityTrigger;

    /**
     * Used to handle passives for artifacts
     *
     * @param player            who triggered the event
     * @param runicItemArtifact the artifact associated with the trigger
     * @param itemStack         the itemStack of the artifact in the event
     * @param abilityTrigger    the type of trigger (cast, hit, kill, etc.)
     */
    public RunicItemArtifactTriggerEvent(Player player, RunicItemArtifact runicItemArtifact, ItemStack itemStack,
                                         RunicArtifactAbilityTrigger abilityTrigger) {
        Bukkit.broadcastMessage("runic ability triggered");
        this.player = player;
        this.runicItemArtifact = runicItemArtifact;
        this.itemStack = itemStack;
        this.abilityTrigger = abilityTrigger;
    }

    private static final HandlerList handlers = new HandlerList();

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

    public RunicItemArtifact getRunicItemArtifact() {
        return this.runicItemArtifact;
    }

    public ItemStack getItemStack() {
        return this.itemStack;
    }

    public RunicArtifactAbilityTrigger getAbilityTrigger() {
        return this.abilityTrigger;
    }

}
