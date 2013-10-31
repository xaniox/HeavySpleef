package de.matzefratze123.heavyspleef.signs.signobjects;

import org.bukkit.ChatColor;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.block.SignChangeEvent;

import de.matzefratze123.heavyspleef.command.HSCommand;
import de.matzefratze123.heavyspleef.core.Game;
import de.matzefratze123.heavyspleef.core.GameManager;
import de.matzefratze123.heavyspleef.signs.SpleefSign;
import de.matzefratze123.heavyspleef.util.Permissions;

public class SpleefSignVote implements SpleefSign {

	@Override
	public void onClick(Player player, Sign sign) {
		if (!GameManager.isActive(player)) {
			player.sendMessage(Game._("onlyLobby"));
			return;
		}
		
		Game game = GameManager.fromPlayer(player);
		
		if (!game.isPreLobby()) {
			player.sendMessage(Game._("onlyLobby"));
			return;
		}
		if (game.hasVote(player)) {
			player.sendMessage(Game._("alreadyVoted"));
			return;
		}
		
		game.addVote(player);
		player.sendMessage(HSCommand._("successfullyVoted"));
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
		e.getPlayer().sendMessage(Game._("spleefSignCreated"));
		
		e.setLine(1, ChatColor.DARK_GRAY + "[" + ChatColor.GOLD + ChatColor.BOLD + "Vote" + ChatColor.DARK_GRAY + "]");
	}

}
