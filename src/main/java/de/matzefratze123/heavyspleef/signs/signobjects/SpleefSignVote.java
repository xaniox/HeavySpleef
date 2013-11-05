package de.matzefratze123.heavyspleef.signs.signobjects;

import org.bukkit.ChatColor;
import org.bukkit.block.Sign;
import org.bukkit.event.block.SignChangeEvent;

import de.matzefratze123.heavyspleef.core.Game;
import de.matzefratze123.heavyspleef.core.GameState;
import de.matzefratze123.heavyspleef.objects.SpleefPlayer;
import de.matzefratze123.heavyspleef.signs.SpleefSign;
import de.matzefratze123.heavyspleef.util.LanguageHandler;
import de.matzefratze123.heavyspleef.util.Permissions;

public class SpleefSignVote implements SpleefSign {

	@Override
	public void onClick(SpleefPlayer player, Sign sign) {
		if (!player.isActive()) {
			player.sendMessage(LanguageHandler._("onlyLobby"));
			return;
		}
		
		Game game = player.getGame();
		
		if (game.getGameState() != GameState.LOBBY) {
			player.sendMessage(LanguageHandler._("onlyLobby"));
			return;
		}
		if (player.isReady()) {
			player.sendMessage(LanguageHandler._("alreadyVoted"));
			return;
		}
		
		player.setReady(true);
		player.sendMessage(LanguageHandler._("successfullyVoted"));
	}

	@Override
	public String getId() {
		return "sign.vote";
	}

	@Override
	public String[] getLines() {
		String[] lines = new String[3];
		
		lines[0] = "[Vote]";
		
		return lines;
	}

	@Override
	public Permissions getPermission() {
		return Permissions.SIGN_VOTE;
	}

	@Override
	public void onPlace(SignChangeEvent e) {
		e.getPlayer().sendMessage(LanguageHandler._("spleefSignCreated"));
		
		e.setLine(1, ChatColor.DARK_GRAY + "[" + ChatColor.GOLD + ChatColor.BOLD + "Vote" + ChatColor.DARK_GRAY + "]");
	}

}
