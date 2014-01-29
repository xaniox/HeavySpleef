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
package de.matzefratze123.heavyspleef.signs;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import de.matzefratze123.heavyspleef.HeavySpleef;
import de.matzefratze123.heavyspleef.objects.SpleefPlayer;
import de.matzefratze123.heavyspleef.util.I18N;
import de.matzefratze123.heavyspleef.util.Permissions;

public class SpleefSignExecutor implements Listener {

	private List<SpleefSign> registeredSigns = new ArrayList<SpleefSign>();

	private static SpleefSignExecutor instance;

	static {
		if (instance == null)
			instance = new SpleefSignExecutor();
	}

	public static SpleefSignExecutor getInstance() {
		return instance;
	}

	public void registerSign(SpleefSign sign) {
		if (hasSign(sign.getId()))
			throw new SpleefSignAlreadyExistsException("Spleef sign is already registered!");

		registeredSigns.add(sign);
	}

	private boolean hasSign(String id) {
		for (SpleefSign sign : registeredSigns) {
			if (sign.getId().equalsIgnoreCase(id))
				return true;
		}

		return false;
	}

	public void unregisterSign(SpleefSign sign) {
		Validate.notNull(sign, "SpleefSign cannot be null");

		unregisterSign(sign.getId());
	}

	public void unregisterSign(String id) {
		Validate.notNull(id, "SpleefSign id cannot be null");

		for (int i = 0; i < registeredSigns.size(); i++) {
			SpleefSign sign = registeredSigns.get(i);

			if (sign.getId().equalsIgnoreCase(id)) {
				registeredSigns.remove(sign);
				return;
			}
		}
	}

	@EventHandler
	public void execute(PlayerInteractEvent e) {
		Player player = e.getPlayer();
		Block block = e.getClickedBlock();

		if (e.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;

		if (block.getType() != Material.SIGN_POST && block.getType() != Material.WALL_SIGN)
			return;

		Sign sign = (Sign) block.getState();
		String striped = ChatColor.stripColor(sign.getLine(0));
		
		if (!striped.equalsIgnoreCase("[Spleef]") && !striped.equalsIgnoreCase("[Splegg]"))
			return;

		SpleefSign matching = matchSign(sign.getLines());
		
		if (matching == null)
			return;
		
		Permissions permission = matching.getPermission();

		// Check permissions
		if (permission != null && !player.hasPermission(permission.getPerm())) {
			player.sendMessage(I18N._("noPermission"));
			return;
		}

		SpleefPlayer spleefPlayer = HeavySpleef.getInstance().getSpleefPlayer(player);
		matching.onClick(spleefPlayer, sign);
	}

	@EventHandler
	public void onSignChange(SignChangeEvent e) {
		if (!e.getLine(0).equalsIgnoreCase("[Spleef]") && !e.getLine(0).equalsIgnoreCase("[Splegg]")) {
			return;
		}
		
		if (!e.getPlayer().hasPermission(Permissions.CREATE_SPLEEF_SIGN.getPerm())) {
			e.getPlayer().sendMessage(I18N._("noPermission"));
			e.getBlock().breakNaturally();
			return;
		}
		
		SpleefSign matching = matchSign(e.getLines());
		
		if (matching == null) {
			return;
		}
		
		boolean containsSpleef = e.getLine(0).toLowerCase().contains("spleef");
		boolean containsSplegg = e.getLine(0).toLowerCase().contains("splegg");
		String str = null;
		
		if (containsSpleef) {
			str = "Spleef";
		} else if (containsSplegg) {
			str = "Splegg";
		}
		
		e.setLine(0, ChatColor.DARK_GRAY + "[" + ChatColor.GOLD + str + ChatColor.DARK_GRAY + "]");
		matching.onPlace(e);
	}

	private SpleefSign matchSign(String[] lines) {
		// Go over all SpleefSigns
		for (SpleefSign spleefSign : registeredSigns) {
			
			Map<Integer, String[]> spleefSignLines = spleefSign.getLines();

			boolean matching = true;

			// Check all lines
			for (int i = 1; i < 4; i++) {
				int offset = i - 1;

				String line = lines[i];
				line = ChatColor.stripColor(line);
				
				
				String[] spleefSignLine = spleefSignLines.get(offset);
				if (spleefSignLine == null)
					continue;
				
				boolean matchingLine = false;
				
				for (String alias : spleefSignLine) {
					alias = ChatColor.stripColor(alias);
	
					// If one line is false the sign is not matching the current
					// SpleefSign
					if (line.equalsIgnoreCase(alias)) {
						matchingLine = true;
						break;
					}
				}
				
				if (!matchingLine) {
					matching = false;
				}
			}
			
			if (matching) {
				return spleefSign;
			}
		}
		
		return null;
	}
	
	public static String[] stripSign(Sign sign) {
		String[] lines = new String[4];
		
		for (int i = 0; i < sign.getLines().length; i++) {
			lines[i] = ChatColor.stripColor(sign.getLine(i));
		}
		
		return lines;
	}

}
