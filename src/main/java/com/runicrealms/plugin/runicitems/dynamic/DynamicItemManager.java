package com.runicrealms.plugin.runicitems.dynamic;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.runicrealms.plugin.runicitems.RunicItems;

public class DynamicItemManager extends PacketAdapter {
    public DynamicItemManager() {
        super(RunicItems.getInstance(), ListenerPriority.HIGHEST, PacketType.Play.Server.SET_SLOT, PacketType.Play.Server.WINDOW_ITEMS);
    }

    @Override
    public void onPacketSending(PacketEvent event) {
        if (event.getPacketType() == PacketType.Play.Server.SET_SLOT) {

        } else if (event.getPacketType() == PacketType.Play.Server.WINDOW_ITEMS) {

        }
    }

    public void

}
