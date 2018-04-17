package pl.plajer.piggybanks;

import lombok.Getter;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import pl.plajer.piggybanks.utils.MetricsLite;
import pl.plajer.piggybanks.utils.UpdateChecker;
import pl.plajer.piggybanks.utils.Utils;

import java.util.Arrays;

public class Main extends JavaPlugin {

    private boolean forceDisable = false;
    private final int MESSAGES_FILE_VERSION = 0;
    private final int CONFIG_FILE_VERSION = 1;
    @Getter
    private FileManager fileManager;
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
