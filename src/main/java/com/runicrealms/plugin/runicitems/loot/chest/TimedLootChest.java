package com.runicrealms.plugin.runicitems.loot.chest;

import com.runicrealms.plugin.runicitems.RunicItems;
import me.filoghost.holographicdisplays.api.HolographicDisplaysAPI;
import me.filoghost.holographicdisplays.api.hologram.Hologram;
import me.filoghost.holographicdisplays.api.hologram.VisibilitySettings;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

public class TimedLootChest extends LootChest {

    private final int duration; // in seconds
    private final Location hologramLocation;
    private final BiConsumer<Hologram, Integer> hologramEditor; // Integer is remaining duration in seconds
    private final Map<UUID, Runnable> finishTasks = new ConcurrentHashMap<>();

    private final Set<UUID> displayedTo;

    public TimedLootChest(
            @NotNull LootChestPosition position,
            @NotNull LootChestTemplate lootChestTemplate,
            @NotNull LootChestConditions conditions,
            int minLevel,
            int itemMinLevel, int itemMaxLevel,
            @NotNull String inventoryTitle,
            int duration,
            @NotNull Location hologramLocation,
            @NotNull BiConsumer<Hologram, Integer> hologramEditor,
            @Nullable String modelID) {
        super(position, lootChestTemplate, conditions, minLevel, itemMinLevel, itemMaxLevel, inventoryTitle, modelID);
        this.duration = duration;
        this.hologramLocation = hologramLocation;
        this.hologramEditor = hologramEditor;
        this.displayedTo = new HashSet<>();
    }

    /**
     * WARNING: this method should not be called outside the LootManager and ClientLootManager classes!
     */
    public void beginDisplay(@NotNull Player player, @NotNull Runnable onFinish) {
        finishTasks.put(player.getUniqueId(), onFinish);
        AtomicInteger counter = new AtomicInteger(this.duration);
        Hologram hologram = HolographicDisplaysAPI.get(RunicItems.getInstance()).createHologram(hologramLocation);
        hologram.getVisibilitySettings().setGlobalVisibility(VisibilitySettings.Visibility.HIDDEN);
        hologram.getVisibilitySettings().setIndividualVisibility(player, VisibilitySettings.Visibility.VISIBLE);
        this.displayedTo.add(player.getUniqueId());
        Bukkit.getScheduler().runTaskLaterAsynchronously(RunicItems.getInstance(), () -> this.showToPlayer(player), 10);

        Bukkit.getScheduler().runTaskTimer(RunicItems.getInstance(), task -> {
            hologramEditor.accept(hologram, counter.get());

            int timeRemaining = counter.get();
            if (timeRemaining <= 0 || !this.displayedTo.contains(player.getUniqueId())) {
                task.cancel();
                this.hideFromPlayer(player);
                hologram.delete();
                Runnable finish = finishTasks.remove(player.getUniqueId());
                if (finish != null) finish.run();
            }

            counter.set(timeRemaining - 1);
        }, 10, 20);
    }

    public int getDuration() {
        return this.duration;
    }

    @Override
    protected LootChestInventory generateInventory(@NotNull Player player) {
        LootChestInventory inventory = super.generateInventory(player);
        inventory.onClose(target -> {
            hideFromPlayer(target);
            Runnable finish = finishTasks.remove(target.getUniqueId());
            if (finish != null) finish.run();
        });
        return inventory;
    }

    @Override
    public boolean shouldUpdateDisplay() {
        return false;
    }

    @Override
    public void onOpen(@NotNull Player player) {
        this.displayedTo.remove(player.getUniqueId());
    }
}
