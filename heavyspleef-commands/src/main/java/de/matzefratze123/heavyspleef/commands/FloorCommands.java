/*
 * This file is part of HeavySpleef.
 * Copyright (c) 2014-2015 matzefratze123
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
package de.matzefratze123.heavyspleef.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.CylinderRegion;
import com.sk89q.worldedit.regions.Polygonal2DRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.regions.RegionSelector;
import com.sk89q.worldedit.world.World;

import de.matzefratze123.heavyspleef.commands.base.Command;
import de.matzefratze123.heavyspleef.commands.base.CommandContext;
import de.matzefratze123.heavyspleef.commands.base.CommandException;
import de.matzefratze123.heavyspleef.commands.base.CommandValidate;
import de.matzefratze123.heavyspleef.commands.base.PlayerOnly;
import de.matzefratze123.heavyspleef.core.Game;
import de.matzefratze123.heavyspleef.core.GameManager;
import de.matzefratze123.heavyspleef.core.HeavySpleef;
import de.matzefratze123.heavyspleef.core.RegionVisualizer;
import de.matzefratze123.heavyspleef.core.floor.Floor;
import de.matzefratze123.heavyspleef.core.floor.SimpleClipboardFloor;
import de.matzefratze123.heavyspleef.core.hook.HookReference;
import de.matzefratze123.heavyspleef.core.i18n.I18N;
import de.matzefratze123.heavyspleef.core.i18n.Messages;
import de.matzefratze123.heavyspleef.core.player.SpleefPlayer;

public class FloorCommands {
	
	private final I18N i18n = I18N.getInstance();
	
	@Command(name = "addfloor", permission = "heavyspleef.addfloor", minArgs = 1,
			descref = Messages.Help.Description.ADDFLOOR,
			usage = "/spleef addfloor <Game> [Name]")
	@PlayerOnly
	public void onCommandAddFloor(CommandContext context, HeavySpleef heavySpleef) throws CommandException {
		Player player = context.getSender();
		
		String gameName = context.getString(0);
		GameManager manager = heavySpleef.getGameManager();
		
		CommandValidate.isTrue(manager.hasGame(gameName), I18N.getInstance().getVarString(Messages.Command.GAME_DOESNT_EXIST)
				.setVariable("game", gameName)
				.toString());
		Game game = manager.getGame(gameName);
		
		WorldEditPlugin plugin = (WorldEditPlugin) heavySpleef.getHookManager().getHook(HookReference.WORLDEDIT).getPlugin();
		com.sk89q.worldedit.entity.Player bukkitPlayer = plugin.wrapPlayer(player);
		World world = new BukkitWorld(player.getWorld());
		
		LocalSession session = plugin.getWorldEdit().getSessionManager().get(bukkitPlayer);
		RegionSelector selector = session.getRegionSelector(world);
		
		Region region;
		
		try {
			region = selector.getRegion();
		} catch (IncompleteRegionException e) {
			player.sendMessage(i18n.getString(Messages.Command.DEFINE_FULL_WORLDEDIT_REGION));
			return;
		}
		
		validateSelectedRegion(region);
		
		//Create a session for copying all blocks
		EditSession editSession = session.createEditSession(bukkitPlayer);
		
		Clipboard clipboard = new BlockArrayClipboard(region);
		ForwardExtentCopy copy = new ForwardExtentCopy(editSession, region, clipboard, region.getMinimumPoint());
		
		try {
			Operations.completeLegacy(copy);
		} catch (MaxChangedBlocksException e) {
			//We do not edit any blocks...
			e.printStackTrace();
			return;
		}
		
		String floorName = context.argsLength() > 1 ? context.getString(1) : generateUniqueFloorName(game);
		Floor floor = new SimpleClipboardFloor(floorName, clipboard);
		
		game.addFloor(floor);
		player.sendMessage(i18n.getVarString(Messages.Command.FLOOR_ADDED)
				.setVariable("floorname", floorName)
				.toString());
		
		//Save the game
		heavySpleef.getDatabaseHandler().saveGame(game, null);
	}

	private void validateSelectedRegion(Region region) throws CommandException {
		if (!(region instanceof CuboidRegion) && !(region instanceof Polygonal2DRegion) && !(region instanceof CylinderRegion)) {
			throw new CommandException(i18n.getString(Messages.Command.WORLDEDIT_SELECTION_NOT_SUPPORTED));
		}
	}
	
	private String generateUniqueFloorName(Game game) {
		final String prefix = "floor_";
		int counter = 0;
		
		while (game.isFloorPresent(prefix + counter)) {
			++counter;
		}
		
		return prefix + counter;
	}
	
	@Command(name = "removefloor", permission = "heavyspleef.removefloor", minArgs = 2,
			descref = Messages.Help.Description.REMOVEFLOOR,
			usage = "/spleef removefloor <Game> <Floorname>")
	public void onCommandRemoveFloor(CommandContext context, HeavySpleef heavySpleef) throws CommandException {
		CommandSender sender = context.getSender();
		
		String gameName = context.getString(0);
		GameManager manager = heavySpleef.getGameManager();
		
		CommandValidate.isTrue(manager.hasGame(gameName), I18N.getInstance().getVarString(Messages.Command.GAME_DOESNT_EXIST)
				.setVariable("game", gameName)
				.toString());
		Game game = manager.getGame(gameName);
		
		String floorName = context.getString(1);
		
		CommandValidate.isTrue(game.isFloorPresent(floorName), i18n.getVarString(Messages.Command.FLOOR_NOT_PRESENT)
				.setVariable("floorname", floorName)
				.toString());
		
		Floor floor = game.removeFloor(floorName);
		sender.sendMessage(i18n.getVarString(Messages.Command.FLOOR_REMOVED)
				.setVariable("floorname", floor.getName())
				.toString());
		
		heavySpleef.getDatabaseHandler().saveGame(game, null);
	}
	
	@Command(name = "showfloor", permission = "heavyspleef.showfloor", minArgs = 2,
			descref = Messages.Help.Description.SHOWFLOOR,
			usage = "/spleef showfloor <Game> <Floorname>")
	public void onCommandShowFloor(CommandContext context, HeavySpleef heavySpleef) throws CommandException {
		Player player = context.getSender();
		SpleefPlayer spleefPlayer = heavySpleef.getSpleefPlayer(player);
		
		String gameName = context.getString(0);
		GameManager manager = heavySpleef.getGameManager();
		
		CommandValidate.isTrue(manager.hasGame(gameName), I18N.getInstance().getVarString(Messages.Command.GAME_DOESNT_EXIST)
				.setVariable("game", gameName)
				.toString());
		Game game = manager.getGame(gameName);
		
		String floorName = context.getString(1);
		
		CommandValidate.isTrue(game.isFloorPresent(floorName), i18n.getVarString(Messages.Command.FLOOR_NOT_PRESENT)
				.setVariable("floorname", floorName)
				.toString());
		
		Floor floor = game.getFloor(floorName);
		RegionVisualizer visualizer = heavySpleef.getRegionVisualizer();
		
		visualizer.visualize(floor.getRegion(), spleefPlayer, game.getWorld());
		player.sendMessage(i18n.getString(Messages.Command.REGION_VISUALIZED));
	}

}
