package com.runicrealms.runicitems.util;

import com.runicrealms.runicitems.item.RunicItem;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;

public class DataUtil {

    public static Color getColorFromData(RunicItem runicItem) {
        try {
            String color = runicItem.getData().get("color").toLowerCase();
            switch (color) {
                case "aqua":
                    return Color.AQUA;
                case "black":
                    return Color.BLACK;
                case "fuchsia":
                    return Color.FUCHSIA;
                case "green":
                    return Color.GREEN;
                case "orange":
                    return Color.ORANGE;
                case "red":
                    return Color.RED;
            }
            return Color.WHITE; // oops
        } catch (NullPointerException e) {
            Bukkit.getLogger().info(ChatColor.RED + "A potion is missing a color field");
            return Color.WHITE;
        }
    }
}
