package com.runicrealms.plugin.runicitems.model;

import com.runicrealms.plugin.rdb.RunicDatabase;
import com.runicrealms.plugin.rdb.model.SessionDataMongo;
import com.runicrealms.plugin.runicitems.item.RunicItem;
import com.runicrealms.plugin.runicitems.RunicItemsAPI;
import org.bson.types.ObjectId;
import org.bukkit.inventory.ItemStack;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.HashMap;
import java.util.UUID;

@SuppressWarnings("unused")
@Document(collection = "items")
public class InventoryData implements SessionDataMongo {
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

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

}
