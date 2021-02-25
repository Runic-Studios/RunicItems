package com.runicrealms.runicitems;

import com.runicrealms.plugin.character.api.CharacterLoadEvent;
import com.runicrealms.plugin.database.Data;
import com.runicrealms.plugin.database.event.CacheSaveEvent;
import com.runicrealms.runicitems.config.ItemLoader;
import com.runicrealms.runicitems.item.*;
import com.runicrealms.runicitems.item.template.*;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;


public class ItemManager implements Listener {

    @EventHandler
    public void onCharacterJoin(CharacterLoadEvent event) {
        if (RunicItems.isDatabaseLoadingEnabled()) {
            if (event.getPlayerCache().getMongoData().has("character." + event.getSlot() + ".inventory")) {
                Data data = event.getPlayerCache().getMongoData().getSection("character." + event.getSlot() + ".inventory");
                for (String key : data.getKeys()) {
                    event.getPlayer().getInventory().setItem(Integer.parseInt(key), ItemLoader.loadItem(data.getSection(key), DupeManager.getNextItemId()).generateItem());
                }
            }
        }
    }

    @EventHandler
    public void onCacheSave(CacheSaveEvent event) {
        if (RunicItems.isDatabaseLoadingEnabled()) {
            ItemStack[] contents = event.getPlayer().getInventory().getContents();
            for (int i = 0; i < contents.length; i++) {
                if (contents[i] != null) {
                    RunicItem runicItem = getRunicItemFromItemStack(contents[i]);
                    if (runicItem != null) {
                        runicItem.addToData(event.getMongoDataSection().getSection("inventory." + i));
                    }
                }
            }
        }
    }

    public static RunicItem getRunicItemFromItemStack(ItemStack itemStack) {
        NBTItem nbtItem = new NBTItem(itemStack);
        if ((!nbtItem.hasNBTData()) || (nbtItem.hasKey("template-id"))) return null;
        RunicItemTemplate template = TemplateManager.getTemplateFromId(nbtItem.getString("template-id"));
        if (template == null) return null;
        if (template instanceof RunicItemArmorTemplate) {
            return RunicItemArmor.getFromItemStack(itemStack);
        } else if (template instanceof RunicItemArtifactTemplate) {
            return RunicItemArtifact.getFromItemStack(itemStack);
        } else if (template instanceof RunicItemBookTemplate) {
            return RunicItemBook.getFromItemStack(itemStack);
        } else if (template instanceof RunicItemGenericTemplate) {
            return RunicItemGeneric.getFromItemStack(itemStack);
        } else if (template instanceof RunicItemOffhandTemplate) {
            return RunicItemOffhand.getFromItemStack(itemStack);
        } else if (template instanceof RunicItemWeaponTemplate) {
            return RunicItemWeapon.getFromItemStack(itemStack);
        }
        return null;
    }

}