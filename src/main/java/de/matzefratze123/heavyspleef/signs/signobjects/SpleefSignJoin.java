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
package de.matzefratze123.heavyspleef.signs.signobjects;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.event.block.SignChangeEvent;

import de.matzefratze123.heavyspleef.HeavySpleef;
import de.matzefratze123.heavyspleef.command.CommandJoin;
import de.matzefratze123.heavyspleef.core.Game;
import de.matzefratze123.heavyspleef.core.GameManager;
import de.matzefratze123.heavyspleef.core.Team;
import de.matzefratze123.heavyspleef.core.Team.Color;
import de.matzefratze123.heavyspleef.objects.SpleefPlayer;
import de.matzefratze123.heavyspleef.signs.SpleefSign;
import de.matzefratze123.heavyspleef.signs.SpleefSignExecutor;
import de.matzefratze123.heavyspleef.util.I18N;
import de.matzefratze123.heavyspleef.util.Permissions;
import de.matzefratze123.heavyspleef.util.Util;

public class SpleefSignJoin implements SpleefSign {

	@Override
	public void onClick(SpleefPlayer player, Sign sign) {
		String[] lines = SpleefSignExecutor.stripSign(sign);
		
		//Check wether there is no game on the third line
		if (lines[2].isEmpty()) {
			if (!player.getBukkitPlayer().hasPermission(Permissions.JOIN_GAME_INV.getPerm())) {
				player.sendMessage(I18N._("noPermission"));
				return;
			}
			
			//Open up Join GUI
			HeavySpleef.getInstance().getJoinGUI().open(player.getBukkitPlayer());
		} else {
			//Check if the game exists
			if (!GameManager.hasGame(lines[2])) {
				player.sendMessage(I18N._("arenaDoesntExists"));
				return;
			}
			
			Game game = GameManager.getGame(lines[2]);
			
			if (!player.getBukkitPlayer().hasPermission(Permissions.SIGN_JOIN.getPerm())
			 && !player.getBukkitPlayer().hasPermission(Permissions.SIGN_JOIN.getPerm() + "." + game.getName().toLowerCase())) {
				return;
			}
			
			Team team;
			
			//Check teams
			if (!lines[3].isEmpty()) {
				try {
					team = game.getComponents().getTeam(Color.byName(lines[3]));
				} catch (Exception ex) {
					player.sendMessage(I18N._("invalidTeam"));
					return;
				}
			} else {
				Color color = null;
				
				//Try to calculate team colors via block neighboors
				Block up = sign.getBlock().getRelative(BlockFace.UP);
				if (up.getType() == Material.WOOL) {
					color = Color.byWoolColor(up.getData());
				}
				
				Block attached = Util.getAttached(sign.getBlock());
				if (attached != null && attached.getType() == Material.WOOL) {
					color = Color.byWoolColor(attached.getData());
				}
				
				team = game.getComponents().getTeam(color);
			}
			
			CommandJoin.joinAndDoChecks(GameManager.getGame(lines[2]), player, team);
		}
	}

	@Override
	public String getId() {
		return "sign-join";
	}

	@Override
	public Map<Integer, String[]> getLines() {
		Map<Integer, String[]> lines = new HashMap<Integer, String[]>();
		
		lines.put(0, new String[]{"[Join]", "Join"});
		
		return lines;
	}

	@Override
	public Permissions getPermission() {
		return null;
	}

	@Override
	public void onPlace(SignChangeEvent e) {
		if (!GameManager.hasGame(e.getLine(2))) {
			e.getPlayer().sendMessage(I18N._("arenaDoesntExists"));
			e.getBlock().breakNaturally();
			return;
		}
		
		if (!e.getLine(3).isEmpty()) {
			Color color = Color.byName(e.getLine(3));
			
			if (color == null) {
				e.getPlayer().sendMessage(I18N._("invalidColor"));
				e.getBlock().breakNaturally();
				return;
			}
			
			e.setLine(3, color.toMessageColorString());
		}
		
		e.getPlayer().sendMessage(I18N._("spleefSignCreated"));
		
		StringBuilder builder = new StringBuilder();
		if (e.getLine(1).startsWith("[")) {
			builder.append(ChatColor.DARK_GRAY + "[");
		}
		
		builder.append(ChatColor.GREEN).append(ChatColor.BOLD).append("Join");
		
		if (e.getLine(1).endsWith("]")) {
			builder.append(ChatColor.DARK_GRAY + "]");
		}
		
		e.setLine(1, builder.toString());
		e.setLine(2, ChatColor.DARK_RED + GameManager.getGame(e.getLine(2)).getName());
	}

}
