package com.runicrealms.plugin.runicitems.loot;

import com.runicrealms.plugin.common.RunicCommon;
import com.runicrealms.plugin.common.util.ColorUtil;
import com.runicrealms.plugin.runicitems.RunicItems;
import com.runicrealms.plugin.runicitems.RunicItemsAPI;
import com.runicrealms.plugin.runicitems.loot.chest.BossTimedLoot;
import com.runicrealms.plugin.runicitems.loot.chest.CustomTimedLoot;
import com.runicrealms.plugin.runicitems.loot.chest.LootChestConditions;
import com.runicrealms.plugin.runicitems.loot.chest.LootChestPosition;
import com.runicrealms.plugin.runicitems.loot.chest.LootChestTemplate;
import com.runicrealms.plugin.runicitems.loot.chest.RegenerativeLootChest;
import com.runicrealms.plugin.runicitems.loot.chest.TimedLoot;
import com.runicrealms.plugin.runicitems.loot.chest.TimedLootChest;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;

// today's sponsor is chat gpt!
public class LootManager implements LootAPI {

    public static final int DEFAULT_MAX_BOSS_LOOT_RANGE = 1024; // In blocks, how close do players need to be to the boss to gain loot

    private final Map<String, LootTable> lootTables = new HashMap<>();
    private final Map<String, LootChestTemplate> lootChestTemplates = new HashMap<>();
    private final Map<Location, RegenerativeLootChest> regenLootChests = new HashMap<>();
    private final Map<String, BossTimedLoot> bossTimedLoot = new HashMap<>();
    private final Map<String, CustomTimedLoot> customTimedLoot = new HashMap<>();
    private final File regenLootChestsFile;
    private final ClientLootManager clientLootManager;
    private FileConfiguration regenLootChestsConfig;
    private int nextRegenLootChestID = 0;
    private BossTimedLootManager bossTimedLootManager;
    private CustomTimedLootManager customTimedLootManager;

