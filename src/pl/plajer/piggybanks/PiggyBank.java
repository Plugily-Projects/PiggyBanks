package pl.plajer.piggybanks;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.entity.Pig;

@Getter
@AllArgsConstructor
public class PiggyBank {

    private Pig piggyBankEntity;
    private Location pigLocation;
    private Hologram piggyHologram;

}
