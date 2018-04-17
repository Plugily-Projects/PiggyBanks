package pl.plajer.piggybanks;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.gmail.filoghost.holographicdisplays.api.VisibilityManager;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import pl.plajer.piggybanks.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PiggyManager {

    @Getter
    @Setter
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
        for(World world : Bukkit.getServer().getWorlds()) {
            for(Entity entity : Bukkit.getServer().getWorld(world.getName()).getEntities()) {
                if(entity instanceof Pig) {
                    if(uuids.contains(entity.getUniqueId())) {
                        final Hologram hologram = HologramsAPI.createHologram(plugin, entity.getLocation().clone().add(0, 2.2, 0));
                        if(plugin.isEnabled("ProtocolLib")) {
                            new BukkitRunnable(){
                                @Override
                                public void run() {
                                    if(hologram.isDeleted()) this.cancel();
                                    VisibilityManager vm = hologram.getVisibilityManager();
                                    for(Player player : Bukkit.getOnlinePlayers()) {
                                        vm.showTo(player);
                                        vm.setVisibleByDefault(false);
                                        hologram.removeLine(0);
                                        hologram.insertTextLine(0, Utils.colorFileMessage("PiggyBank.Pig.Name-With-Counter").replaceAll("%money%", plugin.getFileManager().getUsersConfig().get("users." + player.getUniqueId()).toString()));
                                    }
                                }
                            }.runTaskTimer(plugin, 10, 10);
                        }
                        hologram.appendTextLine(Utils.colorFileMessage("PiggyBank.Pig.Name"));
                        for(String s : Utils.colorFileMessage("PiggyBank.Pig.Name-Description").split(";")){
                            hologram.appendTextLine(s);
                        }
                        loadedPiggyBanks.add(new PiggyBank((Pig) entity, entity.getLocation(), hologram));
                    }
                }
            }
        }
    }

    public void teleportScheduler() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for(PiggyBank pgb : loadedPiggyBanks) {
                pgb.getPiggyBankEntity().teleport(pgb.getPigLocation());
            }
        }, 20 * 3, 20 * 3);
    }
}
