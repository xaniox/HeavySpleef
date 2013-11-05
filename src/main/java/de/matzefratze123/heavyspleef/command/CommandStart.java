/**
 *   HeavySpleef - The simple spleef plugin for bukkit
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

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.matzefratze123.heavyspleef.HeavySpleef;
import de.matzefratze123.heavyspleef.command.UserType.Type;
import de.matzefratze123.heavyspleef.core.GameManager;
import de.matzefratze123.heavyspleef.core.Game;
import de.matzefratze123.heavyspleef.core.GameState;
import de.matzefratze123.heavyspleef.core.GameType;
import de.matzefratze123.heavyspleef.core.Team;
import de.matzefratze123.heavyspleef.core.flag.FlagType;
import de.matzefratze123.heavyspleef.hooks.WorldEditHook;
import de.matzefratze123.heavyspleef.objects.SpleefPlayer;
import de.matzefratze123.heavyspleef.util.Permissions;

@UserType(Type.PLAYER)
public class CommandStart extends HSCommand {

	public CommandStart() {
		setPermission(Permissions.START_GAME);
		setUsage("/spleef start [name]");
		setHelp("Starts a game");
		setOnlyIngame(true);
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		SpleefPlayer player = HeavySpleef.getInstance().getSpleefPlayer((Player)sender);
		Game game;

		if (args.length <= 0) {
			if (!player.isActive()) {
				player.sendMessage(_("notIngame"));
				return;
			}

			game = player.getGame();
		} else {
			if (!GameManager.hasGame(args[0])) {
				player.sendMessage(_("arenaDoesntExists"));
				return;
			}

			permissionsCheck: {
				Game playerGame = player.getGame();

				if (playerGame != null && playerGame.getName().equalsIgnoreCase(args[0]))
					break permissionsCheck;
				if (player.getBukkitPlayer().hasPermission(Permissions.START_GAME_OTHER.getPerm()))
					break permissionsCheck;

				player.sendMessage(_("noPermission"));
				return;
			}

			game = GameManager.getGame(args[0]);
		}

		start(player, game);
	}

	public static void start(SpleefPlayer player, Game game) {
		if (game.getGameState() == GameState.DISABLED) {
			player.sendMessage(_("gameIsDisabled"));
			return;
		}
		if (game.getType() == GameType.CYLINDER
				&& !HeavySpleef.getInstance().getHookManager()
						.getService(WorldEditHook.class).hasHook()) {
			player.sendMessage(_("noWorldEdit"));
			return;
		}
		if (game.getGameState() == GameState.COUNTING || game.getGameState() == GameState.INGAME) {
			player.sendMessage(_("cantStartGameWhileRunning"));
			return;
		}

		int minplayers = game.getFlag(FlagType.MINPLAYERS);

		if (game.getIngamePlayers().size() < minplayers || game.getIngamePlayers().size() < 2) {
			player.sendMessage(_("notEnoughPlayers", String.valueOf(minplayers)));
			return;
		}
		if (game.getFlag(FlagType.TEAM)) {
			int c = 0;
			for (Team team : game.getComponents().getTeams()) {
				if (team.hasPlayersLeft())
					c++;
				if (team.getPlayers().length < team.getMinPlayers()) {
					player.sendMessage(_("teamNeedMorePlayers", team.getColor()
							.name().toLowerCase(),
							String.valueOf(team.getMinPlayers())));

				}
			}

			// Check if there is only one team
			if (c <= 1) {
				player.sendMessage(_("minimumTwoTeams"));
			}
		}

		game.countdown();
		player.sendMessage(_("gameStarted"));
	}

}
