package de.matzefratze123.heavyspleef.flag.defaults;

import java.util.List;
import java.util.Map;

import org.bukkit.Location;

import de.matzefratze123.heavyspleef.core.GameProperty;
import de.matzefratze123.heavyspleef.core.event.GameListener;
import de.matzefratze123.heavyspleef.core.event.PlayerJoinGameEvent;
import de.matzefratze123.heavyspleef.core.flag.Flag;
import de.matzefratze123.heavyspleef.core.flag.Required;
import de.matzefratze123.heavyspleef.flag.presets.LocationFlag;

@Flag(name = "lobby")
@Required
public class LobbyTeleportationFlag extends LocationFlag {

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
		description.add("Defines the lobby teleportation point");
	}
	
	@GameListener
	public void onPlayerJoinGame(PlayerJoinGameEvent event) {
		Location value = getValue();
		
		event.setTeleportationLocation(value);
	}

}
