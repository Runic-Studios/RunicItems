package com.runicrealms.runicitems.model;

import com.runicrealms.plugin.rdb.RunicDatabase;
import com.runicrealms.plugin.rdb.model.SessionDataMongo;
import com.runicrealms.plugin.rdb.model.SessionDataNested;
import com.runicrealms.runicitems.DupeManager;
import com.runicrealms.runicitems.RunicItemsAPI;
import com.runicrealms.runicitems.config.ItemLoader;
import com.runicrealms.runicitems.item.RunicItem;
import org.bson.types.ObjectId;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.params.ScanParams;
import redis.clients.jedis.resps.ScanResult;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

@Document(collection = "items")
public class InventoryData implements SessionDataMongo, SessionDataNested {
    @Id
    private ObjectId id;
    @Field("playerUuid")
    private UUID uuid;
    private HashMap<Integer, RunicItem[]> contentsMap = new HashMap<>();

    @SuppressWarnings("unused")
    public InventoryData() {
        // Default constructor for Spring
    }

    /**
     * Build the character's inventory data from their current inventory contents (used for saving)
     * <p>
     *
     * @param id       of the document to prevent replication
     * @param uuid     of the player
     * @param slot     of the character
     * @param contents the character's current inventory contents
     */
    public InventoryData(ObjectId id, UUID uuid, Integer slot, RunicItem[] contents) {
        this.id = id;
        this.uuid = uuid;
        this.contentsMap.put(slot, contents);
    }

    /**
     * Build the character's inventory data from jedis
     *
     * @param uuid       of the player
     * @param jedis      the jedis resource
     * @param slotToLoad the slot of the character to load (-1 for all slots)
     */
    public InventoryData(UUID uuid, Jedis jedis, int slotToLoad) {
        this.uuid = uuid;
        String database = RunicDatabase.getAPI().getDataAPI().getMongoDatabase().getName();
        if (slotToLoad == -1) { // Load all slots
            for (int slot = 1; slot <= RunicDatabase.getAPI().getDataAPI().getMaxCharacterSlot(); slot++) {
                if (jedis.smembers(database + ":" + uuid + ":itemData").contains(String.valueOf(slot))) {
                    loadInventoryForSlot(slot, jedis);
                }
            }
            // Load a specific slot
        } else {
            if (jedis.smembers(database + ":" + uuid + ":itemData").contains(String.valueOf(slotToLoad))) {
                loadInventoryForSlot(slotToLoad, jedis);
            }
        }
    }

    /**
     * Inventory data is nested in redis, so here's a handy method to get the key
     *
     * @param uuid of the player
     * @param slot of the character
     * @return a string representing the location in jedis
     */
    public static String getJedisKey(UUID uuid, int slot) {
        return uuid + ":character:" + slot + ":inventory";
    }

    /**
     * When player logs out, converts their inventory to a RunicItem array
     *
     * @param itemStacks the players inventory
     * @return an array of RunicItems
     */
    public static RunicItem[] getRunicItemContents(ItemStack[] itemStacks) {
        RunicItem[] contents = new RunicItem[41];
        for (int i = 0; i < itemStacks.length; i++) {
            if (itemStacks[i] == null) {
                continue;
            }
            contents[i] = RunicItemsAPI.getRunicItemFromItemStack(itemStacks[i]);
        }
        return contents;
    }

    public static void removeAllKeysWithPrefix(Jedis jedis, String keyPrefix) {
        String cursor = "0";
        ScanParams scanParams = new ScanParams().match(keyPrefix + "*").count(100);

        do {
            // Scan the keyspace for keys matching the prefix
            ScanResult<String> scanResult = jedis.scan(cursor, scanParams);
            cursor = scanResult.getCursor();
            List<String> keys = scanResult.getResult();

            // Delete the matching keys using pipelining
            if (!keys.isEmpty()) {
                Pipeline pipeline = jedis.pipelined();
                keys.forEach(pipeline::del);
                pipeline.sync();
            }

        } while (!cursor.equals("0"));
    }

    @SuppressWarnings("unchecked")
    @Override
    public InventoryData addDocumentToMongo() {
        MongoTemplate mongoTemplate = RunicDatabase.getAPI().getDataAPI().getMongoTemplate();
        return mongoTemplate.save(this);
    }

