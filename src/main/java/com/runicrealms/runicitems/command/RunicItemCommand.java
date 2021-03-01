package com.runicrealms.runicitems.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CatchUnknown;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.Conditions;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.Syntax;
import com.runicrealms.runicitems.DupeManager;
import com.runicrealms.runicitems.ItemManager;
import com.runicrealms.runicitems.RunicItems;
import com.runicrealms.runicitems.TemplateManager;
import com.runicrealms.runicitems.item.RunicItem;
import com.runicrealms.runicitems.item.template.RunicItemTemplate;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

@CommandAlias("ri|runicitems|runicitem")
public class RunicItemCommand extends BaseCommand {

    private static final String PREFIX = "&5[RunicItems] &6» &r";

    public RunicItemCommand() {
        RunicItems.getCommandManager().getCommandCompletions().registerAsyncCompletion("item-ids", context -> TemplateManager.getTemplates().keySet());
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
        RunicItem item = template.generateItem(count, DupeManager.getNextItemId(), null, null);
        player.getInventory().addItem(item.generateItem());
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + "&dGiven you &5" + count + "x &r" + item.getDisplayableItem().getDisplayName()));
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
        RunicItem item = template.generateItem(count, DupeManager.getNextItemId(), null, null);
        target.getInventory().addItem(item.generateItem());
        target.sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + "&dGiven you &5" + count + "x &r" + item.getDisplayableItem().getDisplayName()));
    }

    @Subcommand("clear|c")
    @Conditions("is-op")
    @Syntax("<player> [item] [amount]")
    @CommandCompletion("@players @item-ids @nothing")
    public void onCommandClear(CommandSender sender, String[] args) {
        if (args.length < 1) { sender.sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + "&dInvalid syntax! Please check &7/runicitem help")); return; }
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
                    RunicItem item = ItemManager.getRunicItemFromItemStack(contents[i]);
                    if (item == null) {
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + "&dError removing items!"));
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
        RunicItems.setDatabaseLoadingEnabled(!RunicItems.isDatabaseLoadingEnabled());
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + "&d" +
                (RunicItems.isDatabaseLoadingEnabled() ? "Enabled" : "Disabled") +
                " items loading from database/saving to database. This is for testing only."));
    }

    @Subcommand("get-nbt")
    @Conditions("is-op")
    public void onCommandGetNbt(Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType() == Material.AIR) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + "&dYou are not holding an item!"));
            return;
        }
        NBTItem nbtItem = new NBTItem(item);
        if (!nbtItem.hasNBTData()) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + "&dThe item you are holding doesn't have any NBT data."));
            return;
        }
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + "&dItem NBT Data: "));
        for (String key : nbtItem.getKeys()) {
            if (!key.equals("display"))
            switch (nbtItem.getType(key)) {
                case NBTTagByte:
                    player.sendMessage(ChatColor.GREEN + "- " + key + " : " + nbtItem.getByte(key));
                    break;
                case NBTTagByteArray:
                    player.sendMessage(ChatColor.GREEN + "- " + key + " : " + Arrays.toString(nbtItem.getByteArray(key)));
                    break;
                case NBTTagCompound:
                    player.sendMessage(ChatColor.GREEN + "- " + key + " : " + nbtItem.getCompound(key).toString());
                    break;
                case NBTTagDouble:
                    player.sendMessage(ChatColor.GREEN + "- " + key + " : " + nbtItem.getDouble(key));
                    break;
                case NBTTagFloat:
                    player.sendMessage(ChatColor.GREEN + "- " + key + " : " + nbtItem.getFloat(key));
                    break;
                case NBTTagInt:
                    player.sendMessage(ChatColor.GREEN + "- " + key + " : " + nbtItem.getInteger(key));
                    break;
                case NBTTagIntArray:
                    player.sendMessage(ChatColor.GREEN + "- " + key + " : " + Arrays.toString(nbtItem.getIntArray(key)));
                    break;
                case NBTTagList:
                    player.sendMessage(ChatColor.GREEN + "- " + key + " : " + nbtItem.getStringList(key));
                    break;
                case NBTTagLong:
                    player.sendMessage(ChatColor.GREEN + "- " + key + " : " + nbtItem.getLong(key));
                    break;
                case NBTTagShort:
                    player.sendMessage(ChatColor.GREEN + "- " + key + " : " + nbtItem.getShort(key));
                    break;
                case NBTTagString:
                    player.sendMessage(ChatColor.GREEN + "- " + key + " : " + nbtItem.getString(key));
                    break;
                default: {
                    player.sendMessage(ChatColor.GREEN + "- " + key + " : unknown type");
                }
            }
        }
    }

    @Subcommand("dupe-item")
    @Conditions("is-op")
    public void onCommandDupeItem(Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType() == Material.AIR) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + "&dYou are not holding an item!"));
            return;
        }
        int slot = -1;
        for (int i = 0; i < 35; i++) {
            ItemStack slotItem = player.getInventory().getItem(i);
            if (slotItem == null || slotItem.getType() == Material.AIR) {
                slot = i;
                break;
            }
        }
        if (slot != -1) {
            player.getInventory().setItem(slot, item);
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + "&dAdded duped item to your inventory!"));
        } else {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + "&dYou do not have space in your inventory!"));
        }
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
