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


import static de.matzefratze123.heavyspleef.core.flag.FlagType.*;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.matzefratze123.heavyspleef.HeavySpleef;
import de.matzefratze123.heavyspleef.config.ConfigUtil;
import de.matzefratze123.heavyspleef.core.Game;
import de.matzefratze123.heavyspleef.core.GameManager;
import de.matzefratze123.heavyspleef.core.GameType;
import de.matzefratze123.heavyspleef.core.Team;
import de.matzefratze123.heavyspleef.core.flag.FlagType;
import de.matzefratze123.heavyspleef.hooks.HookManager;
import de.matzefratze123.heavyspleef.hooks.TagAPIHook;
import de.matzefratze123.heavyspleef.hooks.VaultHook;
import de.matzefratze123.heavyspleef.hooks.WorldEditHook;
import de.matzefratze123.heavyspleef.listener.TagListener;
import de.matzefratze123.heavyspleef.util.LanguageHandler;
import de.matzefratze123.heavyspleef.util.Permissions;
import de.matzefratze123.heavyspleef.util.PvPTimerManager;
import de.matzefratze123.heavyspleef.util.Util;

public class CommandJoin extends HSCommand {
	
	public CommandJoin() {
		setMaxArgs(2);
		setMinArgs(0);
		setOnlyIngame(true);
		setUsage("/spleef join <arena> [team]");
		setTabHelp(new String[]{"<arena>", "<arena> [team]"});
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		Player player = (Player)sender;
		
		if (args.length == 0) {
			//Inventory menu
			if (!player.hasPermission(Permissions.JOIN_GAME_INV.getPerm())) {
				player.sendMessage(getUsage());
				return;
			}
			
			HeavySpleef.getInstance().getInventoryMenu().open(player);
			return;
		}
		
		if (!GameManager.hasGame(args[0].toLowerCase())) {
			player.sendMessage(_("arenaDoesntExists"));
			return;
		}
		
		Game game = GameManager.getGame(args[0].toLowerCase());
		
		if (!player.hasPermission(Permissions.JOIN_GAME.getPerm()) &&
			!player.hasPermission(Permissions.JOIN_GAME.getPerm() + "." + game.getName().toLowerCase())) {
				player.sendMessage(LanguageHandler._("noPermission"));
				return;
		}
		
		if (args.length == 1) {
			if (game.getFlag(FlagType.TEAM)) {
				player.sendMessage(_("specifieTeam", game.getTeamColors().toString()));
				return;
			}
			
			doFurtherChecks(game, player, null);
		} else if (args.length >= 2) {
			if (!game.getFlag(FlagType.TEAM)) {
				doFurtherChecks(game, player, null);
				return;
			}
			
			ChatColor color = null;
			
			for (ChatColor colors : Team.allowedColors) {
				if (colors.name().equalsIgnoreCase(args[1]))
					color = colors;
			}
			
			if (color == null) {
				player.sendMessage(getUsage());
				return;
			}
			
			doFurtherChecks(game, player, color);
		}
	}
	
	public static void doFurtherChecks(final Game game, final Player player, ChatColor teamColor) {
		int jackpotToPay = game.getFlag(ENTRY_FEE) == null ? HeavySpleef.getSystemConfig().getInt("general.defaultToPay", 0) : game.getFlag(ENTRY_FEE);
		
		if (game.isDisabled()) {
			player.sendMessage(_("gameIsDisabled"));
			return;
		}
		if (!game.isFinal()) {
			player.sendMessage(_("isntReadyToPlay"));
			return;
		}
		if (game.getType() == GameType.CYLINDER && !HeavySpleef.getInstance().getHookManager().getService(WorldEditHook.class).hasHook()) {
			player.sendMessage(_("noWorldEdit"));
			return;
		}
		
		if (HeavySpleef.getInstance().getHookManager().getService(VaultHook.class).hasHook()) {
			if (HeavySpleef.getInstance().getHookManager().getService(VaultHook.class).getHook().getBalance(player.getName()) < jackpotToPay) {
				player.sendMessage(_("notEnoughMoneyToJoin"));
				return;
			}
		}
		
		if (GameManager.isSpectating(player)) {
			player.sendMessage(_("alreadySpectating"));
			return;
		}
		if (GameManager.isActive(player)) {
			player.sendMessage(_("cantJoinMultipleGames"));
			return;
		}
		if (game.isIngame()) {
			player.sendMessage(_("gameAlreadyRunning"));
			game.addToQueue(player, teamColor);
			return;
		}
		
		final Team team = game.getTeam(teamColor);
		boolean is1vs1 = game.getFlag(ONEVSONE);
		
		int maxplayers = game.getFlag(MAXPLAYERS);
		if (maxplayers > 0 && game.getPlayers().length >= maxplayers) {
			player.sendMessage(_("maxPlayersReached"));
			game.addToQueue(player, team != null ? team.getColor() : null);
			return;
		}
		
		if (game.getFlag(FlagType.TEAM)) {
			if (team == null) {
				player.sendMessage(_("specifieTeam", game.getTeamColors().toString()));
				return;
			}
			
			if (team.getMaxPlayers() > 0 && team.getPlayers().length >= team.getMaxPlayers()) {
				player.sendMessage(_("maxPlayersInTeam"));
				return;
			}
		}
		
		if ((game.isCounting() && HeavySpleef.getSystemConfig().getBoolean("general.joinAtCountdown") && !is1vs1) || (!game.isCounting())) {
			int pvptimer = HeavySpleef.getSystemConfig().getInt("general.pvptimer");
			
			if (pvptimer > 0) {
				player.sendMessage(_("teleportWillCommence", game.getName(), String.valueOf(pvptimer)));
				player.sendMessage(_("dontMove"));
			}
			
			PvPTimerManager.addToTimer(player, new Runnable() {
				
				@Override
				public void run() {
					if (game.getFlag(TEAM)) {
						team.join(player);
						game.broadcast(_("playerJoinedTeam", player.getName(), team.getColor() + Util.toFriendlyString(team.getColor().name())), ConfigUtil.getBroadcast("player-join"));
						
						if (HookManager.getInstance().getService(TagAPIHook.class).hasHook())
							TagListener.setTag(player, team.getColor());
					}
					
					game.join(player);
					player.sendMessage(_("playerJoinedToPlayer", game.getName()));
					
					game.removeFromQueue(player);
					PvPTimerManager.cancelTimerTask(player);
				}
			});
		} else if (game.isCounting()){
			player.sendMessage(_("gameAlreadyRunning"));
			game.addToQueue(player, teamColor);
			return;
		}
	}

}
