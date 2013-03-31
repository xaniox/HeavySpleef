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

import static me.matzefratze123.heavyspleef.core.flag.FlagType.JACKPOTAMOUNT;
import static me.matzefratze123.heavyspleef.core.flag.FlagType.MAXPLAYERS;
import static me.matzefratze123.heavyspleef.core.flag.FlagType.ONEVSONE;
import me.matzefratze123.heavyspleef.HeavySpleef;
import me.matzefratze123.heavyspleef.core.Game;
import me.matzefratze123.heavyspleef.core.GameManager;
import me.matzefratze123.heavyspleef.core.Type;
import me.matzefratze123.heavyspleef.utility.LanguageHandler;
import me.matzefratze123.heavyspleef.utility.Permissions;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandJoin extends HSCommand {

	public CommandJoin() {
		setMaxArgs(2);
		setMinArgs(0);
		setOnlyIngame(true);
		setUsage("/spleef join <Arena>");
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		Player player = (Player)sender;
		
		if (args.length == 0) {
			if (!player.hasPermission(Permissions.JOIN_GAME_INV.getPerm())) {
				player.sendMessage(getUsage());
				return;
			}
			HeavySpleef.selector.open(player);
			return;
		}
		
		if (!player.hasPermission(Permissions.JOIN_GAME.getPerm())) {
			player.sendMessage(LanguageHandler._("noPermission"));
			return;
		}
		if (!GameManager.hasGame(args[0].toLowerCase())) {
			player.sendMessage(_("arenaDoesntExists"));
			return;
		}
		Game game = GameManager.getGame(args[0].toLowerCase());
		
		if (game.isDisabled()) {
			player.sendMessage(_("gameIsDisabled"));
			return;
		}
		if (!game.isFinal()) {
			player.sendMessage(_("isntReadyToPlay"));
			return;
		}
		if (game.getType() == Type.CYLINDER && !HeavySpleef.hooks.hasWorldEdit()) {
			player.sendMessage(_("noWorldEdit"));
			return;
		}
		int jackpotToPay = game.getFlag(JACKPOTAMOUNT) == null ? HeavySpleef.instance.getConfig().getInt("general.defaultToPay", 5) : game.getFlag(JACKPOTAMOUNT);
		
		if (args.length == 1) {
			if (HeavySpleef.hooks.hasVault()) {
				if (HeavySpleef.hooks.getVaultEconomy().getBalance(player.getName()) < jackpotToPay) {
					player.sendMessage(_("notEnoughMoneyToJoin"));
					return;
				}
			}
			
			join(player, game);
		} else if (args.length == 2) {
			if (!player.hasPermission(Permissions.JOIN_GAME_OTHERS.getPerm())) {
				player.sendMessage(LanguageHandler._("noPermission"));
				return;
			}
			Player target = Bukkit.getPlayer(args[1]);
			if (target == null) {
				player.sendMessage(_("playerNotOnline"));
				return;
			}
			if (HeavySpleef.hooks.hasVault()) {
				if (HeavySpleef.hooks.getVaultEconomy().getBalance(target.getName()) < jackpotToPay) {
					player.sendMessage(_("targetHasntEnoughMoney"));
					return;
				}
			}
			
			boolean join = join(target, game);
			if (join)
				player.sendMessage(_("targetHasJoined", target.getName(), game.getName()));
			else
				player.sendMessage(_("targetAddedToQueue", target.getName()));
		}
	}
	
	private boolean join(Player player, Game game) {
		if (GameManager.isInAnyGame(player)) {
			player.sendMessage(_("cantJoinMultipleGames"));
			return false;
		}
		if (game.isIngame()) {
			player.sendMessage(_("gameAlreadyRunning"));
			if (HeavySpleef.instance.getConfig().getBoolean("queues.useQueues"))
				GameManager.addQueue(player, game.getName());
			return false;
		}
		
		boolean is1vs1 = game.getFlag(ONEVSONE) == null ? false : game.getFlag(ONEVSONE);
		int maxplayers = game.getFlag(MAXPLAYERS) == null ? -1 : game.getFlag(MAXPLAYERS);
		
		if (game.isCounting() && plugin.getConfig().getBoolean("general.joinAtCountdown") && !is1vs1) {
			player.sendMessage(_("playerJoinedToPlayer", game.getName()));
			game.addPlayer(player);
			return true;
		} else if (game.isCounting()){
			player.sendMessage(_("gameAlreadyRunning"));
			if (HeavySpleef.instance.getConfig().getBoolean("queues.useQueues"))
				GameManager.addQueue(player, game.getName());
			return false;
		}
		if (maxplayers > 0 && game.getPlayers().length >= maxplayers) {
			player.sendMessage(_("maxPlayersReached"));
			if (HeavySpleef.instance.getConfig().getBoolean("queues.useQueues"))
				GameManager.addQueue(player, game.getName());
			return false;
		}
		
		player.sendMessage(_("playerJoinedToPlayer", game.getName()));
		game.addPlayer(player);
		if (GameManager.queues.containsKey(player.getName()))
			GameManager.queues.remove(player.getName());
		return true;
	}

}