    public LootManager() {
        File lootFolder = new File(RunicItems.getInstance().getDataFolder(), "loot");

        // LOOT TABLES
        File lootTableFolder = RunicCommon.getConfigAPI().getSubFolder(lootFolder, "loot-tables");
        // Map of loot table identifier -> config
        Map<String, FileConfiguration> configs = new HashMap<>();
        // map of loot table identifier -> list of other loot table identifiers that are subtables of it
        Map<String, List<String>> configDependencies = new HashMap<>();

        // Get config files and their dependencies
        for (File lootTableFile : Objects.requireNonNull(lootTableFolder.listFiles())) {
            if (!lootTableFile.isDirectory() && (lootTableFile.getName().endsWith(".yml") || lootTableFile.getName().endsWith(".yaml"))) {
                try {
                    FileConfiguration config = RunicCommon.getConfigAPI().getYamlConfigFromFile(lootTableFile.getName(), lootTableFolder);
                    String identifier = Objects.requireNonNull(config.getString("identifier"));
                    List<String> deps = new ArrayList<>();
                    if (config.isList("subtables")) {
                        deps = config.getStringList("subtables");
                    }
                    configs.put(identifier, config);
                    configDependencies.put(identifier, deps);
                } catch (Exception exception) {
                    Bukkit.getLogger().log(Level.SEVERE, "ERROR loading loot table " + lootTableFile.getName() + ":");
                    exception.printStackTrace();
                }
            }
        }

        // Parse config files and dependencies in proper order
        try {
            List<String> sortedIdentifiers = topologicalSort(configDependencies);
            for (String identifier : sortedIdentifiers) {
                FileConfiguration config = configs.get(identifier);
                try {
                    LootTable lootTable = parseLootTable(config);
                    lootTables.put(identifier, lootTable);
                } catch (Exception exception) {
                    Bukkit.getLogger().log(Level.SEVERE, "ERROR loading loot table " + identifier + ":");
                    exception.printStackTrace();
                }
            }
        } catch (Exception exception) {
            Bukkit.getLogger().log(Level.SEVERE, "ERROR loading loot tables:");
            exception.printStackTrace();
        }


        // LOOT CHEST TEMPLATES
        File chestTypesFolder = RunicCommon.getConfigAPI().getSubFolder(lootFolder, "chest-types");

        for (File chestTypeFile : Objects.requireNonNull(chestTypesFolder.listFiles())) {
            if (!chestTypeFile.isDirectory() && (chestTypeFile.getName().endsWith(".yml") || chestTypeFile.getName().endsWith(".yaml"))) {
                FileConfiguration config = RunicCommon.getConfigAPI().getYamlConfigFromFile(chestTypeFile.getName(), chestTypesFolder);
                LootChestTemplate lootChestTemplate;

                try {
                    lootChestTemplate = parseLootChestTemplate(config);
                } catch (IllegalArgumentException e) {
                    Bukkit.broadcastMessage(ColorUtil.format("&c" + e.getMessage()));
                    e.printStackTrace();
                    continue;
                }

                lootChestTemplates.put(lootChestTemplate.getIdentifier(), lootChestTemplate);
            }
        }

        // LOOT CHESTS.yml
        regenLootChestsFile = new File(lootFolder, "regenerative-chests.yml");
        if (!regenLootChestsFile.exists()) {
            try {
                if (!regenLootChestsFile.createNewFile())
                    throw new IOException("Could not create regenerative-chests.yml file!");
            } catch (IOException exception) {
                throw new RuntimeException(exception);
            }
        }
        try {
            regenLootChestsConfig = RunicCommon.getConfigAPI().getYamlConfigFromFile(regenLootChestsFile);
            nextRegenLootChestID = regenLootChestsConfig.getInt("next-id");
            if (regenLootChestsConfig.contains("chests") && regenLootChestsConfig.isConfigurationSection("chests")) {
                for (String chestID : Objects.requireNonNull(regenLootChestsConfig.getConfigurationSection("chests")).getKeys(false)) {
                    try {
                        RegenerativeLootChest chest = parseRegenerativeLootChest(regenLootChestsConfig.getConfigurationSection("chests." + chestID), chestID);
                        regenLootChests.put(chest.getPosition().getLocation(), chest);
                    } catch (Exception exception) {
                        Bukkit.getLogger().log(Level.SEVERE, "ERROR loading regenerative-chests.yml chest ID " + chestID + ":");
                        exception.printStackTrace();
                    }
                }
            }
        } catch (Exception exception) {
            Bukkit.getLogger().log(Level.SEVERE, "ERROR loading regenerative-chests.yml:");
            exception.printStackTrace();
        }

        // LOAD CLIENT CHESTS
        this.clientLootManager = new ClientLootManager(getRegenerativeLootChests());

        // LOAD TIMED LOOT
        Set<TimedLoot> timedLoot = new HashSet<>();
        File timedLootFolder = RunicCommon.getConfigAPI().getSubFolder(lootFolder, "timed-loot");

        for (File timedLootFile : Objects.requireNonNull(timedLootFolder.listFiles())) {
            if (!timedLootFile.isDirectory() && (timedLootFile.getName().endsWith(".yml") || timedLootFile.getName().endsWith(".yaml"))) {
                try {
                    FileConfiguration config = RunicCommon.getConfigAPI().getYamlConfigFromFile(timedLootFile.getName(), timedLootFolder);
                    TimedLoot loot = parseTimedLoot(config);
                    timedLoot.add(loot);
                } catch (Exception exception) {
                    Bukkit.getLogger().log(Level.SEVERE, "ERROR loading timed loot " + timedLootFile.getName() + ":");
                    exception.printStackTrace();
                }
            }
        }

        timedLoot.stream()
                .filter(loot -> loot instanceof BossTimedLoot)
                .map(loot -> (BossTimedLoot) loot)
                .forEach(loot -> bossTimedLoot.put(loot.getMmBossID(), loot));
        this.bossTimedLootManager = new BossTimedLootManager(bossTimedLoot.values());

        timedLoot.stream()
                .filter(loot -> loot instanceof CustomTimedLoot)
                .map(loot -> (CustomTimedLoot) loot)
                .forEach(loot -> customTimedLoot.put(loot.getIdentifier(), loot));
        this.customTimedLootManager = new CustomTimedLootManager(customTimedLoot.values());
    }

