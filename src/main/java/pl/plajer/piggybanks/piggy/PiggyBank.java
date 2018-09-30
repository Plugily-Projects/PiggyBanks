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

import org.bukkit.Location;
import org.bukkit.entity.Pig;

public class PiggyBank {

  private Pig piggyBankEntity;
  private Location pigLocation;
  private Hologram piggyHologram;

  public PiggyBank(Pig piggyBankEntity, Location pigLocation, Hologram piggyHologram) {
    this.piggyBankEntity = piggyBankEntity;
    this.pigLocation = pigLocation;
    this.piggyHologram = piggyHologram;
  }

  public Pig getPiggyBankEntity() {
    return piggyBankEntity;
  }

  public Location getPigLocation() {
    return pigLocation;
  }

  public Hologram getPiggyHologram() {
    return piggyHologram;
  }

}
