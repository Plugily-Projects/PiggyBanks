package pl.plajer.piggybanks.utils;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.BlockIterator;
import pl.plajer.piggybanks.Main;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Plajer
 * <p>
 * Created at 9 lis 2017
 */
public class Utils {

    private static Main plugin = JavaPlugin.getPlugin(Main.class);

    public static String colorFileMessage(String message) {
        return ChatColor.translateAlternateColorCodes('&', plugin.getFileManager().getMessagesConfig().getString(message));
    }

    public static Entity getTargetEntity(Player p){
        List<Entity> nearbyE = p.getNearbyEntities(10, 10, 10);
        ArrayList<LivingEntity> livingE = new ArrayList<>();
        for(Entity e : nearbyE) {
            if(e instanceof LivingEntity) {
                livingE.add((LivingEntity) e);
            }
        }

        Entity target = null;
        BlockIterator bItr = new BlockIterator(p, 10);
        Block block;
        Location loc;
        int bx, by, bz;
        double ex, ey, ez;
        // loop through player's line of sight
        while(bItr.hasNext()) {
            block = bItr.next();
            bx = block.getX();
            by = block.getY();
            bz = block.getZ();
            // check for entities near this block in the line of sight
            for(LivingEntity e : livingE) {
                loc = e.getLocation();
                ex = loc.getX();
                ey = loc.getY();
                ez = loc.getZ();
                if((bx - .75 <= ex && ex <= bx + 1.75) && (bz - .75 <= ez && ez <= bz + 1.75) && (by - 1 <= ey && ey <= by + 2.5)) {
                    // entity is close enough, set target and stop
                    target = e;
                    break;
                }
            }
        }
        return target;
    }

}
