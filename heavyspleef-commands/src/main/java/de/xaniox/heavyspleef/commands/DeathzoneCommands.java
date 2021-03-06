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

import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.regions.*;
import com.sk89q.worldedit.world.World;
import de.xaniox.heavyspleef.commands.base.*;
import de.xaniox.heavyspleef.core.HeavySpleef;
import de.xaniox.heavyspleef.core.Permissions;
import de.xaniox.heavyspleef.core.RegionVisualizer;
import de.xaniox.heavyspleef.core.game.Game;
import de.xaniox.heavyspleef.core.game.GameManager;
import de.xaniox.heavyspleef.core.hook.HookReference;
import de.xaniox.heavyspleef.core.i18n.I18N;
import de.xaniox.heavyspleef.core.i18n.I18NManager;
import de.xaniox.heavyspleef.core.i18n.Messages;
import de.xaniox.heavyspleef.core.player.SpleefPlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class DeathzoneCommands {
	
	private final I18N i18n = I18NManager.getGlobal();
	
	@Command(name = "adddeathzone", permission = Permissions.PERMISSION_ADD_DEATHZONE, minArgs = 1,
			descref = Messages.Help.Description.ADDDEATHZONE,
			usage = "/spleef adddeathzone <Game> [Deathzone-Name]")
	@PlayerOnly
	public void onCommandAddDeathzone(CommandContext context, HeavySpleef heavySpleef) throws CommandException {
		SpleefPlayer player = heavySpleef.getSpleefPlayer(context.getSender());
		
		String gameName = context.getString(0);
		GameManager manager = heavySpleef.getGameManager();
		
		CommandValidate.isTrue(manager.hasGame(gameName), i18n.getVarString(Messages.Command.GAME_DOESNT_EXIST)
				.setVariable("game", gameName)
				.toString());
		Game game = manager.getGame(gameName);
		
		WorldEditPlugin plugin = (WorldEditPlugin) heavySpleef.getHookManager().getHook(HookReference.WORLDEDIT).getPlugin();
		com.sk89q.worldedit.entity.Player bukkitPlayer = plugin.wrapPlayer(player.getBukkitPlayer());
		World world = new BukkitWorld(player.getBukkitPlayer().getWorld());
		
		LocalSession session = plugin.getWorldEdit().getSessionManager().get(bukkitPlayer);
		RegionSelector selector = session.getRegionSelector(world);
		
		Region region;
		
		try {
			region = selector.getRegion().clone();
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
	
	@TabComplete("adddeathzone")
	public void onAddDeathzoneTabComplete(CommandContext context, List<String> list, HeavySpleef heavySpleef) {
		GameManager manager = heavySpleef.getGameManager();
		
		if (context.argsLength() == 1) {
			for (Game game : manager.getGames()) {
				list.add(game.getName());
			}
		}
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
	
	@Command(name = "removedeathzone", permission = Permissions.PERMISSION_REMOVE_DEATHZONE, minArgs = 2,
			descref = Messages.Help.Description.REMOVEDEATHZONE,
			usage = "/spleef removedeathzone <Game> <Deathzone-Name>")
	public void onCommandRemoveDeathzone(CommandContext context, HeavySpleef heavySpleef) throws CommandException {
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
	
	@TabComplete("removedeathzone")
	public void onRemoveDeathzoneTabComplete(CommandContext context, List<String> list, HeavySpleef heavySpleef) throws CommandException {
		GameManager manager = heavySpleef.getGameManager();
		
		if (context.argsLength() == 1) {
			for (Game game : manager.getGames()) {
				list.add(game.getName());
			}
		} else if (context.argsLength() == 2) {
			Game game = manager.getGame(context.getString(0));
			for (String deathzoneName : game.getDeathzones().keySet()) {
				list.add(deathzoneName);
			}
		}
	}
	
	@Command(name = "showdeathzone", permission = Permissions.PERMISSION_SHOW_DEATHZONE, minArgs = 2,
			descref = Messages.Help.Description.SHOWDEATHZONE,
			usage = "/spleef showdeathzone <Game> <Deathzone-Name>")
	@PlayerOnly
	public void onCommandShowDeathzone(CommandContext context, HeavySpleef heavySpleef) throws CommandException {
		SpleefPlayer player = heavySpleef.getSpleefPlayer(context.getSender());
		
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
		
		Region region = game.getDeathzone(deathzoneName);
		RegionVisualizer visualizer = heavySpleef.getRegionVisualizer();
		
		visualizer.visualize(region, player, game.getWorld());
		player.sendMessage(i18n.getString(Messages.Command.REGION_VISUALIZED));
	}
	
	@TabComplete("showdeathzone")
	public void onShowDeathzoneTabComplete(CommandContext context, List<String> list, HeavySpleef heavySpleef) throws CommandException {
		GameManager manager = heavySpleef.getGameManager();
		
		if (context.argsLength() == 1) {
			for (Game game : manager.getGames()) {
				list.add(game.getName());
			}
		} else if (context.argsLength() == 2) {
			Game game = manager.getGame(context.getString(0));
			for (String deathzoneName : game.getDeathzones().keySet()) {
				list.add(deathzoneName);
			}
		}
	}
	
}