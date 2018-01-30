package pl.plajer.piggybanks;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.gmail.filoghost.holographicdisplays.api.VisibilityManager;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Pig;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import pl.plajer.piggybanks.utils.Utils;

public class PiggyManager {

    private List<PiggyBank> loadedPiggyBanks = new ArrayList<>();
    private Main plugin;

    public PiggyManager(Main plugin) {
        this.plugin = plugin;
    }

    public void loadPiggyBanks() {
        List<String> piggies = plugin.getFileManager().getPiggyBanksConfig().getStringList("piggybanks");
        List<UUID> uuids = new ArrayList<>();
        if(piggies != null) {
            for(String s : piggies) {
                UUID u = UUID.fromString(s);
                uuids.add(u);
            }
        }
        if(uuids.isEmpty()) return;
        int i = 0;
        for(World world : Bukkit.getServer().getWorlds()) {
            for(Entity entity : Bukkit.getServer().getWorld(world.getName()).getEntities()) {
                if(entity instanceof Pig) {
                    if(uuids.contains(entity.getUniqueId())) {
                        final Hologram hologram = HologramsAPI.createHologram(Main.getInstance(), entity.getLocation().clone().add(0, 2.2, 0));
                        if(plugin.getProtocolLibUse()) {
                            new BukkitRunnable(){
                                @Override
                                public void run() {
                                    if(hologram.isDeleted()) this.cancel();
                                    VisibilityManager vm = hologram.getVisibilityManager();
                                    for(Player player : Bukkit.getOnlinePlayers()) {
                                        vm.showTo(player);
                                        vm.setVisibleByDefault(false);
                                        hologram.removeLine(0);
                                        hologram.insertTextLine(0, Utils.colorRawMessage("PiggyBank.Pig.Name-With-Counter").replaceAll("%money%", plugin.getFileManager().getUsersConfig().get("users." + player.getUniqueId()).toString()));
                                    }
                                }
                            }.runTaskTimer(Main.getInstance(), 10, 10);
                        }
                        hologram.appendTextLine(Utils.colorRawMessage("PiggyBank.Pig.Name"));
                        for(String s : Utils.colorRawMessage("PiggyBank.Pig.Name-Description").split(";")){
                            hologram.appendTextLine(s);
                        }
                        loadedPiggyBanks.add(new PiggyBank((Pig) entity, entity.getLocation(), hologram));
                        i++;
                    }
                }
            }
        }
    }

    public void teleportScheduler() {
        Bukkit.getScheduler().runTaskTimer(Main.getInstance(), () -> {
            for(PiggyBank pgb : loadedPiggyBanks) {
                pgb.getPiggyBankEntity().teleport(pgb.getPigLocation());
            }
        }, 20 * 3, 20 * 3);
    }

    public List<PiggyBank> getLoadedPiggyBanks() {
        return loadedPiggyBanks;
    }

    public void setLoadedPiggyBanks(List<PiggyBank> loadedPiggyBanks) {
        this.loadedPiggyBanks = loadedPiggyBanks;
    }
}
