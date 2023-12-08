package com.runicrealms.plugin.runicitems.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CatchUnknown;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Conditions;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.Syntax;
import com.runicrealms.plugin.common.util.ColorUtil;
import com.runicrealms.plugin.common.util.Pair;
import com.runicrealms.plugin.rdb.RunicDatabase;
import com.runicrealms.plugin.runicitems.DupeManager;
import com.runicrealms.plugin.runicitems.LootManager;
import com.runicrealms.plugin.runicitems.RunicItems;
import com.runicrealms.plugin.runicitems.RunicItemsAPI;
import com.runicrealms.plugin.runicitems.TemplateManager;
import com.runicrealms.plugin.runicitems.item.RunicItem;
import com.runicrealms.plugin.runicitems.item.RunicItemArmor;
import com.runicrealms.plugin.runicitems.item.RunicItemDynamic;
import com.runicrealms.plugin.runicitems.item.perk.ItemPerk;
import com.runicrealms.plugin.runicitems.item.perk.ItemPerkManager;
import com.runicrealms.plugin.runicitems.item.perk.ItemPerkType;
import com.runicrealms.plugin.runicitems.item.stats.RunicItemRarity;
import com.runicrealms.plugin.runicitems.item.template.RunicItemArmorTemplate;
import com.runicrealms.plugin.runicitems.item.template.RunicItemTemplate;
import com.runicrealms.plugin.runicitems.item.template.RunicItemWeaponTemplate;
import com.runicrealms.plugin.runicitems.item.util.RunicItemClass;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@CommandAlias("ri|runicitems|runicitem")
@CommandPermission("runic.op")
public class RunicItemCommand extends BaseCommand {

    public static final String PREFIX = "&5[RunicItems] &6» &r";

