package com.runicrealms.runicitems.config;

import com.runicrealms.runicitems.RunicItems;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class ConfigUtil {

    public static FileConfiguration getYamlConfigFile(String fileName, File folder) {
        FileConfiguration config;
        File file;
        file = new File(folder, fileName);
        config = new YamlConfiguration();
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            config.load(file);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return config;
    }

    public static File getSubFolder(File folder, String subfolder) {
        for (File file : folder.listFiles()) {
            if (file.getName().equalsIgnoreCase(subfolder)) {
                return file;
            }
        }
        return null;
    }

    public static void initDirs() {
        if (!RunicItems.getInstance().getDataFolder().exists()) {
            RunicItems.getInstance().getDataFolder().mkdirs();
        }
        File itemsFolder = getSubFolder(RunicItems.getInstance().getDataFolder(), "items");
        if (itemsFolder == null) {
            itemsFolder = new File(RunicItems.getInstance().getDataFolder(), "items");
            itemsFolder.mkdirs();
        }
    }

}
