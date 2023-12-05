package com.runicrealms.plugin.runicitems.player;

import com.google.common.collect.Sets;
import com.runicrealms.plugin.common.util.ArmorType;
import com.runicrealms.plugin.runicitems.ItemManager;
import com.runicrealms.plugin.runicitems.item.RunicItem;
import com.runicrealms.plugin.runicitems.item.RunicItemArmor;
import com.runicrealms.plugin.runicitems.item.RunicItemOffhand;
import com.runicrealms.plugin.runicitems.item.RunicItemWeapon;
import com.runicrealms.plugin.runicitems.item.event.RunicStatUpdateEvent;
import com.runicrealms.plugin.runicitems.item.perk.ActiveItemPerksChangeEvent;
import com.runicrealms.plugin.runicitems.item.perk.ItemPerk;
import com.runicrealms.plugin.runicitems.item.perk.ItemPerkType;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

/**
 * A simple container which caches the player's stats and updates their armor stats
 */
public class PlayerStatHolder {

    // Save for memory/performance reasons
    private static final Set<ItemPerk> EMPTY_SET = Collections.unmodifiableSet(new HashSet<>());

    private final Player player;
    private final Map<ItemPerkType, Integer> itemPerksExceedingMax = new HashMap<>(); // ItemPerks that exceed the max, and how much they would've been
    private RunicItemArmor helmet;
    private RunicItemArmor chestplate;
    private RunicItemArmor leggings;
    private RunicItemArmor boots;
    private RunicItemOffhand offhand;
    private RunicItemWeapon weapon;
    private AddedStats cachedStats;

    public PlayerStatHolder(Player player) {
        this.player = player;
        this.updateHelmet();
        this.updateChestplate();
        this.updateLeggings();
        this.updateBoots();
        this.updateOffhand();
        this.updateWeapon();
        this.cachedStats = getTotalStats();
    }

    public RunicItemArmor getBoots() {
        return this.boots;
    }

    public RunicItemArmor getChestplate() {
        return this.chestplate;
    }

    public RunicItemArmor getHelmet() {
        return this.helmet;
    }

    public RunicItemArmor getLeggings() {
        return this.leggings;
    }

    public RunicItemOffhand getOffhand() {
        return this.offhand;
    }

    public Player getPlayer() {
        return this.player;
    }

    public AddedStats getTotalStats() {
        return this.cachedStats;
    }

    public RunicItemWeapon getWeapon() {
        return this.weapon;
    }

    public Map<ItemPerkType, Integer> getItemPerksExceedingMax() {
        return this.itemPerksExceedingMax;
    }

    private void updateBoots() {
        if (this.player.getInventory().getBoots() != null && this.player.getInventory().getBoots().getType() != Material.AIR) {
            try {
                this.boots = (RunicItemArmor) ItemManager.getRunicItemFromItemStack(this.player.getInventory().getBoots());
            } catch (Exception exception) {
                Bukkit.getLogger().log(Level.INFO, "[RunicItems] Error loading player " + this.player.getName() + " boots!");
                exception.printStackTrace();
                this.boots = null;
            }
        } else {
            this.boots = null;
        }
        this.updateTotalStats();
    }

    private void updateChestplate() {
        if (this.player.getInventory().getChestplate() != null && this.player.getInventory().getChestplate().getType() != Material.AIR) {
            try {
                this.chestplate = (RunicItemArmor) ItemManager.getRunicItemFromItemStack(this.player.getInventory().getChestplate());
            } catch (Exception exception) {
                Bukkit.getLogger().log(Level.INFO, "[RunicItems] Error loading player " + this.player.getName() + " chestplate!");
                exception.printStackTrace();
                this.chestplate = null;
            }
        } else {
            this.chestplate = null;
        }
        this.updateTotalStats();
    }

    private void updateHelmet() {
        if (this.player.getInventory().getHelmet() != null && this.player.getInventory().getHelmet().getType() != Material.AIR) {
            try {
                this.helmet = (RunicItemArmor) ItemManager.getRunicItemFromItemStack(this.player.getInventory().getHelmet());
            } catch (Exception exception) {
                Bukkit.getLogger().log(Level.INFO, "[RunicItems] Error loading player " + this.player.getName() + " helmet!");
                exception.printStackTrace();
                this.helmet = null;
            }
        } else {
            this.helmet = null;
        }
        this.updateTotalStats();
    }

