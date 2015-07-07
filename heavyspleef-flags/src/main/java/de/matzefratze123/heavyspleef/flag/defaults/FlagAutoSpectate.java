package de.matzefratze123.heavyspleef.flag.defaults;

import java.util.List;

import de.matzefratze123.heavyspleef.core.Game;
import de.matzefratze123.heavyspleef.core.event.PlayerLeaveGameEvent;
import de.matzefratze123.heavyspleef.core.event.Subscribe;
import de.matzefratze123.heavyspleef.core.flag.Flag;
import de.matzefratze123.heavyspleef.core.i18n.Messages;
import de.matzefratze123.heavyspleef.core.player.SpleefPlayer;
import de.matzefratze123.heavyspleef.flag.presets.BaseFlag;

@Flag(name = "autospectate", parent = FlagSpectate.class)
public class FlagAutoSpectate extends BaseFlag {

	@Override
	public void getDescription(List<String> description) {
		description.add("Automatically makes players spectate upon losing in Spleef");
	}
	
	@Subscribe
	public void onPlayerLeftGame(PlayerLeaveGameEvent event) {
		FlagSpectate flag = (FlagSpectate) getParent();
		SpleefPlayer player = event.getPlayer();
		Game game = event.getGame();
		
		if (flag.spectate(player, game)) {
			player.sendMessage(getI18N().getVarString(Messages.Player.PLAYER_SPECTATE)
					.setVariable("game", game.getName())
					.toString());
		}
	}

}
