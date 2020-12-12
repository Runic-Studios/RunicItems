package com.runicrealms.runicitems;

import com.runicrealms.plugin.database.MongoData;
import com.runicrealms.plugin.database.MongoDataSection;
import com.runicrealms.plugin.database.PlayerMongoData;
import com.runicrealms.runicitems.config.ItemLoader;
import com.runicrealms.runicitems.item.RunicItem;

import java.util.HashMap;
import java.util.Map;

public class PlayerInventory {

    private Map<Integer, RunicItem> items = new HashMap<Integer, RunicItem>();
    private int characterSlot;
    private MongoData mongoData;

    public PlayerInventory(String uuid, int characterSlot) { // DO NOT call this on the main thread!
        this.characterSlot = characterSlot;
        this.mongoData = new PlayerMongoData(uuid);
        MongoDataSection inventorySection = this.mongoData.getSection("character." + characterSlot + ".inventory");
        for (String key : inventorySection.getKeys()) {
            this.items.put(Integer.parseInt(key), ItemLoader.loadItem(inventorySection.getSection(key)));
        }
    }

    public void save() {
        for (Integer slot : this.items.keySet()) {
            this.items.get(slot).addToData(this.mongoData.getSection("character." + this.characterSlot + ".inventory." + slot));
        }
    }

    public Map<Integer, RunicItem> getItems() {
        return this.items;
    }

    public MongoData getMongoData() {
        return this.mongoData;
    }

}
