/**
 *   HeavySpleef - Advanced spleef plugin for bukkit
 *   
 *   Copyright (C) 2013 matzefratze123
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package de.matzefratze123.heavyspleef.signs.signobjects;

import org.bukkit.ChatColor;
import org.bukkit.block.Sign;
import org.bukkit.event.block.SignChangeEvent;

import de.matzefratze123.heavyspleef.core.Game;
import de.matzefratze123.heavyspleef.core.GameState;
import de.matzefratze123.heavyspleef.objects.SpleefPlayer;
import de.matzefratze123.heavyspleef.signs.SpleefSign;
import de.matzefratze123.heavyspleef.util.I18N;
import de.matzefratze123.heavyspleef.util.Permissions;

public class SpleefSignVote implements SpleefSign {

	@Override
	public void onClick(SpleefPlayer player, Sign sign) {
		if (!player.isActive()) {
			player.sendMessage(I18N._("onlyLobby"));
			return;
		}
		
		Game game = player.getGame();
		
		if (game.getGameState() != GameState.LOBBY) {
			player.sendMessage(I18N._("onlyLobby"));
			return;
		}
		if (player.isReady()) {
			player.sendMessage(I18N._("alreadyVoted"));
			return;
		}
		
		player.setReady(true);
		player.sendMessage(I18N._("successfullyVoted"));
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
		e.getPlayer().sendMessage(I18N._("spleefSignCreated"));
		
		e.setLine(1, ChatColor.DARK_GRAY + "[" + ChatColor.GOLD + ChatColor.BOLD + "Vote" + ChatColor.DARK_GRAY + "]");
	}

}
