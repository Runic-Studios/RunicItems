package com.runicrealms.runicitems.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CatchUnknown;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.Conditions;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.Syntax;
import com.runicrealms.runicitems.Plugin;
import com.runicrealms.runicitems.TemplateManager;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandAlias("ri|runicitems")
public class RunicItemCommand extends BaseCommand {

    private static final String PREFIX = "&5[RunicItems] &6» &r";

    public RunicItemCommand() {
        Plugin.getCommandManager().getCommandCompletions().registerAsyncCompletion("item-ids", context -> TemplateManager.getTemplates().keySet());
    }

    @Default
    @CatchUnknown
    @Subcommand("help|h")
    public void onCommandHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + "&dAvailable commands: "));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + "&7/runicitem help"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + "&7/runicitem get <item> [amount] [perfect-rolls]"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + "&7/runicitem give <player> <item> [amount] [perfect-rolls]"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + "&7/runicitem clear <player> [item] [amount]"));
    }

    @Subcommand("get")
    @Conditions("is-player")
    @Syntax("<item> [amount] [perfect-rolls]")
    @CommandCompletion("@item-ids @nothing true|false")
    public void onCommandGet(Player player) {

    }

    @Subcommand("give")
    @Syntax("<player> <item> [amount] [perfect-rolls]")
    @CommandCompletion("@players @item-ids @nothing true|false")
    public void onCommandGive(CommandSender sender) {

    }

    @Subcommand("clear|c")
    @Syntax("<player> [item] [amount]")
    @CommandCompletion("@players @item-ids @nothing")
    public void onCommandClear(CommandSender sender) {

    }

}
