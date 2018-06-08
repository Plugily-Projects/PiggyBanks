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

import lombok.Getter;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import pl.plajer.piggybanks.utils.MetricsLite;
import pl.plajer.piggybanks.utils.UpdateChecker;
import pl.plajer.piggybanks.utils.Utils;

import java.util.Arrays;
import java.util.List;

public class Main extends JavaPlugin {

    private boolean forceDisable = false;
    private final int MESSAGES_FILE_VERSION = 0;
    private final int CONFIG_FILE_VERSION = 1;
    private List<String> filesToGenerate = Arrays.asList("messages", "piggybanks", "users");
    @Getter
    private PiggyListeners piggyListeners;
    @Getter
    private PiggyManager piggyManager;
    @Getter
    private Economy economy = null;

    @Override
    public void onEnable() {
        for(String plugin : Arrays.asList("Vault", "HolographicDisplays")) {
            if(isEnabled(plugin)) {
                Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[PiggyBanks] " + plugin + " dependency not found!");
                Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[PiggyBanks] Plugin is turning off...");
                forceDisable = true;
                getServer().getPluginManager().disablePlugin(this);
                return;
            }
        }
        if(isEnabled("ProtocolLib")) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "[PiggyBanks] Detected ProtocolLib plugin!");
            Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "[PiggyBanks] Enabling private statistic holograms.");
        } else {
            Bukkit.getConsoleSender().sendMessage(ChatColor.GRAY + "[PiggyBanks] ProtocolLib plugin isn't installed!");
            Bukkit.getConsoleSender().sendMessage(ChatColor.GRAY + "[PiggyBanks] Disabling private statistic holograms.");
        }
        saveDefaultConfig();
        for(String file : filesToGenerate){
            ConfigurationManager.getConfig(file);
        }
        new ConfigurationManager(this);
        new Commands(this);
        new MenuHandler(this);
        piggyListeners = new PiggyListeners(this);
        piggyManager = new PiggyManager(this);
        new MetricsLite(this);
        piggyManager.loadPiggyBanks();
        piggyManager.teleportScheduler();
        setupEconomy();

        if(!ConfigurationManager.getConfig("messages").isSet("File-Version-Do-Not-Edit") || !ConfigurationManager.getConfig("messages").get("File-Version-Do-Not-Edit").equals(MESSAGES_FILE_VERSION)) {
            getLogger().info("Your messages file is outdated! Updating...");
            ConfigurationManager.updateConfig("messages");
            FileConfiguration config = ConfigurationManager.getConfig("messages");
            config.set("File-Version-Do-Not-Edit", MESSAGES_FILE_VERSION);
            ConfigurationManager.saveConfig(config, "messages");
            getLogger().info("File successfully updated!");
        }
        if(!getConfig().isSet("File-Version-Do-Not-Edit") || !getConfig().get("File-Version-Do-Not-Edit").equals(CONFIG_FILE_VERSION)) {
            getLogger().info("Your config file is outdated! Updating...");
            ConfigurationManager.updateConfig("config");
            getConfig().set("File-Version-Do-Not-Edit", CONFIG_FILE_VERSION);
            saveConfig();
            getLogger().info("File successfully updated!");
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[PiggyBanks] Warning! Your config.yml file was updated and all comments were removed! If you want to get comments back please generate new config.yml file!");
        }

        String currentVersion = "v" + Bukkit.getPluginManager().getPlugin("PiggyBanks").getDescription().getVersion();
        if(this.getConfig().getBoolean("update-notify")) {
            try {
                UpdateChecker.checkUpdate(currentVersion);
                String latestVersion = UpdateChecker.getLatestVersion();
                if(latestVersion != null) {
                    latestVersion = "v" + latestVersion;
                    Bukkit.getConsoleSender().sendMessage(Utils.colorFileMessage("Other.Plugin-Up-To-Date").replaceAll("%old%", currentVersion).replaceAll("%new%", latestVersion));
                }
            } catch(Exception ex) {
                Bukkit.getConsoleSender().sendMessage(Utils.colorFileMessage("Other.Plugin-Update-Check-Failed"));
            }
        }
    }

    @Override
    public void onDisable() {
        if(forceDisable) return;
        for(PiggyBank pgb : piggyManager.getLoadedPiggyBanks()) {
            pgb.getPiggyHologram().delete();
        }
        getPiggyManager().getLoadedPiggyBanks().clear();
    }

    public boolean isEnabled(String plugin){
        return getServer().getPluginManager().getPlugin(plugin) != null;
    }

    private boolean setupEconomy() {
        if(!isEnabled("Vault")) return false;
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if(rsp == null) {
            return false;
        }
        economy = rsp.getProvider();
        return economy != null;
    }
}
