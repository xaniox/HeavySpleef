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

import java.util.HashMap;
import java.util.Map;

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
	public Map<Integer, String[]> getLines() {
		Map<Integer, String[]> lines = new HashMap<Integer, String[]>();
		
		lines.put(0, new String[]{"[Vote]", "Vote"});
		
		return lines;
	}

	@Override
	public Permissions getPermission() {
		return Permissions.SIGN_VOTE;
	}

	@Override
	public void onPlace(SignChangeEvent e) {
		e.getPlayer().sendMessage(I18N._("spleefSignCreated"));
		
		StringBuilder builder = new StringBuilder();
		if (e.getLine(1).startsWith("[")) {
			builder.append(ChatColor.DARK_GRAY + "[");
		}
		
		builder.append(ChatColor.GOLD).append(ChatColor.BOLD).append("Vote");
		
		if (e.getLine(1).endsWith("]")) {
			builder.append(ChatColor.DARK_GRAY + "]");
		}
		
		e.setLine(1, builder.toString());
	}

}
