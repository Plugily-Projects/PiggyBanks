package pl.plajer.piggybanks;

import java.util.List;

import com.gmail.filoghost.holographicdisplays.api.VisibilityManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Player;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import org.bukkit.scheduler.BukkitRunnable;
import pl.plajer.piggybanks.utils.Utils;

public class Commands implements CommandExecutor {

    private Main plugin;

    public Commands(Main plugin) {
        this.plugin = plugin;
        plugin.getCommand("piggybanks").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(cmd.getName().equalsIgnoreCase("piggybanks")) {
            if(args.length == 0) {
                sender.sendMessage(Utils.colorRawMessage("PiggyBank.Command.Help-Command.Header"));
                sender.sendMessage(Utils.colorRawMessage("PiggyBank.Command.Help-Command.Description"));
                return true;
            }
            if(args[0].equalsIgnoreCase("create")) {
                if(!(sender instanceof Player)) {
                    sender.sendMessage(Utils.colorRawMessage("PiggyBank.Command.Only-Player"));
                    return true;
                }
                if(!(sender.hasPermission("piggybanks.admin.create"))) {
                    sender.sendMessage(Utils.colorRawMessage("PiggyBank.Command.No-Permission"));
                    return true;
                }
                if(args.length == 1){
                    sender.sendMessage(Utils.colorRawMessage("PiggyBank.Command.Pig-Type"));
                    return true;
                }
                if(!(args[1].equalsIgnoreCase("adult") || args[1].equalsIgnoreCase("baby"))){
                    sender.sendMessage(Utils.colorRawMessage("PiggyBank.Command.Pig-Type"));
                    return true;
                }
                Player p = (Player) sender;
                Pig pig = (Pig) p.getWorld().spawnEntity(p.getLocation(), EntityType.PIG);
                pig.setAI(false);
                pig.setCollidable(false);
                if(args[1].equalsIgnoreCase("adult")){
                    pig.setAdult();
                } else{
                    pig.setBaby();
                }
                pig.setAgeLock(true);
                pig.setInvulnerable(true);
                List<String> list = plugin.getFileManager().getPiggyBanksConfig().getStringList("piggybanks");
                list.add(pig.getUniqueId().toString());
                plugin.getFileManager().getPiggyBanksConfig().set("piggybanks", list);
                plugin.getFileManager().savePiggyBanksConfig();
                Hologram hologram = HologramsAPI.createHologram(plugin, pig.getLocation().clone().add(0, 2.2, 0));
                if(plugin.getProtocolLibUse()) {
                    new BukkitRunnable(){
                        @Override
                        public void run() {
                            if(hologram.isDeleted()) this.cancel();
                            VisibilityManager vm = hologram.getVisibilityManager();
                            for(Player player : Bukkit.getOnlinePlayers()) {
                                if(hologram.isDeleted()) this.cancel();
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
                sender.sendMessage(Utils.colorRawMessage("PiggyBank.Pig.Created-Successfully"));
                List<PiggyBank> pigBanks = plugin.getPiggyManager().getLoadedPiggyBanks();
                pigBanks.add(new PiggyBank(pig, pig.getLocation(), hologram));
                plugin.getPiggyManager().setLoadedPiggyBanks(pigBanks);
                return true;
            }
            if(args[0].equalsIgnoreCase("remove")) {
                if(!(sender instanceof Player)) {
                    sender.sendMessage(Utils.colorRawMessage("PiggyBank.Command.Only-Player"));
                    return true;
                }
                if(!(sender.hasPermission("piggybanks.admin.remove"))) {
                    sender.sendMessage(Utils.colorRawMessage("PiggyBank.Command.No-Permission"));
                    return true;
                }
                Entity target = Utils.getTargetEntity((Player) sender);
                if(target == null) {
                    sender.sendMessage(Utils.colorRawMessage("PiggyBank.Pig.Target-Invalid"));
                    return true;
                } else {
                    if(!target.getType().equals(EntityType.PIG)) {
                        sender.sendMessage(Utils.colorRawMessage("PiggyBank.Pig.Target-Invalid"));
                        return true;
                    }
                    List<String> list = plugin.getFileManager().getPiggyBanksConfig().getStringList("piggybanks");
                    if(!list.contains(target.getUniqueId().toString())) {
                        sender.sendMessage(Utils.colorRawMessage("PiggyBank.Pig.Target-Invalid"));
                        return true;
                    }
                    list.remove(target.getUniqueId().toString());
                    plugin.getFileManager().getPiggyBanksConfig().set("piggybanks", list);
                    plugin.getFileManager().savePiggyBanksConfig();
                    for(PiggyBank pgb : plugin.getPiggyManager().getLoadedPiggyBanks()) {
                        if(pgb.getPiggyBankEntity().equals(target)) {
                            pgb.getPiggyBankEntity().remove();
                            pgb.getPiggyHologram().delete();
                            plugin.getPiggyManager().getLoadedPiggyBanks().remove(pgb);
                            sender.sendMessage(Utils.colorRawMessage("PiggyBank.Pig.Removed"));
                            return true;
                        }
                    }
                    return true;
                }
            }
            if(args[0].equalsIgnoreCase("list")) {
                if(!(sender.hasPermission("piggybanks.admin.list"))) {
                    sender.sendMessage(Utils.colorRawMessage("PiggyBank.Command.No-Permission"));
                    return true;
                }
                sender.sendMessage(Utils.colorRawMessage("PiggyBank.Command.Loaded-Piggies"));
                int i = 0;
                for(PiggyBank pgb : plugin.getPiggyManager().getLoadedPiggyBanks()) {
                    sender.sendMessage(ChatColor.GOLD + "x: " + pgb.getPigLocation().getBlockX() + " y: " + pgb.getPigLocation().getBlockY() + " z: " + pgb.getPigLocation().getBlockZ());
                    i++;
                }
                if(i == 0) {
                    sender.sendMessage(Utils.colorRawMessage("PiggyBank.Command.No-Loaded-Piggies"));
                }
                return true;
            }
        }
        return false;
    }


}
