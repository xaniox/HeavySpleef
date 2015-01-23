package de.matzefratze123.heavyspleef.flag.defaults;

import java.util.List;
import java.util.Map;

import de.matzefratze123.heavyspleef.core.GameProperty;
import de.matzefratze123.heavyspleef.core.QuitCause;
import de.matzefratze123.heavyspleef.core.event.GameListener;
import de.matzefratze123.heavyspleef.core.event.PlayerLeaveGameEvent;
import de.matzefratze123.heavyspleef.flag.presets.LocationFlag;

public class FlagWinPoint extends LocationFlag {

	@Override
	public void defineGameProperties(Map<GameProperty, Object> properties) {}

	@Override
	public boolean hasGameProperties() {
		return false;
	}

	@Override
	public boolean hasBukkitListenerMethods() {
		return false;
	}

	@Override
	public void getDescription(List<String> description) {
		description.add("Defines the teleportation point for the winners of the Spleef game");
	}
	
	@GameListener
	public void onPlayerLeaveGame(PlayerLeaveGameEvent event) {
		if (getValue() == null) {
			return;
		}
		
		if (event.getCause() != QuitCause.WIN) {
			return;
		}
		
		event.setTeleportationLocation(getValue());
	}

}
