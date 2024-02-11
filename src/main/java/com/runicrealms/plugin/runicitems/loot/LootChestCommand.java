package com.runicrealms.plugin.runicitems.loot;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CatchUnknown;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Subcommand;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.MovingObjectPositionBlock;
import com.runicrealms.plugin.runicitems.RunicItems;
import com.runicrealms.plugin.runicitems.loot.chest.LootChestConditions;
import com.runicrealms.plugin.runicitems.loot.chest.LootChestPosition;
import com.runicrealms.plugin.runicitems.loot.chest.LootChestTemplate;
import com.runicrealms.plugin.runicitems.loot.chest.RegenerativeLootChest;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@CommandAlias("lootchest")
@CommandPermission("runic.op")
public class LootChestCommand extends BaseCommand implements Listener {

    private final Map<UUID, LootChestInfo> creatingChests = new ConcurrentHashMap<>();
    private final Set<UUID> deletingChests = new HashSet<>();

    public LootChestCommand() {
        RunicItems.getCommandManager().getCommandCompletions().registerAsyncCompletion("chest-templates", context ->
                RunicItems.getLootAPI().getChestTemplates().stream().map(LootChestTemplate::getIdentifier).collect(Collectors.toList()));
        RunicItems.getCommandManager().getCommandCompletions().registerAsyncCompletion("chest-models", context -> Arrays.stream(LootManager.LootChestModel.values()).map(LootManager.LootChestModel::name).toList());
        Bukkit.getPluginManager().registerEvents(this, RunicItems.getInstance());
        ProtocolLibrary.getProtocolManager().getAsynchronousManager().registerAsyncHandler(new PacketAdapter(RunicItems.getInstance(), ListenerPriority.LOW, PacketType.Play.Client.USE_ITEM) {
            @Override
            public void onPacketReceiving(PacketEvent event) {
                if (event.getPacketType() == PacketType.Play.Client.USE_ITEM && event.getPacket().getHands().read(0) == EnumWrappers.Hand.MAIN_HAND) {
                    onUsePacketEvent(event);
                }
            }
        }).start();
    }

    private static boolean isInt(String number) {
        try {
            Integer.parseInt(number);
            return true;
        } catch (Exception exception) {
            return false;
        }
    }

    private static String joinArgs(String[] array, int startPos) {
        if (array == null || startPos < 0 || startPos >= array.length) throw new IllegalArgumentException();
        StringBuilder builder = new StringBuilder();
        for (int i = startPos; i < array.length; i++) {
            builder.append(array[i]);
            if (i < array.length - 1) {
                builder.append(" ");
            }
        }
        return builder.toString();
    }

    @Subcommand("create")
    @CommandCompletion("@chest-templates @range:1-60 @range:1-60 @range:1-60 @nothing @nothing @chest-models @nothing")
    public void onCommandCreate(Player player, String[] args) {
        if (args.length < 4) {
            player.sendMessage(ChatColor.RED + "Usage: /lootchest create <chest-template> <min-level> <item-min-level> <item-max-level> [regeneration-time] [model] [title]");
            return;
        }
        String template = args[0];
        if (!RunicItems.getLootAPI().isLootChestTemplate(template)) {
            player.sendMessage(ChatColor.RED + "That is not a valid chest template identifier!");
            return;
        }
        LootChestTemplate chestTemplate = RunicItems.getLootAPI().getLootChestTemplate(args[0]);
        if (!isInt(args[1]) || !isInt(args[2]) || !isInt(args[3])) {
            player.sendMessage(ChatColor.RED + "min-level, item-min-level, and item-max-level must be integers!");
            return;
        }
        int minLevel = Integer.parseInt(args[1]);
        int itemMinLevel = Integer.parseInt(args[2]);
        int itemMaxLevel = Integer.parseInt(args[3]);

        Integer regenerationTime = null;
        String title = null;

        if (args.length >= 5) {
            if (!isInt(args[4])) {
                player.sendMessage(ChatColor.RED + "Regeneration time must be an integer!");
                return;
            }
            regenerationTime = Integer.parseInt(args[4]);
        }

        LootManager.LootChestModel model = args.length >= 6 ? LootManager.LootChestModel.getModel(args[5]) : LootManager.LootChestModel.NORMAL;

        if (args.length >= 7) title = joinArgs(args, 6);

        if (regenerationTime == null) {
            if (template.equalsIgnoreCase("common-chest")) {
                regenerationTime = 600;
            } else if (template.equalsIgnoreCase("uncommon-chest")) {
                regenerationTime = 900;
            } else if (template.equalsIgnoreCase("rare-chest")) {
                regenerationTime = 1200;
            } else if (template.equalsIgnoreCase("epic-chest")) {
                regenerationTime = 2700;
            } else {
                player.sendMessage(ChatColor.RED + "Since you are creating a loot chest with a custom template, please specify the [regeneration-time].");
                player.sendMessage(ChatColor.RED + "Usage: /lootchest create <chest-template> <item-min-level> <item-max-level> [regeneration-time] [title]");
                return;
            }
        }

        if (title == null) {
            if (template.equalsIgnoreCase("common-chest")) {
                title = "Common Loot Chest";
            } else if (template.equalsIgnoreCase("uncommon-chest")) {
                title = "&aUncommon Loot Chest";
            } else if (template.equalsIgnoreCase("rare-chest")) {
                title = "&bRare Loot Chest";
            } else if (template.equalsIgnoreCase("epic-chest")) {
                title = "&dEpic Loot Chest";
            } else {
                player.sendMessage(ChatColor.RED + "Since you are creating a loot chest with a custom template, please specify the [title].");
                player.sendMessage(ChatColor.RED + "Usage: /lootchest create <chest-template> <item-min-level> <item-max-level> [regeneration-time] [title]");
                return;
            }
        }

        deletingChests.remove(player.getUniqueId());

        creatingChests.put(player.getUniqueId(), new LootChestInfo(chestTemplate, minLevel, itemMinLevel, itemMaxLevel, regenerationTime, title, model != null ? model : LootManager.LootChestModel.NORMAL));
        player.sendMessage(ChatColor.GREEN + "Right click a chest to turn it into a loot chest, or type /lootchest cancel.");
    }

