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

import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import pl.plajer.piggybanks.Main;
import pl.plajerlair.core.services.exception.ReportedException;
import pl.plajerlair.core.utils.MigratorUtils;

/**
 * @author Plajer
 * <p>
 * Created at 21.07.2018
 */
public class LanguageMigrator {

  public static final int CONFIG_FILE_VERSION = 2;
  private static Main plugin = JavaPlugin.getPlugin(Main.class);

  public static void configUpdate() {
    try {
      if (plugin.getConfig().getInt("File-Version-Do-Not-Edit") == CONFIG_FILE_VERSION) {
        return;
      }
      Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "[PiggyBanks] [System notify] Your config file is outdated! Updating...");
      File file = new File(plugin.getDataFolder() + "/config.yml");

      MigratorUtils.removeLineFromFile(file, "File-Version-Do-Not-Edit: " + plugin.getConfig().getInt("File-Version-Do-Not-Edit"));

      switch (plugin.getConfig().getInt("File-Version-Do-Not-Edit")) {
        case 1:
          MigratorUtils.addNewLines(file, "# Item dropped when player withdraw money from piggy bank\r\ndrop-item: gold_ingot\r\nFile-Version-Do-Not-Edit: 2");
          break;
      }
      Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "[PiggyBanks] [System notify] Config updated, no comments were removed :)");
      Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "[PiggyBanks] [System notify] You're using latest config file version! Nice!");
    } catch (Exception ex) {
      new ReportedException(plugin, ex);
    }
  }

}