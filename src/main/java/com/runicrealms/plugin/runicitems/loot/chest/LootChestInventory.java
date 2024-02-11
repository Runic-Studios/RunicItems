package com.runicrealms.plugin.runicitems.loot.chest;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

public class LootChestInventory implements Listener, InventoryHolder {

    private final List<Consumer<Player>> onOpenActions = new LinkedList<>();
    private final List<Consumer<Player>> onCloseActions = new LinkedList<>();
    private final Inventory inventory;
    private final LootChest lootChest;

    public LootChestInventory(LootChest lootChest,
                              Collection<ItemStack> items,
                              int inventorySize,
                              String inventoryTitle,
                              Consumer<Player> onOpenAction) {
        this.lootChest = lootChest;
        if (inventorySize % 9 != 0 || inventorySize < items.size())
            throw new IllegalArgumentException("Cannot create LootChest with invalid inventory size " + inventorySize);
        this.inventory = Bukkit.createInventory(this, inventorySize, inventoryTitle);
        Random random = new Random();
        List<Integer> availableSlots = new ArrayList<>(inventorySize);
        for (int i = 0; i < inventorySize; i++) availableSlots.add(i);
        for (ItemStack item : items) {
            if (availableSlots.isEmpty()) break;
            int randomSlot = availableSlots.remove(random.nextInt(availableSlots.size()));
            inventory.setItem(randomSlot, item);
        }
        if (onOpenAction != null) this.onOpenActions.add(onOpenAction);
    }

    @NotNull
    @Override
    public Inventory getInventory() {
        return this.inventory;
    }

    public LootChest getLootChest() {
        return this.lootChest;
    }

    public void onOpen(Consumer<Player> onOpen) {
        onOpenActions.add(onOpen);
    }

    public void open(Player player) {
        player.openInventory(inventory);
        onOpenActions.forEach(action -> action.accept(player));
        onOpenActions.clear();
    }

    public void onClose(Consumer<Player> onClose) {
        onCloseActions.add(onClose);
    }

    public void close(Player player) {
        if (player.getOpenInventory().getTopInventory().getHolder() == this) {
            onCloseActions.forEach(action -> action.accept(player));
        }
        onCloseActions.clear();
    }

}
