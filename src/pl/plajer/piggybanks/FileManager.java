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

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;
import java.util.HashMap;
import java.util.Scanner;
import java.util.logging.Level;

public class FileManager {

    private Main plugin;

    private FileConfiguration messagesConfig = null;
    private File messagesConfigFile = null;
    private FileConfiguration piggybanksConfig = null;
    private File piggybanksConfigFile = null;
    private FileConfiguration usersConfig = null;
    private File usersConfigFile = null;

    public FileManager(Main plugin) {
        this.plugin = plugin;
    }

    /*
     * messages.yml
     */

    public void saveDefaultMessagesConfig() {
        if(messagesConfigFile == null) {
            messagesConfigFile = new File(plugin.getDataFolder(), "messages.yml");
        }
        if(!messagesConfigFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }
    }

    public FileConfiguration getMessagesConfig() {
        if(messagesConfig == null) {
            reloadMessagesConfig();
        }
        return messagesConfig;
    }

    public void reloadMessagesConfig() {
        if(messagesConfigFile == null) {
            messagesConfigFile = new File(plugin.getDataFolder(), "messages.yml");
        }
        messagesConfig = YamlConfiguration.loadConfiguration(messagesConfigFile);

        // Look for defaults in the jar
        try {
            Reader defConfigStream = new InputStreamReader(plugin.getResource("messages.yml"));
            if(defConfigStream != null) {
                YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
                messagesConfig.setDefaults(defConfig);
            }
        } catch(Exception e) {
            System.out.println("[PiggyBank] System could not reload configuration!");
            e.printStackTrace();
        }
    }

    public void saveMessagesConfig() {
        if(messagesConfig == null || messagesConfigFile == null) {
            return;
        }
        try {
            getMessagesConfig().save(messagesConfigFile);
        } catch(IOException ex) {
            plugin.getLogger().log(Level.SEVERE, "Could not save config to " + messagesConfigFile, ex);
        }
    }

    /*
     * piggybanks.yml
     */

    public void saveDefaultPiggyBanksFile() {
        if(piggybanksConfigFile == null) {
            piggybanksConfigFile = new File(plugin.getDataFolder(), "piggybanks.yml");
        }
        if(!piggybanksConfigFile.exists()) {
            plugin.saveResource("piggybanks.yml", false);
        }
    }

    public FileConfiguration getPiggyBanksConfig() {
        if(piggybanksConfig == null) {
            reloadPiggyBanksConfig();
        }
        return piggybanksConfig;
    }

    public void reloadPiggyBanksConfig() {
        if(piggybanksConfigFile == null) {
            piggybanksConfigFile = new File(plugin.getDataFolder(), "piggybanks.yml");
        }
        piggybanksConfig = YamlConfiguration.loadConfiguration(piggybanksConfigFile);

        // Look for defaults in the jar
        try {
            Reader defConfigStream = new InputStreamReader(plugin.getResource("piggybanks.yml"));
            if(defConfigStream != null) {
                YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
                piggybanksConfig.setDefaults(defConfig);
            }
        } catch(Exception e) {
            System.out.println("[PiggyBank] System could not reload configuration!");
            e.printStackTrace();
        }
    }

    public void savePiggyBanksConfig() {
        if(piggybanksConfig == null || piggybanksConfigFile == null) {
            return;
        }
        try {
            getPiggyBanksConfig().save(piggybanksConfigFile);
        } catch(IOException ex) {
            plugin.getLogger().log(Level.SEVERE, "Could not save config to " + piggybanksConfigFile, ex);
        }
    }

    /*
     * users.yml
     */

    public void saveDefaultUsersFile() {
        if(usersConfigFile == null) {
            usersConfigFile = new File(plugin.getDataFolder(), "users.yml");
        }
        if(!usersConfigFile.exists()) {
            plugin.saveResource("users.yml", false);
        }
    }

    public FileConfiguration getUsersConfig() {
        if(usersConfig == null) {
            reloadUsersConfig();
        }
        return usersConfig;
    }

    public void reloadUsersConfig() {
        if(usersConfigFile == null) {
            usersConfigFile = new File(plugin.getDataFolder(), "users.yml");
        }
        usersConfig = YamlConfiguration.loadConfiguration(usersConfigFile);

        // Look for defaults in the jar
        try {
            Reader defConfigStream = new InputStreamReader(plugin.getResource("users.yml"));
            if(defConfigStream != null) {
                YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
                usersConfig.setDefaults(defConfig);
            }
        } catch(Exception e) {
            System.out.println("[PiggyBank] System could not reload configuration!");
            e.printStackTrace();
        }
    }

    public void saveUsersConfig() {
        if(usersConfig == null || usersConfigFile == null) {
            return;
        }
        try {
            getUsersConfig().save(usersConfigFile);
        } catch(IOException ex) {
            plugin.getLogger().log(Level.SEVERE, "Could not save config to " + usersConfigFile, ex);
        }
    }

    public void updateConfig(String file) {
        HashMap<String, Object> newConfig = getConfigVals(file);
        FileConfiguration c;
        if(file.equals("config.yml")) {
            c = plugin.getConfig();
        } else {
            c = getMessagesConfig();
        }
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

    private HashMap<String, Object> getConfigVals(String file) {
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
    private String stringFromInputStream(InputStream in) {
        return new Scanner(in).useDelimiter("\\A").next();
    }

}