    // The next two methods are responsible for dealing with the order in which we load loot tables
    // since now we have subtables as a feature
    private static void dfs(Map<String, List<String>> dependencies,
                            String identifier,
                            Map<String, Boolean> visited,
                            Map<String, Boolean> recursionStack,
                            List<String> result) {
        visited.put(identifier, true);
        recursionStack.put(identifier, true);
        for (String neighbor : dependencies.get(identifier)) {
            if (!visited.getOrDefault(neighbor, false)) {
                dfs(dependencies, neighbor, visited, recursionStack, result);
            } else if (recursionStack.get(neighbor)) {
                // A cycle is detected because the neighbor node is part of the recursion stack.
                throw new IllegalStateException("Circular dependency detected: " + identifier + " -> " + neighbor);
            }
        }
        recursionStack.put(identifier, false); // Remove the node from the recursion stack once it is finished.
        result.add(identifier);
    }

    private static List<String> topologicalSort(Map<String, List<String>> dependencies) {
        Map<String, Boolean> visited = new HashMap<>();
        Map<String, Boolean> recursionStack = new HashMap<>();
        List<String> result = new ArrayList<>();
        for (String identifier : dependencies.keySet()) {
            if (!visited.getOrDefault(identifier, false)) {
                dfs(dependencies, identifier, visited, recursionStack, result);
            }
        }
        Collections.reverse(result); // reverse to get the correct order
        return result;
    }

    private LootTable parseLootTable(FileConfiguration config) {
        String identifier = Objects.requireNonNull(config.getString("identifier"));
        List<LootTable.LootItem> items = new ArrayList<>();
        ConfigurationSection itemsSection = config.getConfigurationSection("items");
        assert itemsSection != null;
        for (String templateID : itemsSection.getKeys(false)) {
            ConfigurationSection section = itemsSection.getConfigurationSection(templateID);
            assert section != null;
            int weight = section.getInt("weight");
            if (weight == 0) throw new IllegalArgumentException("Loot table has invalid weight for " + templateID);
            if (templateID.equalsIgnoreCase("script")) {
                items.add(new LootTable.LootScriptItem(weight));
            } else {
                if (!RunicItemsAPI.isTemplate(templateID))
                    throw new IllegalArgumentException("Loot table has invalid template ID " + templateID);
                int minStackSize = section.getInt("stack-size.min", 1);
                int maxStackSize = section.getInt("stack-size.max", 1);
                items.add(new LootTable.LootItem(templateID, weight, minStackSize, maxStackSize));
            }
        }

        if (config.isList("subtables")) {
            List<String> subtables = config.getStringList("subtables");
            for (String subtable : subtables) {
                LootTable sub = lootTables.get(subtable);
                Objects.requireNonNull(sub, "Subtable " + subtable + " doesn't exist");
                items.addAll(sub.getItems());
            }
        }

        return new LootTable(identifier, items);
    }

    private LootChestTemplate parseLootChestTemplate(FileConfiguration config) throws IllegalArgumentException {
        String identifier = Objects.requireNonNull(config.getString("identifier"));

        ConfigurationSection lootTableSection = config.getConfigurationSection("loot-tables");

        if (lootTableSection == null) {
            throw new IllegalArgumentException("");
        }

        List<LootChestTemplate.Table> tables = new ArrayList<>();

        for (String key : lootTableSection.getKeys(false)) {
            if (!lootTables.containsKey(key)) {
                throw new IllegalArgumentException("Loot chest template " + identifier + " has invalid loot table " + key);
            }

            int minCount = lootTableSection.getInt(key + ".count.min");
            int maxCount = lootTableSection.getInt(key + ".count.max");

            if (minCount == 0 || maxCount == 0) {
                throw new IllegalArgumentException("Loot chest template " + identifier + " must have count.min and count.max on " + key + "!");
            }

            tables.add(new LootChestTemplate.Table(this.lootTables.get(key), minCount, maxCount));
        }

        return new LootChestTemplate(identifier, 27, tables.toArray(LootChestTemplate.Table[]::new));
    }

