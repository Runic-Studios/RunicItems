package com.runicrealms.runicitems.player;

import com.runicrealms.runicitems.ItemManager;
import com.runicrealms.runicitems.Stat;
import com.runicrealms.runicitems.item.*;
import com.runicrealms.runicitems.item.stats.RunicArtifactAbility;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

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

    public PlayerStatHolder(Player player) {
        this.player = player;
        this.updateHelmet();
        this.updateChestplate();
        this.updateLeggings();
        this.updateBoots();
        this.updateOffhand();
        this.updateWeapon();
    }

    public AddedPlayerStats getTotalStats() {
        Map<Stat, Integer> stats = new HashMap<>();
        RunicArtifactAbility ability = null;
        int health = 0;
        if (this.helmet != null) {
            this.helmet.calculateAddedStats().forEach((stat, value) -> {
                if (!stats.containsKey(stat)) stats.put(stat, 0);
                stats.put(stat, stats.get(stat) + value);
            });
            health += this.helmet.getHealth();
        }
        if (this.chestplate != null) {
            this.chestplate.calculateAddedStats().forEach((stat, value) -> {
                if (!stats.containsKey(stat)) stats.put(stat, 0);
                stats.put(stat, stats.get(stat) + value);
            });
            health += this.chestplate.getHealth();
        }
        if (this.leggings != null) {
            this.leggings.calculateAddedStats().forEach((stat, value) -> {
                if (!stats.containsKey(stat)) stats.put(stat, 0);
                stats.put(stat, stats.get(stat) + value);
            });
            health += this.leggings.getHealth();
        }
        if (this.boots != null) {
            this.boots.calculateAddedStats().forEach((stat, value) -> {
                if (!stats.containsKey(stat)) stats.put(stat, 0);
                stats.put(stat, stats.get(stat) + value);
            });
            health += this.boots.getHealth();
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
        return new AddedPlayerStats(stats, health, ability);
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

    public void updateHelmet() {
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
    }

    public void updateChestplate() {
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
    }

    public void updateLeggings() {
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
    }

    public void updateBoots() {
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
    }
}
