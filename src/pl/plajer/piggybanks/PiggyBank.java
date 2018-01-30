package pl.plajer.piggybanks;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import org.bukkit.Location;
import org.bukkit.entity.Pig;

import java.util.UUID;

public class PiggyBank {

    private Pig piggyBankEntity;
    private Hologram piggyHologram;
    private Location pigLocation;

    public PiggyBank(Pig piggyBankEntity, Location pigLocation, Hologram piggyHologram) {
        this.piggyBankEntity = piggyBankEntity;
        this.pigLocation = pigLocation;
        this.piggyHologram = piggyHologram;
    }

    public Pig getPiggyBankEntity() {
        return piggyBankEntity;
    }

    public Hologram getPiggyHologram() {
        return piggyHologram;
    }

    public Location getPigLocation() {
        return pigLocation;
    }
}