    private RegenerativeLootChest parseRegenerativeLootChest(ConfigurationSection section, String chestID) {
        if (section == null)
            throw new IllegalArgumentException("Chest " + chestID + " does not exist in configuration");
        ConfigurationSection locationSection = section.getConfigurationSection("location");
        if (locationSection == null)
            throw new IllegalArgumentException("Location section missing for chest " + chestID);
        String world = locationSection.getString("world");
        int x = locationSection.getInt("x");
        int y = locationSection.getInt("y");
        int z = locationSection.getInt("z");
        BlockFace direction = BlockFace.valueOf(Objects.requireNonNull(locationSection.getString("direction")).toUpperCase());
        if (world == null || x == 0 || y == 0 || z == 0)
            throw new IllegalArgumentException("One or more location values missing for chest " + chestID);
        Location location = new Location(Objects.requireNonNull(Bukkit.getWorld(world), "World " + world + " does not exist for chest " + chestID), x, y, z);
        String chestTemplate = section.getString("template");
        int regenerationTime = section.getInt("regeneration-time");
        ConfigurationSection itemLevelSection = section.getConfigurationSection("item-level");
        if (itemLevelSection == null)
            throw new IllegalArgumentException("Item level section missing for chest " + chestID);
        int minLevel = section.getInt("min-level");
        int itemMinLevel = itemLevelSection.getInt("min");
        int itemMaxLevel = itemLevelSection.getInt("max");
        if (itemMinLevel == 0 || itemMaxLevel == 0)
            throw new IllegalArgumentException("One or more item level values missing for chest " + chestID);
        String title = section.getString("title");
        if (title == null)
            throw new IllegalArgumentException("Title missing for chest " + chestID);
        if (chestTemplate == null || regenerationTime == 0)
            throw new IllegalArgumentException("Chest template or regeneration time missing for chest " + chestID);
        LootChestConditions conditions;
        if (section.isConfigurationSection("conditions")) {
            conditions = LootChestConditions.loadFromConfig(Objects.requireNonNull(section.getConfigurationSection("conditions")));
        } else {
            conditions = new LootChestConditions();
        }

        LootChestModel model = LootChestModel.getModel(section.getString("model"));

        return new RegenerativeLootChest(
                new LootChestPosition(location, direction),
                lootChestTemplates.get(chestTemplate),
                conditions,
                minLevel,
                itemMinLevel, itemMaxLevel,
                regenerationTime,
                ColorUtil.format(title),
                model != null ? model.getModelID() : null);
    }

