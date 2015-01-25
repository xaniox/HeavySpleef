package de.matzefratze123.heavyspleef.flag.defaults;

import java.util.List;

import de.matzefratze123.heavyspleef.core.event.GameListener;
import de.matzefratze123.heavyspleef.core.event.PlayerJoinGameEvent;
import de.matzefratze123.heavyspleef.core.flag.Flag;
import de.matzefratze123.heavyspleef.flag.presets.IntegerFlag;

@Flag(name = "autostart")
public class FlagAutostart extends IntegerFlag {

	@Override
	public void getDescription(List<String> description) {
		description.add("Defines the count of players needed to automatically start the game");
	}
	
	@GameListener
	public void onPlayerJoin(PlayerJoinGameEvent event) {
		int playersNow = event.getGame().getPlayers().size() + 1;
		if (playersNow >= getValue()) {
			event.setStartGame(true);
		}
	}
	
}
