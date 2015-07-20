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

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import lombok.Getter;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.material.Attachable;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;
import org.dom4j.Element;

import com.google.common.collect.Sets;
import com.sk89q.worldedit.bukkit.BukkitUtil;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;

import de.matzefratze123.heavyspleef.commands.base.Command;
import de.matzefratze123.heavyspleef.commands.base.CommandContext;
import de.matzefratze123.heavyspleef.commands.base.CommandException;
import de.matzefratze123.heavyspleef.commands.base.CommandValidate;
import de.matzefratze123.heavyspleef.commands.base.PlayerOnly;
import de.matzefratze123.heavyspleef.commands.base.TabComplete;
import de.matzefratze123.heavyspleef.core.HeavySpleef;
import de.matzefratze123.heavyspleef.core.Permissions;
import de.matzefratze123.heavyspleef.core.PlayerPostActionHandler.PostActionCallback;
import de.matzefratze123.heavyspleef.core.config.ConfigType;
import de.matzefratze123.heavyspleef.core.config.DefaultConfig;
import de.matzefratze123.heavyspleef.core.config.SignLayoutConfiguration;
import de.matzefratze123.heavyspleef.core.event.GameEndEvent;
import de.matzefratze123.heavyspleef.core.event.GameStateChangeEvent;
import de.matzefratze123.heavyspleef.core.event.PlayerJoinGameEvent;
import de.matzefratze123.heavyspleef.core.event.PlayerLeaveGameEvent;
import de.matzefratze123.heavyspleef.core.event.Subscribe;
import de.matzefratze123.heavyspleef.core.event.Subscribe.Priority;
import de.matzefratze123.heavyspleef.core.extension.ExtensionLobbyWall.SignRow.BlockFace2D;
import de.matzefratze123.heavyspleef.core.extension.ExtensionLobbyWall.SignRow.SignLooper;
import de.matzefratze123.heavyspleef.core.extension.ExtensionLobbyWall.SignRow.SignRowValidationException;
import de.matzefratze123.heavyspleef.core.game.Game;
import de.matzefratze123.heavyspleef.core.game.GameManager;
import de.matzefratze123.heavyspleef.core.game.JoinRequester;
import de.matzefratze123.heavyspleef.core.game.JoinRequester.JoinValidationException;
import de.matzefratze123.heavyspleef.core.i18n.I18N;
import de.matzefratze123.heavyspleef.core.i18n.I18NManager;
import de.matzefratze123.heavyspleef.core.i18n.Messages;
import de.matzefratze123.heavyspleef.core.layout.SignLayout;
import de.matzefratze123.heavyspleef.core.player.SpleefPlayer;
import de.matzefratze123.heavyspleef.core.script.Variable;

@Extension(name = "lobby-wall", hasCommands = true)
public class ExtensionLobbyWall extends GameExtension {

	private static final String INGAME_PLAYER_PREFIX_KEY = "ingame-player-prefix";
	private static final String DEAD_PLAYER_PREFIX_KEY = "dead-player-prefix";
	
	private static final String DEFAULT_INGAME_PLAYER_PREFIX = "";
	private static final String DEFAULT_DEAD_PLAYER_PREFIX = ChatColor.GRAY.toString();
	
	private static final int MAX_SIGN_CHARS = 16;
		
