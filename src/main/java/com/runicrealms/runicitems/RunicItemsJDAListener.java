package com.runicrealms.runicitems;

import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class RunicItemsJDAListener extends ListenerAdapter {

    @Override
    public void onReady(ReadyEvent event) {
        DupeManager.setupJda();
    }

}
