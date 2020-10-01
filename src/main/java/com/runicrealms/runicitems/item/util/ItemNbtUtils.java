package com.runicrealms.runicitems.item.util;

import com.runicrealms.runicitems.Plugin;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class ItemNbtUtils {

    public static void setNbt(ItemStack item, String key, Byte value) {
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(new NamespacedKey(Plugin.getInstance(), key), PersistentDataType.BYTE, value);
        item.setItemMeta(meta);
    }

    public static void setNbt(ItemStack item, String key, byte[] value) {
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(new NamespacedKey(Plugin.getInstance(), key), PersistentDataType.BYTE_ARRAY, value);
        item.setItemMeta(meta);
    }

    public static void setNbt(ItemStack item, String key, Double value) {
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(new NamespacedKey(Plugin.getInstance(), key), PersistentDataType.DOUBLE, value);
        item.setItemMeta(meta);
    }

    public static void setNbt(ItemStack item, String key, Float value) {
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(new NamespacedKey(Plugin.getInstance(), key), PersistentDataType.FLOAT, value);
        item.setItemMeta(meta);
    }

    public static void setNbt(ItemStack item, String key, Integer value) {
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(new NamespacedKey(Plugin.getInstance(), key), PersistentDataType.INTEGER, value);
        item.setItemMeta(meta);
    }

    public static void setNbt(ItemStack item, String key, int[] value) {
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(new NamespacedKey(Plugin.getInstance(), key), PersistentDataType.INTEGER_ARRAY, value);
        item.setItemMeta(meta);
    }

    public static void setNbt(ItemStack item, String key, Long value) {
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(new NamespacedKey(Plugin.getInstance(), key), PersistentDataType.LONG, value);
        item.setItemMeta(meta);
    }

    public static void setNbt(ItemStack item, String key, long[] value) {
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(new NamespacedKey(Plugin.getInstance(), key), PersistentDataType.LONG_ARRAY, value);
        item.setItemMeta(meta);
    }

    public static void setNbt(ItemStack item, String key, Short value) {
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(new NamespacedKey(Plugin.getInstance(), key), PersistentDataType.SHORT, value);
        item.setItemMeta(meta);
    }

    public static void setNbt(ItemStack item, String key, String value) {
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(new NamespacedKey(Plugin.getInstance(), key), PersistentDataType.STRING, value);
        item.setItemMeta(meta);
    }

    public static void setNbt(ItemStack item, String key, PersistentDataContainer value) {
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(new NamespacedKey(Plugin.getInstance(), key), PersistentDataType.TAG_CONTAINER, value);
        item.setItemMeta(meta);
    }

    public static Byte getNbtByte(ItemStack item, String key) {
        return item.getItemMeta().getPersistentDataContainer().get(new NamespacedKey(Plugin.getInstance(), key), PersistentDataType.BYTE);
    }

    public static byte[] getNbtByteArray(ItemStack item, String key) {
        return item.getItemMeta().getPersistentDataContainer().get(new NamespacedKey(Plugin.getInstance(), key), PersistentDataType.BYTE_ARRAY);
    }

    public static Double getNbtDouble(ItemStack item, String key) {
        return item.getItemMeta().getPersistentDataContainer().get(new NamespacedKey(Plugin.getInstance(), key), PersistentDataType.DOUBLE);
    }

    public static Float getNbtFloat(ItemStack item, String key) {
        return item.getItemMeta().getPersistentDataContainer().get(new NamespacedKey(Plugin.getInstance(), key), PersistentDataType.FLOAT);
    }

    public static Integer getNbtInteger(ItemStack item, String key) {
        return item.getItemMeta().getPersistentDataContainer().get(new NamespacedKey(Plugin.getInstance(), key), PersistentDataType.INTEGER);
    }

    public static int[] getNbtIntegerArray(ItemStack item, String key) {
        return item.getItemMeta().getPersistentDataContainer().get(new NamespacedKey(Plugin.getInstance(), key), PersistentDataType.INTEGER_ARRAY);
    }

    public static Long getNbtLong(ItemStack item, String key) {
        return item.getItemMeta().getPersistentDataContainer().get(new NamespacedKey(Plugin.getInstance(), key), PersistentDataType.LONG);
    }

    public static long[] getNbtLongArray(ItemStack item, String key) {
        return item.getItemMeta().getPersistentDataContainer().get(new NamespacedKey(Plugin.getInstance(), key), PersistentDataType.LONG_ARRAY);
    }

    public static Short getNbtShort(ItemStack item, String key) {
        return item.getItemMeta().getPersistentDataContainer().get(new NamespacedKey(Plugin.getInstance(), key), PersistentDataType.SHORT);
    }

    public static String getNbtString(ItemStack item, String key) {
        return item.getItemMeta().getPersistentDataContainer().get(new NamespacedKey(Plugin.getInstance(), key), PersistentDataType.STRING);
    }

    public static PersistentDataContainer getNbtTagContainer(ItemStack item, String key) {
        return item.getItemMeta().getPersistentDataContainer().get(new NamespacedKey(Plugin.getInstance(), key), PersistentDataType.TAG_CONTAINER);
    }

    public static boolean hasNbtByte(ItemStack item, String key) {
        return item.getItemMeta().getPersistentDataContainer().has(new NamespacedKey(Plugin.getInstance(), key), PersistentDataType.BYTE);
    }

    public static boolean hasNbtByteArray(ItemStack item, String key) {
        return item.getItemMeta().getPersistentDataContainer().has(new NamespacedKey(Plugin.getInstance(), key), PersistentDataType.BYTE_ARRAY);
    }

    public static boolean hasNbtDouble(ItemStack item, String key) {
        return item.getItemMeta().getPersistentDataContainer().has(new NamespacedKey(Plugin.getInstance(), key), PersistentDataType.DOUBLE);
    }

    public static boolean hasNbtFloat(ItemStack item, String key) {
        return item.getItemMeta().getPersistentDataContainer().has(new NamespacedKey(Plugin.getInstance(), key), PersistentDataType.FLOAT);
    }

    public static boolean hasNbtInteger(ItemStack item, String key) {
        return item.getItemMeta().getPersistentDataContainer().has(new NamespacedKey(Plugin.getInstance(), key), PersistentDataType.INTEGER);
    }

    public static boolean hasNbtIntegerArray(ItemStack item, String key) {
        return item.getItemMeta().getPersistentDataContainer().has(new NamespacedKey(Plugin.getInstance(), key), PersistentDataType.INTEGER_ARRAY);
    }

    public static boolean hasNbtLong(ItemStack item, String key) {
        return item.getItemMeta().getPersistentDataContainer().has(new NamespacedKey(Plugin.getInstance(), key), PersistentDataType.LONG);
    }

    public static boolean hasNbtLongArray(ItemStack item, String key) {
        return item.getItemMeta().getPersistentDataContainer().has(new NamespacedKey(Plugin.getInstance(), key), PersistentDataType.LONG_ARRAY);
    }

    public static boolean hasNbtShort(ItemStack item, String key) {
        return item.getItemMeta().getPersistentDataContainer().has(new NamespacedKey(Plugin.getInstance(), key), PersistentDataType.SHORT);
    }

    public static boolean hasNbtString(ItemStack item, String key) {
        return item.getItemMeta().getPersistentDataContainer().has(new NamespacedKey(Plugin.getInstance(), key), PersistentDataType.STRING);
    }

    public static boolean hasNbtTagContainer(ItemStack item, String key) {
        return item.getItemMeta().getPersistentDataContainer().has(new NamespacedKey(Plugin.getInstance(), key), PersistentDataType.TAG_CONTAINER);
    }


}
