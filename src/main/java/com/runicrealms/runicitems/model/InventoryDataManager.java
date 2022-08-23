package com.runicrealms.runicitems.model;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.character.api.CharacterQuitEvent;
import com.runicrealms.plugin.character.api.CharacterSelectEvent;
import com.runicrealms.plugin.database.PlayerMongoData;
import com.runicrealms.plugin.database.PlayerMongoDataSection;
import com.runicrealms.plugin.database.event.MongoSaveEvent;
import com.runicrealms.plugin.redis.RedisManager;
import com.runicrealms.plugin.redis.RedisUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.UUID;

public class InventoryDataManager implements Listener {

    /**
     * Important: fire BEFORE loading stats
     *
     * @param event when the character is first selected, before it is loaded
     */
    @EventHandler(priority = EventPriority.LOW) // fires early
    public void onCharacterLoad(CharacterSelectEvent event) {
        int slot = event.getCharacterData().getBaseCharacterInfo().getSlot();
        loadInventoryData(event.getPlayer().getUniqueId(), slot);
    }

    /**
     * Important: fire BEFORE loading stats
     *
     * @param event when the character is first selected, before it is loaded
     */
    @EventHandler(priority = EventPriority.LOW) // fires early
    public void onCharacterQuit(CharacterQuitEvent event) {
        int slot = event.getSlot();
        InventoryData inventoryData = loadInventoryData(event.getPlayer().getUniqueId(), slot);
        inventoryData.writeToJedis(RunicCore.getRedisManager().getJedisPool());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onDatabaseSave(MongoSaveEvent event) {
        InventoryData inventoryData = loadInventoryData(event.getUuid(), event.getSlot());
        inventoryData.writeToMongo(event.getMongoData(), event.getSlot());
    }

    /**
     * Checks redis to see if the currently selected character's inventory is in session storage.
     * And if it is, returns the InventoryData object
     *
     * @param uuid of player to check
     * @param slot of the character
     * @return a InventoryData object if it is found in redis
     */
    public InventoryData checkRedisForInventoryData(UUID uuid, Integer slot) {
        JedisPool jedisPool = RunicCore.getRedisManager().getJedisPool();
        try (Jedis jedis = jedisPool.getResource()) { // try-with-resources to close the connection for us // todo: there should be one jedis opened on the select
            jedis.auth(RedisManager.REDIS_PASSWORD);
            String key = QuestProfileData.getQuestPointsJedisKey(uuid, slot); // if it has this key, it has loaded the quest data
            if (jedis.exists(key)) {
                Bukkit.broadcastMessage(ChatColor.GREEN + "redis inventory data found, building data from redis");
                jedis.expire(key, RedisUtil.EXPIRE_TIME);
                return new QuestProfileData(uuid, slot, jedis);
            }
        }
        Bukkit.broadcastMessage(ChatColor.RED + "redis quest data not found");
        return null;
    }

    /**
     * Creates an InventoryData object. Tries to build it from session storage (Redis) first,
     * then falls back to Mongo
     *
     * @param uuid of player who is attempting to load their data
     * @param slot the slot of the character
     */
    public InventoryData loadInventoryData(UUID uuid, Integer slot) {
        // Step 1: check if quest data is cached in redis
        InventoryData inventoryData = checkRedisForInventoryData(uuid, slot);
        if (inventoryData != null) return inventoryData;
        // Step 2: check mongo documents
        PlayerMongoData playerMongoData = new PlayerMongoData(uuid.toString());
        PlayerMongoDataSection character = playerMongoData.getCharacter(slot);
        return new InventoryData(uuid, character, slot);
    }
}
