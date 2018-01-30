package pl.plajer.piggybanks;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import net.milkbowl.vault.economy.Economy;
import pl.plajer.piggybanks.utils.MetricsLite;
import pl.plajer.piggybanks.utils.UpdateChecker;
import pl.plajer.piggybanks.utils.Utils;

public class Main extends JavaPlugin {

    private FileManager fileManager;
    private PiggyListeners piggyListeners;
    private PiggyManager piggyManager;

    private static Main instance;
    private Economy econ = null;
    private Boolean useProtocolLib;
    private final int MESSAGES_FILE_VERSION = 0;
    private final int CONFIG_FILE_VERSION = 0;

    @Override
    public void onEnable() {
        if(getServer().getPluginManager().getPlugin("HolographicDisplays") == null) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[PiggyBanks] Holographic Displays dependency not found!");
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[PiggyBanks] Plugin is turning off...");
            getServer().getPluginManager().disablePlugin(this);
        }
        if(getServer().getPluginManager().getPlugin("Vault") == null) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[PiggyBanks] Vault dependency not found!");
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[PiggyBanks] Plugin is turning off...");
            getServer().getPluginManager().disablePlugin(this);
        }
        if(setupProtocolLib()) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "[PiggyBanks] Detected ProtocolLib plugin!");
            Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "[PiggyBanks] Enabling private statistic holograms.");
        } else {
            Bukkit.getConsoleSender().sendMessage(ChatColor.GRAY + "[PiggyBanks] ProtocolLib plugin isn't installed!");
            Bukkit.getConsoleSender().sendMessage(ChatColor.GRAY + "[PiggyBanks] Disabling private statistic holograms.");
        }
        instance = this;
        new Commands(this);
        new MenuHandler(this);
        fileManager = new FileManager(this);
        piggyListeners = new PiggyListeners(this);
        piggyManager = new PiggyManager(this);
        new MetricsLite(this);
        saveDefaultConfig();
        getFileManager().saveDefaultUsersFile();
        getFileManager().saveDefaultPiggyBanksFile();
        getFileManager().saveDefaultMessagesConfig();
        piggyManager.loadPiggyBanks();
        piggyManager.teleportScheduler();
        setupEconomy();

        if(!fileManager.getMessagesConfig().isSet("File-Version-Do-Not-Edit") || !fileManager.getMessagesConfig().get("File-Version-Do-Not-Edit").equals(MESSAGES_FILE_VERSION)) {
            getLogger().info("Your messages file is outdated! Updating...");
            fileManager.updateConfig("messages.yml");
            fileManager.getMessagesConfig().set("File-Version-Do-Not-Edit", MESSAGES_FILE_VERSION);
            fileManager.saveMessagesConfig();
            getLogger().info("File successfully updated!");
        }
        if(!getConfig().isSet("File-Version-Do-Not-Edit") || !getConfig().get("File-Version-Do-Not-Edit").equals(CONFIG_FILE_VERSION)) {
            getLogger().info("Your config file is outdated! Updating...");
            fileManager.updateConfig("config.yml");
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
                    Bukkit.getConsoleSender().sendMessage(Utils.colorRawMessage("Other.Plugin-Up-To-Date").replaceAll("%old%", currentVersion).replaceAll("%new%", latestVersion));
                }
            } catch(Exception ex) {
                Bukkit.getConsoleSender().sendMessage(Utils.colorRawMessage("Other.Plugin-Update-Check-Failed"));
            }
        }
    }


    @Override
    public void onDisable() {
        for(PiggyBank pgb : piggyManager.getLoadedPiggyBanks()) {
            pgb.getPiggyHologram().delete();
        }
        getPiggyManager().getLoadedPiggyBanks().clear();
    }

    public FileManager getFileManager() {
        return fileManager;
    }

    public PiggyListeners getPiggyListeners() {
        return piggyListeners;
    }

    public PiggyManager getPiggyManager() {
        return piggyManager;
    }

    public static Main getInstance() {
        return instance;
    }

    public Economy getEconomy() {
        return econ;
    }

    public Boolean getProtocolLibUse() {
        return useProtocolLib;
    }

    private boolean setupEconomy() {
        if(getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if(rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }

    private boolean setupProtocolLib() {
        if(getServer().getPluginManager().getPlugin("ProtocolLib") == null) {
            useProtocolLib = false;
            return false;
        }
        useProtocolLib = true;
        return useProtocolLib != null;
    }

}
