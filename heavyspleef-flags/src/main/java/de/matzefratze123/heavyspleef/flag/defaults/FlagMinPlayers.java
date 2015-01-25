package de.matzefratze123.heavyspleef.flag.defaults;

import java.util.List;

import de.matzefratze123.heavyspleef.core.event.GameListener;
import de.matzefratze123.heavyspleef.core.event.GameListener.Priority;
import de.matzefratze123.heavyspleef.core.event.PlayerJoinGameEvent;
import de.matzefratze123.heavyspleef.core.flag.Flag;
import de.matzefratze123.heavyspleef.flag.presets.IntegerFlag;

@Flag(name = "min-players")
public class FlagMinPlayers extends IntegerFlag {

	@Override
	public void getDescription(List<String> description) {
		description.add("Defines the minimum count of players required to start a game");
	}
	
	@GameListener(priority = Priority.HIGH)
	public void onPlayerJoin(PlayerJoinGameEvent event) {
		int playersNow = event.getGame().getPlayers().size() + 1;
		if (playersNow < getValue()) {
			event.setStartGame(false);
		}
	}

}
