package com.runicrealms.runicitems.weaponskin;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class WeaponSkinHolder {

    private final UUID owner;
    private final Map<WeaponSkin, Boolean> skins = new HashMap<>(); // map of skin, active

    public WeaponSkinHolder(UUID owner) {
        this.owner = owner;
    }

    public boolean ownsSkin(WeaponSkin skin) {
        return skins.containsKey(skin);
    }

    public void setSkinActive(WeaponSkin skin, boolean active) {
        skins.put(skin, active);
    }

    public boolean skinActive(WeaponSkin skin) {
        return skins.containsKey(skin) && skins.get(skin);
    }

    public UUID getOwner() {
        return this.owner;
    }

    public Set<WeaponSkin> getSkinsOwned() {
        return skins.keySet();
    }

}
