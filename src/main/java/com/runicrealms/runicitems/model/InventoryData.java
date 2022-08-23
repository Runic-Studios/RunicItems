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
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class InventoryData implements SessionData {
    private static final int PLAYER_INVENTORY_SIZE = 41;
    private final UUID uuid;
    private final Integer slot;
    private final ItemStack[] contents;

    /**
     * Build the character's inventory data from mongo
     *
     * @param uuid      of the player
     * @param character the character section of their mongo document
     * @param slot      of the character
     */
    public InventoryData(UUID uuid, PlayerMongoDataSection character, int slot) {
        Player player;
        this.uuid = uuid;
        this.slot = slot;
        this.contents = new ItemStack[PLAYER_INVENTORY_SIZE];
        if (character.has("inventory")) {
            Data data = character.getSection("inventory");
            for (String key : data.getKeys()) {
                if (!key.equalsIgnoreCase("type")) {
                    try {
                        RunicItem item = ItemLoader.loadItem(data.getSection(key), DupeManager.getNextItemId());
                        if (item != null)
                            contents[Integer.parseInt(key)] = item.generateItem();
                    } catch (Exception exception) {
                        Bukkit.getLogger().log
                                (
                                        Level.WARNING,
                                        "[RunicItems] ERROR loading item " + key + " for player uuid" + this.uuid
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
    public InventoryData(UUID uuid, int slot, Jedis jedis) {
        this.uuid = uuid;
        this.slot = slot;
        this.contents = new ItemStack[PLAYER_INVENTORY_SIZE];
        for (int i = 0; i < contents.length; i++) {
            if (jedis.exists(parentKey + ":" + questNoUserData.getQuestID())) {
                Map<String, String> questDataMap = jedis.hgetAll(parentKey + ":" + questNoUserData.getQuestID()); // get the parent key of the section
                RunicItem item; // todo: RunicItem item = loadItemFromJedis();
                contents[i] = item.generateItem();
            }
        }
    }

    /**
     * @param jedisPool
     */
    public void writeToJedis(JedisPool jedisPool) {
        Bukkit.broadcastMessage("writing inventory data to jedis");
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.auth(RedisManager.REDIS_PASSWORD);
//            String key = getJedisKey(this.uuid, RunicCoreAPI.getCharacterSlot(this.uuid));
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

    public ItemStack[] getContents() {
        return contents;
    }
}
