package de.matzefratze123.heavyspleef.flag.defaults;

import java.util.List;

import org.bukkit.entity.Player;

import de.matzefratze123.heavyspleef.core.flag.Flag;
import de.matzefratze123.heavyspleef.core.player.SpleefPlayer;
import de.matzefratze123.heavyspleef.flag.presets.BooleanFlag;

@Flag(name = "allow-fly", parent = FlagSpectate.class)
public class FlagAllowSpectateFly extends BooleanFlag {
	
	@Override
	public void getDescription(List<String> description) {
		description.add("Enables the ability to fly while spectating");
	}
	
	/* Spectate leave is handled by restoring the player state in parent flag */
	public void onSpectateEnter(SpleefPlayer spleefPlayer) {		
		boolean value = getValue();
		Player player = spleefPlayer.getBukkitPlayer();
		
		player.setAllowFlight(value);
		player.setFlying(value);
	}

}
