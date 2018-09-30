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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import pl.plajer.piggybanks.Main;
import pl.plajer.piggybanks.utils.Utils;
import pl.plajerlair.core.services.exception.ReportedException;
import pl.plajerlair.core.utils.ConfigUtils;
import pl.plajerlair.core.utils.UpdateChecker;

public class PiggyListeners implements Listener {

  private List<Integer> piggyValues = Arrays.asList(1, 5, 10, 25, 50, 100, -1);
  private Main plugin;
  private Map<Player, Pig> openedPiggies = new HashMap<>();

  public PiggyListeners(Main plugin) {
    this.plugin = plugin;
    plugin.getServer().getPluginManager().registerEvents(this, plugin);
  }


  @EventHandler
  public void onPiggyBankDamage(EntityDamageEvent e) {
    try {
      if (e.getEntity() instanceof Pig) {
        for (PiggyBank pgb : plugin.getPiggyManager().getLoadedPiggyBanks()) {
          if (pgb.getPiggyBankEntity().equals(e.getEntity())) {
            e.setCancelled(true);
          }
        }
      }
    } catch (Exception ex) {
      new ReportedException(plugin, ex);
    }
  }

  @EventHandler
  public void onPiggyBankDamage(EntityDamageByEntityEvent e) {
    try {
      if (e.getEntity() instanceof Pig && e.getDamager() instanceof Player) {
        Player attacker = (Player) e.getDamager();
        Pig piggy = (Pig) e.getEntity();
        for (PiggyBank pgb : plugin.getPiggyManager().getLoadedPiggyBanks()) {
          if (pgb.getPiggyBankEntity().equals(piggy)) {
            e.setCancelled(true);
            if (!e.getDamager().hasPermission("piggybanks.use")) {
              e.getDamager().sendMessage(Utils.colorMessage("PiggyBank.Pig.No-Permission"));
              return;
            }
            openedPiggies.put(attacker, piggy);
            Inventory piggyBank = Bukkit.createInventory(null, 3 * 9, Utils.colorMessage("PiggyBank.Menu.Title"));
            if (plugin.getConfig().getBoolean("enderchest-enabled")) {
              piggyBank = Bukkit.createInventory(null, 5 * 9, Utils.colorMessage("PiggyBank.Menu.Title"));
              ItemStack ender = new ItemStack(Material.ENDER_CHEST, 1);
              ItemMeta enderMeta = ender.getItemMeta();
              enderMeta.setDisplayName(Utils.colorMessage("PiggyBank.Menu.Enderchest-Name"));
              enderMeta.setLore(Collections.singletonList(Utils.colorMessage("PiggyBank.Menu.Enderchest-Lore")));
              ender.setItemMeta(enderMeta);
              piggyBank.setItem(31, ender);
            }
            ItemStack balance = new ItemStack(Material.BOOK, 1);
            ItemMeta balanceMeta = balance.getItemMeta();
            balanceMeta.setDisplayName(Utils.colorMessage("PiggyBank.Menu.Balance-Icon").replaceAll("%coins%",
                String.valueOf(ConfigUtils.getConfig(plugin, "users").get("users." + e.getDamager().getUniqueId()))).replaceAll("null", "0"));
            balance.setItemMeta(balanceMeta);
            piggyBank.setItem(4, balance);
            for (int i = 0; i < 7; i++) {
              ItemStack itemStack;
              if (i == 0) {
                itemStack = new ItemStack(Material.GOLD_NUGGET, 1);
              } else if (i >= 1 && i <= 4) {
                itemStack = new ItemStack(Material.GOLD_INGOT, 1);
              } else {
                itemStack = new ItemStack(Material.GOLD_BLOCK, 1);
              }
              ItemMeta itemMeta = itemStack.getItemMeta();
              itemMeta.setDisplayName(Utils.colorMessage("PiggyBank.Menu.Withdraw-Format").replaceAll("%coins%", String.valueOf(piggyValues.get(i)).replaceAll("-1", "all")));
              itemMeta.setLore(Collections.singletonList(Utils.colorMessage("PiggyBank.Menu.Withdraw-Lore").replaceAll("%coins%", String.valueOf(piggyValues.get(i)).replaceAll("-1", "all"))));
              itemStack.setItemMeta(itemMeta);
              piggyBank.setItem(i + 10, itemStack);
            }
            attacker.openInventory(piggyBank);
            return;
          }
        }
      }
    } catch (Exception ex) {
      new ReportedException(plugin, ex);
    }
  }

  @EventHandler
  public void onPiggyBankClick(PlayerInteractEntityEvent e) {
    try {
      //To avoid event called twice
      if (e.getHand() == EquipmentSlot.OFF_HAND) {
        return;
      }
      if (e.getRightClicked().getType().equals(EntityType.PIG)) {
        for (PiggyBank pgb : plugin.getPiggyManager().getLoadedPiggyBanks()) {
          if (pgb.getPiggyBankEntity().equals(e.getRightClicked())) {
            Integer money = 1;
            if (e.getPlayer().isSneaking()) {
              money = 10;
            }
            if (plugin.getEconomy().getBalance(e.getPlayer()) >= money) {
              plugin.getEconomy().withdrawPlayer(e.getPlayer(), money);
              e.getRightClicked().getWorld().spawnParticle(Particle.LAVA, e.getRightClicked().getLocation(), 1);
              FileConfiguration config = ConfigUtils.getConfig(plugin, "users");
              config.set("users." + e.getPlayer().getUniqueId(), config.getInt("users." + e.getPlayer().getUniqueId()) + money);
              ConfigUtils.saveConfig(plugin, config, "users");
              e.getPlayer().sendMessage(Utils.colorMessage("PiggyBank.Money-Deposited").replaceAll("%coins%", String.valueOf(money)));
            } else {
              e.getPlayer().sendMessage(Utils.colorMessage("PiggyBank.Money-No-Money-Balance"));
            }
          }
        }
      }
    } catch (Exception ex) {
      new ReportedException(plugin, ex);
    }
  }

  @EventHandler
  public void onJoin(PlayerJoinEvent e) {
    try {
      FileConfiguration config = ConfigUtils.getConfig(plugin, "users");
      if (!config.isSet("users." + e.getPlayer().getUniqueId())) {
        config.set("users." + e.getPlayer().getUniqueId(), 0);
        ConfigUtils.saveConfig(plugin, config, "users");
      }
      if (e.getPlayer().hasPermission("piggybabnks.admin.notify")) {
        String currentVersion = "v" + Bukkit.getPluginManager().getPlugin("PiggyBanks").getDescription().getVersion();
        if (plugin.getConfig().getBoolean("update-notify")) {
          try {
            UpdateChecker.checkUpdate(plugin, currentVersion, 52634);
            String latestVersion = UpdateChecker.getLatestVersion();
            if (latestVersion != null) {
              latestVersion = "v" + latestVersion;
              e.getPlayer().sendMessage(Utils.colorMessage("Other.Plugin-Up-To-Date").replaceAll("%old%", currentVersion).replaceAll("%new%", latestVersion));
            }
          } catch (Exception ex) {
            Bukkit.getConsoleSender().sendMessage(Utils.colorMessage("Other.Plugin-Update-Check-Failed"));
          }
        }
      }
    } catch (Exception ex) {
      new ReportedException(plugin, ex);
    }
  }

  @EventHandler
  public void onQuit(PlayerQuitEvent e) {
    openedPiggies.remove(e.getPlayer());
  }

  public Map<Player, Pig> getOpenedPiggies() {
    return openedPiggies;
  }
}
