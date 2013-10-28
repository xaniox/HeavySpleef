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
package de.matzefratze123.heavyspleef.listener;


import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import de.matzefratze123.heavyspleef.HeavySpleef;
import de.matzefratze123.heavyspleef.command.CommandHub;
import de.matzefratze123.heavyspleef.command.CommandJoin;
import de.matzefratze123.heavyspleef.command.CommandLeave;
import de.matzefratze123.heavyspleef.command.CommandStart;
import de.matzefratze123.heavyspleef.core.Game;
import de.matzefratze123.heavyspleef.core.GameCuboid;
import de.matzefratze123.heavyspleef.core.GameManager;
import de.matzefratze123.heavyspleef.core.SignWall;
import de.matzefratze123.heavyspleef.core.Team;
import de.matzefratze123.heavyspleef.util.Permissions;
import de.matzefratze123.heavyspleef.util.Util;

public class SignListener implements Listener {

	@EventHandler
	public void onSignChange(SignChangeEvent e) {
		Player p = e.getPlayer();
		Block block = e.getBlock();
		
		String line1, line2, line3, line4;
		
		line1 = ChatColor.stripColor(e.getLine(0));
		line2 = ChatColor.stripColor(e.getLine(1));
		line3 = ChatColor.stripColor(e.getLine(2));
		line4 = ChatColor.stripColor(e.getLine(3));
		
		if (p == null)
			return;
		if (!line1.equalsIgnoreCase("[Spleef]"))
			return;
		if (!p.hasPermission(Permissions.CREATE_SPLEEF_SIGN.getPerm())) {
			p.sendMessage(GameCuboid._("notAllowedToCreateSpleefSigns"));
			block.breakNaturally();
			return;
		}
		
		if (line2.equalsIgnoreCase("[Join]")) {
			if (!line3.isEmpty() && !GameManager.hasGame(line3.toLowerCase())) {
				p.sendMessage(GameCuboid._("arenaDoesntExists"));
				block.breakNaturally();
				return;
			}
			if (!line4.isEmpty()) {
				ChatColor color = null;
				
				for (ChatColor c : Team.allowedColors) {
					if (c.name().equalsIgnoreCase(line4))
						color = c;
				}
				
				if (color == null) {
					p.sendMessage(Game._("invalidColor"));
					block.breakNaturally();
					return;
				}
				
				line4 = color + Util.toFriendlyString(line4);
			}
			
			p.sendMessage(Game._("spleefSignCreated"));
			
			e.setLine(0, ChatColor.DARK_BLUE + "[Spleef]");
			e.setLine(1, ChatColor.DARK_RED + "[Join]");
			e.setLine(3, line4);
		} else if (line2.equalsIgnoreCase("[Start]")) {
			if (!GameManager.hasGame(line3.toLowerCase())) {
				p.sendMessage(GameCuboid._("arenaDoesntExists"));
				block.breakNaturally();
				return;
			}
			p.sendMessage(Game._("spleefSignCreated"));
			
			e.setLine(0, ChatColor.DARK_BLUE + "[Spleef]");
			e.setLine(1, ChatColor.DARK_RED + "[Start]");
		} else if (line2.equalsIgnoreCase("[Leave]")) {
			p.sendMessage(Game._("spleefSignCreated"));
			
			e.setLine(0, ChatColor.DARK_BLUE + "[Spleef]");
			e.setLine(1, ChatColor.DARK_RED + "[Leave]");
		} else if (line2.equalsIgnoreCase("[HUB]")) {
			p.sendMessage(Game._("spleefSignCreated"));
			
			e.setLine(0, ChatColor.DARK_BLUE + "[Spleef]");
			e.setLine(1, ChatColor.DARK_RED + "[HUB]");
		} else if (line2.equalsIgnoreCase("[Vote]")) {
			p.sendMessage(Game._("spleefSignCreated"));
			
			e.setLine(0, ChatColor.DARK_BLUE + "[Spleef]");
			e.setLine(1, ChatColor.DARK_RED + "[Vote]");
		}
	}
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent e) {
		Player p = e.getPlayer();
		Block block = e.getClickedBlock();
		
		if (block == null)
			return;
		if (p == null)
			return;
		if (e.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;
		
		BlockState state = block.getState();
		if (state.getType() != Material.SIGN_POST && state.getType() != Material.WALL_SIGN)
			return;
		if (!(state instanceof Sign))
			return;
		
		Sign sign = (Sign) state;
		
		String line1, line2, line3, line4;
		
		line1 = ChatColor.stripColor(sign.getLine(0));
		line2 = ChatColor.stripColor(sign.getLine(1));
		line3 = ChatColor.stripColor(sign.getLine(2));
		line4 = ChatColor.stripColor(sign.getLine(3));
		
		if (line1.equalsIgnoreCase("[Spleef]") && line2.equalsIgnoreCase("[Join]")) {
			if (!p.hasPermission(Permissions.SIGN_JOIN.getPerm())) {
				p.sendMessage(Game._("noPermission"));
				return;
			}
			
			if (line3.isEmpty()) {
				if (!p.hasPermission(Permissions.JOIN_GAME_INV.getPerm())) {
					p.sendMessage(Game._("noPermission"));
					return;
				}
				
				HeavySpleef.getInstance().getJoinGUI().open(p);
				return;
			} else {
				if (!GameManager.hasGame(line3)) {
					p.sendMessage(Game._("arenaDoesntExists"));
					return;
				}
				ChatColor color = null;
				
				if (!line4.isEmpty()) {
					try {
						color = ChatColor.valueOf(line4.toUpperCase());
					} catch (Exception ex) {
						p.sendMessage(Game._("invalidTeam"));
						return;
					}
				} else {
					blockCalculationUp: {
						Block up = block.getRelative(BlockFace.UP);
						if (up.getType() != Material.WOOL)
							break blockCalculationUp;
						color = Team.woolDyeToChatColor(up.getData());
					}
					blockCalculationFace: {
						Block attached = SignWall.getAttachedBlock(sign);
						if (attached == null)
							break blockCalculationFace;
						if (attached.getType() != Material.WOOL)
							break blockCalculationFace;
						
						color = Team.woolDyeToChatColor(attached.getData());
					}
				}
				
				CommandJoin.doFurtherChecks(GameManager.getGame(line3), p, color);
			}
		} else if (line1.equalsIgnoreCase("[Spleef]") && line2.equalsIgnoreCase("[Start]")) {
			if (!p.hasPermission(Permissions.SIGN_START.getPerm())) {
				p.sendMessage(Game._("noPermission"));
				return;
			}
			if (!GameManager.hasGame(line3)) {
				p.sendMessage(Game._("arenaDoesntExists"));
				return;
			}
			CommandStart.start(p, GameManager.getGame(line3));
		} else if (line1.equalsIgnoreCase("[Spleef]") && line2.equalsIgnoreCase("[Leave]")) {
			if (!p.hasPermission(Permissions.SIGN_LEAVE.getPerm())) {
				p.sendMessage(Game._("noPermission"));
				return;
			}
			
			CommandLeave.leave(p);
		} else if (line1.equalsIgnoreCase("[Spleef]") && line2.equalsIgnoreCase("[HUB]")) {
			if (!p.hasPermission(Permissions.SIGN_HUB.getPerm())) {
				p.sendMessage(Game._("noPermission"));
				return;
			}
			
			CommandHub.tpToHub(p);
		} else if (line1.equalsIgnoreCase("[Spleef]") && line2.equalsIgnoreCase("[Vote]")) {
			if (!p.hasPermission(Permissions.SIGN_VOTE.getPerm())) {
				p.sendMessage(Game._("noPermission"));
				return;
			}
			
			if (!GameManager.isActive(p)) {
				p.sendMessage(Game._("onlyLobby"));
				return;
			}
			
			Game game = GameManager.fromPlayer(p);
			
			if (!game.isPreLobby()) {
				p.sendMessage(Game._("onlyLobby"));
				return;
			}
			if (game.hasVote(p)) {
				p.sendMessage(Game._("alreadyVoted"));
				return;
			}
			
			game.addVote(p);
			p.sendMessage(Game._("successfullyVoted"));
		}
		
	}
	
}
