package com.runicrealms.plugin.runicitems.loot.chest;

import com.runicrealms.plugin.common.resourcepack.ResourcePackManager;
import com.runicrealms.plugin.runicitems.RunicItems;
import com.runicrealms.plugin.runicitems.loot.LootHolder;
import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.entity.Dummy;
import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.ModeledEntity;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class LootChest implements LootHolder {

    protected static final BlockData AIR_BLOCK_DATA = Material.AIR.createBlockData();
    protected static final BlockData BARRIER_BLOCK_DATA = Material.BARRIER.createBlockData();

    protected final LootChestPosition position;
    protected final LootChestTemplate lootChestTemplate;
    protected final LootChestConditions conditions;
    protected final int minLevel;
    protected final int itemMinLevel;
    protected final int itemMaxLevel;
    protected final String inventoryTitle;

    protected final ModeledEntity entity;
    protected final BlockData blockData;
    private final String modelID;
    protected ActiveModel model;

    public LootChest(
            @NotNull LootChestPosition position,
            @NotNull LootChestTemplate lootChestTemplate,
            @NotNull LootChestConditions conditions,
            int minLevel,
            int itemMinLevel, int itemMaxLevel,
            @NotNull String inventoryTitle,
            @Nullable String modelID) {
        this.position = position;
        this.lootChestTemplate = lootChestTemplate;
        this.conditions = conditions;
        this.minLevel = minLevel;
        this.itemMinLevel = itemMinLevel;
        this.itemMaxLevel = itemMaxLevel;
        this.inventoryTitle = inventoryTitle;

        this.modelID = modelID;

        Dummy<?> dummy = new Dummy<>();
        dummy.setRenderRadius(0);

        Location target = position.getLocation().clone();
        target.setX(target.getX() + .5);
        target.setZ(target.getZ() + .5);

        target.setDirection(this.position.getDirection().getDirection());

        dummy.setLocation(target);
        dummy.setYHeadRot(target.getYaw());
        dummy.setYBodyRot(target.getYaw());

        this.entity = ModelEngineAPI.createModeledEntity(dummy);
//        this.entity.setRenderDistance(0);

        this.blockData = Material.CHEST.createBlockData();
        ((Directional) this.blockData).setFacing(this.position.getDirection());

        Bukkit.getScheduler().runTaskLater(RunicItems.getInstance(), () -> this.position.getLocation().getBlock().setType(Material.AIR), 10);
    }

    public LootChest(@NotNull LootChestPosition position, @NotNull LootChestTemplate lootChestTemplate, @NotNull LootChestConditions conditions, int minLevel, int itemMinLevel, int itemMaxLevel, @NotNull String inventoryTitle) {
        this(position, lootChestTemplate, conditions, minLevel, itemMinLevel, itemMaxLevel, inventoryTitle, null);
    }

    @NotNull
    public LootChestPosition getPosition() {
        return this.position;
    }

    @NotNull
    public LootChestTemplate getLootChestTemplate() {
        return this.lootChestTemplate;
    }

    @NotNull
    public LootChestConditions getConditions() {
        return this.conditions;
    }

    public int getMinLevel() {
        return this.minLevel;
    }

    @Override
    public int getItemMinLevel() {
        return this.itemMinLevel;
    }

    @Override
    public int getItemMaxLevel() {
        return this.itemMaxLevel;
    }

    public String getInventoryTitle() {
        return this.inventoryTitle;
    }

    @NotNull
    public String getModelID() {
        return this.modelID;
    }

    protected LootChestInventory generateInventory(@NotNull Player player) {
        return lootChestTemplate.generateInventory(this, player);
    }

    public void openInventory(@NotNull Player player) {
        boolean canOpen = this.conditions.attempt(player);

        if (!canOpen) {
            return;
        }

        this.generateInventory(player).open(player);
    }

    public void playOpenAnimation() {
        this.setActiveModel();
        this.model.getAnimationHandler().playAnimation("hit", 0, 0, 2, false);
    }

    public void showToPlayer(@NotNull Player player) {
        player.sendBlockChange(this.position.getLocation(), ResourcePackManager.isPackActive(player) ? BARRIER_BLOCK_DATA : this.blockData);
        this.setActiveModel();

        if (this.model == null || !ResourcePackManager.isPackActive(player)) {
            return;
        }

        ((Dummy<?>) this.entity.getBase()).setForceHidden(player, false);
        ((Dummy<?>) this.entity.getBase()).setForceViewing(player, true);
//        this.entity.getRangeManager().forceSpawn(player);
    }

    public void hideFromPlayer(@NotNull Player player, boolean showParticles) {
        player.sendBlockChange(this.position.getLocation(), AIR_BLOCK_DATA);
        if (showParticles) {
            player.spawnParticle(Particle.REDSTONE, this.position.getLocation(),
                    25, 0.5f, 0.5f, 0.5f, 0, new Particle.DustOptions(Color.WHITE, 20));
        }

        if (this.model == null || !ResourcePackManager.isPackActive(player)) {
            return;
        }

        ((Dummy<?>) this.entity.getBase()).setForceHidden(player, true);
        ((Dummy<?>) this.entity.getBase()).setForceViewing(player, false);
    }

    public void hideFromPlayer(@NotNull Player player) {
        this.hideFromPlayer(player, true);
    }

    @Override
    public boolean equals(@Nullable Object object) {
        return (object instanceof LootChest lootChest) && lootChest.position.equals(position);
    }

    /**
     * Declares if this loot chest should have its block updated automatically by the ClientLootManager.
     */
    public abstract boolean shouldUpdateDisplay();

    /**
     * Code that should be executed when the chest is opened
     *
     * @param player the player who opened the chest
     */
    public abstract void onOpen(@NotNull Player player);

    private void setActiveModel() {
        if (this.model != null) {
            return;
        }

        this.model = ModelEngineAPI.createActiveModel(this.modelID != null ? this.modelID : "chest_wooden");

        if (this.model != null) {
            this.entity.addModel(this.model, false);
        } else {
            RunicItems.getInstance().getLogger().warning("There was an error loading the " + (modelID != null ? modelID : "chest_wooden") + " model!");
        }
    }
}
