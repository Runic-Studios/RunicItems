package com.runicrealms.plugin.runicitems.player;

import com.google.common.collect.Sets;
import com.runicrealms.plugin.rdb.RunicDatabase;
import com.runicrealms.plugin.runicitems.ItemManager;
import com.runicrealms.plugin.runicitems.RunicItems;
import com.runicrealms.plugin.runicitems.Stat;
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
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * A simple container which caches the player's stats and updates their armor stats
 */
public class PlayerEquipmentCache {

    /*

    =========================================== PSA ===========================================
    if you plan to change this class plz consult with excel
    everything has a purpose
    so many things are specifically set up to produce intended behavior within and outside of this class

     */

    private static final long WEAPON_PERKS_COOLDOWN_TICKS = 20 * 5;

    // Save for memory/performance reasons
    private static final Set<ItemPerk> EMPTY_SET = Collections.unmodifiableSet(new HashSet<>());
    private static final Map<Stat, Integer> EMPTY_STAT_MAP = Collections.emptyMap();

    private final Player player;
    private final Map<ItemPerkType, Integer> itemPerksExceedingMax = new HashMap<>(); // ItemPerks that exceed the max, and how much they would've been
    private volatile RunicItemArmor helmet;
    private volatile RunicItemArmor chestplate;
    private volatile RunicItemArmor leggings;
    private volatile RunicItemArmor boots;
    private volatile RunicItemOffhand offhand;
    private volatile RunicItemWeapon weapon;
    private AddedStats cachedStats;
    private Set<StatsModifier> statsModifiers = new HashSet<>();

    /*
    - we store the "most recently used weapon" for a player
    - if the player has no recently used weapon (just logged in?) and they swap to one, they hear the beacon activate noise and gain item perks
    - if the player is holding their weapon and then swaps to a potion, they lose the item perk but no beacon noise plays
    - if the player is holding a potion and they swap to their recently used weapon, they regain the item perk but no beacon noise plays
    - if the player swaps to a weapon that is not their "recently used weapon" then they lose all weapon perks, beacon deactivate sound plays,
        and an internal countdown immediately begins until they can use weapon perks again.
        Upon the ending of that countdown, their "most recently used weapon" is reset (as if they just logged in)
    - Note that the cooldown will still activate even if the weapon they swapped to had no actual perks on it
     */
    private @Nullable RecentWeapon recentWeapon = null;

    private @Nullable BukkitTask cooldownTask = null;


    public PlayerEquipmentCache(Player player) {
        this.cachedStats = new AddedStats(new HashMap<>(), null, 0);
        this.player = player;
        updateAllItems(true, false);
    }

    private static boolean canUseWeapon(Player player, RunicItemWeapon weapon) {
        return weapon.getLevel() <= player.getLevel()
                && RunicDatabase.getAPI().getCharacterAPI().getPlayerClass(player)
                .equalsIgnoreCase(weapon.getRunicClass().getIdentifier());
    }

    public Player getPlayer() {
        return this.player;
    }

    public AddedStats getTotalStats() {
        return this.cachedStats;
    }

    public Map<ItemPerkType, Integer> getItemPerksExceedingMax() {
        return this.itemPerksExceedingMax;
    }

