package com.runicrealms.runicitems.model;

import co.aikar.taskchain.TaskChain;
import co.aikar.taskchain.TaskChainAbortAction;
import com.runicrealms.plugin.rdb.RunicDatabase;
import com.runicrealms.plugin.rdb.event.CharacterDeleteEvent;
import com.runicrealms.plugin.rdb.event.CharacterLoadedEvent;
import com.runicrealms.plugin.rdb.event.CharacterQuitEvent;
import com.runicrealms.plugin.rdb.event.CharacterSelectEvent;
import com.runicrealms.plugin.rdb.event.MongoSaveEvent;
import com.runicrealms.plugin.rdb.model.CharacterField;
import com.runicrealms.runicitems.RunicItems;
import com.runicrealms.runicitems.api.DataAPI;
import com.runicrealms.runicitems.item.RunicItem;
import org.bson.types.ObjectId;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import redis.clients.jedis.Jedis;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

public class InventoryDataManager implements DataAPI, Listener {
    public static final TaskChainAbortAction<Player, String, ?> CONSOLE_LOG = new TaskChainAbortAction<>() {
        public void onAbort(TaskChain<?> chain, Player player, String message) {
            Bukkit.getLogger().log(Level.SEVERE, message);
        }
    };
    private static final int REDIS_TASK_PERIOD = 30; // Seconds
    /*
    Only used to load data during CharacterLoadEvent. Not used during session
     */
    private final Map<UUID, InventoryData> inventoryDataMap = new HashMap<>();

    public InventoryDataManager() {
        Bukkit.getPluginManager().registerEvents(this, RunicItems.getInstance());
        startInventorySaveTask();
    }