    /**
     * This method should be used only once on login to convert the player's persistent
     * RunicItem data to ItemStacks
     *
     * @param slot of the character
     * @return an array of ItemStacks that can be set to player's inventory
     */
    public ItemStack[] generateItemStackContents(int slot) {
        ItemStack[] itemStacks = new ItemStack[41];
        RunicItem[] contents = contentsMap.get(slot);
        if (contents == null) return new ItemStack[41]; // New characters
        for (int i = 0; i < contents.length; i++) {
            if (contents[i] == null) {
                continue;
            }
            itemStacks[i] = contents[i].generateItem();
        }
        return itemStacks;
    }

    public HashMap<Integer, RunicItem[]> getContentsMap() {
        return contentsMap;
    }

    public void setContentsMap(HashMap<Integer, RunicItem[]> contentsMap) {
        this.contentsMap = contentsMap;
    }

    @Override
    public Map<String, String> getDataMapFromJedis(Jedis jedis, Object o, int... ints) {
        return null;
    }

    @Override
    public List<String> getFields() {
        return null;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    @Override
    public Map<String, String> toMap(Object nestedObject) {
        RunicItem runicItem = (RunicItem) nestedObject;
        return runicItem.addToRedis();
    }

    @Override
    public void writeToJedis(Jedis jedis, int... ignored) {
        Bukkit.getLogger().severe("WRITING ITEMS DATA");
        String database = RunicDatabase.getAPI().getDataAPI().getMongoDatabase().getName();
        // Remove all sub-keys to prevent duplication of items
        for (int slot : this.contentsMap.keySet()) {
            String key = getJedisKey(this.uuid, slot);
            removeAllKeysWithPrefix(jedis, database + ":" + key);
        }
        // Inform the server that this player should be saved to mongo on next task (jedis data is refreshed)
        Pipeline pipeline = jedis.pipelined();
        pipeline.sadd(database + ":" + "markedForSave:items", this.uuid.toString());
        for (int slot : this.contentsMap.keySet()) {
            // Ensure the system knows that there is data in redis
            pipeline.sadd(database + ":" + this.uuid + ":itemData", String.valueOf(slot));
            pipeline.expire(database + ":" + this.uuid + ":itemData", RunicDatabase.getAPI().getRedisAPI().getExpireTime());
            String key = getJedisKey(this.uuid, slot);
            RunicItem[] contents = contentsMap.get(slot);
            for (int i = 0; i < contents.length; i++) {
                if (contents[i] != null) { // There is an item
                    RunicItem runicItem = contents[i]; // We can properly map it to a RunicItem
                    if (runicItem != null) {
                        try {
                            pipeline.hmset(database + ":" + key + ":" + i, this.toMap(runicItem));
                            pipeline.expire(database + ":" + key + ":" + i, RunicDatabase.getAPI().getRedisAPI().getExpireTime());
                        } catch (Exception e) {
                            // Log the exception and/or handle it accordingly
                            Bukkit.getLogger().severe("Failed to serialize RunicItem for slot " + slot + ", index " + i + ": " + e.getMessage());
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        pipeline.sync(); // Sends Redis commands as a single command
    }

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    private void loadContentsFromRedis(RunicItem[] contents, String parentKey, Jedis jedis) {
        String database = RunicDatabase.getAPI().getDataAPI().getMongoDatabase().getName();
        for (int i = 0; i < contents.length; i++) {
            if (jedis.exists(database + ":" + parentKey + ":" + i)) {
                Map<String, String> itemDataMap = jedis.hgetAll(database + ":" + parentKey + ":" + i); // get all the item data
                try {
                    RunicItem item = ItemLoader.loadItem(itemDataMap, DupeManager.getNextItemId());
                    if (item != null)
                        contents[i] = item;
                } catch (Exception exception) {
                    Bukkit.getLogger().log
                            (
                                    Level.WARNING,
                                    "[RunicItems] ERROR loading item " + i + " from redis for player uuid" + this.uuid
                            );
                    exception.printStackTrace();
                }
            }
        }
    }

    private void loadInventoryForSlot(int slot, Jedis jedis) {
        this.contentsMap.put(slot, new RunicItem[41]);
        RunicItem[] contents = contentsMap.get(slot);
        String parentKey = getJedisKey(uuid, slot);
        loadContentsFromRedis(contents, parentKey, jedis);
    }

}
