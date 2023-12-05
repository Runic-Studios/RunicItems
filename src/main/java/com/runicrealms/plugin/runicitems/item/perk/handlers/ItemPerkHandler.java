package com.runicrealms.plugin.runicitems.item.perk.handlers;

import com.runicrealms.plugin.runicitems.RunicItems;
import com.runicrealms.plugin.runicitems.item.perk.ActiveItemPerksChangeEvent;
import com.runicrealms.plugin.runicitems.item.perk.ItemPerk;
import com.runicrealms.plugin.runicitems.item.perk.ItemPerkManager;
import com.runicrealms.plugin.runicitems.item.perk.ItemPerkType;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public abstract class ItemPerkHandler implements Listener {

    protected String identifier;
    protected ItemPerkType type;

    public ItemPerkHandler(String identifier, int maxStacks) {
        this.identifier = identifier;
        this.type = new ItemPerkType(identifier, maxStacks);
        ItemPerkManager.registerItemPerk(this);
        Bukkit.getPluginManager().registerEvents(this, RunicItems.getInstance());
    }

    public ItemPerkType getType() {
        return this.type;
    }

    /**
     * Called when the number of stacks of this item perk changes.
     *
     * @param stacks Number of stacks, 0 indicates no stacks (item perk deactivated).
     */
    protected abstract void onChange(int stacks);

    @EventHandler
    public void onActiveItemPerksChange(ActiveItemPerksChangeEvent event) {
        boolean success = false;
        for (ItemPerk perk : event.getNewItemPerks()) {
            if (perk.getType() == type) {
                success = true;
                boolean found = false;
                for (ItemPerk oldPerk : event.getOldItemPerks()) {
                    if (oldPerk.getType() == type) {
                        if (oldPerk.getStacks() == perk.getStacks()) return;
                        onChange(perk.getStacks());
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    onChange(perk.getStacks());
                    break;
                }
                break;
            }
        }
        if (!success) {
            for (ItemPerk oldPerk : event.getOldItemPerks()) {
                if (oldPerk.getType() == type) {
                    onChange(0);
                    break;
                }
            }
        }
    }

}