    @Override
    public InventoryData loadInventoryData(UUID uuid, int slotToLoad) {
        // Step 1: Check if inventory data is cached in redis
        try (Jedis jedis = RunicDatabase.getAPI().getRedisAPI().getNewJedisResource()) {
            Set<String> redisInventoryList = RunicDatabase.getAPI().getRedisAPI().getRedisDataSet(uuid, "itemData", jedis);
            boolean dataInRedis = RunicDatabase.getAPI().getRedisAPI().determineIfDataInRedis(redisInventoryList, slotToLoad);
            if (dataInRedis) {
                return new InventoryData(uuid, jedis, slotToLoad);
            }
            // Step 2: Check the mongo database
            Query query = new Query();
            query.addCriteria(Criteria.where(CharacterField.PLAYER_UUID.getField()).is(uuid));
            MongoTemplate mongoTemplate = RunicDatabase.getAPI().getDataAPI().getMongoTemplate();
            List<InventoryData> results = mongoTemplate.find(query, InventoryData.class);
            if (results.size() > 0) {
                InventoryData result = results.get(0);
                result.writeToJedis(jedis);
                return result;
            }
            // Step 3: If no data is found, we create some data and save it to the collection
            InventoryData newData = new InventoryData
                    (
                            new ObjectId(),
                            uuid,
                            slotToLoad,
                            new RunicItem[41]
                    );
            newData.addDocumentToMongo();
            newData.writeToJedis(jedis);
            return newData;
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onCharacterDelete(CharacterDeleteEvent event) {
        event.getPluginsToDeleteData().add("items");
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        int slot = event.getSlot();
        // Removes player from the save task
        try (Jedis jedis = RunicDatabase.getAPI().getRedisAPI().getNewJedisResource()) {
            String database = RunicDatabase.getAPI().getDataAPI().getMongoDatabase().getName();
            jedis.srem(database + ":markedForSave:items", String.valueOf(player.getUniqueId()));
        }
        // 1. Delete from Redis
        String database = RunicDatabase.getAPI().getDataAPI().getMongoDatabase().getName();
        try (Jedis jedis = RunicDatabase.getAPI().getRedisAPI().getNewJedisResource()) {
            jedis.srem(database + ":" + uuid + ":itemData", String.valueOf(slot));
        }
        // 2. Delete from Mongo
        Query query = new Query();
        query.addCriteria(Criteria.where(CharacterField.PLAYER_UUID.getField()).is(uuid));
        Update update = new Update();
        update.unset("contentsMap." + slot);
        MongoTemplate mongoTemplate = RunicDatabase.getAPI().getDataAPI().getMongoTemplate();
        mongoTemplate.updateFirst(query, update, InventoryData.class);
        // 3. Mark this deletion as complete
        event.getPluginsToDeleteData().remove("items");
    }

    /**
     * Sets the player's gear FIRST (so that stats load)
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onCharacterLoad(CharacterLoadedEvent event) {
        InventoryData inventoryData = inventoryDataMap.get(event.getPlayer().getUniqueId());
        ItemStack[] contents = inventoryData.generateItemStackContents(event.getCharacterSelectEvent().getSlot());
        if (contents != null) { // Inventory exists for this character
            event.getPlayer().getInventory().setContents(contents);
        }
//        Bukkit.getLogger().severe("INVENTORY IS SET HERE 1");
        inventoryDataMap.remove(event.getPlayer().getUniqueId());
    }

    /**
     * Important: fire BEFORE loading stats
     *
     * @param event when the character is first selected, before it is loaded
     */
    @EventHandler(priority = EventPriority.LOW) // fires early
    public void onCharacterQuit(CharacterQuitEvent event) {
        Player player = event.getPlayer();
        int slot = event.getSlot();
        saveInventory(player, slot);
    }

    /**
     * Important: fire BEFORE loading stats
     *
     * @param event when the character is first selected, before it is loaded
     */
    @EventHandler(priority = EventPriority.LOW) // fires early
    public void onCharacterSelect(CharacterSelectEvent event) {
        // For benchmarking
        long startTime = System.nanoTime();
        event.getPluginsToLoadData().add("inventory");
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        int slot = event.getSlot();
        TaskChain<?> chain = RunicItems.newChain();
        chain
                .asyncFirst(() -> loadInventoryData(uuid, slot))
                .abortIfNull(CONSOLE_LOG, player, "RunicItems failed to load on select!")
                .syncLast(inventoryData -> {
                    inventoryDataMap.put(event.getPlayer().getUniqueId(), inventoryData);
                    event.getPluginsToLoadData().remove("inventory");
                    // Calculate elapsed time
                    long endTime = System.nanoTime();
                    long elapsedTime = endTime - startTime;
                    // Log elapsed time in milliseconds
                    Bukkit.getLogger().info("RunicItems took: " + elapsedTime / 1_000_000 + "ms to load");
                })
                .execute();
    }

    /**
     * Saves player skill tree info when the server is shut down
     * for EACH alt the player has used during the runtime of this server.
     * Works even if the player is now entirely offline
     */
    @EventHandler
    public void onDatabaseSave(MongoSaveEvent event) {
        // Shutdown JDA
        RunicItems.getJda().shutdownNow();
        // Cancel the task timer
        RunicItems.getMongoTask().getTask().cancel();
        // Manually save all data (flush players marked for save)
        RunicItems.getMongoTask().saveAllToMongo(() -> event.markPluginSaved("items"));
    }

    private void saveInventory(Player player, int slot) {
        UUID uuid = player.getUniqueId();
        TaskChain<?> chain = RunicItems.newChain();
        chain
                .asyncFirst(() -> loadInventoryData(uuid, slot))
                .abortIfNull(CONSOLE_LOG, player, "RunicItems failed to save on quit!")
                .sync(inventoryData -> {
                    // Sync current inventory to object from Redis/Mongo (ensure they match)
                    inventoryData.getContentsMap().put(slot,
                            InventoryData.getRunicItemContents(player.getInventory().getContents()));
                    return inventoryData;
                    // todo: prevent them from logging in during this time
                })
                .asyncLast(inventoryData -> {
                    try (Jedis jedis = RunicDatabase.getAPI().getRedisAPI().getNewJedisResource()) {
                        inventoryData.writeToJedis(jedis);
                    }
                })
                .execute();
    }

    /**
     * Periodic task to save player items
     */
    private void startInventorySaveTask() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(RunicItems.getInstance(), () -> {
            for (UUID uuid : RunicDatabase.getAPI().getCharacterAPI().getLoadedCharacters()) {
                Player player = Bukkit.getPlayer(uuid);
                if (player == null) continue; // Player not online
                int slot = RunicDatabase.getAPI().getCharacterAPI().getCharacterSlot(uuid);
                saveInventory(player, slot);
            }
        }, 0, REDIS_TASK_PERIOD * 20L);
    }

}
