package com.runicrealms.runicitems;

import com.runicrealms.plugin.character.api.CharacterLoadEvent;
import com.runicrealms.plugin.character.api.CharacterQuitEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.Map;

public class PlayerInventoryManager implements Listener {

    private static Map<Player, PlayerInventory> playerInventories = new HashMap<Player, PlayerInventory>();

    @EventHandler
    public void onCharacterJoin(CharacterLoadEvent event) {
        playerInventories.put(event.getPlayer(), new PlayerInventory(event.getPlayer().getUniqueId().toString(), event.getSlot()));
    }

    @EventHandler
    public void onCharacterQuit(CharacterQuitEvent event) {
        Bukkit.getScheduler().runTaskAsynchronously(Plugin.getInstance(), () -> {
            playerInventories.get(event.getPlayer()).save();
            playerInventories.remove(event.getPlayer());
        });

    }

}
