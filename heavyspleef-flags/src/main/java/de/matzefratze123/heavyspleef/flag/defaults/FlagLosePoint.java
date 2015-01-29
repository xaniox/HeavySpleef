package de.matzefratze123.heavyspleef.flag.defaults;

import java.util.List;
import java.util.Map;

import de.matzefratze123.heavyspleef.core.GameProperty;
import de.matzefratze123.heavyspleef.core.QuitCause;
import de.matzefratze123.heavyspleef.core.event.GameListener;
import de.matzefratze123.heavyspleef.core.event.PlayerLeaveGameEvent;
import de.matzefratze123.heavyspleef.core.flag.Flag;
import de.matzefratze123.heavyspleef.flag.presets.LocationFlag;

@Flag(name = "lose")
public class FlagLosePoint extends LocationFlag {

	@Override
	public void defineGameProperties(Map<GameProperty, Object> properties) {}

	@Override
	public void getDescription(List<String> description) {
		description.add("Defines the teleportation point for players ");
	}
	
	@GameListener
	public void onPlayerLeaveGame(PlayerLeaveGameEvent event) {
		if (getValue() == null) {
			return;
		}
		
		if (event.getCause() != QuitCause.LOSE) {
			return;
		}
		
		event.setTeleportationLocation(getValue());
	}
	
}
