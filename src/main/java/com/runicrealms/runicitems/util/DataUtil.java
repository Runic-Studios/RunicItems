package com.runicrealms.runicitems.util;

import com.runicrealms.runicitems.item.RunicItem;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;

public class DataUtil {

    public static Color getColorFromData(RunicItem runicItem) {
        try {
            String color = runicItem.getData().get("color").toLowerCase();
            return switch (color) {
                case "aqua" -> Color.AQUA;
                case "black" -> Color.BLACK;
                case "fuchsia" -> Color.FUCHSIA;
                case "green" -> Color.GREEN;
                case "lime" -> Color.LIME;
                case "orange" -> Color.ORANGE;
                case "red" -> Color.RED;
                default -> Color.WHITE;
            };
        } catch (NullPointerException e) {
            Bukkit.getLogger().info(ChatColor.RED + "A potion is missing a color field");
            return Color.WHITE;
        }
    }
}