    @Subcommand("delete")
    public void onCommandDelete(Player player) {
        deletingChests.add(player.getUniqueId());
        creatingChests.remove(player.getUniqueId());
        player.sendMessage(ChatColor.GREEN + "Right click a loot chest to revert it back to a normal block, or type /lootchest cancel.");
    }

    @Default
    @CatchUnknown
    public void onCommand(Player player) {
        player.sendMessage(ChatColor.RED + "Usage: /lootchest create <chest-template> <item-min-level> <item-max-level> [regeneration-time] [title]");
        player.sendMessage(ChatColor.RED + "Or: /lootchest delete");
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        creatingChests.remove(event.getPlayer().getUniqueId());
    }

    @Subcommand("cancel")
    public void onCommandCancel(Player player) {
        if (creatingChests.containsKey(player.getUniqueId())) {
            creatingChests.remove(player.getUniqueId());
            player.sendMessage(ChatColor.GREEN + "Canceled creating loot chest!");
        } else {
            if (deletingChests.contains(player.getUniqueId())) {
                deletingChests.remove(player.getUniqueId());
                player.sendMessage(ChatColor.GREEN + "Canceled deleting loot chest!");
            } else {
                player.sendMessage(ChatColor.RED + "You are not currently creating or deleting a loot chest, nothing to cancel");
            }
        }
    }

    private void onUsePacketEvent(PacketEvent event) {
        if (deletingChests.contains(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
            MovingObjectPositionBlock position = event.getPacket().getMovingBlockPositions().readSafely(0);
            if (position == null) {
                return;
            }
            Location location = position.getBlockPosition().toLocation(event.getPlayer().getWorld());
            RegenerativeLootChest lootChest = RunicItems.getLootAPI().getRegenerativeLootChest(location);
            if (lootChest != null) {
                deletingChests.remove(event.getPlayer().getUniqueId());
                RunicItems.getLootAPI().deleteRegenerativeLootChest(lootChest);
                event.getPlayer().sendMessage(ChatColor.GREEN + "Removed loot chest! Don't worry the leftover model will be removed on server restart.");
            } else {
                event.getPlayer().sendMessage(ChatColor.RED + "That is not a loot chest! Type /lootchest cancel to cancel deleting a loot chest.");
            }
        } else if (creatingChests.containsKey(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
            MovingObjectPositionBlock position = event.getPacket().getMovingBlockPositions().readSafely(0);
            if (position == null) {
                return;
            }
            Location location = position.getBlockPosition().toLocation(event.getPlayer().getWorld());

            if (location.getBlock().getType() != Material.CHEST) {
                event.getPlayer().sendMessage(ChatColor.RED + "That is not a chest!");
                return;
            }

            Directional directional = (Directional) location.getBlock().getBlockData();

            LootChestInfo chestInfo = creatingChests.get(event.getPlayer().getUniqueId());
            RunicItems.getLootAPI().createRegenerativeLootChest(new RegenerativeLootChest(
                    new LootChestPosition(location, directional.getFacing()),
                    chestInfo.template,
                    new LootChestConditions(),
                    chestInfo.minLevel,
                    chestInfo.itemMinLevel, chestInfo.itemMaxLevel,
                    chestInfo.regenerationTime,
                    chestInfo.title,
                    chestInfo.model.getModelID()));
            creatingChests.remove(event.getPlayer().getUniqueId());
            event.getPlayer().sendMessage(ChatColor.GREEN + "Added loot chest! It will be a bit buggy until the server restarts...");
        }
    }

    private record LootChestInfo(
            @NotNull LootChestTemplate template,
            int minLevel,
            int itemMinLevel, int itemMaxLevel,
            int regenerationTime,
            @NotNull String title,
            @NotNull LootManager.LootChestModel model) {
    }

}
