package com.runicrealms.runicitems.player;

import com.codingforcookies.armorequip.ArmorEquipEvent;
import com.codingforcookies.armorequip.ArmorType;
import com.runicrealms.runicitems.ItemManager;
import com.runicrealms.runicitems.Stat;
import com.runicrealms.runicitems.item.*;
import com.runicrealms.runicitems.item.stats.RunicArtifactAbility;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class PlayerStatHolder {

    private final Player player;
    private RunicItemArmor helmet;
    private RunicItemArmor chestplate;
    private RunicItemArmor leggings;
    private RunicItemArmor boots;
    private RunicItemOffhand offhand;
    private RunicItemWeapon weapon;

    private AddedPlayerStats cachedStats;

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

    public AddedPlayerStats getTotalStats() {
        return this.cachedStats;
    }

    public void updateTotalStats() {
        Map<Stat, Integer> stats = new HashMap<>();
        RunicArtifactAbility ability = null;
        int health = 0;
        if (this.helmet != null) {
            AddedArmorStats addedStats = this.helmet.calculateAddedStats();
            addedStats.getStats().forEach((stat, value) -> {
                if (!stats.containsKey(stat)) stats.put(stat, 0);
                stats.put(stat, stats.get(stat) + value);
            });
            health += addedStats.getHealth();
        }
        if (this.chestplate != null) {
            AddedArmorStats addedStats = this.chestplate.calculateAddedStats();
            addedStats.getStats().forEach((stat, value) -> {
                if (!stats.containsKey(stat)) stats.put(stat, 0);
                stats.put(stat, stats.get(stat) + value);
            });
            health += addedStats.getHealth();
        }
        if (this.leggings != null) {
            AddedArmorStats addedStats = this.leggings.calculateAddedStats();
            addedStats.getStats().forEach((stat, value) -> {
                if (!stats.containsKey(stat)) stats.put(stat, 0);
                stats.put(stat, stats.get(stat) + value);
            });
            health += addedStats.getHealth();
        }
        if (this.boots != null) {
            AddedArmorStats addedStats = this.boots.calculateAddedStats();
            addedStats.getStats().forEach((stat, value) -> {
                if (!stats.containsKey(stat)) stats.put(stat, 0);
                stats.put(stat, stats.get(stat) + value);
            });
            health += addedStats.getHealth();
        }
        if (this.offhand != null) {
            this.offhand.getStats().forEach((stat, roll) -> {
                if (!stats.containsKey(stat)) stats.put(stat, 0);
                stats.put(stat, stats.get(stat) + roll.getValue());
            });
        }
        if (this.weapon != null) {
            this.weapon.getStats().forEach((stat, roll) -> {
                if (!stats.containsKey(stat)) stats.put(stat, 0);
                stats.put(stat, stats.get(stat) + roll.getValue());
            });
            ability = this.weapon instanceof RunicItemArtifact ? ((RunicItemArtifact) this.weapon).getAbility() : null;
        }
        this.cachedStats = new AddedPlayerStats(stats, health, ability);
    }

    public Player getPlayer() {
        return this.player;
    }

    public RunicItemArmor getHelmet() {
        return this.helmet;
    }

    public RunicItemArmor getChestplate() {
        return this.chestplate;
    }

    public RunicItemArmor getLeggings() {
        return this.leggings;
    }

    public RunicItemArmor getBoots() {
        return this.boots;
    }

    public RunicItemOffhand getOffhand() {
        return this.offhand;
    }

    public RunicItemWeapon getWeapon() {
        return this.weapon;
    }

    public void updateItems() {
        updateHelmet();
        updateChestplate();
        updateLeggings();
        updateBoots();
        updateOffhand();
        updateWeapon();
        updateTotalStats();
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
