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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Item;
import org.bukkit.entity.Pig;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import pl.plajer.piggybanks.piggy.PiggyBank;
import pl.plajer.piggybanks.utils.Utils;
import pl.plajerlair.core.services.exception.ReportedException;
import pl.plajerlair.core.utils.ConfigUtils;

/**
 * @author Plajer
 * <p>
 * Created at 9 lis 2017
 */
public class MenuHandler implements Listener {

  private Material dropItem;
  private Main plugin;

  public MenuHandler(Main plugin) {
    this.plugin = plugin;
    plugin.getServer().getPluginManager().registerEvents(this, plugin);
    try {
      dropItem = Material.valueOf(plugin.getConfig().getString("drop-item").toUpperCase());
    } catch (Exception e) {
      Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[PiggyBanks] Drop item is invalid, using default one...");
      //todo migrator
      dropItem = Material.GOLD_INGOT;
    }
  }

  @EventHandler
  public void onMenuClick(InventoryClickEvent e) {
    try {
      if (e.getInventory() == null || e.getCurrentItem() == null) {
        return;
      }
      if (e.getInventory().getName().equals(Utils.colorMessage("PiggyBank.Menu.Title"))) {
        e.setCancelled(true);
        if (plugin.getPiggyListeners().getOpenedPiggies().get(e.getWhoClicked()) == null) {
          e.getWhoClicked().sendMessage(Utils.colorMessage("PiggyBank.Menu.Error-Occurred"));
          return;
        }
        Pig piggyBank = plugin.getPiggyListeners().getOpenedPiggies().get(e.getWhoClicked());
        for (PiggyBank pgb : plugin.getPiggyManager().getLoadedPiggyBanks()) {
          if (pgb.getPiggyBankEntity().equals(piggyBank)) {
            if (e.getCurrentItem().getType().equals(Material.ENDER_CHEST)) {
              e.getWhoClicked().openInventory(e.getWhoClicked().getEnderChest());
              return;
            }
            if (e.getCurrentItem().getType().equals(Material.BOOK)) {
              return;
            }
            if (ConfigUtils.getConfig(plugin, "users").getInt("users." + e.getWhoClicked().getUniqueId()) > 0) {
              String clickedItem = e.getCurrentItem().getItemMeta().getDisplayName();
              Integer number = null;
              Pattern p = Pattern.compile("\\d+");
              Matcher m = p.matcher(clickedItem);
              while (m.find()) {
                number = Integer.valueOf(m.group());
              }
              if (number == null) {
                number = ConfigUtils.getConfig(plugin, "users").getInt("users." + e.getWhoClicked().getUniqueId());
              }
              if (!(ConfigUtils.getConfig(plugin, "users").getInt("users." + e.getWhoClicked().getUniqueId()) >= number)) {
                e.getWhoClicked().sendMessage(Utils.colorMessage("PiggyBank.Money-Not-Enough-Money-In-Piggy"));
                return;
              }
              piggyBank.getWorld().spawnParticle(Particle.HEART, piggyBank.getLocation().clone().add(0, 1, 0), 1);
              plugin.getEconomy().depositPlayer(Bukkit.getOfflinePlayer(e.getWhoClicked().getUniqueId()), number);
              FileConfiguration config = ConfigUtils.getConfig(plugin, "users");
              config.set("users." + e.getWhoClicked().getUniqueId(), config.getInt("users." + e.getWhoClicked().getUniqueId()) - number);
              ConfigUtils.saveConfig(plugin, config, "users");
              e.getWhoClicked().sendMessage(Utils.colorMessage("PiggyBank.Money-Withdrawn").replaceAll("%coins%", String.valueOf(number)));
              final Item item = piggyBank.getWorld().dropItemNaturally(piggyBank.getLocation().clone().add(0, 0.25, 0), new ItemStack(dropItem));
              item.setPickupDelay(10000);
              Bukkit.getScheduler().runTaskLater(plugin, item::remove, 25);
              ItemStack balance = new ItemStack(Material.BOOK, 1);
              ItemMeta balanceMeta = balance.getItemMeta();
              balanceMeta.setDisplayName(Utils.colorMessage("PiggyBank.Menu.Balance-Icon").replaceAll("%coins%",
                  String.valueOf(ConfigUtils.getConfig(plugin, "users").get("users." + e.getWhoClicked().getUniqueId()))).replaceAll("null", "0"));
              balance.setItemMeta(balanceMeta);
              e.getInventory().setItem(4, balance);
            } else {
              e.getWhoClicked().sendMessage(Utils.colorMessage("PiggyBank.Money-No-Money-In-Piggy"));
            }
          }
        }
      }
    } catch (Exception ex) {
      new ReportedException(plugin, ex);
    }
  }

  @EventHandler
  public void onMenuClose(InventoryCloseEvent e) {
    try {
      if (e.getInventory() == null) {
        return;
      }
      if (e.getInventory().getName().equals(Utils.colorMessage("PiggyBank.Menu.Title"))) {
        plugin.getPiggyListeners().getOpenedPiggies().remove(e.getPlayer());
      }
    } catch (Exception ex) {
      new ReportedException(plugin, ex);
    }
  }

}
