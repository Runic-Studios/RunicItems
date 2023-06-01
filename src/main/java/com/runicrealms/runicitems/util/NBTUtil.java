package com.runicrealms.runicitems.util;

import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;

import java.util.HashSet;
import java.util.Set;

public class NBTUtil {

    public static Object getNBTObject(NBTItem nbtItem, String key) {
        if (!nbtItem.hasNBTData() || !nbtItem.hasKey(key))
            throw new IllegalArgumentException("NBTItem does not contain key \"" + key + "\"!");
        return switch (nbtItem.getType(key)) {
            case NBTTagByte -> nbtItem.getByte(key);
            case NBTTagByteArray -> nbtItem.getByteArray(key);
            case NBTTagCompound -> nbtItem.getCompound(key);
            case NBTTagDouble -> nbtItem.getDouble(key);
            case NBTTagFloat -> nbtItem.getFloat(key);
            case NBTTagInt -> nbtItem.getInteger(key);
            case NBTTagIntArray -> nbtItem.getIntArray(key);
            case NBTTagList -> nbtItem.getStringList(key);
            case NBTTagLong -> nbtItem.getLong(key);
            case NBTTagShort -> nbtItem.getShort(key);
            case NBTTagString -> nbtItem.getString(key);
            default -> throw new IllegalArgumentException("Unknown NBTItem key type \"" + key + "\"!");
        };
    }

    public static boolean isNBTSimilar(ItemStack itemOne, ItemStack itemTwo, boolean checkId, boolean checkLastCount) {
        if (itemOne.getType() != itemTwo.getType()) return false;

        NBTItem nbtItemOne = new NBTItem(itemOne);
        NBTItem nbtItemTwo = new NBTItem(itemTwo);

        if (!nbtItemOne.hasKey("weapon-skin") && !nbtItemTwo.hasKey("weapon-skin")) {
            if (itemOne.getItemMeta() instanceof Damageable && itemTwo.getItemMeta() instanceof Damageable) {
                if (((Damageable) itemOne.getItemMeta()).getDamage() != ((Damageable) itemTwo.getItemMeta()).getDamage())
                    return false;
            }
        }
        Set<String> keys = new HashSet<>(nbtItemOne.getKeys());
        keys.addAll(nbtItemTwo.getKeys());
        if (!checkId) {
            keys.remove("id");
        }
        if (!checkLastCount) {
            keys.remove("last-count");
        }
        keys.remove("Unbreakable");
        keys.remove("HideFlags");
        keys.remove("display");
        keys.remove("AttributeModifiers");
        keys.remove("isRI");
        keys.remove("CanDestroy"); // for gathering tools
        keys.remove("weapon-skin");
        try {
            for (String key : keys) {
                if (
                        !nbtItemOne.hasKey(key) ||
                                !nbtItemTwo.hasKey(key)) {
                    return false;
                }
                Object obj1 = getNBTObject(nbtItemOne, key);
                Object obj2 = getNBTObject(nbtItemTwo, key);
                if (obj1 != obj2 && !obj1.equals(obj2)) {
                    return false;
                }
            }
        } catch (Exception exception) {
            exception.printStackTrace();
            return false;
        }
        return true;
    }

}
