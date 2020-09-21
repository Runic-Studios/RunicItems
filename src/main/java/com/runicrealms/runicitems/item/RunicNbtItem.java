package com.runicrealms.runicitems.item;

import com.runicrealms.runicitems.Plugin;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public abstract class RunicNbtItem {

    public abstract ItemStack getItemStack();

    public void setNbt(String key, Byte value) {
        ItemMeta meta = this.getItemStack().getItemMeta();
        meta.getPersistentDataContainer().set(new NamespacedKey(Plugin.getInstance(), key), PersistentDataType.BYTE, value);
        this.getItemStack().setItemMeta(meta);
    }

    public void setNbt(String key, byte[] value) {
        ItemMeta meta = this.getItemStack().getItemMeta();
        meta.getPersistentDataContainer().set(new NamespacedKey(Plugin.getInstance(), key), PersistentDataType.BYTE_ARRAY, value);
        this.getItemStack().setItemMeta(meta);
    }

    public void setNbt(String key, Double value) {
        ItemMeta meta = this.getItemStack().getItemMeta();
        meta.getPersistentDataContainer().set(new NamespacedKey(Plugin.getInstance(), key), PersistentDataType.DOUBLE, value);
        this.getItemStack().setItemMeta(meta);
    }

    public void setNbt(String key, Float value) {
        ItemMeta meta = this.getItemStack().getItemMeta();
        meta.getPersistentDataContainer().set(new NamespacedKey(Plugin.getInstance(), key), PersistentDataType.FLOAT, value);
        this.getItemStack().setItemMeta(meta);
    }

    public void setNbt(String key, Integer value) {
        ItemMeta meta = this.getItemStack().getItemMeta();
        meta.getPersistentDataContainer().set(new NamespacedKey(Plugin.getInstance(), key), PersistentDataType.INTEGER, value);
        this.getItemStack().setItemMeta(meta);
    }

    public void setNbt(String key, int[] value) {
        ItemMeta meta = this.getItemStack().getItemMeta();
        meta.getPersistentDataContainer().set(new NamespacedKey(Plugin.getInstance(), key), PersistentDataType.INTEGER_ARRAY, value);
        this.getItemStack().setItemMeta(meta);
    }

    public void setNbt(String key, Long value) {
        ItemMeta meta = this.getItemStack().getItemMeta();
        meta.getPersistentDataContainer().set(new NamespacedKey(Plugin.getInstance(), key), PersistentDataType.LONG, value);
        this.getItemStack().setItemMeta(meta);
    }

    public void setNbt(String key, long[] value) {
        ItemMeta meta = this.getItemStack().getItemMeta();
        meta.getPersistentDataContainer().set(new NamespacedKey(Plugin.getInstance(), key), PersistentDataType.LONG_ARRAY, value);
        this.getItemStack().setItemMeta(meta);
    }

    public void setNbt(String key, Short value) {
        ItemMeta meta = this.getItemStack().getItemMeta();
        meta.getPersistentDataContainer().set(new NamespacedKey(Plugin.getInstance(), key), PersistentDataType.SHORT, value);
        this.getItemStack().setItemMeta(meta);
    }

    public void setNbt(String key, String value) {
        ItemMeta meta = this.getItemStack().getItemMeta();
        meta.getPersistentDataContainer().set(new NamespacedKey(Plugin.getInstance(), key), PersistentDataType.STRING, value);
        this.getItemStack().setItemMeta(meta);
    }

    public void setNbt(String key, PersistentDataContainer value) {
        ItemMeta meta = this.getItemStack().getItemMeta();
        meta.getPersistentDataContainer().set(new NamespacedKey(Plugin.getInstance(), key), PersistentDataType.TAG_CONTAINER, value);
        this.getItemStack().setItemMeta(meta);
    }

    public Byte getNbtByte(String key) {
        return this.getItemStack().getItemMeta().getPersistentDataContainer().get(new NamespacedKey(Plugin.getInstance(), key), PersistentDataType.BYTE);
    }

    public byte[] getNbtByteArray(String key) {
        return this.getItemStack().getItemMeta().getPersistentDataContainer().get(new NamespacedKey(Plugin.getInstance(), key), PersistentDataType.BYTE_ARRAY);
    }

    public Double getNbtDouble(String key) {
        return this.getItemStack().getItemMeta().getPersistentDataContainer().get(new NamespacedKey(Plugin.getInstance(), key), PersistentDataType.DOUBLE);
    }

    public Float getNbtFloat(String key) {
        return this.getItemStack().getItemMeta().getPersistentDataContainer().get(new NamespacedKey(Plugin.getInstance(), key), PersistentDataType.FLOAT);
    }

    public Integer getNbtInteger(String key) {
        return this.getItemStack().getItemMeta().getPersistentDataContainer().get(new NamespacedKey(Plugin.getInstance(), key), PersistentDataType.INTEGER);
    }

    public int[] getNbtIntegerArray(String key) {
        return this.getItemStack().getItemMeta().getPersistentDataContainer().get(new NamespacedKey(Plugin.getInstance(), key), PersistentDataType.INTEGER_ARRAY);
    }

    public Long getNbtLong(String key) {
        return this.getItemStack().getItemMeta().getPersistentDataContainer().get(new NamespacedKey(Plugin.getInstance(), key), PersistentDataType.LONG);
    }

    public long[] getNbtLongArray(String key) {
        return this.getItemStack().getItemMeta().getPersistentDataContainer().get(new NamespacedKey(Plugin.getInstance(), key), PersistentDataType.LONG_ARRAY);
    }

    public Short getNbtShort(String key) {
        return this.getItemStack().getItemMeta().getPersistentDataContainer().get(new NamespacedKey(Plugin.getInstance(), key), PersistentDataType.SHORT);
    }

    public String getNbtString(String key) {
        return this.getItemStack().getItemMeta().getPersistentDataContainer().get(new NamespacedKey(Plugin.getInstance(), key), PersistentDataType.STRING);
    }

    public PersistentDataContainer getNbtTagContainer(String key) {
        return this.getItemStack().getItemMeta().getPersistentDataContainer().get(new NamespacedKey(Plugin.getInstance(), key), PersistentDataType.TAG_CONTAINER);
    }

    public boolean hasNbtByte(String key) {
        return this.getItemStack().getItemMeta().getPersistentDataContainer().has(new NamespacedKey(Plugin.getInstance(), key), PersistentDataType.BYTE);
    }

    public boolean hasNbtByteArray(String key) {
        return this.getItemStack().getItemMeta().getPersistentDataContainer().has(new NamespacedKey(Plugin.getInstance(), key), PersistentDataType.BYTE_ARRAY);
    }

    public boolean hasNbtDouble(String key) {
        return this.getItemStack().getItemMeta().getPersistentDataContainer().has(new NamespacedKey(Plugin.getInstance(), key), PersistentDataType.DOUBLE);
    }

    public boolean hasNbtFloat(String key) {
        return this.getItemStack().getItemMeta().getPersistentDataContainer().has(new NamespacedKey(Plugin.getInstance(), key), PersistentDataType.FLOAT);
    }

    public boolean hasNbtInteger(String key) {
        return this.getItemStack().getItemMeta().getPersistentDataContainer().has(new NamespacedKey(Plugin.getInstance(), key), PersistentDataType.INTEGER);
    }

    public boolean hasNbtIntegerArray(String key) {
        return this.getItemStack().getItemMeta().getPersistentDataContainer().has(new NamespacedKey(Plugin.getInstance(), key), PersistentDataType.INTEGER_ARRAY);
    }

    public boolean hasNbtLong(String key) {
        return this.getItemStack().getItemMeta().getPersistentDataContainer().has(new NamespacedKey(Plugin.getInstance(), key), PersistentDataType.LONG);
    }

    public boolean hasNbtLongArray(String key) {
        return this.getItemStack().getItemMeta().getPersistentDataContainer().has(new NamespacedKey(Plugin.getInstance(), key), PersistentDataType.LONG_ARRAY);
    }

    public boolean hasNbtShort(String key) {
        return this.getItemStack().getItemMeta().getPersistentDataContainer().has(new NamespacedKey(Plugin.getInstance(), key), PersistentDataType.SHORT);
    }

    public boolean hasNbtString(String key) {
        return this.getItemStack().getItemMeta().getPersistentDataContainer().has(new NamespacedKey(Plugin.getInstance(), key), PersistentDataType.STRING);
    }

    public boolean hasNbtTagContainer(String key) {
        return this.getItemStack().getItemMeta().getPersistentDataContainer().has(new NamespacedKey(Plugin.getInstance(), key), PersistentDataType.TAG_CONTAINER);
    }


}
