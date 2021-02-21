package com.runicrealms.runicitems.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CatchUnknown;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.Conditions;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.Syntax;
import com.runicrealms.runicitems.ItemManager;
import com.runicrealms.runicitems.Plugin;
import com.runicrealms.runicitems.TemplateManager;
import com.runicrealms.runicitems.item.RunicItem;
import com.runicrealms.runicitems.item.template.RunicItemTemplate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@CommandAlias("ri|runicitems|runicitem")
public class RunicItemCommand extends BaseCommand {

    private static final String PREFIX = "&5[RunicItems] &6» &r";

    public RunicItemCommand() {
        Plugin.getCommandManager().getCommandCompletions().registerAsyncCompletion("item-ids", context -> TemplateManager.getTemplates().keySet());
    }

    @Default
    @CatchUnknown
    @Conditions("is-op")
    @Subcommand("help|h")
    public void onCommandHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + "&dAvailable commands: "));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + "&7/runicitem help"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + "&7/runicitem get <item> [amount]"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + "&7/runicitem give <player> <item> [amount]"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + "&7/runicitem clear <player> [item] [amount]"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + "&7/runicitem toggle-database &dWARNING - don't use if you don't know what this does!"));
    }

    @Subcommand("get")
    @Conditions("is-player|is-op")
    @Syntax("<item> [amount]")
    @CommandCompletion("@item-ids @nothing")
    public void onCommandGet(Player player, String[] args) {
        if (args.length == 0) { player.sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + "&dInvalid syntax! Please check &7/runicitem help")); return; }
        RunicItemTemplate template = TemplateManager.getTemplateFromId(args[0]);
        int count = 1;
        if (template == null) { player.sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + "&dThat item ID does not exist!")); return; }
        if (args.length >= 2) {
            if (isInt(args[1])) {
                count = Integer.parseInt(args[1]);
            } else { player.sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + "&dThat is not a valid amount!")); return; }
        }
        RunicItem item = template.generateItem(count);
        player.getInventory().addItem(item.getCurrentItem());
    }

    @Subcommand("give")
    @Conditions("is-op")
    @Syntax("<player> <item> [amount]")
    @CommandCompletion("@players @item-ids @nothing")
    public void onCommandGive(CommandSender sender, String[] args) {
        if (args.length < 2) { sender.sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + "&dInvalid syntax! Please check &7/runicitem help")); return; }
        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) { sender.sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + "&dThat is not a valid player!")); return; }
        RunicItemTemplate template = TemplateManager.getTemplateFromId(args[1]);
        int count = 1;
        if (template == null) { sender.sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + "&dThat item ID does not exist!")); return; }
        if (args.length >= 3) {
            if (!isInt(args[2])) { sender.sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + "&dThat is not a valid amount!")); return; }
            count = Integer.parseInt(args[2]);
            if (count < 1) { sender.sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + "&dThat is not a valid amount!")); return; }
        }
        RunicItem item = template.generateItem(count);
        target.getInventory().addItem(item.getCurrentItem());
    }

    @Subcommand("clear|c")
    @Conditions("is-op")
    @Syntax("<player> [item] [amount]")
    @CommandCompletion("@players @item-ids @nothing")
    public void onCommandClear(CommandSender sender, String[] args) {
        if (args.length < 1) { sender.sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + "&dInvalid syntax! Please check &7/runciitem help")); return; }
        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) { sender.sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + "&dThat is not a valid player!")); return; }
        RunicItemTemplate template = null;
        if (args.length >= 2) {
            template = TemplateManager.getTemplateFromId(args[1]);
            if (template == null) { sender.sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + "&dThat item ID does not exist!")); return; }
        }
        int amount = -1;
        if (args.length >= 3) {
            if (!isInt(args[2])) { sender.sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + "&dThat is not a valid amount!")); return; }
            amount = Integer.parseInt(args[2]);
            if (amount < 1) { sender.sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + "&dThat is not a valid amount!")); return; }
        }
        int amountRemoved = 0;
        ItemStack[] contents = target.getInventory().getContents();
        for (int i = 0; i < contents.length; i++) {
            if (contents[i] != null && contents[i].getType() != Material.AIR) {
                if (amount == -1 || amountRemoved < amount) {
                    RunicItem item = ItemManager.getItemFromItemStack(contents[i]);
                    if (item == null) {
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + "&dError remove items!"));
                        return;
                    }
                    boolean removeItem = false;
                    if (template == null) {
                        removeItem = true;
                    } else if (item.getTemplateId().equalsIgnoreCase(template.getId())) {
                        removeItem = true;
                    }
                    if (removeItem) {
                        if (contents[i].getAmount() <= amount - amountRemoved || amount == -1) {
                            amountRemoved += contents[i].getAmount();
                            target.getInventory().setItem(i, new ItemStack(Material.AIR));
                        } else {
                            amountRemoved += amount - amountRemoved;
                            target.getInventory().getItem(i).setAmount(target.getInventory().getItem(i).getAmount() - (amount - amountRemoved));
                        }
                    }
                }
            }
        }
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&dCleared items from player's inventory!"));
    }

    @Subcommand("toggle-database")
    @Conditions("is-op")
    public void onCommandDisableDatabase(CommandSender sender) {
        Plugin.setDatabaseLoadingEnabled(!Plugin.isDatabaseLoadingEnabled());
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + "&d" +
                (Plugin.isDatabaseLoadingEnabled() ? "Enabled" : "Disabled") +
                " items loading from database/saving to database. This is for testing only."));
    }

    private static boolean isInt(String number) {
        try {
            Integer.parseInt(number);
        } catch (Exception exception) {
            return false;
        }
        return true;
    }

    private static boolean isBoolean(String bool) {
        return ("true".equalsIgnoreCase(bool) || "false".equalsIgnoreCase(bool));
    }

}