	@Command(name = "addwall", permission = Permissions.PERMISSION_ADD_WALL,
			descref = Messages.Help.Description.ADDWALL,
			usage = "/spleef addwall <game>", minArgs = 1)
	@PlayerOnly
	public static void onAddWallCommand(CommandContext context, HeavySpleef heavySpleef) throws CommandException {
		SpleefPlayer player = heavySpleef.getSpleefPlayer(context.getSender());
		GameManager manager = heavySpleef.getGameManager();
		final I18N i18n = I18NManager.getGlobal();
		
		String gameName = context.getString(0);
		CommandValidate.isTrue(manager.hasGame(gameName), i18n.getVarString(Messages.Command.GAME_DOESNT_EXIST)
				.setVariable("game", gameName)
				.toString());
		
		Game game = manager.getGame(gameName);
		heavySpleef.getPostActionHandler().addPostAction(player, PlayerInteractEvent.class, new PostActionCallback<PlayerInteractEvent>() {

			@Override
			public void onPostAction(PlayerInteractEvent event, SpleefPlayer player, Object cookie) {
				Game game = (Game) cookie;
				
				Block clickedBlock = event.getClickedBlock();
				if (clickedBlock.getType() != Material.WALL_SIGN) {
					player.sendMessage(i18n.getString(Messages.Command.BLOCK_NOT_A_SIGN));
					return;
				}
				
				event.setCancelled(true);
				
				Sign sign = (Sign) clickedBlock.getState();
				SignRow row = SignRow.generateRow(sign);
				ExtensionLobbyWall wall = new ExtensionLobbyWall(row);
				
				game.addExtension(wall);
				wall.updateWall(game, false);
				player.sendMessage(i18n.getString(Messages.Command.WALL_ADDED));
			}
			
		}, game);
		
		player.sendMessage(i18n.getString(Messages.Command.CLICK_ON_SIGN_TO_ADD_WALL));
	}
	
	@TabComplete("addwall")
	public static void onAddWallTabComplete(CommandContext context, List<String> list, HeavySpleef heavySpleef) throws CommandException {
		GameManager manager = heavySpleef.getGameManager();
		
		if (context.argsLength() == 1) {
			for (Game game : manager.getGames()) {
				list.add(game.getName());
			}
		}
	}
	
	@Command(name = "removewall", permission = Permissions.PERMISSION_REMOVE_WALL, minArgs = 1,
			descref = Messages.Help.Description.REMOVEWALL, usage = "/spleef removewall <game>")
	@PlayerOnly
	public static void onRemoveWallCommand(CommandContext context, HeavySpleef heavySpleef) throws CommandException {
		SpleefPlayer player = heavySpleef.getSpleefPlayer(context.getSender());
		GameManager manager = heavySpleef.getGameManager();
		final I18N i18n = I18NManager.getGlobal();
		
		String gameName = context.getString(0);
		CommandValidate.isTrue(manager.hasGame(gameName), i18n.getVarString(Messages.Command.GAME_DOESNT_EXIST)
				.setVariable("game", gameName)
				.toString());
		
		Game game = manager.getGame(gameName);
		heavySpleef.getPostActionHandler().addPostAction(player, PlayerInteractEvent.class, new PostActionCallback<PlayerInteractEvent>() {
			
			@Override
			public void onPostAction(PlayerInteractEvent event, SpleefPlayer player, Object cookie) {
				Game game = (Game) cookie;
				Block clickedBlock = event.getClickedBlock();
				
				if (clickedBlock.getType() != Material.WALL_SIGN) {
					player.sendMessage(i18n.getString(Messages.Command.BLOCK_NOT_A_SIGN));
					return;
				}
				
				event.setCancelled(true);
				
				int removed = 0;
				Set<ExtensionLobbyWall> candidates = game.getExtensionsByType(ExtensionLobbyWall.class);
				for (ExtensionLobbyWall candidate : candidates) {
					Location start = candidate.getStart();
					Location end = candidate.getEnd();
					
					com.sk89q.worldedit.Vector startVec = BukkitUtil.toVector(start);
					com.sk89q.worldedit.Vector endVec = BukkitUtil.toVector(end);
					com.sk89q.worldedit.Vector blockVec = BukkitUtil.toVector(clickedBlock);
					
					Region region = new CuboidRegion(startVec, endVec);
					
					if (!region.contains(blockVec)) {
						continue;
					}
					
					candidate.clearAll();
					game.removeExtension(candidate);
					removed++;
				}
				
				if (removed > 0) {
					player.sendMessage(i18n.getVarString(Messages.Command.WALLS_REMOVED)
							.setVariable("count", String.valueOf(removed))
							.toString());
				} else {
					player.sendMessage(i18n.getString(Messages.Command.NO_WALLS_FOUND));
				}
			}
		}, game);
		
		player.sendMessage(i18n.getString(Messages.Command.CLICK_ON_WALL_TO_REMOVE));
	}
	
	/* An instance of the global i18n */
	private final I18N i18n = I18NManager.getGlobal();
	/* The actual sign row we're using */
	private SignRow row;
	
