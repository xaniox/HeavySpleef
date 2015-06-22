package de.matzefratze123.heavyspleef.flag.defaults;

import java.util.List;

import de.matzefratze123.heavyspleef.core.event.PlayerJoinGameEvent;
import de.matzefratze123.heavyspleef.core.event.PlayerLeaveGameEvent;
import de.matzefratze123.heavyspleef.core.event.Subscribe;
import de.matzefratze123.heavyspleef.core.flag.Flag;
import de.matzefratze123.heavyspleef.flag.presets.BaseFlag;

@Flag(name = "scoreboard", parent = FlagTeam.class)
public class FlagTeamScoreboard extends BaseFlag {

	@Override
	public void getDescription(List<String> description) {
		description.add("Enables the sidebar scoreboard for team games");
	}
	
	@Subscribe
	public void onPlayerJoinGame(PlayerJoinGameEvent event) {
		
	}
	
	@Subscribe
	public void onPlayerLeaveGame(PlayerLeaveGameEvent event) {
		
	}

}