    // Synchronized ensures no overlapping calls. This function runs async always anyway.
    private synchronized void updateTotalStats(boolean onLogin, boolean weaponSwitched) {
        Set<ItemPerk> oldPerks = this.cachedStats.getItemPerks();
        this.cachedStats = new AddedStats(new HashMap<>(), new HashSet<>(), 0);
        if (this.helmet != null) this.cachedStats.combine(this.helmet.getAddedStats());
        if (this.chestplate != null) this.cachedStats.combine(this.chestplate.getAddedStats());
        if (this.leggings != null) this.cachedStats.combine(this.leggings.getAddedStats());
        if (this.boots != null) this.cachedStats.combine(this.boots.getAddedStats());
        if (this.offhand != null) this.cachedStats.combine(this.offhand.getAddedStats());
        AddedStats modifierStats = new AddedStats(new HashMap<>(), new HashSet<>(), 0);
        for (StatsModifier modifier : this.statsModifiers) {
            modifierStats.combine(modifier.getChanges(this.cachedStats));
        }
        this.cachedStats.combine(modifierStats);

        Boolean beaconNoise = null; // Null indicates default behavior, true indicates yes, false indicates no

        if (this.weapon == null || canUseWeapon(this.player, this.weapon)) {
            if (this.weapon != null && !weaponSwitched && cooldownTask == null) {
                this.cachedStats.combine(this.weapon.getAddedStats()); // Default behavior
            } else if (this.weapon != null && weaponSwitched) {

                // Add just the stats no perks
                AddedStats weaponStats = this.weapon.getAddedStats();
                this.cachedStats.combine(new AddedStats(weaponStats.getAddedStats(), null, weaponStats.getAddedHealth()));

                // Because this logic is very confusing, I will try to outline the thought process behind each statement
                if (cooldownTask == null) { // if we are on cooldown we do nothing
                    if (this.recentWeapon != null && !this.recentWeapon.matchesItem(this.weapon)) {
                        // We had a weapon equipped, and we just swapped to a different weapon
                        // If the previous weapon had perks, activate cooldown, beacon deactivate, don't add any new perks.
                        // Else apply perks normally.
                        if (this.recentWeapon.hasItemPerks()) {
                            // Both the old weapon and the new one have perks
                            beaconNoise = true; // Play beacon deactivate
                            cooldownTask = new BukkitRunnable() {
                                @Override
                                public void run() {
                                    cooldownTask = null;
                                    recentWeapon = null; // Reset our recent weapon, so we can get weapon perks like normal
                                    updateWeaponAndTotal(false);
                                }
                            }.runTaskLaterAsynchronously(RunicItems.getInstance(), WEAPON_PERKS_COOLDOWN_TICKS);
                        } else if (this.weapon.hasItemPerks()) {
                            // Only the new one has perks, the old one didn't
                            // ... we should apply the perks normally
                            this.cachedStats.combine(new AddedStats(EMPTY_STAT_MAP, weaponStats.getItemPerks(), 0));
                        }
                    } else {
                        // Either we didn't have a previous weapon (cooldown ended/login) or we swapped back to our previous weapon
                        // Does matter, just reapply stats as normal
                        this.cachedStats.combine(new AddedStats(EMPTY_STAT_MAP, weaponStats.getItemPerks(), 0));
                        if (this.recentWeapon != null) {
                            // This implies that we just equipped the same weapon as the last weapon (work through the logic)
                            // Suppress beacon noises:
                            beaconNoise = false;
                        }
                    }
                }
            } else if (this.weapon == null && weaponSwitched && this.recentWeapon != null) {
                beaconNoise = false; // We de-equipped a weapon, suppress noise because we didn't swap to a perks weapon
            }
        } else if (weapon != null) { // We equipped a weapon but we can't use it
            beaconNoise = false; // Suppress noise
        }

        Set<ItemPerk> perks = this.cachedStats.getItemPerks();
        this.itemPerksExceedingMax.clear();
        if (perks != null) {
            this.cachedStats.setItemPerks(perks.stream().map(perk -> {
                if (perk.getStacks() > perk.getType().getMaxStacks()) {
                    itemPerksExceedingMax.put(perk.getType(), perk.getStacks());
                    return new ItemPerk(perk.getType(), perk.getType().getMaxStacks());
                }
                return perk;
            }).collect(Collectors.toSet()));
        }

        if (oldPerks == null) oldPerks = EMPTY_SET;
        Set<ItemPerk> newPerks = this.cachedStats.getItemPerks();
        if (newPerks == null) newPerks = EMPTY_SET;
        if (!Sets.intersection(oldPerks, newPerks).equals(Sets.union(oldPerks, newPerks))) {
            if (!weaponSwitched) beaconNoise = null; // We didn't switch weapons, disregard funky logic
            boolean playSounds = beaconNoise == null ? !onLogin : !onLogin && beaconNoise;
            ActiveItemPerksChangeEvent event = new ActiveItemPerksChangeEvent(this.player, oldPerks, newPerks, playSounds);
            if (!Bukkit.isPrimaryThread()) {
                Bukkit.getPluginManager().callEvent(event);
            } else {
                Bukkit.getScheduler().runTaskAsynchronously(RunicItems.getInstance(), () -> Bukkit.getPluginManager().callEvent(event));
            }
        } else {
            if (beaconNoise != null && beaconNoise) {
                // This is for the rare case where a player switches from a perks weapon, to a non-weapon item, to a new weapon
                // Here, the total perks would not be changing for the final swap but we still play the deactivation noise with cooldown
                player.playSound(player.getLocation(), Sound.BLOCK_BEACON_DEACTIVATE, 1.0F, 2.0F);
            }
        }
    }

    public void updateAllItems(boolean onLogin, boolean callEvent) {
        if (!onLogin && Bukkit.isPrimaryThread())
            throw new IllegalStateException("Cannot run update stats on main thread!");
        updateHelmet();
        updateChestplate();
        updateLeggings();
        updateBoots();
        updateOffhand();
        updateWeaponAndTotal(onLogin); // also updates total
        if (callEvent) Bukkit.getPluginManager().callEvent(new RunicStatUpdateEvent(this.player, this));
    }