    private TimedLoot parseTimedLoot(FileConfiguration config) {
        ConfigurationSection section = config.getConfigurationSection("chest");
        if (section == null) throw new IllegalArgumentException("Timed loot needs to have chest key!");
        ConfigurationSection locationSection = section.getConfigurationSection("location");
        if (locationSection == null)
            throw new IllegalArgumentException("Location section missing for timed loot " + config.getName());
        String world = locationSection.getString("world");
        int x = locationSection.getInt("x");
        int y = locationSection.getInt("y");
        int z = locationSection.getInt("z");
        BlockFace direction = BlockFace.valueOf(Objects.requireNonNull(locationSection.getString("direction")).toUpperCase());
        if (world == null || x == 0 || y == 0 || z == 0)
            throw new IllegalArgumentException("One or more location values missing for timed loot " + config.getName());
        Location location = new Location(Objects.requireNonNull(Bukkit.getWorld(world), "Timed loot world " + world + " does not exist!"), x, y, z);
        String chestTemplate = section.getString("template");
        ConfigurationSection itemLevelSection = section.getConfigurationSection("item-level");
        if (itemLevelSection == null)
            throw new IllegalArgumentException("Item level section missing for timed loot " + config.getName());
        int minLevel = section.getInt("min-level");
        int itemMinLevel = itemLevelSection.getInt("min");
        int itemMaxLevel = itemLevelSection.getInt("max");
        if (itemMinLevel == 0 || itemMaxLevel == 0)
            throw new IllegalArgumentException("One or more item level values missing for timed loot " + config.getName());
        String title = section.getString("title");
        if (title == null)
            throw new IllegalArgumentException("Title missing for timed loot " + config.getName());
        if (chestTemplate == null)
            throw new IllegalArgumentException("Chest template or regeneration time missing for timed loot " + config.getName());
        LootChestConditions conditions;
        if (section.isConfigurationSection("conditions")) {
            conditions = LootChestConditions.loadFromConfig(Objects.requireNonNull(section.getConfigurationSection("conditions")));
        } else {
            conditions = new LootChestConditions();
        }
        int duration = section.getInt("duration");
        if (duration == 0)
            throw new IllegalArgumentException("Timed loot chest " + config.getName() + " missing duration!");
        ConfigurationSection hologramLocationSection = section.getConfigurationSection("hologram.location");
        if (hologramLocationSection == null)
            throw new IllegalArgumentException("Hologram location section missing for timed loot " + config.getName());
        String holoWorld = hologramLocationSection.getString("world");
        double holoX = hologramLocationSection.getDouble("x");
        double holoY = hologramLocationSection.getDouble("y");
        double holoZ = hologramLocationSection.getDouble("z");
        if (holoWorld == null || holoX == 0 || holoY == 0 || holoZ == 0)
            throw new IllegalArgumentException("One or more hologram-location values missing for timed loot " + config.getName());
        Location hologramLocation = new Location(Objects.requireNonNull(Bukkit.getWorld(holoWorld), "Timed loot hologram location world " + holoWorld + " does not exist!"), holoX, holoY, holoZ);
        List<String> hologramLines = section.getStringList("hologram.lines");
        if (hologramLines.isEmpty())
            throw new IllegalArgumentException("Hologram.lines missing from timed loot " + config.getName());
        String type = config.getString("type");

        LootChestModel model = LootChestModel.getModel(section.getString("model"));

        TimedLootChest chest = new TimedLootChest(
                new LootChestPosition(location, direction),
                lootChestTemplates.get(chestTemplate),
                conditions,
                minLevel,
                itemMinLevel, itemMaxLevel,
                ColorUtil.format(title),
                duration,
                hologramLocation,
                (hologram, time) -> {
                    hologram.getLines().clear();
                    for (String line : hologramLines) {
                        hologram.getLines().appendText(ColorUtil.format(line.replaceAll("%time%", String.valueOf(time))));
                    }
                },
                model != null ? model.getModelID() : null);

        if ("boss".equalsIgnoreCase(type)) {
            String mmID = config.getString("boss.mm-id");
            double lootDamageThreshold = config.getDouble("boss.loot-damage-threshold");

            if (mmID == null) {
                throw new IllegalArgumentException("Boss timed loot chest missing boss.mm-id");
            }

            String completeWorld = config.getString("boss.location.world");
            double completeX = config.getDouble("boss.location.x");
            double completeY = config.getDouble("boss.location.y");
            double completeZ = config.getDouble("boss.location.z");
            float completeYaw = (float) config.getDouble("boss.location.yaw");
            float completePitch = (float) config.getDouble("boss.location.pitch");

            if (completeWorld == null || completeX == 0 || completeZ == 0 || Bukkit.getWorld(completeWorld) == null) {
                throw new IllegalArgumentException("Boss timed loot chest missing boss.location data");
            }

            int lootRange = config.getInt("boss.loot-range", DEFAULT_MAX_BOSS_LOOT_RANGE);

            return new BossTimedLoot(chest, mmID, lootDamageThreshold, lootRange, new Location(Bukkit.getWorld(completeWorld), completeX, completeY, completeZ, completeYaw, completePitch));
        } else if ("custom".equalsIgnoreCase(type)) {
            String identifier = config.getString("custom.identifier");

            if (identifier == null) {
                throw new IllegalArgumentException("Timed loot missing identifier " + config.getName());
            }

            return new CustomTimedLoot(chest, identifier);
        } else throw new IllegalArgumentException("Bad type for timed loot chest: " + type);
    }

