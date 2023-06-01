package com.runicrealms.runicitems.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CatchUnknown;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.Conditions;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Subcommand;
import com.runicrealms.plugin.common.util.ColorUtil;
import com.runicrealms.runicitems.RunicItems;
import com.runicrealms.runicitems.weaponskin.WeaponSkin;
import org.bukkit.entity.Player;

import java.util.stream.Collectors;

@CommandAlias("weaponskin")
@Conditions("is-op")
public class WeaponSkinCommand extends BaseCommand {

    @Default
    @CatchUnknown
    public void onCommand(Player player) {
        player.sendMessage(ColorUtil.format("&aUse: /weaponskin equip|unequip <skin-1> <skin-2> ..."));
    }

    @Subcommand("equip")
    @CommandCompletion("@weaponskins")
    public void onCommandEquip(Player player, String[] args) {
        for (String skinName : args) {
            WeaponSkin weaponSkin = RunicItems.getWeaponSkinAPI().getAllSkins()
                    .stream()
                    .filter((skin) -> skin.id().equalsIgnoreCase(skinName))
                    .findFirst().orElse(null);
            if (weaponSkin == null) {
                player.sendMessage(ColorUtil.format("&cFailed to find skin: " + skinName));
                continue;
            }
            if (!RunicItems.getWeaponSkinAPI().canActivateSkin(player, weaponSkin)) {
                player.sendMessage(ColorUtil.format("&cPlayer does not have permission for skin: " + skinName));
                continue;
            }
            RunicItems.getWeaponSkinAPI().activateSkin(player, weaponSkin);
            player.sendMessage(ColorUtil.format("&aLoaded weapon skin: " + skinName));
        }
    }

    @Subcommand("unequip")
    @CommandCompletion("@weaponskins")
    public void onCommandUnequip(Player player, String[] args) {
        for (String skinName : args) {
            WeaponSkin weaponSkin = RunicItems.getWeaponSkinAPI().getAllSkins()
                    .stream()
                    .filter((skin) -> skin.id().equalsIgnoreCase(skinName))
                    .findFirst().orElse(null);
            if (weaponSkin == null) {
                player.sendMessage(ColorUtil.format("&cFailed to find skin: " + skinName));
                continue;
            }
            RunicItems.getWeaponSkinAPI().deactivateSkin(player, weaponSkin);
            player.sendMessage(ColorUtil.format("&aUnequiped weapon skin: " + skinName));
        }
    }

    @Subcommand("list")
    public void onCommandList(Player player) {
        player.sendMessage(ColorUtil.format("&aAll skins: " + RunicItems.getWeaponSkinAPI().getAllSkins().stream().map(WeaponSkin::id).collect(Collectors.joining(", "))));
    }

}
