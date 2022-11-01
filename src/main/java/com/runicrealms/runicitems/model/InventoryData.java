package com.runicrealms.runicitems.model;

import com.runicrealms.plugin.database.Data;
import com.runicrealms.plugin.database.MongoDataSection;
import com.runicrealms.plugin.database.PlayerMongoData;
import com.runicrealms.plugin.database.PlayerMongoDataSection;
import com.runicrealms.plugin.model.SessionDataNested;
import com.runicrealms.plugin.redis.RedisUtil;
import com.runicrealms.runicitems.DupeManager;
import com.runicrealms.runicitems.ItemManager;
import com.runicrealms.runicitems.config.ItemLoader;
import com.runicrealms.runicitems.item.RunicItem;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import redis.clients.jedis.Jedis;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class InventoryData implements SessionDataNested {
    private static final int PLAYER_INVENTORY_SIZE = 41;
    public static final String PATH_LOCATION = "inventory";
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
        String parentKey = getJedisKey(uuid, slot);
        for (int i = 0; i < contents.length; i++) {
            if (jedis.exists(parentKey + ":" + i)) {
                Map<String, String> itemDataMap = jedis.hgetAll(parentKey + ":" + i); // get all the item data
                try {
                    RunicItem item = ItemLoader.loadItem(itemDataMap, DupeManager.getNextItemId());
                    if (item != null)
                        contents[i] = item.generateItem();
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

    /**
     * Quests data is nested in redis, so here's a handy method to get the key
     *
     * @param uuid of the player
     * @param slot of the character
     * @return a string representing the location in jedis
     */
    public static String getJedisKey(UUID uuid, int slot) {
        return uuid + ":character:" + slot + ":" + PATH_LOCATION;
    }

    @Override
    public List<String> getFields() {
        return null;
    }

    @Override
    public Map<String, String> toMap(Object nestedObject) {
        RunicItem runicItem = (RunicItem) nestedObject;
        return runicItem.addToJedis();
    }

    @Override
    public Map<String, String> getDataMapFromJedis(Jedis jedis, Object o, int... ints) {
        return null;
    }

    /**
     * Write the character's inventory data to jedis
     *
     * @param jedis the jedis resource
     * @param slot  of the character
     */
    @Override
    public void writeToJedis(Jedis jedis, int... slot) {
        String key = getJedisKey(this.uuid, this.getSlot());
        RedisUtil.removeAllFromRedis(jedis, key); // removes all sub-keys
        for (int i = 0; i < contents.length; i++) {
            if (contents[i] != null) {
                RunicItem runicItem = ItemManager.getRunicItemFromItemStack(contents[i]);
                if (runicItem != null) {
                    jedis.hmset(key + ":" + i, this.toMap(runicItem));
                    jedis.expire(key + ":" + i, RedisUtil.EXPIRE_TIME);
                }
            }
        }
    }

    @Override
    public PlayerMongoData writeToMongo(PlayerMongoData playerMongoData, int... slot) {
        MongoDataSection character = playerMongoData.getCharacter(slot[0]);
        character.remove("inventory"); // reset the stored inventory section
        for (int i = 0; i < contents.length; i++) {
            if (contents[i] != null) {
                RunicItem runicItem = ItemManager.getRunicItemFromItemStack(contents[i]);
                if (runicItem != null)
                    runicItem.addToDataSection(character, "inventory." + i);
            }
        }
        return playerMongoData;
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