    @Override
    public void createRegenerativeLootChest(@NotNull RegenerativeLootChest regenerativeLootChest) {
        int id = nextRegenLootChestID;
        regenLootChestsConfig.set("chests." + id + ".location.world", Objects.requireNonNull(regenerativeLootChest.getPosition().getLocation().getWorld()).getName());
        regenLootChestsConfig.set("chests." + id + ".location.x", regenerativeLootChest.getPosition().getLocation().getBlockX());
        regenLootChestsConfig.set("chests." + id + ".location.y", regenerativeLootChest.getPosition().getLocation().getBlockY());
        regenLootChestsConfig.set("chests." + id + ".location.z", regenerativeLootChest.getPosition().getLocation().getBlockZ());
        regenLootChestsConfig.set("chests." + id + ".location.direction", regenerativeLootChest.getPosition().getDirection().toString());
        regenLootChestsConfig.set("chests." + id + ".template", regenerativeLootChest.getLootChestTemplate().getIdentifier());
        regenLootChestsConfig.set("chests." + id + ".regeneration-time", regenerativeLootChest.getRegenerationTime());
        regenLootChestsConfig.set("chests." + id + ".min-level", regenerativeLootChest.getMinLevel());
        regenLootChestsConfig.set("chests." + id + ".item-level.min", regenerativeLootChest.getItemMinLevel());
        regenLootChestsConfig.set("chests." + id + ".item-level.max", regenerativeLootChest.getItemMaxLevel());
        regenLootChestsConfig.set("chests." + id + ".title", regenerativeLootChest.getInventoryTitle());
        regenLootChestsConfig.set("chests." + id + ".model-id", regenerativeLootChest.getModelID());
        if (regenerativeLootChest.getConditions().getConditionsList().size() > 0) {
            ConfigurationSection conditionsSection = regenLootChestsConfig.createSection("chests." + id + ".conditions");
            regenerativeLootChest.getConditions().addToConfig(conditionsSection);
        }
        nextRegenLootChestID++;
        regenLootChestsConfig.set("next-id", nextRegenLootChestID);
        saveRegenLootChestConfigAsync();
        regenLootChests.put(regenerativeLootChest.getPosition().getLocation(), regenerativeLootChest);
        this.clientLootManager.addRegenerativeLootChest(regenerativeLootChest);
    }

    @Override
    public void deleteRegenerativeLootChest(@NotNull RegenerativeLootChest regenerativeLootChest) {
        try {
            for (String key : Objects.requireNonNull(regenLootChestsConfig.getConfigurationSection("chests")).getKeys(false)) {
                if (Objects.requireNonNull(regenLootChestsConfig.getString("chests." + key + ".location.world"))
                        .equalsIgnoreCase(Objects.requireNonNull(regenerativeLootChest.getPosition().getLocation().getWorld()).getName())
                        && regenLootChestsConfig.getInt("chests." + key + ".location.x") == regenerativeLootChest.getPosition().getLocation().getBlockX()
                        && regenLootChestsConfig.getInt("chests." + key + ".location.y") == regenerativeLootChest.getPosition().getLocation().getBlockY()
                        && regenLootChestsConfig.getInt("chests." + key + ".location.z") == regenerativeLootChest.getPosition().getLocation().getBlockZ()) {
                    regenLootChestsConfig.set("chests." + key, null);
                    saveRegenLootChestConfigAsync();
                    this.regenLootChests.remove(regenerativeLootChest.getPosition().getLocation());
                    this.clientLootManager.deleteRegenerativeLootChest(regenerativeLootChest);
                    return;
                }
            }
        } catch (Exception exception) {
            Bukkit.getLogger().log(Level.SEVERE, "ERROR removing regenerative loot chest from config:");
            throw exception;
        }
        throw new IllegalArgumentException("Cannot remove regenerative loot chest from config: none exists at given location!");
    }

