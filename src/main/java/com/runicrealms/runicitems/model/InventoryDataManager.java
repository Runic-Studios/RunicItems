package com.runicrealms.runicitems.model;

import com.runicrealms.plugin.character.api.CharacterQuitEvent;
import com.runicrealms.plugin.character.api.CharacterSelectEvent;
import com.runicrealms.plugin.database.PlayerMongoData;
import com.runicrealms.plugin.database.PlayerMongoDataSection;
import com.runicrealms.plugin.database.event.MongoSaveEvent;
import com.runicrealms.plugin.redis.RedisUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import redis.clients.jedis.Jedis;

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
        InventoryData inventoryData = loadInventoryData(event.getPlayer().getUniqueId(), slot, event.getJedis());
        event.getPlayer().getInventory().setContents(inventoryData.getContents());
    }

    /**
     * Important: fire BEFORE loading stats
     *
     * @param event when the character is first selected, before it is loaded
     */
    @EventHandler(priority = EventPriority.LOW) // fires early
    // todo: this is bugged during an rstop. rstops needs to kick everyone BEFORE the mongo save event
    public void onCharacterQuit(CharacterQuitEvent event) {
        InventoryData inventoryData = new InventoryData
                (
                        event.getPlayer().getUniqueId(),
                        event.getSlot(),
                        event.getPlayer().getInventory().getContents()
                );
        inventoryData.writeToJedis(event.getJedis());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onDatabaseSave(MongoSaveEvent event) {
        Player onlinePlayer = Bukkit.getPlayer(event.getUuid());
        InventoryData inventoryData;
        if (onlinePlayer != null) {
            inventoryData = new InventoryData(event.getUuid(), event.getSlot(), onlinePlayer.getInventory().getContents());
        } else {
            inventoryData = loadInventoryData(event.getUuid(), event.getSlot(), event.getJedis()); // from redis
        }
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
    public InventoryData checkRedisForInventoryData(UUID uuid, Integer slot, Jedis jedis) {
        String key = InventoryData.getJedisKey(uuid, slot); // if it has this key, it has loaded the quest data
        if (jedis.exists(key)) {
            Bukkit.broadcastMessage(ChatColor.GREEN + "redis inventory data found, building data from redis");
            jedis.expire(key, RedisUtil.EXPIRE_TIME);
            return new InventoryData(uuid, slot, jedis);
        }
        Bukkit.broadcastMessage(ChatColor.RED + "redis quest data not found");
        return null;
    }

    /**
     * Creates an InventoryData object. Tries to build it from session storage (Redis) first,
     * then falls back to Mongo
     *
     * @param uuid  of player who is attempting to load their data
     * @param slot  the slot of the character
     * @param jedis the jedis resource
     * @return an InventoryData object
     */
    public InventoryData loadInventoryData(UUID uuid, Integer slot, Jedis jedis) {
        // Step 1: check if quest data is cached in redis
        InventoryData inventoryData = checkRedisForInventoryData(uuid, slot, jedis);
        if (inventoryData != null) return inventoryData;
        // Step 2: check mongo documents
        PlayerMongoData playerMongoData = new PlayerMongoData(uuid.toString());
        PlayerMongoDataSection character = playerMongoData.getCharacter(slot);
        return new InventoryData(uuid, character, slot);
    }
}
