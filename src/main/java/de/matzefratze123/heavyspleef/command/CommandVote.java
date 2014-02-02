/*
 *   HeavySpleef - Advanced spleef plugin for bukkit
 *   
 *   Copyright (C) 2013-2014 matzefratze123
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
package de.matzefratze123.heavyspleef.command;

import static de.matzefratze123.heavyspleef.util.I18N._;

import java.util.List;

import org.bukkit.entity.Player;

import de.matzefratze123.api.hs.command.Command;
import de.matzefratze123.api.hs.command.CommandHelp;
import de.matzefratze123.api.hs.command.CommandListener;
import de.matzefratze123.api.hs.command.CommandPermissions;
import de.matzefratze123.heavyspleef.HeavySpleef;
import de.matzefratze123.heavyspleef.command.handler.UserType;
import de.matzefratze123.heavyspleef.command.handler.UserType.Type;
import de.matzefratze123.heavyspleef.core.Game;
import de.matzefratze123.heavyspleef.core.GameState;
import de.matzefratze123.heavyspleef.core.flag.FlagType;
import de.matzefratze123.heavyspleef.objects.SpleefPlayer;
import de.matzefratze123.heavyspleef.util.Permissions;

@UserType(Type.PLAYER)
public class CommandVote implements CommandListener {
	
	@Command(value = "vote", onlyIngame = true)
	@CommandPermissions(value = {Permissions.VOTE})
	@CommandHelp(usage = "/spleef vote", description = "Votes to start the game")
	public void execute(Player bukkitPlayer) {
		SpleefPlayer player = HeavySpleef.getInstance().getSpleefPlayer(bukkitPlayer);
		
		if (!HeavySpleef.getSystemConfig().getGeneralSection().isVotesEnabled()) {
			player.sendMessage(_("votesDisabled"));
			return;
		}
		
		if (!player.isActive()) {
			player.sendMessage(_("onlyLobby"));
			return;
		}
		
		Game game = player.getGame();
		if (game.getGameState() != GameState.LOBBY) {
			player.sendMessage(_("onlyLobby"));
			return;
		}
		if (player.isReady()) {
			player.sendMessage(_("alreadyVoted"));
			return;
		}
		
		player.setReady(true);
		player.sendMessage(_("successfullyVoted"));
	}
	
	public static void tryStart(Game game) {
		int percentNeeded = HeavySpleef.getSystemConfig().getGeneralSection().getVotesNeeded();
		int minPlayers = game.getFlag(FlagType.MINPLAYERS);
		List<SpleefPlayer> ingame = game.getIngamePlayers();
		
		if (game.hasFlag(FlagType.MINPLAYERS) && minPlayers >= 2 && ingame.size() < minPlayers) {
			return;
		}
		if (ingame.size() < 2) {
			return;
		}
		
		int voted = 0;
		
		for (SpleefPlayer player : ingame) {
			if (player.isReady()) {
				voted++;
			}
		}
		
		int percentVoted = (voted * 100)/ingame.size();
		if (percentVoted < percentNeeded) {
			return;
		}
		
		game.countdown();
	}

}
