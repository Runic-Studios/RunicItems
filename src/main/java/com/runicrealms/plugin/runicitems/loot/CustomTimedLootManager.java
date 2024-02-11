package com.runicrealms.plugin.runicitems.loot;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CatchUnknown;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Subcommand;
import com.runicrealms.plugin.runicitems.RunicItems;
import com.runicrealms.plugin.runicitems.loot.chest.CustomTimedLoot;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@CommandAlias("customloot")
public class CustomTimedLootManager extends BaseCommand {

    private final Map<String, CustomTimedLoot> customLoot = new HashMap<>();

    public CustomTimedLootManager(Collection<CustomTimedLoot> customTimedLoot) {
        for (CustomTimedLoot loot : customTimedLoot) {
            this.customLoot.put(loot.getIdentifier(), loot);
        }
        RunicItems.getCommandManager().registerCommand(this);
    }

    @Default
    @CatchUnknown
    public void onCommand(CommandSender sender) {
        sender.sendMessage(ChatColor.RED + "Usage: /customloot spawn <identifier> <player>");
    }

    @Subcommand("spawn")
    public void onSpawnCommand(CommandSender sender, String[] args) {
        if (args.length != 2) {
            onCommand(sender);
            return;
        }
        String identifier = args[0];
        String playerName = args[1];
        if (!customLoot.containsKey(identifier)) {
            sender.sendMessage(ChatColor.RED + "That is not a valid identifier of a custom loot file!");
            return;
        }
        Player player = Bukkit.getPlayer(playerName);
        if (player == null) {
            sender.sendMessage(ChatColor.RED + "That player is not online!");
            return;
        }
        RunicItems.getLootAPI().displayTimedLootChest(player, customLoot.get(identifier).getLootChest());
        if (sender instanceof Player) {
            sender.sendMessage(ChatColor.GREEN + "Displayed custom loot " + customLoot + " to player " + playerName);
        }
    }

}