	@SuppressWarnings("unused")
	private ExtensionLobbyWall() {}
	
	public ExtensionLobbyWall(Location start, Location end) throws SignRowValidationException {
		this.row = new SignRow(start, end);
	}
	
	public ExtensionLobbyWall(SignRow row) {
		this.row = row;
	}
	
	public Location getStart() {
		return row.getStart();
	}
	
	public Location getEnd() {
		return row.getEnd();
	}
	
	public BlockFace2D getDirection() {
		return row.getDirection();
	}
	
	public void clearAll() {
		row.clearAll();
	}
	
	@Subscribe(priority = Priority.MONITOR)
	public void onGameStateChange(GameStateChangeEvent event) {
		Game game = event.getGame();
		updateWall(game, false);
	}
	
	@Subscribe(priority = Priority.MONITOR)
	public void onPlayerJoin(PlayerJoinGameEvent event) {
		Game game = event.getGame();
		updateWall(game, false);
	}
	
	@Subscribe(priority = Priority.MONITOR)
	public void onPlayerLeave(PlayerLeaveGameEvent event) {
		if (event.isCancelled()) {
			return;
		}
		
		Game game = event.getGame();
		updateWall(game, false);
	}
	
	@Subscribe(priority = Priority.MONITOR)
	public void onGameEnd(GameEndEvent event) {
		Game game = event.getGame();
		updateWall(game, true);
	}
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		SpleefPlayer player = heavySpleef.getSpleefPlayer(event.getPlayer());
		Block clicked = event.getClickedBlock();
		if (clicked == null || (clicked.getType() != Material.WALL_SIGN && clicked.getType() != Material.SIGN_POST)) {
			return;
		}
		
		if (!clicked.getLocation().equals(row.getStart())) {
			return;
		}
		
		Game game = getGame();
		if (game == null) {
			return;
		}
		
