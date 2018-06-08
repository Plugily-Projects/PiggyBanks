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

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Item;
import org.bukkit.entity.Pig;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import pl.plajer.piggybanks.utils.Utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Plajer
 * <p>
 * Created at 9 lis 2017
 */
public class MenuHandler implements Listener {

    private Main plugin;

    public MenuHandler(Main plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @SuppressWarnings("deprecation")
    @EventHandler
    public void onMenuClick(InventoryClickEvent e) {
        if(e.getInventory() == null || e.getCurrentItem() == null) {
            return;
        }
        if(e.getInventory().getName().equals(Utils.colorFileMessage("PiggyBank.Menu.Title"))) {
            e.setCancelled(true);
            if(plugin.getPiggyListeners().getOpenedPiggies().get(e.getWhoClicked()) == null) {
                e.getWhoClicked().sendMessage(Utils.colorFileMessage("PiggyBank.Menu.Error-Occurred"));
                e.setCancelled(true);
                return;
            }
            Pig piggyBank = plugin.getPiggyListeners().getOpenedPiggies().get(e.getWhoClicked());
            for(PiggyBank pgb : plugin.getPiggyManager().getLoadedPiggyBanks()) {
                if(pgb.getPiggyBankEntity().equals(piggyBank)) {
                    if(e.getCurrentItem().getType().equals(Material.ENDER_CHEST)){
                        e.getWhoClicked().openInventory(e.getWhoClicked().getEnderChest());
                        return;
                    }
                    if(ConfigurationManager.getConfig("users").getInt("users." + e.getWhoClicked().getUniqueId()) > 0) {
                        String clickedItem = e.getCurrentItem().getItemMeta().getDisplayName();
                        Integer number = null;
                        Pattern p = Pattern.compile("\\d+");
                        Matcher m = p.matcher(clickedItem);
                        while(m.find()) {
                            number = Integer.valueOf(m.group());
                        }
                        if(number == null) {
                            number = ConfigurationManager.getConfig("users").getInt("users." + e.getWhoClicked().getUniqueId());
                        }
                        if(!(ConfigurationManager.getConfig("users").getInt("users." + e.getWhoClicked().getUniqueId()) >= number)) {
                            e.getWhoClicked().sendMessage(Utils.colorFileMessage("PiggyBank.Money-Not-Enough-Money-In-Piggy"));
                            return;
                        }
                        piggyBank.getWorld().playEffect(piggyBank.getLocation().clone().add(0, 1, 0), Effect.HEART, 1);
                        plugin.getEconomy().depositPlayer(Bukkit.getOfflinePlayer(e.getWhoClicked().getUniqueId()), number);
                        FileConfiguration config = ConfigurationManager.getConfig("users");
                        config.set("users." + e.getWhoClicked().getUniqueId(), config.getInt("users." + e.getWhoClicked().getUniqueId()) - number);
                        ConfigurationManager.saveConfig(config, "users");
                        e.getWhoClicked().sendMessage(Utils.colorFileMessage("PiggyBank.Money-Withdrawn").replaceAll("%coins%", String.valueOf(number)));
                        final Item item = piggyBank.getWorld().dropItemNaturally(piggyBank.getLocation().clone().add(0, 0.25, 0), new ItemStack(Material.GOLD_INGOT));
                        item.setPickupDelay(10000);
                        Bukkit.getScheduler().runTaskLater(plugin, item::remove, 25);
                        ItemStack balance = new ItemStack(Material.BOOK, 1);
                        ItemMeta balanceMeta = balance.getItemMeta();
                        balanceMeta.setDisplayName(Utils.colorFileMessage("PiggyBank.Menu.Balance-Icon").replaceAll("%coins%", String.valueOf(ConfigurationManager.getConfig("users").get("users." + e.getWhoClicked().getUniqueId()))).replaceAll("null", "0"));
                        balance.setItemMeta(balanceMeta);
                        e.getInventory().setItem(4, balance);
                    } else {
                        e.getWhoClicked().sendMessage(Utils.colorFileMessage("PiggyBank.Money-No-Money-In-Piggy"));
                    }
                }
            }
        }
    }

    @EventHandler
    public void onMenuClose(InventoryCloseEvent e) {
        if(e.getInventory() == null) {
            return;
        }
        if(e.getInventory().getName().equals(Utils.colorFileMessage("PiggyBank.Menu.Title"))) {
            plugin.getPiggyListeners().getOpenedPiggies().remove(e.getPlayer());
        }
    }

}
