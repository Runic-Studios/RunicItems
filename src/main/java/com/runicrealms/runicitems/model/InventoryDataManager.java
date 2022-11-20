package com.runicrealms.runicitems.model;

import com.runicrealms.plugin.api.RunicCoreAPI;
import com.runicrealms.plugin.character.api.CharacterQuitEvent;
import com.runicrealms.plugin.character.api.CharacterSelectEvent;
import com.runicrealms.plugin.database.PlayerMongoData;
import com.runicrealms.plugin.database.PlayerMongoDataSection;
import com.runicrealms.plugin.database.event.MongoSaveEvent;
import org.bukkit.Bukkit;
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
    public void onCharacterQuit(CharacterQuitEvent event) {
        InventoryData inventoryData = new InventoryData
                (
                        event.getPlayer().getUniqueId(),
                        event.getSlot(),
                        event.getPlayer().getInventory().getContents()
                );
        inventoryData.writeToJedis(event.getJedis());
    }

    /**
     * Saves player skill tree info when the server is shut down
     * for EACH alt the player has used during the runtime of this server.
     * Works even if the player is now entirely offline
     */
    @EventHandler
    public void onDatabaseSave(MongoSaveEvent event) {
        for (UUID uuid : event.getPlayersToSave().keySet()) {
            for (int characterSlot : event.getPlayersToSave().get(uuid).getCharactersToSave()) {
                PlayerMongoData playerMongoData = event.getPlayersToSave().get(uuid).getPlayerMongoData();
                saveInventoryToMongo(uuid, characterSlot, event.getJedis(), playerMongoData);
            }
        }
        event.markPluginSaved("items");
    }

    /**
     * Removes the inventory section from the player's mongo data, then writes to it with their updated inventory
     * from either jedis (if they're offline) or their inventory (if they're online)
     * Also saves the character mongo section
     *
     * @param uuid            of the player to save
     * @param slot            of the character
     * @param jedis           the jedis resource (of the MongoSaveEvent)
     * @param playerMongoData the player's mongo data
     */
    private void saveInventoryToMongo(UUID uuid, int slot, Jedis jedis, PlayerMongoData playerMongoData) {
        Player onlinePlayer = Bukkit.getPlayer(uuid);
        InventoryData inventoryData;
        if (onlinePlayer != null) {
            inventoryData = new InventoryData(uuid, slot, onlinePlayer.getInventory().getContents());
        } else {
            inventoryData = loadInventoryData(uuid, slot, jedis); // from redis
        }
        inventoryData.writeToMongo(playerMongoData, slot);
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
        String key = InventoryData.getJedisKey(uuid, slot);
        if (!RunicCoreAPI.getNestedJedisKeys(key, jedis).isEmpty()) {
            return new InventoryData(uuid, slot, jedis);
        }
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
