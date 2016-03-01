/*
 * This file is part of HeavySpleef.
 * Copyright (c) 2014-2016 Matthias Werning
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package de.xaniox.heavyspleef.commands;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.CylinderRegion;
import com.sk89q.worldedit.regions.Polygonal2DRegion;
import com.sk89q.worldedit.regions.Region;
import de.xaniox.heavyspleef.commands.base.*;
import de.xaniox.heavyspleef.core.HeavySpleef;
import de.xaniox.heavyspleef.core.Permissions;
import de.xaniox.heavyspleef.core.extension.ExtensionRegistry;
import de.xaniox.heavyspleef.core.extension.GameExtension;
import de.xaniox.heavyspleef.core.flag.AbstractFlag;
import de.xaniox.heavyspleef.core.floor.Floor;
import de.xaniox.heavyspleef.core.game.Game;
import de.xaniox.heavyspleef.core.game.GameManager;
import de.xaniox.heavyspleef.core.i18n.I18N;
import de.xaniox.heavyspleef.core.i18n.I18NManager;
import de.xaniox.heavyspleef.core.i18n.Messages;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class CommandInfo {
	
	private final I18N i18n = I18NManager.getGlobal();
	
	@Command(name = "info", minArgs = 1, usage = "/spleef info <game>",
			descref = Messages.Help.Description.INFO,
			permission = Permissions.PERMISSION_INFO)
	public void onInfoCommand(CommandContext context, HeavySpleef heavySpleef) throws CommandException {
		CommandSender sender = context.getSender();
		if (sender instanceof Player) {
			sender = heavySpleef.getSpleefPlayer(sender);
		}
		
		String gameName = context.getString(0);
		GameManager manager = heavySpleef.getGameManager();
		
		CommandValidate.isTrue(manager.hasGame(gameName), i18n.getVarString(Messages.Command.GAME_DOESNT_EXIST)
				.setVariable("game", gameName)
				.toString());
		Game game = manager.getGame(gameName);
		
		String gameStateName = game.getGameState().name().toLowerCase();
		gameStateName = Character.toUpperCase(gameStateName.charAt(0)) + gameStateName.substring(1);
		Map<String, AbstractFlag<?>> flags = game.getFlagManager().getPresentFlags();
		Collection<Floor> floors = game.getFloors();
		Map<String, Region> deathzones = game.getDeathzones();
		Set<GameExtension> extensions = game.getExtensions();
		
		StringBuilder builder = new StringBuilder();
		builder.append(ChatColor.GOLD + "-------- " + ChatColor.DARK_GRAY + ChatColor.BOLD + "[ " + ChatColor.GOLD
				+ i18n.getString(Messages.Command.GAME_INFORMATION) + ChatColor.DARK_GRAY + ChatColor.BOLD + " ] " + ChatColor.GOLD + "--------");
		builder.append('\n');
		
		builder.append(ChatColor.GOLD + "| ")
				.append(ChatColor.BLUE + i18n.getString(Messages.Command.NAME) + ": " + ChatColor.YELLOW + game.getName()).append('\n');
		builder.append(ChatColor.GOLD + "| ")
				.append(ChatColor.BLUE + i18n.getString(Messages.Command.WORLD) + ": " + ChatColor.YELLOW + game.getWorld().getName()).append('\n');
		builder.append(ChatColor.GOLD + "| ")
				.append(ChatColor.BLUE + i18n.getString(Messages.Command.GAME_STATE) + ": " + ChatColor.YELLOW + gameStateName).append('\n');
		builder.append(ChatColor.GOLD + "| ")
				.append(ChatColor.BLUE + i18n.getString(Messages.Command.FLAGS) + ": " + ChatColor.YELLOW + flags.size()).append('\n');

		for (Entry<String, AbstractFlag<?>> entry : flags.entrySet()) {
			builder.append(ChatColor.GOLD + "| ")
					.append(ChatColor.DARK_GRAY + " - " + ChatColor.YELLOW + entry.getKey() + ": " + entry.getValue().getValueAsString()).append('\n');
		}

		builder.append(ChatColor.GOLD + "| ")
				.append(ChatColor.BLUE + i18n.getString(Messages.Command.FLOORS) + ": " + ChatColor.YELLOW + floors.size()).append('\n');

		for (Floor floor : floors) {
			Region region = floor.getRegion();
			Vector minPos = region.getMinimumPoint();
			Vector maxPos = region.getMaximumPoint();

			builder.append(ChatColor.GOLD + "| ")
					.append(ChatColor.DARK_GRAY + " - " + ChatColor.YELLOW + floor.getName() + ": " + getRegionTypeName(region) + " " + vectorAsString(minPos)
							+ " -> " + vectorAsString(maxPos)).append('\n');
		}

		builder.append(ChatColor.GOLD + "| ")
				.append(ChatColor.BLUE + i18n.getString(Messages.Command.DEATH_ZONES) + ": " + ChatColor.YELLOW + deathzones.size()).append('\n');

		for (Entry<String, Region> entry : deathzones.entrySet()) {
			String name = entry.getKey();
			Region region = entry.getValue();
			
			Vector minPos = region.getMinimumPoint();
			Vector maxPos = region.getMaximumPoint();

			builder.append(ChatColor.GOLD + "| ")
					.append(ChatColor.DARK_GRAY + " - " + ChatColor.YELLOW + name + ": " + getRegionTypeName(region) + ", " + vectorAsString(minPos)
							+ " -> " + vectorAsString(maxPos)).append('\n');
		}

		builder.append(ChatColor.GOLD + "| ")
				.append(ChatColor.BLUE + i18n.getString(Messages.Command.EXTENSIONS) + ": " + ChatColor.YELLOW + extensions.size()).append('\n');

		ExtensionRegistry extRegistry = heavySpleef.getExtensionRegistry();

		for (GameExtension ext : extensions) {
			builder.append(ChatColor.GOLD + "| ")
					.append(ChatColor.DARK_GRAY + " - " + ChatColor.YELLOW + extRegistry.getExtensionName(ext.getClass())).append('\n');
		}

		builder.append(ChatColor.GOLD + "----------------------------------");
		sender.sendMessage(builder.toString());
	}
	
	private String getRegionTypeName(Region region) {
		String regionType = null;
		if (region instanceof CuboidRegion) {
			regionType = i18n.getString(Messages.Command.CUBOID);
		} else if (region instanceof CylinderRegion) {
			regionType = i18n.getString(Messages.Command.CYLINDRICAL);
		} else if (region instanceof Polygonal2DRegion) {
			regionType = i18n.getString(Messages.Command.POLYGONAL);
		}
		
		return regionType;
	}
	
	private String vectorAsString(Vector vector) {
		StringBuilder builder = new StringBuilder();
		
		builder.append('(');
		builder.append(vector.getBlockX());
		builder.append(',');
		builder.append(vector.getBlockY());
		builder.append(',');
		builder.append(vector.getBlockZ());
		builder.append(')');
		
		return builder.toString();
	}
	
	@TabComplete("info")
	public void onInfoTabComplete(CommandContext context, List<String> list, HeavySpleef heavySpleef) {
		GameManager manager = heavySpleef.getGameManager();
		
		if (context.argsLength() == 1) {
			for (Game game : manager.getGames()) {
				list.add(game.getName());
			}
		}
	}
	
}