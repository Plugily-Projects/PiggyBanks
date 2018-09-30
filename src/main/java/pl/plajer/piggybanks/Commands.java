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

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.gmail.filoghost.holographicdisplays.api.VisibilityManager;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import pl.plajer.piggybanks.piggy.PiggyBank;
import pl.plajer.piggybanks.utils.Utils;
import pl.plajerlair.core.services.exception.ReportedException;
import pl.plajerlair.core.utils.ConfigUtils;

public class Commands implements CommandExecutor {

  private Main plugin;

  public Commands(Main plugin) {
    this.plugin = plugin;
    plugin.getCommand("piggybanks").setExecutor(this);
  }

  private boolean isSenderPlayer(CommandSender sender) {
    if (!(sender instanceof Player)) {
      sender.sendMessage(Utils.colorMessage("PiggyBank.Command.Only-Player"));
      return false;
    }
    return true;
  }

  private boolean hasPermission(CommandSender sender, String permission) {
    if (!sender.hasPermission(permission)) {
      sender.sendMessage(Utils.colorMessage("PiggyBank.Command.No-Permission"));
      return false;
    }
    return true;
  }

  @Override
  public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
    try {
      if (cmd.getName().equalsIgnoreCase("piggybanks")) {
        if (args.length == 0) {
          sender.sendMessage(Utils.colorMessage("PiggyBank.Command.Help-Command.Header"));
          sender.sendMessage(Utils.colorMessage("PiggyBank.Command.Help-Command.Description"));
          return true;
        }
        if (args[0].equalsIgnoreCase("create")) {
          if (!isSenderPlayer(sender)) {
            return true;
          }
          if (!hasPermission(sender, "piggybanks.admin.create")) {
            return true;
          }
          if (args.length == 1) {
            sender.sendMessage(Utils.colorMessage("PiggyBank.Command.Pig-Type"));
            return true;
          }
          if (!(args[1].equalsIgnoreCase("adult") || args[1].equalsIgnoreCase("baby"))) {
            sender.sendMessage(Utils.colorMessage("PiggyBank.Command.Pig-Type"));
            return true;
          }
          Player p = (Player) sender;
          Pig pig = (Pig) p.getWorld().spawnEntity(p.getLocation(), EntityType.PIG);
          pig.setAI(false);
          pig.setCollidable(false);
          if (args[1].equalsIgnoreCase("adult")) {
            pig.setAdult();
          } else {
            pig.setBaby();
          }
          pig.setAgeLock(true);
          List<String> list = ConfigUtils.getConfig(plugin, "piggybanks").getStringList("piggybanks");
          list.add(pig.getUniqueId().toString());
          FileConfiguration config = ConfigUtils.getConfig(plugin, "piggybanks");
          config.set("piggybanks", list);
          ConfigUtils.saveConfig(plugin, config, "piggybanks");
          Hologram hologram = HologramsAPI.createHologram(plugin, pig.getLocation().clone().add(0, 2.2, 0));
          if (plugin.getServer().getPluginManager().isPluginEnabled("ProtocolLib")) {
            new BukkitRunnable() {
              @Override
              public void run() {
                if (hologram.isDeleted()) {
                  this.cancel();
                }
                VisibilityManager vm = hologram.getVisibilityManager();
                for (Player player : Bukkit.getOnlinePlayers()) {
                  if (hologram.isDeleted()) {
                    this.cancel();
                  }
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
          sender.sendMessage(Utils.colorMessage("PiggyBank.Pig.Created-Successfully"));
          List<PiggyBank> pigBanks = plugin.getPiggyManager().getLoadedPiggyBanks();
          pigBanks.add(new PiggyBank(pig, pig.getLocation(), hologram));
          plugin.getPiggyManager().setLoadedPiggyBanks(pigBanks);
          return true;
        }
        if (args[0].equalsIgnoreCase("remove")) {
          if (!isSenderPlayer(sender)) {
            return true;
          }
          if (!hasPermission(sender, "piggybanks.admin.remove")) {
            return true;
          }
          Entity target = Utils.getTargetEntity((Player) sender);
          if (target == null) {
            sender.sendMessage(Utils.colorMessage("PiggyBank.Pig.Target-Invalid"));
            return true;
          } else {
            if (!target.getType().equals(EntityType.PIG)) {
              sender.sendMessage(Utils.colorMessage("PiggyBank.Pig.Target-Invalid"));
              return true;
            }
            List<String> list = ConfigUtils.getConfig(plugin, "piggybanks").getStringList("piggybanks");
            if (!list.contains(target.getUniqueId().toString())) {
              sender.sendMessage(Utils.colorMessage("PiggyBank.Pig.Target-Invalid"));
              return true;
            }
            list.remove(target.getUniqueId().toString());
            FileConfiguration config = ConfigUtils.getConfig(plugin, "piggybanks");
            config.set("piggybanks", list);
            ConfigUtils.saveConfig(plugin, config, "piggybanks");
            for (PiggyBank pgb : plugin.getPiggyManager().getLoadedPiggyBanks()) {
              if (pgb.getPiggyBankEntity().equals(target)) {
                pgb.getPiggyBankEntity().remove();
                pgb.getPiggyHologram().delete();
                plugin.getPiggyManager().getLoadedPiggyBanks().remove(pgb);
                sender.sendMessage(Utils.colorMessage("PiggyBank.Pig.Removed"));
                return true;
              }
            }
            return true;
          }
        }
        if (args[0].equalsIgnoreCase("list")) {
          if (!hasPermission(sender, "piggybanks.admin.list")) {
            return true;
          }
          sender.sendMessage(Utils.colorMessage("PiggyBank.Command.Loaded-Piggies"));
          int i = 0;
          for (PiggyBank pgb : plugin.getPiggyManager().getLoadedPiggyBanks()) {
            sender.sendMessage(ChatColor.GOLD + "x: " + pgb.getPigLocation().getBlockX() + " y: " + pgb.getPigLocation().getBlockY() + " z: " + pgb.getPigLocation().getBlockZ());
            i++;
          }
          if (i == 0) {
            sender.sendMessage(Utils.colorMessage("PiggyBank.Command.No-Loaded-Piggies"));
          }
          return true;
        }
      }
      return false;
    } catch (Exception ex) {
      new ReportedException(plugin, ex);
      return false;
    }
  }


}
