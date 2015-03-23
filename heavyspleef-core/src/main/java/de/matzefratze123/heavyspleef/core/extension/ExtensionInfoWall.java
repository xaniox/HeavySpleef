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
package de.matzefratze123.heavyspleef.core.extension;

import org.bukkit.Location;

import de.matzefratze123.heavyspleef.commands.base.Command;
import de.matzefratze123.heavyspleef.commands.base.CommandContext;
import de.matzefratze123.heavyspleef.commands.base.CommandException;
import de.matzefratze123.heavyspleef.commands.base.CommandValidate;
import de.matzefratze123.heavyspleef.commands.base.PlayerOnly;
import de.matzefratze123.heavyspleef.core.Game;
import de.matzefratze123.heavyspleef.core.GameManager;
import de.matzefratze123.heavyspleef.core.HeavySpleef;
import de.matzefratze123.heavyspleef.core.PlayerPostActionHandler.PostActionCallback;
import de.matzefratze123.heavyspleef.core.PlayerPostActionHandler.PostActionType;
import de.matzefratze123.heavyspleef.core.event.GameListener;
import de.matzefratze123.heavyspleef.core.event.GameListener.Priority;
import de.matzefratze123.heavyspleef.core.event.GameStateChangeEvent;
import de.matzefratze123.heavyspleef.core.event.PlayerJoinGameEvent;
import de.matzefratze123.heavyspleef.core.event.PlayerLeaveGameEvent;
import de.matzefratze123.heavyspleef.core.event.PlayerLoseGameEvent;
import de.matzefratze123.heavyspleef.core.extension.ExtensionInfoWall.WallValidationException.Cause;
import de.matzefratze123.heavyspleef.core.i18n.I18N;
import de.matzefratze123.heavyspleef.core.i18n.Messages;
import de.matzefratze123.heavyspleef.core.player.SpleefPlayer;

public class ExtensionInfoWall implements GameExtension {

	@Command(name = "addwall", permission = "heavyspleef.addwall", 
			description = "Adds a wall consisting of signs to show the status of a particular game", 
			usage = "/spleef addwall <game>", minArgs = 1)
	@PlayerOnly
	public static void onAddWallCommand(CommandContext context, HeavySpleef heavySpleef) throws CommandException {
		SpleefPlayer player = heavySpleef.getSpleefPlayer(context.getSender());
		GameManager manager = heavySpleef.getGameManager();
		final I18N i18n = I18N.getInstance();
		
		String gameName = context.getString(0);
		CommandValidate.isTrue(manager.hasGame(gameName), i18n.getVarString(Messages.Command.GAME_DOESNT_EXIST)
				.setVariable("game", gameName)
				.toString());
		
		Game game = manager.getGame(gameName);
		heavySpleef.getPostActionHandler().addPostAction(player, PostActionType.PLAYER_INTERACT, new PostActionCallback() {

			@Override
			public void onPostAction(SpleefPlayer player, Object cookie) {
				//TODO: Add wall
			}
			
		}, game);
	}
	
	@Command(name = "removewall", permission = "heavyspleef.removewall",
			description = "Removes a wall by right-clicking it", usage = "/spleef removewall")
	@PlayerOnly
	public static void onRemoveWallCommand(CommandContext context, HeavySpleef heavySpleef) throws CommandException {
		SpleefPlayer player = heavySpleef.getSpleefPlayer(context.getSender());
		GameManager manager = heavySpleef.getGameManager();
		final I18N i18n = I18N.getInstance();
		
		String gameName = context.getString(0);
		CommandValidate.isTrue(manager.hasGame(gameName), i18n.getVarString(Messages.Command.GAME_DOESNT_EXIST)
				.setVariable("game", gameName)
				.toString());
		
		Game game = manager.getGame(gameName);
		heavySpleef.getPostActionHandler().addPostAction(player, PostActionType.PLAYER_INTERACT, new PostActionCallback() {
			
			@Override
			public void onPostAction(SpleefPlayer player, Object cookie) {
				//TODO: Remove the wall
			}
		}, game);
	}
	
	/* The start location of this wall */
	private Location start;
	/* The end location of this wall */
	private Location end;
	/* The direction looking straight from start to end */
	private BaseDirection direction;
	
	public ExtensionInfoWall(Location start, Location end) throws WallValidationException {
		this.start = start;
		this.end = end;
		
		recalculate();
	}
	
	/**
	 * Validates locations and recalculates their direction
	 */
	private void recalculate() throws WallValidationException {
		//The two defining points must lie on the x-axis or y-axis
		if (start.getBlockX() != end.getBlockX() && start.getBlockZ() != end.getBlockZ()) {
			throw new WallValidationException(Cause.NOT_IN_LINE);
		}
		
		if (start.getBlockY() != end.getBlockY()) {
			throw new WallValidationException(Cause.NOT_SAME_Y_AXIS);
		}
		
		if (start.getBlockX() == end.getBlockX()) {
			if (start.getBlockZ() < end.getBlockZ()) {
				direction = BaseDirection.SOUTH;
			} else {
				direction = BaseDirection.NORTH;
			}
		} else if (start.getBlockZ() == end.getBlockZ()) {
			if (start.getBlockX() < end.getBlockX()) {
				direction = BaseDirection.EAST;
			} else {
				direction = BaseDirection.WEST;
			}
		}
	}
	
	@GameListener
	public void onGameStateChange(GameStateChangeEvent event) {
		Game game = event.getGame();
		
		updateWall(game);
	}
	
	@GameListener(priority = Priority.MONITOR)
	public void onPlayerJoin(PlayerJoinGameEvent event) {
		
	}
	
	@GameListener
	public void onPlayerLeave(PlayerLeaveGameEvent event) {
		
	}
	
	@GameListener
	public void onPlayerLose(PlayerLoseGameEvent event) {
		
	}
	
	private void updateWall(Game game) {
		
	}
	
	public enum BaseDirection {
		
		NORTH,
		SOUTH,
		WEST,
		EAST;
		
	}
	
	public static class WallValidationException extends Exception {

		private static final long serialVersionUID = -1720873353345614589L;
		
		private Cause cause;
		
		public WallValidationException(Cause cause) {
			this.cause = cause;
		}
		
		public WallValidationException(Cause cause, String message) {
			super(message);
			
			this.cause = cause;
		}
		
		public Cause getExceptionCause() {
			return cause;
		}
		
		public enum Cause {
			
			NOT_IN_LINE, NOT_SAME_Y_AXIS;
			
		}
		
	}
	
}
