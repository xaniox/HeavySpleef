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

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.event.block.SignChangeEvent;

import de.matzefratze123.heavyspleef.HeavySpleef;
import de.matzefratze123.heavyspleef.command.CommandJoin;
import de.matzefratze123.heavyspleef.command.HSCommand;
import de.matzefratze123.heavyspleef.core.GameManager;
import de.matzefratze123.heavyspleef.core.SignWall;
import de.matzefratze123.heavyspleef.core.Team;
import de.matzefratze123.heavyspleef.objects.SpleefPlayer;
import de.matzefratze123.heavyspleef.signs.SpleefSign;
import de.matzefratze123.heavyspleef.signs.SpleefSignExecutor;
import de.matzefratze123.heavyspleef.util.LanguageHandler;
import de.matzefratze123.heavyspleef.util.Permissions;
import de.matzefratze123.heavyspleef.util.Util;

public class SpleefSignJoin implements SpleefSign {

	@Override
	public void onClick(SpleefPlayer player, Sign sign) {
		String[] lines = SpleefSignExecutor.stripSign(sign);
		
		//Check wether there is no game on the third line
		if (lines[2].isEmpty()) {
			if (!player.getBukkitPlayer().hasPermission(Permissions.JOIN_GAME_INV.getPerm())) {
				player.sendMessage(LanguageHandler._("noPermission"));
				return;
			}
			
			//Open up Join GUI
			HeavySpleef.getInstance().getJoinGUI().open(player.getBukkitPlayer());
		} else {
			//Check if the game exists
			if (!GameManager.hasGame(lines[2])) {
				player.sendMessage(LanguageHandler._("arenaDoesntExists"));
				return;
			}
			
			ChatColor color = null;
			
			//Check teams
			if (!lines[3].isEmpty()) {
				try {
					color = ChatColor.valueOf(lines[3].toUpperCase());
				} catch (Exception ex) {
					player.sendMessage(LanguageHandler._("invalidTeam"));
					return;
				}
			} else {
				//Try to calculate team colors via block neighboors
				Block up = sign.getBlock().getRelative(BlockFace.UP);
				if (up.getType() == Material.WOOL) {
					color = Team.woolDyeToChatColor(up.getData());
				}
				
				Block attached = SignWall.getAttachedBlock(sign);
				if (attached != null && attached.getType() == Material.WOOL)
					color = Team.woolDyeToChatColor(attached.getData());
			}
			
			CommandJoin.doFurtherChecks(GameManager.getGame(lines[2]), player, color);
		}
	}

	@Override
	public String getId() {
		return "sign.join";
	}

	@Override
	public String[] getLines() {
		String[] lines = new String[3];
		
		lines[0] = "[Join]";
		
		return lines;
	}

	@Override
	public Permissions getPermission() {
		return Permissions.SIGN_JOIN;
	}

	@Override
	public void onPlace(SignChangeEvent e) {
		if (!GameManager.hasGame(e.getLine(2))) {
			e.getPlayer().sendMessage(HSCommand._("arenaDoesntExists"));
			e.getBlock().breakNaturally();
			return;
		}
		
		if (!e.getLine(3).isEmpty()) {
			ChatColor color = null;
			
			for (ChatColor c : Team.allowedColors) {
				if (c.name().equalsIgnoreCase(e.getLine(3)))
					color = c;
			}
			
			if (color == null) {
				e.getPlayer().sendMessage(LanguageHandler._("invalidColor"));
				e.getBlock().breakNaturally();
				return;
			}
			
			e.setLine(3, color + Util.formatMaterialName(e.getLine(3)));
		}
		
		e.getPlayer().sendMessage(HSCommand._("spleefSignCreated"));
		
		e.setLine(1, ChatColor.DARK_GRAY + "[" + ChatColor.GREEN + ChatColor.BOLD + "Join" + ChatColor.DARK_GRAY + "]");
		e.setLine(2, ChatColor.DARK_RED + GameManager.getGame(e.getLine(2)).getName());
	}

}
