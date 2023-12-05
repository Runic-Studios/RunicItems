package com.runicrealms.plugin.runicitems.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CatchUnknown;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Private;
import com.runicrealms.plugin.common.util.ColorUtil;
import com.runicrealms.plugin.runicitems.RunicItemsAPI;
import com.runicrealms.plugin.runicitems.item.template.RunicItemWeaponTemplate;
import com.runicrealms.plugin.runicitems.weaponskin.ui.WeaponAppearancesUI;
import org.bukkit.Material;
import org.bukkit.entity.Player;

@CommandAlias("weaponry")
@Private
@CommandPermission("runic.default")
public class WeaponryCommand extends BaseCommand {

    @Default
    @CatchUnknown
    public void onCommand(Player player) {
        if (player.getInventory().getItemInMainHand().getType() == Material.AIR
                || (!(RunicItemsAPI.getItemStackTemplate(player.getInventory().getItemInMainHand()) instanceof RunicItemWeaponTemplate))) {
            player.sendMessage(ColorUtil.format("&cYou must be holding a weapon to modify your weapon appearances!"));
            return;
        }
        player.openInventory(new WeaponAppearancesUI(player).getInventory());
    }

}
