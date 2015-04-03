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

import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
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
import de.matzefratze123.heavyspleef.core.hook.HookReference;
import de.matzefratze123.heavyspleef.core.i18n.I18N;
import de.matzefratze123.heavyspleef.core.i18n.Messages;
import de.matzefratze123.heavyspleef.core.player.SpleefPlayer;

public class DeathzoneCommands {
	
	private final I18N i18n = I18N.getInstance();
	
	@Command(name = "adddeathzone", permission = "heavyspleef.adddeathzone", minArgs = 1,
			descref = Messages.Help.Description.ADDDEATHZONE,
			usage = "/spleef adddeathzone <Game> [Deathzone-Name]")
	@PlayerOnly
	public void onCommandAddDeathzone(CommandContext context, HeavySpleef heavySpleef) throws CommandException {
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
		
		String deathzoneName = context.argsLength() > 1 ? context.getString(1) : generateUniqueDeathzoneName(game);
		
		game.addDeathzone(deathzoneName, region);
		player.sendMessage(i18n.getVarString(Messages.Command.DEATHZONE_ADDED)
				.setVariable("deathzonename", deathzoneName)
				.toString());
		
		//Save the game
		heavySpleef.getDatabaseHandler().saveGame(game, null);
	}

	private void validateSelectedRegion(Region region) throws CommandException {
		if (!(region instanceof CuboidRegion) && !(region instanceof Polygonal2DRegion) && !(region instanceof CylinderRegion)) {
			throw new CommandException(i18n.getString(Messages.Command.WORLDEDIT_SELECTION_NOT_SUPPORTED));
		}
	}
	
	private String generateUniqueDeathzoneName(Game game) {
		final String prefix = "deathzone_";
		int counter = 0;
		
		while (game.isDeathzonePresent(prefix + counter)) {
			++counter;
		}
		
		return prefix + counter;
	}
	
	@Command(name = "removedeathzone", permission = "heavyspleef.removedeathzone", minArgs = 2,
			descref = Messages.Help.Description.REMOVEDEATHZONE,
			usage = "/spleef removedeathzone <Game> <Deathzone-Name>")
	public void onCommandRemoveDeathzone(CommandContext context, HeavySpleef heavySpleef) throws CommandException {
		CommandSender sender = context.getSender();
		
		String gameName = context.getString(0);
		GameManager manager = heavySpleef.getGameManager();
		
		CommandValidate.isTrue(manager.hasGame(gameName), i18n.getVarString(Messages.Command.GAME_DOESNT_EXIST)
				.setVariable("game", gameName)
				.toString());
		Game game = manager.getGame(gameName);
		
		String deathzoneName = context.getString(1);
		
		CommandValidate.isTrue(game.isDeathzonePresent(deathzoneName), i18n.getVarString(Messages.Command.DEATHZONE_NOT_PRESENT)
				.setVariable("deathzonename", deathzoneName)
				.toString());
		
		game.removeDeathzone(deathzoneName);
		sender.sendMessage(i18n.getVarString(Messages.Command.DEATHZONE_REMOVED)
				.setVariable("deathzonename", deathzoneName)
				.toString());
		
		heavySpleef.getDatabaseHandler().saveGame(game, null);
	}
	
	@Command(name = "showdeathzone", permission = "heavyspleef.showdeathzone", minArgs = 2,
			descref = Messages.Help.Description.SHOWDEATHZONE,
			usage = "/spleef showdeathzone <Game> <Deathzone-Name>")
	public void onCommandShowDeathzone(CommandContext context, HeavySpleef heavySpleef) throws CommandException {
		Player player = context.getSender();
		SpleefPlayer spleefPlayer = heavySpleef.getSpleefPlayer(player);
		
		String gameName = context.getString(0);
		GameManager manager = heavySpleef.getGameManager();
		
		CommandValidate.isTrue(manager.hasGame(gameName), I18N.getInstance().getVarString(Messages.Command.GAME_DOESNT_EXIST)
				.setVariable("game", gameName)
				.toString());
		Game game = manager.getGame(gameName);
		
		String deathzoneName = context.getString(1);
		
		CommandValidate.isTrue(game.isDeathzonePresent(deathzoneName), i18n.getVarString(Messages.Command.DEATHZONE_NOT_PRESENT)
				.setVariable("deathzonename", deathzoneName)
				.toString());
		
		Region region = game.getDeathzone(deathzoneName);
		RegionVisualizer visualizer = heavySpleef.getRegionVisualizer();
		
		visualizer.visualize(region, spleefPlayer, game.getWorld());
		player.sendMessage(i18n.getString(Messages.Command.REGION_VISUALIZED));
	}
	
}
