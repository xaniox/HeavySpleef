package de.matzefratze123.heavyspleef.flag.defaults;

import java.util.List;
import java.util.Map;

import de.matzefratze123.heavyspleef.core.GameProperty;
import de.matzefratze123.heavyspleef.core.event.GameListener;
import de.matzefratze123.heavyspleef.core.event.PlayerJoinGameEvent;
import de.matzefratze123.heavyspleef.core.flag.Flag;
import de.matzefratze123.heavyspleef.flag.presets.LocationFlag;

@Flag(name = "lobby")
public class FlagLobby extends LocationFlag {

	@Override
	public void defineGameProperties(Map<GameProperty, Object> properties) {}

	@Override
	public void getDescription(List<String> description) {
		description.add("Defines the lobby point for a Spleef game");
	}
	
	@GameListener
	public void onPlayerJoinGame(PlayerJoinGameEvent event) {
		if (getValue() == null) {
			return;
		}
		
		event.setTeleportationLocation(getValue());
	}

}
