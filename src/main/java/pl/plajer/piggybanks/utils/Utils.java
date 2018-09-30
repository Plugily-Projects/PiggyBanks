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

package pl.plajer.piggybanks.utils;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.BlockIterator;

import pl.plajer.piggybanks.Main;
import pl.plajerlair.core.utils.ConfigUtils;

/**
 * @author Plajer
 * <p>
 * Created at 9 lis 2017
 */
public class Utils {

  public static String colorMessage(String message) {
    return ChatColor.translateAlternateColorCodes('&', ConfigUtils.getConfig(JavaPlugin.getPlugin(Main.class), "messages").getString(message));
  }

  public static Entity getTargetEntity(Player p) {
    List<Entity> nearbyE = p.getNearbyEntities(10, 10, 10);
    ArrayList<LivingEntity> livingE = new ArrayList<>();
    for (Entity e : nearbyE) {
      if (e instanceof LivingEntity) {
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
    while (bItr.hasNext()) {
      block = bItr.next();
      bx = block.getX();
      by = block.getY();
      bz = block.getZ();
      // check for entities near this block in the line of sight
      for (LivingEntity e : livingE) {
        loc = e.getLocation();
        ex = loc.getX();
        ey = loc.getY();
        ez = loc.getZ();
        if ((bx - .75 <= ex && ex <= bx + 1.75) && (bz - .75 <= ez && ez <= bz + 1.75) && (by - 1 <= ey && ey <= by + 2.5)) {
          // entity is close enough, set target and stop
          target = e;
          break;
        }
      }
    }
    return target;
  }

}
