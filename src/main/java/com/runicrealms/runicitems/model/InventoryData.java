package com.runicrealms.runicitems.model;

import com.runicrealms.plugin.database.Data;
import com.runicrealms.plugin.database.MongoDataSection;
import com.runicrealms.plugin.database.PlayerMongoData;
import com.runicrealms.plugin.database.PlayerMongoDataSection;
import com.runicrealms.plugin.model.SessionData;
import com.runicrealms.plugin.redis.RedisManager;
import com.runicrealms.plugin.redis.RedisUtil;
import com.runicrealms.runicitems.DupeManager;
import com.runicrealms.runicitems.ItemManager;
import com.runicrealms.runicitems.config.ItemLoader;
import com.runicrealms.runicitems.item.RunicItem;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class InventoryData implements SessionData {
    private static final int PLAYER_INVENTORY_SIZE = 41;
    public static final String PATH_LOCATION = "inventory";
    public static final String DATA_LOCATION = "data";
    private final UUID uuid;
    private final Integer slot;
    private final ItemStack[] contents;

    /**
     * Build the character's inventory data from their current inventory contents (used for saving)
     *
     * @param uuid     of the player
     * @param slot     of the character
     * @param contents the character's current inventory contents
     */
    public InventoryData(UUID uuid, Integer slot, ItemStack[] contents) {
        this.uuid = uuid;
        this.slot = slot;
        this.contents = contents;
    }

    /**
     * Build the character's inventory data from mongo
     *
     * @param uuid      of the player
     * @param character the character section of their mongo document
     * @param slot      of the character
     */
    public InventoryData(UUID uuid, PlayerMongoDataSection character, int slot) {
        this.uuid = uuid;
        this.slot = slot;
        this.contents = new ItemStack[PLAYER_INVENTORY_SIZE];
        if (character.has("inventory")) {
            Data data = character.getSection("inventory");
            for (String key : data.getKeys()) {
                if (!key.equalsIgnoreCase("type")) { // todo: can probably remove
                    try {
                        RunicItem item = ItemLoader.loadItem(data.getSection(key), DupeManager.getNextItemId());
                        if (item != null)
                            contents[Integer.parseInt(key)] = item.generateItem();
                    } catch (Exception exception) {
                        Bukkit.getLogger().log
                                (
                                        Level.WARNING,
                                        "[RunicItems] ERROR loading item " + key + " from mongo for player uuid" + this.uuid
                                );
                        exception.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * Build the character's quest profile data from jedis
     *
     * @param uuid  of the player
     * @param slot  of the character
     * @param jedis the jedis resource
     */
//    public InventoryData(UUID uuid, int slot, Jedis jedis) {
//        this.uuid = uuid;
//        this.slot = slot;
//        this.contents = new ItemStack[PLAYER_INVENTORY_SIZE];
//        String parentKey = getJedisKey(uuid, slot);
//        for (int i = 0; i < contents.length; i++) {
//            if (jedis.exists(parentKey + ":" + i)) {
//                Map<String, String> itemDataMap = jedis.hgetAll(parentKey + ":" + i); // get all the item data
//                try {
//                    RunicItem item = ItemLoader.loadItem(itemDataMap, DupeManager.getNextItemId());
//                    if (item != null)
//                        contents[i] = item.generateItem();
//                } catch (Exception exception) {
//                    Bukkit.getLogger().log
//                            (
//                                    Level.WARNING,
//                                    "[RunicItems] ERROR loading item " + i + " from redis for player uuid" + this.uuid
//                            );
//                    exception.printStackTrace();
//                }
//            }
//        }
//    }

    /**
     * @param jedisPool
     */
    public void writeToJedis(JedisPool jedisPool) {
        Bukkit.broadcastMessage("writing inventory data to jedis");
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.auth(RedisManager.REDIS_PASSWORD);
            String key = getJedisKey(this.uuid, this.getSlot());
            jedis.set(key, "true"); // quick check to see if inventory data is written
            Map<Integer, Map<String, String>> itemDataMap = this.toItemMap();
            if (!itemDataMap.isEmpty()) {
                for (Integer itemSlot : itemDataMap.keySet()) {
                    if (itemDataMap.get(itemSlot) == null) continue;
                    jedis.hmset(key + ":" + itemSlot, itemDataMap.get(itemSlot));
                    jedis.expire(key + ":" + itemSlot, RedisUtil.EXPIRE_TIME);
                }
            }
        }
    }

    /**
     * Quests data is nested in redis, so here's a handy method to get the key
     *
     * @param uuid of the player
     * @param slot of the character
     * @return a string representing the location in jedis
     */
    public static String getJedisKey(UUID uuid, int slot) {
        return uuid + ":character:" + slot + ":" + PATH_LOCATION + ":" + DATA_LOCATION;
    }

    @Override
    public Map<String, String> toMap() {
        return null;
    }

    // todo: the parent interface method should be updated and split into 'SimpleSessionData' and 'NestedSessionData'. add a generic object parameter
    public Map<Integer, Map<String, String>> toItemMap() {
        Map<Integer, Map<String, String>> itemDataMap = new HashMap<>();
        for (int i = 0; i < contents.length; i++) {
            if (contents[i] != null) {
                RunicItem runicItem = ItemManager.getRunicItemFromItemStack(contents[i]);
                if (runicItem != null)
                    itemDataMap.put(i, runicItem.addToJedis());
            }
        }
        return itemDataMap;
    }

    @Override
    public void writeToMongo(PlayerMongoData playerMongoData, int... slot) {
        MongoDataSection character = playerMongoData.getCharacter(slot[0]);
        character.remove("inventory"); // reset the stored inventory section
        character.save();
        for (int i = 0; i < contents.length; i++) {
            if (contents[i] != null) {
                RunicItem runicItem = ItemManager.getRunicItemFromItemStack(contents[i]);
                if (runicItem != null)
                    runicItem.addToDataSection(character, "inventory." + i);
            }
        }
        character.save();
    }

    public UUID getUuid() {
        return uuid;
    }

    public Integer getSlot() {
        return slot;
    }

    public ItemStack[] getContents() {
        return contents;
    }
}
