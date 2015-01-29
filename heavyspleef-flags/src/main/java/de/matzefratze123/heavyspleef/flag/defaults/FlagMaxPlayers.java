package de.matzefratze123.heavyspleef.flag.defaults;

import java.util.List;

import de.matzefratze123.heavyspleef.core.event.GameListener;
import de.matzefratze123.heavyspleef.core.event.PlayerJoinGameEvent;
import de.matzefratze123.heavyspleef.core.event.PlayerJoinGameEvent.JoinResult;
import de.matzefratze123.heavyspleef.core.flag.Flag;
import de.matzefratze123.heavyspleef.core.i18n.Messages;
import de.matzefratze123.heavyspleef.flag.presets.IntegerFlag;

@Flag(name = "max-players")
public class FlagMaxPlayers extends IntegerFlag {
	
	@Override
	public void getDescription(List<String> description) {
		description.add("Defines the maximum count of players for a Spleef game");
	}
	
	@GameListener
	public void onPlayerJoinGame(PlayerJoinGameEvent event) {
		int playersCount = event.getGame().getPlayers().size();
		if (playersCount >= getValue()) {
			event.setJoinResult(JoinResult.DENY);
			event.setMessage(getI18N().getVarString(Messages.Player.MAX_PLAYER_COUNT_REACHED)
					.setVariable("max", String.valueOf(getValue()))
					.toString());
		}
	}
	
}