    public void updateItems() {
        if (!Bukkit.isPrimaryThread()) throw new IllegalStateException("Cannot run update stats on async thread!");
        updateHelmet();
        updateChestplate();
        updateLeggings();
        updateBoots();
        updateOffhand();
        updateWeapon();
        updateTotalStats();
        Bukkit.getPluginManager().callEvent(new RunicStatUpdateEvent(this.player, this));
    }

    public void updateItems(ArmorType... armorTypes) {
        for (ArmorType armorType : armorTypes) {
            switch (armorType) {
                case HELMET:
                    updateHelmet();
                case CHESTPLATE:
                    updateChestplate();
                case LEGGINGS:
                    updateLeggings();
                case BOOTS:
                    updateBoots();
            }
        }
        updateTotalStats();
    }

    private void updateLeggings() {
        if (this.player.getInventory().getLeggings() != null && this.player.getInventory().getLeggings().getType() != Material.AIR) {
            try {
                this.leggings = (RunicItemArmor) ItemManager.getRunicItemFromItemStack(this.player.getInventory().getLeggings());
            } catch (Exception exception) {
                Bukkit.getLogger().log(Level.INFO, "[RunicItems] Error loading player " + this.player.getName() + " leggings!");
                exception.printStackTrace();
                this.leggings = null;
            }
        } else {
            this.leggings = null;
        }
        this.updateTotalStats();
    }

    public void updateOffhand() {
        if (this.player.getInventory().getItemInOffHand().getType() != Material.AIR) {
            try {
                RunicItem item = ItemManager.getRunicItemFromItemStack(this.player.getInventory().getItemInOffHand());
                if (item instanceof RunicItemOffhand) {
                    this.offhand = (RunicItemOffhand) item;
                } else {
                    this.offhand = null;
                }
            } catch (Exception exception) {
                Bukkit.getLogger().log(Level.INFO, "[RunicItems] Error loading player " + this.player.getName() + " offhand!");
                exception.printStackTrace();
                this.offhand = null;
            }
        } else {
            this.offhand = null;
        }
        this.updateTotalStats();
    }

    public void updateTotalStats() {
        Set<ItemPerk> oldPerks = this.cachedStats.getItemPerks();
        this.cachedStats = new AddedStats(new HashMap<>(), new HashSet<>(), 0);
        if (this.helmet != null) this.cachedStats.combine(this.helmet.getAddedStats());
        if (this.chestplate != null) this.cachedStats.combine(this.chestplate.getAddedStats());
        if (this.leggings != null) this.cachedStats.combine(this.leggings.getAddedStats());
        if (this.boots != null) this.cachedStats.combine(this.boots.getAddedStats());
        if (this.offhand != null) this.cachedStats.combine(this.offhand.getAddedStats());
        if (this.weapon != null) this.cachedStats.combine(this.weapon.getAddedStats());

        Set<ItemPerk> perks = this.cachedStats.getItemPerks();
        this.itemPerksExceedingMax.clear();
        if (perks != null) {
            Iterator<ItemPerk> iterator = perks.iterator();
            while (iterator.hasNext()) { // do not replace with for, you will get CME
                ItemPerk perk = iterator.next();
                if (perk.getStacks() > perk.getType().getMaxStacks()) {
                    ItemPerk newPerk = new ItemPerk(perk.getType(), perk.getType().getMaxStacks());
                    this.itemPerksExceedingMax.put(perk.getType(), perk.getStacks());
                    perks.remove(perk);
                    perks.add(newPerk);
                }
            }
        }

        if (oldPerks == null) oldPerks = EMPTY_SET;
        Set<ItemPerk> newPerks = this.cachedStats.getItemPerks();
        if (newPerks == null) newPerks = EMPTY_SET;
        if (!Sets.intersection(oldPerks, newPerks).equals(Sets.union(oldPerks, newPerks))) {
            Bukkit.getPluginManager().callEvent(new ActiveItemPerksChangeEvent(this.player, oldPerks, newPerks));
        }
    }

    public void updateWeapon() {
        if (this.player.getInventory().getItemInMainHand().getType() != Material.AIR) {
            try {
                RunicItem item = ItemManager.getRunicItemFromItemStack(this.player.getInventory().getItemInMainHand());
                if (item instanceof RunicItemWeapon) {
                    this.weapon = (RunicItemWeapon) item;
                } else {
                    this.weapon = null;
                }
            } catch (Exception exception) {
                Bukkit.getLogger().log(Level.INFO, "[RunicItems] Error loading player " + this.player.getName() + "weapon!");
                exception.printStackTrace();
                this.weapon = null;
            }
        } else {
            this.weapon = null;
        }
        this.updateTotalStats();
    }

}
