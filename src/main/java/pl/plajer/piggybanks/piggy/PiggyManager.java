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

package pl.plajer.piggybanks.piggy;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.gmail.filoghost.holographicdisplays.api.VisibilityManager;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import pl.plajer.piggybanks.Main;
import pl.plajer.piggybanks.utils.Utils;
import pl.plajerlair.core.utils.ConfigUtils;

public class PiggyManager {

  private List<PiggyBank> loadedPiggyBanks = new ArrayList<>();
  private Main plugin;

  public PiggyManager(Main plugin) {
    this.plugin = plugin;
    loadPiggyBanks();
    teleportScheduler();
  }

  private void loadPiggyBanks() {
    List<String> piggies = ConfigUtils.getConfig(plugin, "piggybanks").getStringList("piggybanks");
    List<UUID> uuids = new ArrayList<>();
    if (piggies != null) {
      for (String s : piggies) {
        UUID u = UUID.fromString(s);
        uuids.add(u);
      }
    }
    if (uuids.isEmpty()) {
      return;
    }
    for (World world : Bukkit.getServer().getWorlds()) {
      for (Entity entity : Bukkit.getServer().getWorld(world.getName()).getEntities()) {
        if (entity instanceof Pig) {
          if (uuids.contains(entity.getUniqueId())) {
            final Hologram hologram = HologramsAPI.createHologram(plugin, entity.getLocation().clone().add(0, 2.2, 0));
            if (plugin.getServer().getPluginManager().isPluginEnabled("ProtocolLib")) {
              new BukkitRunnable() {
                @Override
                public void run() {
                  if (hologram.isDeleted()) {
                    this.cancel();
                  }
                  VisibilityManager vm = hologram.getVisibilityManager();
                  for (Player player : Bukkit.getOnlinePlayers()) {
                    vm.showTo(player);
                    vm.setVisibleByDefault(false);
                    hologram.removeLine(0);
                    hologram.insertTextLine(0, Utils.colorMessage("PiggyBank.Pig.Name-With-Counter").replaceAll("%money%",
                        ConfigUtils.getConfig(plugin, "users").get("users." + player.getUniqueId()).toString()));
                  }
                }
              }.runTaskTimer(plugin, 10, 10);
            }
            hologram.appendTextLine(Utils.colorMessage("PiggyBank.Pig.Name"));
            for (String s : Utils.colorMessage("PiggyBank.Pig.Name-Description").split(";")) {
              hologram.appendTextLine(s);
            }
            loadedPiggyBanks.add(new PiggyBank((Pig) entity, entity.getLocation(), hologram));
          }
        }
      }
    }
  }

  private void teleportScheduler() {
    Bukkit.getScheduler().runTaskTimer(plugin, () -> {
      for (PiggyBank pgb : loadedPiggyBanks) {
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
