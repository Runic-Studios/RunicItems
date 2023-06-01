package com.runicrealms.runicitems.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CatchUnknown;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import com.runicrealms.plugin.common.util.ColorUtil;
import com.runicrealms.runicitems.RunicItemsAPI;
import com.runicrealms.runicitems.item.template.RunicItemArtifactTemplate;
import com.runicrealms.runicitems.item.template.RunicItemWeaponTemplate;
import com.runicrealms.runicitems.weaponskin.gui.WeaponryGUI;
import org.bukkit.Material;
import org.bukkit.entity.Player;

@CommandAlias("weaponry")
public class WeaponryCommand extends BaseCommand {

    @Default
    @CatchUnknown
    public void onCommand(Player player) {
        if (player.getInventory().getItemInMainHand().getType() == Material.AIR
                || (!(RunicItemsAPI.getItemStackTemplate(player.getInventory().getItemInMainHand()) instanceof RunicItemWeaponTemplate)
                && !(RunicItemsAPI.getItemStackTemplate(player.getInventory().getItemInMainHand()) instanceof RunicItemArtifactTemplate))) {
            player.sendMessage(ColorUtil.format("&cYou must be holding a weapon while opening the weaponry!"));
            return;
        }
        player.openInventory(new WeaponryGUI(player).getInventory());
    }

}
