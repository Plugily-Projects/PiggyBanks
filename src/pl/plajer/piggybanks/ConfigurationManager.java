/*
 * PiggyBanks - Simple piggies for your server
 * Copyright (C) 2018  Plajer's Lair - maintained by Plajer
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package pl.plajer.piggybanks;

import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Scanner;

/**
 * @author Plajer
 * <p>
 * Created at 08.06.2018
 */
public class ConfigurationManager {

    private static Main plugin;

    public ConfigurationManager(Main plugin) {
        ConfigurationManager.plugin = plugin;
    }

    public static FileConfiguration getConfig(String filename) {
        File file = new File(plugin.getDataFolder() + File.separator + filename + ".yml");
        if(!file.exists()) {
            plugin.getLogger().info("Creating " + filename + ".yml because it does not exist!");
            plugin.saveResource(filename + ".yml", true);
        }
        file = new File(plugin.getDataFolder(), filename + ".yml");
        YamlConfiguration config = new YamlConfiguration();
        try {
            config.load(file);
        } catch(InvalidConfigurationException | IOException ex) {
            ex.printStackTrace();
            Bukkit.getConsoleSender().sendMessage("Cannot load file " + filename + ".yml!");
            Bukkit.getConsoleSender().sendMessage("Create blank file " + filename + ".yml or restart the server!");
        }
        return config;
    }

    public static void saveConfig(FileConfiguration config, String name) {
        try {
            config.save(new File(plugin.getDataFolder(), name + ".yml"));
        } catch(IOException e) {
            e.printStackTrace();
            Bukkit.getConsoleSender().sendMessage("Cannot save file " + name + ".yml!");
            Bukkit.getConsoleSender().sendMessage("Create blank file " + name + ".yml or restart the server!");
        }
    }

    public static void updateConfig(String file) {
        HashMap<String, Object> newConfig = getConfigVals(file);
        FileConfiguration c = getConfig(file);
        for(String var : c.getKeys(false)) {
            newConfig.remove(var);
        }
        if(newConfig.size() != 0) {
            for(String key : newConfig.keySet()) {
                c.set(key, newConfig.get(key));
            }
            try {
                c.save(new File(plugin.getDataFolder(), file));
            } catch(IOException ignored) {
            }
        }
    }

    private static HashMap<String, Object> getConfigVals(String file) {
        HashMap<String, Object> var = new HashMap<>();
        YamlConfiguration config = new YamlConfiguration();
        try {
            config.loadFromString(stringFromInputStream(Main.class.getResourceAsStream("/" + file)));
        } catch(InvalidConfigurationException ignored) {
        }
        for(String key : config.getKeys(false)) {
            var.put(key, config.get(key));
        }
        return var;
    }

    @SuppressWarnings("resource")
    private static String stringFromInputStream(InputStream in) {
        return new Scanner(in).useDelimiter("\\A").next();
    }

}
