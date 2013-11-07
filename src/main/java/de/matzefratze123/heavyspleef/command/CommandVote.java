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
package de.matzefratze123.heavyspleef.command;

import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.matzefratze123.heavyspleef.HeavySpleef;
import de.matzefratze123.heavyspleef.command.UserType.Type;
import de.matzefratze123.heavyspleef.core.Game;
import de.matzefratze123.heavyspleef.core.GameState;
import de.matzefratze123.heavyspleef.core.flag.FlagType;
import de.matzefratze123.heavyspleef.objects.SpleefPlayer;
import de.matzefratze123.heavyspleef.util.Permissions;

@UserType(Type.PLAYER)
public class CommandVote extends HSCommand {

	public CommandVote() {
		setPermission(Permissions.VOTE);
		setOnlyIngame(true);
		setUsage("/spleef vote");
		setHelp("Votes to start the game");
	}
	
	@Override
	public void execute(CommandSender sender, String[] args) {
		Player bukkitPlayer = (Player)sender;
		SpleefPlayer player = HeavySpleef.getInstance().getSpleefPlayer(bukkitPlayer);
		
		if (!HeavySpleef.getSystemConfig().getBoolean("general.autostart-vote-enabled", true)) {
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
		tryStart(game);
	}
	
	private void tryStart(Game game) {
		int percentNeeded = HeavySpleef.getSystemConfig().getInt("autostart-vote", 70);
		int minPlayers = game.getFlag(FlagType.MINPLAYERS);
		List<SpleefPlayer> ingame = game.getIngamePlayers();
		
		if (minPlayers >= 2 && ingame.size() < minPlayers) {
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
