package com.runicrealms.runicitems;

import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class RunicItemsJDAListener extends ListenerAdapter {

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        DupeManager.setupJda();
    }

}