    @Override
    public boolean isLootChestTemplate(@NotNull String identifier) {
        return lootChestTemplates.containsKey(identifier);
    }

    @NotNull
    @Override
    public Collection<LootChestTemplate> getChestTemplates() {
        return lootChestTemplates.values();
    }

    @NotNull
    @Override
    public Collection<RegenerativeLootChest> getRegenerativeLootChests() {
        return regenLootChests.values();
    }

    @Override
    public void displayTimedLootChest(@NotNull Player player, @NotNull TimedLootChest chest) {
        clientLootManager.displayTimedLootChest(player, chest);
    }

    @Nullable
    @Override
    public BossTimedLoot getBossTimedLoot(@NotNull String mmID) {
        return bossTimedLoot.get(mmID);
    }

    @Nullable
    @Override
    public CustomTimedLoot getCustomTimedLoot(@NotNull String identifier) {
        return customTimedLoot.get(identifier);
    }

    @Override
    public BossTimedLootManager getBossTimedLootManager() {
        return this.bossTimedLootManager;
    }

    @Override
    public CustomTimedLootManager getCustomTimedLootManager() {
        return this.customTimedLootManager;
    }

    private void saveRegenLootChestConfigAsync() {
        Bukkit.getScheduler().runTaskAsynchronously(RunicItems.getInstance(), () -> {
            try {
                regenLootChestsConfig.save(regenLootChestsFile);
            } catch (IOException exception) {
                throw new RuntimeException(exception);
            }
        });
    }

    @Override
    public LootTable getLootTable(@NotNull String identifier) {
        if (!lootTables.containsKey(identifier))
            throw new IllegalArgumentException("Cannot get loot table " + identifier + " because it does not exist");
        return lootTables.get(identifier);
    }

    @Override
    public Collection<LootTable> getLootTables() {
        return lootTables.values();
    }

    @Override
    public LootChestTemplate getLootChestTemplate(@NotNull String identifier) {
        if (!lootChestTemplates.containsKey(identifier))
            throw new IllegalArgumentException("Cannot get loot chest template " + identifier + " because it does nto exist");
        return lootChestTemplates.get(identifier);
    }

    @Override
    @Nullable
    public RegenerativeLootChest getRegenerativeLootChest(@NotNull Location location) {
        return regenLootChests.get(location);
    }

    /**
     * Models the loot chest can have
     */
    public enum LootChestModel {
        WATER("chest_aqua"),
        BONE("chest_bone"),
        FOREST("chest_forest"),
        GOLDEN("chest_golden"),
        ICE("chest_ice"),
        LIGHT("chest_light"),
        MAGMA("chest_magma"),
        SHADOW("chest_shadow"),
        WIND("chest_wind"),
        NORMAL("chest_wooden");

        private final String modelID;

        LootChestModel(@NotNull String modelID) {
            this.modelID = modelID;
        }

        @Nullable
        @Contract("null -> null")
        public static LootChestModel getModel(@Nullable String model) {
            if (model == null) {
                return null;
            }

            for (LootChestModel lootChestModel : LootChestModel.values()) {
                if (lootChestModel.name().equalsIgnoreCase(model)) {
                    return lootChestModel;
                }
            }

            return null;
        }

        @NotNull
        public String getModelID() {
            return this.modelID;
        }
    }
}