    public RunicItemCommand() {
        RunicItems.getCommandManager().getCommandCompletions().registerAsyncCompletion("item-ids", context -> {
            if (!context.getSender().isOp()) return new HashSet<>();
            return TemplateManager.getTemplates().keySet();
        });

        RunicItems.getCommandManager().getCommandCompletions().registerAsyncCompletion("flag-parameter", context -> {
            String[] args = context.getContextValue(String[].class);
            List<String> list = new ArrayList<>();

            for (GetRandomFlag flag : GetRandomFlag.values()) {
                boolean contains = false;

                for (String arg : args) {
                    if (!arg.startsWith("-") || !flag.getName().equalsIgnoreCase(arg.substring(1))) {
                        continue;
                    }

                    contains = true;
                    break;
                }

                if (!contains) {
                    list.add("-" + flag.getName());
                }
            }

            return list;
        });

        RunicItems.getCommandManager().getCommandCompletions().registerAsyncCompletion("value-parameter", context -> {
            int index = Integer.parseInt(context.getConfig());
            String[] args = context.getContextValue(String[].class);

            String rawFlag = args[index - 1];

            if (!rawFlag.startsWith("-")) {
                return Collections.emptyList();
            }

            GetRandomFlag flag = GetRandomFlag.getFlag(rawFlag.substring(1));

            if (flag == null) {
                return Collections.emptyList();
            }

            return flag.getComplete().apply(context.getInput());
        });

        final AtomicReference<Set<String>> cachedPerkIDs = new AtomicReference<>();

        RunicItems.getCommandManager().getCommandCompletions().registerAsyncCompletion("perk-ids", context -> {
            if (!context.getSender().isOp()) return new HashSet<>();
            if (cachedPerkIDs.get() == null) {
                cachedPerkIDs.set(ItemPerkManager.getItemPerks().stream().map(ItemPerkType::getIdentifier).collect(Collectors.toUnmodifiableSet()));
            }
            return cachedPerkIDs.get();
        });
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

    @Subcommand("clear|c")
    @Conditions("is-op")
    @Syntax("<player> [item] [amount]")
    @CommandCompletion("@players @item-ids @nothing")
    public void onCommandClear(CommandSender sender, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + "&dInvalid syntax! Please check &7/runicitem help"));
            return;
        }
        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + "&dThat is not a valid player!"));
            return;
        }
        RunicItemTemplate template = null;
        if (args.length >= 2) {
            template = TemplateManager.getTemplateFromId(args[1]);
            if (template == null) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + "&dThat item ID does not exist!"));
                return;
            }
        }
        int amount = -1;
        if (args.length >= 3) {
            if (!isInt(args[2])) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + "&dThat is not a valid amount!"));
                return;
            }
            amount = Integer.parseInt(args[2]);
            if (amount < 1) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + "&dThat is not a valid amount!"));
                return;
            }
        }

        RunicItems.getInventoryAPI().clearInventory(target.getInventory(), amount, template, sender);

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

    @Subcommand("drop")
    @Conditions("is-op")
    @Syntax("<item> <location> [amount]")
    @CommandCompletion("@item-ids @nothing @nothing")
    public void onCommandDrop(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + "&dInvalid syntax! Please check &7/runicitem help"));
            return;
        }
        RunicItemTemplate template = TemplateManager.getTemplateFromId(args[0]);
        int count = 1;
        if (template == null) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + "&dThat item ID does not exist!"));
            return;
        }
        String[] splitLocation = args[1].split(",");
        Location location = new Location(
                Bukkit.getWorld(splitLocation[0]),
                Double.parseDouble(splitLocation[1]),
                Double.parseDouble(splitLocation[2]),
                Double.parseDouble(splitLocation[3])
        );
        if (args.length >= 3) {
            if (isInt(args[2])) {
                count = Integer.parseInt(args[2]);
            } else {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + "&dThat is not a valid amount!"));
                return;
            }
        }
        RunicItem item = template.generateItem(count, DupeManager.getNextItemId(), null, null);
        location.getWorld().dropItem(location, item.generateItem());
    }

    @Subcommand("drop-range")
    @Conditions("is-op")
    @Syntax("<min-level> <max-level> <location> [amount]")
    @CommandCompletion("@range:0-60 @range:0-60 @nothing @nothing")
    public void onCommandDropRange(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + "&dInvalid syntax! Please check &7/runicitem help"));
            return;
        }
        if (!isInt(args[0]) || !isInt(args[1])) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + "&dInvalid syntax! Level min and level max must be integers!"));
            return;
        }
        RunicItemTemplate template = LootManager.getRandomItemInRange(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
        int count = 1;
        if (template == null) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + "&dThat item ID does not exist!"));
            return;
        }
        String[] splitLocation = args[2].split(",");
        Location location = new Location(
                Bukkit.getWorld(splitLocation[0]),
                Double.parseDouble(splitLocation[1]),
                Double.parseDouble(splitLocation[2]),
                Double.parseDouble(splitLocation[3])
        );
        if (args.length >= 4) {
            if (isInt(args[3])) {
                count = Integer.parseInt(args[3]);
            } else {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + "&dThat is not a valid amount!"));
                return;
            }
        }
        RunicItem item = template.generateItem(count, DupeManager.getNextItemId(), null, null);
        location.getWorld().dropItem(location, item.generateItem());
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

    @Subcommand("get")
    @Conditions("is-player|is-op")
    @Syntax("<item> [amount]")
    @CommandCompletion("@item-ids @nothing")
    public void onCommandGet(Player player, String[] args) {
        if (args.length == 0) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + "&dInvalid syntax! Please check &7/runicitem help"));
            return;
        }
        RunicItemTemplate template = TemplateManager.getTemplateFromId(args[0]);
        int count = 1;
        if (template == null) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + "&dThat item ID does not exist!"));
            return;
        }
        if (args.length >= 2) {
            if (isInt(args[1])) {
                count = Integer.parseInt(args[1]);
            } else {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + "&dThat is not a valid amount!"));
                return;
            }
        }
        RunicItem item = template.generateItem(count, DupeManager.getNextItemId(), null, null);
        RunicItemsAPI.addItem(player.getInventory(), item.generateItem());
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

    @Subcommand("get-range")
    @Conditions("is-player|is-op")
    @Syntax("<level-min> <level-max> [amount]")
    @CommandCompletion("@range:0-60 @range:0-60 @nothing")
    public void onCommandGetRange(Player player, String[] args) {
        if (args.length == 1) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + "&dInvalid syntax! Please check &7/runicitem help"));
            return;
        }
        if (!isInt(args[0]) || !isInt(args[1])) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + "&dInvalid syntax! Level min and level max must be integers!"));
            return;
        }
        RunicItemTemplate template = LootManager.getRandomItemInRange(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
        int count = 1;
        if (template == null) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + "&dThat item ID does not exist!"));
            return;
        }
        if (args.length >= 3) {
            if (isInt(args[2])) {
                count = Integer.parseInt(args[2]);
            } else {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + "&dThat is not a valid amount!"));
                return;
            }
        }
        RunicItem item = template.generateItem(count, DupeManager.getNextItemId(), null, null);
        RunicItemsAPI.addItem(player.getInventory(), item.generateItem());
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + "&dGiven you &5" + count + "x &r" + item.getDisplayableItem().getDisplayName()));
    }

    @Subcommand("give")
    @Conditions("is-op")
    @Syntax("<player> <item> [amount]")
    @CommandCompletion("@players @item-ids @nothing")
    public void onCommandGive(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + "&dInvalid syntax! Please check &7/runicitem help"));
            return;
        }
        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + "&dThat is not a valid player!"));
            return;
        }
        RunicItemTemplate template = TemplateManager.getTemplateFromId(args[1]);
        int count = 1;
        if (template == null) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + "&dThat item ID does not exist!"));
            return;
        }
        if (args.length >= 3) {
            if (!isInt(args[2])) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + "&dThat is not a valid amount!"));
                return;
            }
            count = Integer.parseInt(args[2]);
            if (count < 1) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + "&dThat is not a valid amount!"));
                return;
            }
        }

        RunicItem item = template.generateItem(count, DupeManager.getNextItemId(), null, null);
        int dynamicField;
        if (item instanceof RunicItemDynamic && args.length == 4) {
            if (isInt(args[3])) {
                dynamicField = Integer.parseInt(args[3]);
            } else {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + "&dThat is not a valid dyanmic field!"));
                return;
            }
            ((RunicItemDynamic) item).setDynamicField(dynamicField);
        }
        RunicItemsAPI.addItem(target.getInventory(), item.generateItem());
    }

    @Subcommand("give-range")
    @Conditions("is-op")
    @Syntax("<player> <level-min> <level-max> [amount]")
    @CommandCompletion("@players @range:0-60 @range:0-60 @nothing")
    public void onCommandGiveRange(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + "&dInvalid syntax! Please check &7/runicitem help"));
            return;
        }
        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + "&dThat is not a valid player!"));
            return;
        }
        if (!isInt(args[1]) || !isInt(args[2])) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + "&dInvalid syntax! Level min and level max must be integers!"));
            return;
        }
        RunicItemTemplate template = LootManager.getRandomItemInRange(Integer.parseInt(args[1]), Integer.parseInt(args[2]));
        int count = 1;
        if (template == null) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + "&dThat item ID does not exist!"));
            return;
        }
        if (args.length >= 4) {
            if (!isInt(args[3])) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + "&dThat is not a valid amount!"));
                return;
            }
            count = Integer.parseInt(args[3]);
            if (count < 1) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + "&dThat is not a valid amount!"));
                return;
            }
        }
        RunicItem item = template.generateItem(count, DupeManager.getNextItemId(), null, null);
        RunicItemsAPI.addItem(target.getInventory(), item.generateItem());
        RunicItemsAPI.addItem(target.getInventory(), item.generateItem());
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

    @Subcommand("picker")
    @Conditions("is-player|is-op")
    @Syntax("<player> <item> <item> <item> <item> <item>")
    @CommandCompletion("@online @item-ids @item-ids item-ids item-ids item-ids")
    public void onCommandPicker(CommandSender sender, String[] args) {
        if (args.length != 6) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + "&dInvalid syntax! Please check &7/runicitem help"));
            return;
        }

        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + "&dInvalid syntax! Please check &7/runicitem help"));
            return;
        }
        RunicItemClass playerClass = RunicItemClass.getFromIdentifier(RunicDatabase.getAPI().getCharacterAPI().getPlayerClass(target));

        for (int i = 1; i < 6; i++) {
            RunicItemTemplate template = TemplateManager.getTemplateFromId(args[i]);
            if (template == null) continue;

            RunicItemClass itemClass = null;
            if (template instanceof RunicItemArmorTemplate) {
                itemClass = ((RunicItemArmorTemplate) template).getRunicClass();
            } else if (template instanceof RunicItemWeaponTemplate) {
                itemClass = ((RunicItemWeaponTemplate) template).getRunicClass();
            }

            if (itemClass == null || !itemClass.equals(playerClass)) continue;
            RunicItemsAPI.addItem(
                    target.getInventory(),
                    template.generateItem(1, DupeManager.getNextItemId(), null, null).generateItem(),
                    target.getLocation());
            return;
        }
    }

    @Subcommand("add-perk")
    @Conditions("is-player|is-op")
    @CommandCompletion("@perk-ids")
    private void onAddPerk(@NotNull Player player, @NotNull String[] args) {
        if (args.length == 0) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&dYou must specify a perk to add!"));
            return;
        }

        ItemPerkType type = ItemPerkManager.getItemPerkFromIdentifier(args[0]);
        if (type == null) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&d" + args[0] + " is not a valid perk type!"));
            return;
        }

        ItemStack heldItem = player.getInventory().getItemInMainHand();
        if (heldItem.getType() == Material.AIR) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&dYou must be holding an item to add a perk to it!"));
            return;
        }

        RunicItem item = RunicItemsAPI.getRunicItemFromItemStack(heldItem);
        if (!(item instanceof RunicItemArmor armor)) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&dYou must be holding armor to add a perk to it."));
            return;
        }

        // inefficient i know but i don't care it only runs on command and is async

        LinkedHashSet<ItemPerk> perks = armor.getItemPerks();
        LinkedHashSet<ItemPerk> newPerks = new LinkedHashSet<>();

        boolean replaced = false;
        for (ItemPerk perk : perks) {
            if (perk.getType() != type) {
                newPerks.add(perk);
            } else {
                newPerks.add(new ItemPerk(type, perk.getStacks() + 1));
                replaced = true;
            }
        }
        if (!replaced) {
            newPerks.add(new ItemPerk(type, 1));
        }

        armor.getItemPerks().clear();
        newPerks.forEach(perk -> armor.getItemPerks().add(perk));

        player.getInventory().setItemInMainHand(armor.generateItem());

        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&dAdded " + args[0] + " x1 to your held item."));
    }

    @Subcommand("get-random")
    @Conditions("is-player|is-op")
    @CommandCompletion("@flag-parameter:0 @value-parameter:1 @flag-parameter:2 @value-parameter:3 @flag-parameter:4 @value-parameter:5 @flag-parameter:6 @value-parameter:7 @flag-parameter:8 @value-parameter:9 @nothing")
    private void onGetRandom(@NotNull Player player, @NotNull String[] args) {
        Map<GetRandomFlag, String> parameters = new HashMap<>();

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];

            if (!arg.startsWith("-")) {
                continue;
            }

            String parameter = arg.substring(1);

            GetRandomFlag flag = GetRandomFlag.getFlag(parameter);

            if (flag == null || parameters.containsKey(flag)) {
                continue;
            }

            String value = null;

            // If the next argument doesn't start with "-", it's the value of this flag.
            if (i + 1 < args.length && !args[i + 1].startsWith("-")) {
                value = args[i + 1];
                i++; // Skip next arg since it's this flag's value.
            }

            parameters.put(flag, value);
        }

        String rawRange = parameters.get(GetRandomFlag.RANGE);
        String[] parsedRange = rawRange != null ? rawRange.split(",") : null;
        if (parsedRange != null && (parsedRange.length != 2 || !isInt(parsedRange[0].trim()) || !isInt(parsedRange[1].trim()))) {
            player.sendMessage(ColorUtil.format(PREFIX + "&cInvalid range syntax: lower-higher"));
        }

        Pair<Integer, Integer> range = parsedRange != null && parsedRange.length == 2 && isInt(parsedRange[0].trim()) && isInt(parsedRange[1].trim()) ? new Pair<>(Integer.parseInt(parsedRange[0].trim()), Integer.parseInt(parsedRange[1].trim())) : null;

        Set<RunicItemRarity> rarities = null;

        String rarity = parameters.get(GetRandomFlag.RARITY);
        for (String rawRarity : rarity != null ? rarity.split(",") : new String[0]) {
            RunicItemRarity parsed = RunicItemRarity.getFromIdentifier(rawRarity.trim());

            if (parsed == null) {
                player.sendMessage(ColorUtil.format(PREFIX + "&cInvalid rarity of " + rawRarity + " entered!"));
                continue;
            }

            if (rarities == null) {
                rarities = new HashSet<>();
            }

            rarities.add(parsed);
        }

        String playerClass = parameters.get(GetRandomFlag.CLASS);
        RunicItemClass clazz = RunicItemClass.getFromIdentifier(playerClass != null ? playerClass.trim() : null);

        if (clazz == null && playerClass != null) {
            player.sendMessage(ColorUtil.format(PREFIX + "&cInvalid class of " + playerClass.trim() + " entered!"));
        }

        String types = parameters.get(GetRandomFlag.ITEMS);
        Set<LootManager.ItemType> items = null;

        for (String rawType : types != null ? types.split(",") : new String[0]) {
            LootManager.ItemType parsed = LootManager.ItemType.getItemType(rawType.trim());

            if (parsed == null) {
                player.sendMessage(ColorUtil.format(PREFIX + "&cInvalid item type of " + rawType.trim() + " entered!"));
                continue;
            }

            if (items == null) {
                items = new HashSet<>();
            }

            items.add(parsed);
        }

        Float lqm;
        try {
            lqm = Float.parseFloat(parameters.get(GetRandomFlag.LQM));
        } catch (NumberFormatException | NullPointerException e) {
            lqm = null;
        }

        //iterating and picking item is async, slight delay and item is given on main thread after async task is complete
        LootManager.getItem(range, rarities, clazz, items, lqm)
                .thenAccept(template -> {
                    if (template.isEmpty()) {
                        player.sendMessage(ColorUtil.format(PREFIX + "&cThere are no item templates that match your conditions!"));
                        return;
                    }

                    RunicItem item = template.get().generateItem(1, DupeManager.getNextItemId(), null, null);
                    RunicItemsAPI.addItem(player.getInventory(), item.generateItem());
                });
    }

    /**
     * An enum to keep track of parameters for the get-random subcommand
     */
    private enum GetRandomFlag {
        RANGE("range", input -> Stream.of("10,20", "20,30", "30,40", "40,50", "50,60", "60,60").filter(element -> element.startsWith(input)).toList()),
        RARITY("rarity", input -> Stream.of("common", "uncommon", "rare", "epic", "common,uncommon").filter(element -> element.startsWith(input)).toList()),
        CLASS("class", input -> Stream.of("mage", "mage,rouge", "mage,rouge,warrior").filter(element -> element.startsWith(input)).toList()),
        ITEMS("items", input -> Stream.of("helmet", "chestplate", "leggings", "boots", "weapon", "helmet,chestplate,leggings,boots").filter(element -> element.startsWith(input)).toList()),
        LQM("lqm", input -> Stream.of("1", "1.5", "0.5").filter(element -> element.startsWith(input)).toList());

        private final String name;
        private final Function<String, List<String>> complete;

        GetRandomFlag(@NotNull String name, @NotNull Function<String, List<String>> complete) {
            this.name = name;
            this.complete = complete;
        }

        @Nullable
        public static GetRandomFlag getFlag(@NotNull String value) {
            try {
                return GetRandomFlag.valueOf(value.toUpperCase());
            } catch (IllegalArgumentException e) {
                return null;
            }
        }

        @NotNull
        public String getName() {
            return this.name;
        }

        @NotNull
        public Function<String, List<String>> getComplete() {
            return this.complete;
        }
    }
}
