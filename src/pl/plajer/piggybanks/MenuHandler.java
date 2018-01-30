package pl.plajer.piggybanks;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Material;
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
        if(e.getInventory().getName().equals(Utils.colorRawMessage("PiggyBank.Menu.Title"))) {
            e.setCancelled(true);
            if(plugin.getPiggyListeners().getOpenedPiggies().get(e.getWhoClicked()) == null) {
                e.getWhoClicked().sendMessage(Utils.colorRawMessage("PiggyBank.Menu.Error-Occurred"));
                e.setCancelled(true);
                return;
            }
            Pig piggyBank = plugin.getPiggyListeners().getOpenedPiggies().get(e.getWhoClicked());
            for(PiggyBank pgb : plugin.getPiggyManager().getLoadedPiggyBanks()) {
                if(pgb.getPiggyBankEntity().equals(piggyBank)) {
                    if(plugin.getFileManager().getUsersConfig().getInt("users." + e.getWhoClicked().getUniqueId()) > 0) {
                        String clickedItem = e.getCurrentItem().getItemMeta().getDisplayName();
                        Integer number = null;
                        Pattern p = Pattern.compile("\\d+");
                        Matcher m = p.matcher(clickedItem);
                        while(m.find()) {
                            number = Integer.valueOf(m.group());
                        }
                        if(number == null) {
                            number = plugin.getFileManager().getUsersConfig().getInt("users." + e.getWhoClicked().getUniqueId());
                        }
                        if(!(plugin.getFileManager().getUsersConfig().getInt("users." + e.getWhoClicked().getUniqueId()) >= number)) {
                            e.getWhoClicked().sendMessage(Utils.colorRawMessage("PiggyBank.Money-Not-Enough-Money-In-Piggy"));
                            return;
                        }
                        piggyBank.getWorld().playEffect(piggyBank.getLocation().clone().add(0, 1, 0), Effect.HEART, 1);
                        plugin.getEconomy().depositPlayer(Bukkit.getOfflinePlayer(e.getWhoClicked().getUniqueId()), number);
                        plugin.getFileManager().getUsersConfig().set("users." + e.getWhoClicked().getUniqueId(), plugin.getFileManager().getUsersConfig().getInt("users." + e.getWhoClicked().getUniqueId()) - number);
                        plugin.getFileManager().saveUsersConfig();
                        e.getWhoClicked().sendMessage(Utils.colorRawMessage("PiggyBank.Money-Withdrawn").replaceAll("%coins%", String.valueOf(number)));
                        final Item item = piggyBank.getWorld().dropItemNaturally(piggyBank.getLocation().clone().add(0, 0.25, 0), new ItemStack(Material.GOLD_INGOT));
                        item.setPickupDelay(10000);
                        Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> item.remove(), 25);
                        ItemStack balance = new ItemStack(Material.BOOK, 1);
                        ItemMeta balanceMeta = balance.getItemMeta();
                        balanceMeta.setDisplayName(Utils.colorRawMessage("PiggyBank.Menu.Balance-Icon").replaceAll("%coins%", String.valueOf(plugin.getFileManager().getUsersConfig().get("users." + e.getWhoClicked().getUniqueId()))).replaceAll("null", "0"));
                        balance.setItemMeta(balanceMeta);
                        e.getInventory().setItem(4, balance);
                    } else {
                        e.getWhoClicked().sendMessage(Utils.colorRawMessage("PiggyBank.Money-No-Money-In-Piggy"));
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
        if(e.getInventory().getName().equals(Utils.colorRawMessage("PiggyBank.Menu.Title"))) {
            plugin.getPiggyListeners().getOpenedPiggies().remove(e.getPlayer());
        }
    }

}