    public void updateItems(boolean onLogin, StatHolderType... types) {
        if (!onLogin && Bukkit.isPrimaryThread())
            throw new IllegalStateException("Cannot run update stats on main thread!");
        boolean hasUpdatedTotal = false;
        for (StatHolderType type : types) {
            switch (type) {
                case HELMET -> updateHelmet();
                case CHESTPLATE -> updateChestplate();
                case LEGGINGS -> updateLeggings();
                case BOOTS -> updateBoots();
                case WEAPON -> {
                    updateWeaponAndTotal(onLogin);
                    hasUpdatedTotal = true;
                }
                case OFFHAND -> updateOffhand();
            }
        }
        if (!hasUpdatedTotal) updateTotalStats(onLogin, false);
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

        if (this.helmet != null && this.helmet.hasItemPerks()) player.updateInventory(); // Update dynamic lore
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

        if (this.chestplate != null && this.chestplate.hasItemPerks()) player.updateInventory(); // Update dynamic lore
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

        if (this.leggings != null && this.leggings.hasItemPerks()) player.updateInventory(); // Update dynamic lore
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

        if (this.boots != null && this.boots.hasItemPerks()) player.updateInventory(); // Update dynamic lore
    }

    // This one also updates the total because we need to update total before we change the recent weapon to the current one
    private void updateWeaponAndTotal(boolean onLogin) {
        if (this.player.getInventory().getItemInMainHand().getType() != Material.AIR) {
            try {
                RunicItem item = ItemManager.getRunicItemFromItemStack(this.player.getInventory().getItemInMainHand());
                if (item instanceof RunicItemWeapon) {
                    this.weapon = (RunicItemWeapon) item;
                } else {
                    this.weapon = null;
                }
            } catch (Exception exception) {
                Bukkit.getLogger().log(Level.INFO, "[RunicItems] Error loading player " + this.player.getName() + " weapon!");
                exception.printStackTrace();
                this.weapon = null;
            }
        } else {
            this.weapon = null;
        }

        boolean isNotOnCooldown = this.cooldownTask == null; // Check cooldown before we update total stats

        this.updateTotalStats(onLogin, true);

        if (this.weapon != null && this.weapon.hasItemPerks()) {
            // Experimental change that didn't work to force update the weapon
//            PacketContainer container = new PacketContainer(PacketType.Play.Server.SET_SLOT);
//            container.getBytes().write(-2, (byte) 0); // Window ID: -2 means ignore state ID
//            container.getIntegers().write(0, 0); // State ID: bogus value 0
//            container.getShorts().write(0, (short) player.getInventory().getHeldItemSlot()); // Slot number
//            container.getItemModifier().write(0, player.getInventory().getItemInMainHand()); // ItemStack
//            ProtocolLibrary.getProtocolManager().sendServerPacket(player, container);
            player.updateInventory(); // Update dynamic lore
        }

        // For item perks warmup
        if (this.weapon != null && isNotOnCooldown) {
            if (this.recentWeapon != null && this.recentWeapon.matchesItem(this.weapon))
                return; // avoid constructing a new object if we can
            if (!canUseWeapon(this.player, this.weapon)) return;
            this.recentWeapon = new RecentWeapon(this.weapon);
        }
    }

    private void updateOffhand() {
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

        if (this.offhand != null && this.offhand.hasItemPerks()) player.updateInventory(); // Update dynamic lore
    }

    public @Nullable RunicItemArmor getHelmet() {
        return this.helmet;
    }

    public @Nullable RunicItemArmor getChestplate() {
        return this.chestplate;
    }

    public @Nullable RunicItemArmor getLeggings() {
        return this.leggings;
    }

    public @Nullable RunicItemArmor getBoots() {
        return this.boots;
    }

    public @Nullable RunicItemWeapon getWeapon() {
        return this.weapon == null ? null : (canUseWeapon(player, this.weapon) ? this.weapon : null);
    }

    public @Nullable RunicItemOffhand getOffhand() {
        return this.offhand;
    }

    public void addModifier(StatsModifier modifier) {
        this.statsModifiers.add(modifier);
    }

    public void removeModifier(StatsModifier modifier) {
        this.statsModifiers.remove(modifier);
    }

    public enum StatHolderType {
        HELMET, CHESTPLATE, LEGGINGS, BOOTS, WEAPON, OFFHAND
    }

    private record RecentWeapon(String templateID, Set<ItemPerk> itemPerks) {

        private RecentWeapon(RunicItemWeapon weapon) {
            this(weapon.getTemplateId(), weapon.getItemPerks());
        }

        private boolean matchesItem(RunicItem item) {
            if (!(item instanceof RunicItemWeapon weapon)) return false;
            return weapon.getTemplateId().equals(templateID) && itemPerks.equals(weapon.getItemPerks());
        }

        private boolean hasItemPerks() {
            return itemPerks != null && itemPerks.size() > 0;
        }

    }

}
