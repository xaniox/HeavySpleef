package de.matzefratze123.heavyspleef.flag.defaults;

import java.util.List;

import org.bukkit.Location;

import de.matzefratze123.heavyspleef.core.event.GameListener;
import de.matzefratze123.heavyspleef.core.event.GameListener.Priority;
import de.matzefratze123.heavyspleef.core.event.PlayerLeaveGameEvent;
import de.matzefratze123.heavyspleef.core.flag.Flag;
import de.matzefratze123.heavyspleef.flag.presets.LocationFlag;

@Flag(name = "leavepoint")
public class FlagLeavepoint extends LocationFlag {

	@Override
	public void getDescription(List<String> description) {
		description.add("Defines the general teleportation point when leaving a spleef game");
	}
	
	@GameListener(priority = Priority.LOW)
	public void onPlayerLeaveGame(PlayerLeaveGameEvent event) {
		Location value = getValue();
		if (value == null) {
			return;
		}
		
		event.setTeleportationLocation(value);
	}

}
