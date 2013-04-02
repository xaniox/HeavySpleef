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
package me.matzefratze123.heavyspleef.command;

import me.matzefratze123.heavyspleef.HeavySpleef;
import me.matzefratze123.heavyspleef.core.Game;
import me.matzefratze123.heavyspleef.core.GameManager;
import me.matzefratze123.heavyspleef.core.Type;
import me.matzefratze123.heavyspleef.core.flag.FlagType;
import me.matzefratze123.heavyspleef.utility.Permissions;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandStart extends HSCommand {

	public CommandStart() {
		setMaxArgs(1);
		setMinArgs(1);
		setOnlyIngame(true);
		setPermission(Permissions.START_GAME);
		setUsage("/spleef start <Name>");
	}
	
	@Override
	public void execute(CommandSender sender, String[] args) {
		Player player = (Player)sender;
		if (!GameManager.hasGame(args[0].toLowerCase())) {
			sender.sendMessage(_("arenaDoesntExists"));
			return;
		}
		Game game = GameManager.getGame(args[0].toLowerCase());
		start(player, game);
	}

	public static void start(Player player, Game game) {
		if (game.isDisabled()) {
			player.sendMessage(_("gameIsDisabled"));
			return;
		}
		if (game.getType() == Type.CYLINDER && !HeavySpleef.hooks.hasWorldEdit()) {
			player.sendMessage(_("noWorldEdit"));
			return;
		}
		if (game.isCounting() || game.isIngame()) {
			player.sendMessage(_("cantStartGameWhileRunning"));
			return;
		}
		
		int minplayers = game.getFlag(FlagType.MINPLAYERS) == null ? HeavySpleef.instance.getConfig().getInt("general.neededPlayers") : game.getFlag(FlagType.MINPLAYERS);
		
		if (game.getPlayers().length < minplayers || game.getPlayers().length < 2) {
			player.sendMessage(_("notEnoughPlayers", String.valueOf(minplayers)));
			return;
		}
		
		game.countdown();
		player.sendMessage(_("gameStarted"));
	}
	
}
