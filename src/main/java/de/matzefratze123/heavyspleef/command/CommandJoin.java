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

import static de.matzefratze123.heavyspleef.core.flag.FlagType.ENTRY_FEE;
import static de.matzefratze123.heavyspleef.core.flag.FlagType.MAXPLAYERS;
import static de.matzefratze123.heavyspleef.core.flag.FlagType.ONEVSONE;
import static de.matzefratze123.heavyspleef.core.flag.FlagType.TEAM;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.matzefratze123.heavyspleef.HeavySpleef;
import de.matzefratze123.heavyspleef.command.handler.HSCommand;
import de.matzefratze123.heavyspleef.command.handler.Help;
import de.matzefratze123.heavyspleef.command.handler.UserType;
import de.matzefratze123.heavyspleef.command.handler.UserType.Type;
import de.matzefratze123.heavyspleef.config.ConfigUtil;
import de.matzefratze123.heavyspleef.core.Game;
import de.matzefratze123.heavyspleef.core.GameComponents;
import de.matzefratze123.heavyspleef.core.GameManager;
import de.matzefratze123.heavyspleef.core.GameState;
import de.matzefratze123.heavyspleef.core.GameType;
import de.matzefratze123.heavyspleef.core.Team;
import de.matzefratze123.heavyspleef.core.Team.Color;
import de.matzefratze123.heavyspleef.core.flag.FlagType;
import de.matzefratze123.heavyspleef.hooks.HookManager;
import de.matzefratze123.heavyspleef.hooks.VaultHook;
import de.matzefratze123.heavyspleef.hooks.WorldEditHook;
import de.matzefratze123.heavyspleef.objects.SpleefPlayer;
import de.matzefratze123.heavyspleef.util.I18N;
import de.matzefratze123.heavyspleef.util.Permissions;
import de.matzefratze123.heavyspleef.util.PvPTimerManager;
import de.matzefratze123.heavyspleef.util.Util;

@UserType(Type.PLAYER)
public class CommandJoin extends HSCommand {
	
	public CommandJoin() {
		setOnlyIngame(true);
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		Player bukkitPlayer = (Player)sender;
		SpleefPlayer player = HeavySpleef.getInstance().getSpleefPlayer(bukkitPlayer);
		
		if (args.length == 0) {
			//Inventory menu
			if (!bukkitPlayer.hasPermission(Permissions.JOIN_GAME_INV.getPerm())) {
				bukkitPlayer.sendMessage(getUsage());
				return;
			}
			
			HeavySpleef.getInstance().getJoinGUI().open(bukkitPlayer);
		} else {
			if (!GameManager.hasGame(args[0].toLowerCase())) {
				bukkitPlayer.sendMessage(_("arenaDoesntExists"));
				return;
			}
			
			Game game = GameManager.getGame(args[0].toLowerCase());
			
			if (!bukkitPlayer.hasPermission(Permissions.JOIN_GAME.getPerm()) &&
				!bukkitPlayer.hasPermission(Permissions.JOIN_GAME.getPerm() + "." + game.getName().toLowerCase())) {
					bukkitPlayer.sendMessage(I18N._("noPermission"));
					return;
			}
			
			Team team = null;
			
			if (args.length == 1) {
				if (game.getFlag(FlagType.TEAM)) {
					team = ((GameComponents)game.getComponents()).getBestAvailableTeam();
				} else {
					team = null;
					//joinAndDoChecks(game, player, null);
				}
			} else if (args.length >= 2) {
				if (!game.getFlag(FlagType.TEAM)) {
					team = null;
				} else {
					team = game.getComponents().getTeam(Color.byName(args[1]));
					if (team == null) {
						player.sendMessage(getUsage());
						return;
					}
				}
			}
			
			joinAndDoChecks(game, player, team);
		}
	}
	
	public static void joinAndDoChecks(final Game game, final SpleefPlayer player, Team team) {
		int jackpotToPay = game.getFlag(ENTRY_FEE) == null ? HeavySpleef.getSystemConfig().getInt("general.defaultToPay", 0) : game.getFlag(ENTRY_FEE);
		
		if (game.getGameState() == GameState.DISABLED) {
			player.sendMessage(_("gameIsDisabled"));
			return;
		}
		
		if (!game.isReadyToPlay()) {
			player.sendMessage(_("isntReadyToPlay"));
			return;
		}
		
		if (HookManager.getInstance().getService(VaultHook.class).hasHook()) {
			if (HookManager.getInstance().getService(VaultHook.class).getHook().getBalance(player.getName()) < jackpotToPay) {
				player.sendMessage(_("notEnoughMoneyToJoin"));
				return;
			}
		}
		
		if (player.isSpectating()) {
			player.sendMessage(_("alreadySpectating"));
			return;
		}
		if (player.isActive()) {
			player.sendMessage(_("cantJoinMultipleGames"));
			return;
		}
		if (game.getGameState() == GameState.INGAME) {
			player.sendMessage(_("gameAlreadyRunning"));
			game.getQueue().addPlayer(player);
			return;
		}
		
		boolean is1vs1 = game.getFlag(ONEVSONE);
		
		int maxplayers = game.getFlag(MAXPLAYERS);
		if (maxplayers > 0 && game.getIngamePlayers().size() >= maxplayers) {
			player.sendMessage(_("maxPlayersReached"));
			game.getQueue().addPlayer(player);
			return;
		}
		
		if (game.getFlag(FlagType.TEAM)) {
			if (team == null) {
				team = ((GameComponents)game.getComponents()).getBestAvailableTeam();
			} else if (team.getMaxPlayers() > 0 && team.getPlayers().size() >= team.getMaxPlayers()) {
				player.sendMessage(_("maxPlayersInTeam"));
				return;
			}
			
			if (team == null) {
				player.sendMessage(_("maxPlayersInTeam"));
				return;
			}
		}
		
		if ((game.getGameState() == GameState.COUNTING && !is1vs1) || (game.getGameState() != GameState.COUNTING)) {
			int pvptimer = HeavySpleef.getSystemConfig().getInt("general.pvptimer");
			
			if (pvptimer > 0) {
				player.sendMessage(_("teleportWillCommence", game.getName(), String.valueOf(pvptimer)));
				player.sendMessage(_("dontMove"));
			}
			
			PvPTimerManager.addToTimer(player.getBukkitPlayer(), new JoinTimerRunnable(game, player, team));
		} else if (game.getGameState() == GameState.COUNTING){
			player.sendMessage(_("gameAlreadyRunning"));
			game.getQueue().addPlayer(player);
			return;
		}
	}

	@Override
	public Help getHelp(Help help) {
		help.setUsage("/spleef join <arena> [team]");
		help.addHelp("Joins a game");
		
		return help;
	}
	
	static class JoinTimerRunnable implements Runnable {

		private Game game;
		private SpleefPlayer player;
		private Team team;
		
		public JoinTimerRunnable(Game game, SpleefPlayer player, Team team){
			this.game = game;
			this.player = player;
			this.team = team;
		}
		
		@Override
		public void run() {
			if (game.getFlag(TEAM)) {
				team.join(player);
				game.broadcast(_("playerJoinedTeam", player.getName(), team.getColor().toMessageColorString()), ConfigUtil.getBroadcast("player-join"));
			}
			
			game.join(player);
			player.sendMessage(_("playerJoinedToPlayer", game.getName()));
			
			game.getQueue().removePlayer(player);
			PvPTimerManager.cancelTimerTask(player.getBukkitPlayer());
		}
		
	}

}