		try {
			long timer = game.getJoinRequester().request(player, JoinRequester.QUEUE_PLAYER_CALLBACK);
			if (timer > 0) {
				player.sendMessage(i18n.getVarString(Messages.Command.JOIN_TIMER_STARTED)
						.setVariable("timer", String.valueOf(timer))
						.toString());
			}
		} catch (JoinValidationException e) {
			player.sendMessage(e.getMessage());
			JoinRequester.QUEUE_PLAYER_CALLBACK.onJoin(player, game, e.getResult());
		}
	}
	
	public void updateWall(final Game game, final boolean reset) {
		final HeavySpleef heavySpleef = game.getHeavySpleef();
		
		final SignLayoutConfiguration joinConfig = heavySpleef.getConfiguration(ConfigType.JOIN_SIGN_LAYOUT_CONFIG);
		final SignLayoutConfiguration infoConfig = heavySpleef.getConfiguration(ConfigType.INFO_WALL_SIGN_LAYOUT_CONFIG);
		
		final String ingamePrefix = infoConfig.getOption(INGAME_PLAYER_PREFIX_KEY, DEFAULT_INGAME_PLAYER_PREFIX);
		final String deadPrefix = infoConfig.getOption(DEAD_PLAYER_PREFIX_KEY, DEFAULT_DEAD_PLAYER_PREFIX);
		
		final SignLayout joinLayout = joinConfig.getLayout();
		final SignLayout infoLayout = infoConfig.getLayout();
		
		row.loopSigns(new SignLooper() {
			
			boolean ingamePlayers = true;
			Iterator<SpleefPlayer> currentIterator = game.getPlayers().iterator();
			
			@Override
			public LoopReturn loop(int index, Sign sign) {
				if (index == 0) {
					//First sign is the join sign
					DefaultConfig defConfig = heavySpleef.getConfiguration(ConfigType.DEFAULT_CONFIG);
					Set<Variable> vars = Sets.newHashSet();
					vars.add(new Variable("prefix", defConfig.getSignSection().getSpleefPrefix()));
					game.supply(vars, joinLayout.getRequestedVariables());
					
					joinLayout.inflate(sign, vars);
				} else if (index == 1) {
					//Second sign is the informational sign
					infoLayout.inflate(sign, game);
				} else {
					boolean breakLoop = false;
					
					for (int i = 0; i < SignLayout.LINE_COUNT; i++) {
						if (reset) {
							sign.setLine(i, "");
							continue;
						}
						
						String player = null;
						String prefix = null;
						
						if (currentIterator.hasNext()) {
							SpleefPlayer spleefPlayer = currentIterator.next();
							player = spleefPlayer.getName();
							prefix = ingamePlayers ? ingamePrefix + (spleefPlayer.isVip() ? heavySpleef.getVipPrefix() : "") : deadPrefix;
						} else {
							if (ingamePlayers) {
								currentIterator = game.getDeadPlayers().iterator();
								ingamePlayers = false;
								
								if (currentIterator.hasNext()) {
									player = currentIterator.next().getName();
									prefix = deadPrefix;
								} else {
									player = "";
									prefix = "";
								}
							} else {
								player = "";
								prefix = "";
							}
						}
						
						String line = prefix + player;
						if (line.length() > MAX_SIGN_CHARS) {
							line = line.substring(0, MAX_SIGN_CHARS);
						}
						
						sign.setLine(i, line);
					}
					
					if (breakLoop) {
						return LoopReturn.BREAK;
					}
				}
				
				sign.update(true);
				
				return LoopReturn.DEFAULT;
			}
		});
	}
	
	@Override
	public void marshal(Element element) {
		row.marshal(element);
	}
	
	@Override
	public void unmarshal(Element element) {
		row = new SignRow();
		row.unmarshal(element);
	}
	
	@Getter
	public static class SignRow {
		
		/* The world this wall is in */
		private World world;
		/* The start location of this wall */
		private Location start;
		/* The end location of this wall */
		private Location end;
		/* The direction looking straight from start to end */
		private BlockFace2D direction;
		/* The length of this row */
		private int length;
		
		public SignRow() {}
		
		public SignRow(Location start, Location end) throws SignRowValidationException {
			this.world = start.getWorld();
			this.start = start;
			this.end = end;
			
			recalculate();
		}
		
		public static SignRow generateRow(Sign clicked) {
			Block block = clicked.getBlock();
			Location location = block.getLocation();
			Block attachedOn = getAttached(clicked);
			
			Location mod = attachedOn.getLocation().subtract(location);
			BlockFace2D dir = BlockFace2D.byVector2D(mod.getBlockX(), mod.getBlockZ());
			
			BlockFace2D leftDir = dir.left();
			BlockFace2D rightDir = dir.right();
			
			BlockFace leftFace = leftDir.getBlockFace3D();
			BlockFace rightFace = rightDir.getBlockFace3D();
			
			Location start = location;
			Block lastBlock = block;
			
			while (true) {
				Block leftBlock = lastBlock.getRelative(leftFace);
				if (leftBlock.getType() != Material.WALL_SIGN) {
					break;
				}
				
				lastBlock = leftBlock;
				start = leftBlock.getLocation();
			}
			
			Location end = location;
			lastBlock = block;
			
			while (true) {
				Block rightBlock = lastBlock.getRelative(rightFace);
				if (rightBlock.getType() != Material.WALL_SIGN) {
					break;
				}
				
				lastBlock = rightBlock;
				end = rightBlock.getLocation();
			}
			
			try {
				return new SignRow(start, end);
			} catch (SignRowValidationException e) {
				throw new RuntimeException(e);
			}
		}
		
		private static Block getAttached(Sign sign) {
			Attachable data = (Attachable) sign.getData();
			BlockFace attachingBlockFace = data.getFacing().getOppositeFace();
			
			return sign.getBlock().getRelative(attachingBlockFace);
		}
		
		public int getLength() {
			return length;
		}
		
		public void loopSigns(SignLooper looper) {
			Vector startVec = new Vector(start.getBlockX(), start.getBlockY(), start.getBlockZ());
			Vector endVec = new Vector(end.getBlockX(), end.getBlockY(), end.getBlockZ());
			
			Vector directionVec = direction.mod();
			
			int maxDistance = Math.abs(direction == BlockFace2D.NORTH || direction == BlockFace2D.SOUTH ? endVec.getBlockZ() - startVec.getBlockZ()
					: endVec.getBlockX() - startVec.getBlockX());
			BlockIterator iterator = new BlockIterator(world, startVec, directionVec, 0, maxDistance);
			
			for (int i = 0; iterator.hasNext(); i++) {
				Block block = iterator.next();
				
				if (block.getType() != Material.WALL_SIGN) {
					continue;
				}
				
				Sign sign = (Sign) block.getState();
				
				SignLooper.LoopReturn loopReturn = looper.loop(i, sign);
				if (loopReturn == SignLooper.LoopReturn.CONTINUE) {
					continue;
				} else if (loopReturn == SignLooper.LoopReturn.BREAK) {
					break;
				} else if (loopReturn == SignLooper.LoopReturn.RETURN) {
					return;
				}
			}
		}
		
		public void clearAll() {
			loopSigns(new SignLooper() {
				
				@Override
				public LoopReturn loop(int index, Sign sign) {
					for (int i = 0; i < SignLayout.LINE_COUNT; i++) {
						sign.setLine(i, "");
					}
					
					sign.update();
					return LoopReturn.DEFAULT;
				}
			});
		}
		
		/**
		 * Validates locations and recalculates their direction
		 */
		private void recalculate() throws SignRowValidationException {
			//The two defining points must lie on the x-axis or y-axis
			if (start.getBlockX() != end.getBlockX() && start.getBlockZ() != end.getBlockZ()) {
				throw new SignRowValidationException(SignRowValidationException.Cause.NOT_IN_LINE);
			}
			
			if (start.getBlockY() != end.getBlockY()) {
				throw new SignRowValidationException(SignRowValidationException.Cause.NOT_SAME_Y_AXIS);
			}
			
			if (start.getBlockX() == end.getBlockX()) {
				if (start.getBlockZ() < end.getBlockZ()) {
					direction = BlockFace2D.SOUTH;
				} else {
					direction = BlockFace2D.NORTH;
				}
				
				length = Math.abs(end.getBlockZ() - start.getBlockZ()) + 1; 
			} else if (start.getBlockZ() == end.getBlockZ()) {
				if (start.getBlockX() < end.getBlockX()) {
					direction = BlockFace2D.EAST;
				} else {
					direction = BlockFace2D.WEST;
				}
				
				length = Math.abs(end.getBlockX() - start.getBlockX()) + 1;
			}
		}
		
		public void marshal(Element element) {
			Element startElement = element.addElement("start");
			Element xStartElement = startElement.addElement("x");
			Element yStartElement = startElement.addElement("y");
			Element zStartElement = startElement.addElement("z");
			Element worldStartElement = startElement.addElement("world");
			
			Element endElement = element.addElement("end");
			Element xEndElement = endElement.addElement("x");
			Element yEndElement = endElement.addElement("y");
			Element zEndElement = endElement.addElement("z");
			Element worldEndElement = endElement.addElement("world");
			
			worldStartElement.addText(start.getWorld().getName());
			xStartElement.addText(String.valueOf(start.getBlockX()));
			yStartElement.addText(String.valueOf(start.getBlockY()));
			zStartElement.addText(String.valueOf(start.getBlockZ()));
			
			worldEndElement.addText(end.getWorld().getName());
			xEndElement.addText(String.valueOf(end.getBlockX()));
			yEndElement.addText(String.valueOf(end.getBlockY()));
			zEndElement.addText(String.valueOf(end.getBlockZ()));
		}

		public void unmarshal(Element element) {
			Element startElement = element.element("start");
			Element endElement = element.element("end");
			
			Element xStartElement = startElement.element("x");
			Element yStartElement = startElement.element("y");
			Element zStartElement = startElement.element("z");
			Element worldStartElement = startElement.element("world");
			
			Element xEndElement = endElement.element("x");
			Element yEndElement = endElement.element("y");
			Element zEndElement = endElement.element("z");
			Element worldEndElement = endElement.element("world");
			
			World startWorld = Bukkit.getWorld(worldStartElement.getText());
			int startX = Integer.parseInt(xStartElement.getText());
			int startY = Integer.parseInt(yStartElement.getText());
			int startZ = Integer.parseInt(zStartElement.getText());
			
			World endWorld = Bukkit.getWorld(worldEndElement.getText());
			int endX = Integer.parseInt(xEndElement.getText());
			int endY = Integer.parseInt(yEndElement.getText());
			int endZ = Integer.parseInt(zEndElement.getText());
			
			world = startWorld;
			start = new Location(startWorld, startX, startY, startZ);
			end = new Location(endWorld, endX, endY, endZ);
			
			try {
				recalculate();
			} catch (SignRowValidationException e) {
				throw new RuntimeException(e);
			}
		}
		
		public interface SignLooper {
			
			public LoopReturn loop(int index, Sign sign);
			
			public enum LoopReturn {
				
				DEFAULT,
				CONTINUE,
				BREAK,
				RETURN;
				
			}
			
		}
		
		public enum BlockFace2D {
			
			NORTH(0, -1, BlockFace.NORTH, 135, 225),
			SOUTH(0, 1, BlockFace.SOUTH, 315, 45),
			WEST(-1, 0, BlockFace.WEST, 225, 315),
			EAST(1, 0, BlockFace.EAST, 45, 135);
			
			private int xVec;
			private int zVec;
			private BlockFace blockFace;
			private int yawStart;
			private int yawEnd;
			
			private BlockFace2D(int xVec, int zVec, BlockFace blockFace, int yawStart, int yawEnd) {
				this.xVec = xVec;
				this.zVec = zVec;
				this.blockFace = blockFace;
				this.yawStart = yawStart;
				this.yawEnd = yawEnd;
			}
			
			public BlockFace2D opposite() {
				return byVector2D(-xVec, -zVec);
			}
			
			public BlockFace2D right() {
				return byVector2D(-zVec, xVec);
			}
			
			public BlockFace2D left() {
				return byVector2D(zVec, -xVec);
			}
			
			public Vector mod() {
				return new Vector(xVec, 0, zVec);
			}
			
			public BlockFace getBlockFace3D() {
				return blockFace;
			}
			
			public BlockFace2D getOpposite() {
				switch (this) {
				case EAST:
					return WEST;
				case NORTH:
					return SOUTH;
				case SOUTH:
					return NORTH;
				case WEST:
					return EAST;
				default:
					return null;
				}
			}
			
			public static BlockFace2D byVector2D(int x, int z) {
				x = normalize(x);
				z = normalize(z);
				
				for (BlockFace2D direction : values()) {
					if (direction.xVec == x && direction.zVec == z) {
						return direction;
					}
				}
				
				return null;
			}
			
			public static BlockFace2D byYaw(float yaw) {
				float absYaw = Math.abs(yaw);
				
				for (BlockFace2D face : values()) {
					if ((face.yawStart < face.yawEnd && (absYaw <= face.yawStart || absYaw > face.yawEnd))
							|| (face.yawStart > face.yawEnd && (absYaw <= face.yawStart && absYaw > face.yawEnd))) {
						continue;
					}
					
					if (yaw > 0 && (face == EAST || face == WEST)) {
						face = face.getOpposite();
					}
					
					return face;
				}
				
				return null;
			}
			
			private static int normalize(int target) {
				int result = 0;
				
				if (target > 0) {
					result = 1;
				} else if (target < 0) {
					result = -1;
				}
				
				return result;
			}
			
		}
		
		public static class SignRowValidationException extends Exception {

			private static final long serialVersionUID = -1720873353345614589L;
			
			private Cause cause;
			
			public SignRowValidationException(Cause cause) {
				this.cause = cause;
			}
			
			public SignRowValidationException(Cause cause, String message) {
				super(message);
				
				this.cause = cause;
			}
			
			@Override
			public String getMessage() {
				String message = super.getMessage();
				if (cause != null) {
					if (message != null && !message.isEmpty()) {
						message += " ";
					}
					
					message += "[Cause: " + cause.name() + "]";
				}
				
				return message;
			}
			
			public Cause getExceptionCause() {
				return cause;
			}
			
			public enum Cause {
				
				NOT_IN_LINE, 
				NOT_SAME_Y_AXIS;
				
			}
			
		}
		
	}
	
}
