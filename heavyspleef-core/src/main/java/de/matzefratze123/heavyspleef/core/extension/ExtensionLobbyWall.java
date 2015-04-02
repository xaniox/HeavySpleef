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
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.material.Attachable;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;
import org.dom4j.Element;

import com.sk89q.worldedit.bukkit.BukkitUtil;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;

import de.matzefratze123.heavyspleef.commands.base.Command;
import de.matzefratze123.heavyspleef.commands.base.CommandContext;
import de.matzefratze123.heavyspleef.commands.base.CommandException;
import de.matzefratze123.heavyspleef.commands.base.CommandValidate;
import de.matzefratze123.heavyspleef.commands.base.PlayerOnly;
import de.matzefratze123.heavyspleef.core.Game;
import de.matzefratze123.heavyspleef.core.GameManager;
import de.matzefratze123.heavyspleef.core.HeavySpleef;
import de.matzefratze123.heavyspleef.core.PlayerPostActionHandler.PostActionCallback;
import de.matzefratze123.heavyspleef.core.config.ConfigType;
import de.matzefratze123.heavyspleef.core.config.SignLayoutConfiguration;
import de.matzefratze123.heavyspleef.core.event.GameEndEvent;
import de.matzefratze123.heavyspleef.core.event.GameListener;
import de.matzefratze123.heavyspleef.core.event.GameListener.Priority;
import de.matzefratze123.heavyspleef.core.event.GameStateChangeEvent;
import de.matzefratze123.heavyspleef.core.event.PlayerJoinGameEvent;
import de.matzefratze123.heavyspleef.core.event.PlayerLeaveGameEvent;
import de.matzefratze123.heavyspleef.core.extension.ExtensionLobbyWall.WallValidationException.Cause;
import de.matzefratze123.heavyspleef.core.i18n.I18N;
import de.matzefratze123.heavyspleef.core.i18n.Messages;
import de.matzefratze123.heavyspleef.core.layout.SignLayout;
import de.matzefratze123.heavyspleef.core.layout.VariableProvider;
import de.matzefratze123.heavyspleef.core.player.SpleefPlayer;

@Extension(name = "lobby-wall", hasCommands = true)
public class ExtensionLobbyWall implements GameExtension {

	private static final String INGAME_PLAYER_PREFIX_KEY = "ingame-player-prefix";
	private static final String DEAD_PLAYER_PREFIX_KEY = "dead-player-prefix";
	
	private static final String DEFAULT_INGAME_PLAYER_PREFIX = "";
	private static final String DEFAULT_DEAD_PLAYER_PREFIX = ChatColor.GRAY.toString();
	
	@Command(name = "addwall", permission = "heavyspleef.addwall",
			description = "Adds a wall consisting of signs to show the status of a particular game by clicking it", 
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
				ExtensionLobbyWall wall = generateWall(sign);
				
				game.addExtension(wall);
				player.sendMessage(i18n.getString(Messages.Command.WALL_ADDED));
			}
			
		}, game);
		
		player.sendMessage(i18n.getString(Messages.Command.CLICK_ON_SIGN_TO_ADD_WALL));
	}
	
	@Command(name = "removewall", permission = "heavyspleef.removewall", minArgs = 1,
			description = "Removes a wall by clicking it", usage = "/spleef removewall")
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
					removed = 0;
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
	
	private static ExtensionLobbyWall generateWall(Sign clicked) {
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
			return new ExtensionLobbyWall(start, end);
		} catch (WallValidationException e) {
			throw new RuntimeException(e);
		}
	}
	
	private static Block getAttached(Sign sign) {
		Attachable data = (Attachable) sign.getData();
		BlockFace attachingBlockFace = data.getFacing().getOppositeFace();
		
		return sign.getBlock().getRelative(attachingBlockFace);
	}
	
	/* The world this wall is in */
	private World world;
	/* The start location of this wall */
	private Location start;
	/* The end location of this wall */
	private Location end;
	/* The direction looking straight from start to end */
	private BlockFace2D direction;
	
	@SuppressWarnings("unused")
	private ExtensionLobbyWall() {}
	
	public ExtensionLobbyWall(Location start, Location end) throws WallValidationException {
		this.world = start.getWorld();
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
				direction = BlockFace2D.SOUTH;
			} else {
				direction = BlockFace2D.NORTH;
			}
		} else if (start.getBlockZ() == end.getBlockZ()) {
			if (start.getBlockX() < end.getBlockX()) {
				direction = BlockFace2D.EAST;
			} else {
				direction = BlockFace2D.WEST;
			}
		}
	}
	
	public Location getStart() {
		return start;
	}
	
	public Location getEnd() {
		return end;
	}
	
	public BlockFace2D getDirection() {
		return direction;
	}
	
	@GameListener(priority = Priority.MONITOR)
	public void onGameStateChange(GameStateChangeEvent event) {
		Game game = event.getGame();
		updateWall(game, false);
	}
	
	@GameListener(priority = Priority.MONITOR)
	public void onPlayerJoin(PlayerJoinGameEvent event) {
		Game game = event.getGame();
		updateWall(game, false);
	}
	
	@GameListener(priority = Priority.MONITOR)
	public void onPlayerLeave(PlayerLeaveGameEvent event) {
		if (event.isCancelled()) {
			return;
		}
		
		Game game = event.getGame();
		updateWall(game, false);
	}
	
	@GameListener(priority = Priority.MONITOR)
	public void onGameEnd(GameEndEvent event) {
		Game game = event.getGame();
		updateWall(game, true);
	}
	
	private void updateWall(final Game game, final boolean reset) {
		final HeavySpleef heavySpleef = game.getHeavySpleef();
		final VariableProvider<Game> varProvider = new GameVariableProvider();
		
		final SignLayoutConfiguration joinConfig = heavySpleef.getConfiguration(ConfigType.JOIN_SIGN_LAYOUT_CONFIG);
		final SignLayoutConfiguration infoConfig = heavySpleef.getConfiguration(ConfigType.INFO_WALL_SIGN_LAYOUT_CONFIG);
		
		final String ingamePrefix = infoConfig.getOption(INGAME_PLAYER_PREFIX_KEY, DEFAULT_INGAME_PLAYER_PREFIX);
		final String deadPrefix = infoConfig.getOption(DEAD_PLAYER_PREFIX_KEY, DEFAULT_DEAD_PLAYER_PREFIX);
		
		final SignLayout joinLayout = joinConfig.getLayout();
		final SignLayout infoLayout = infoConfig.getLayout();
		
		loopSigns(new SignLooper() {
			
			boolean ingamePlayers = true;
			Iterator<SpleefPlayer> currentIterator = game.getPlayers().iterator();
			
			@Override
			public LoopReturn loop(int index, Sign sign) {
				if (index == 0) {
					//First sign is the join sign
					joinLayout.inflate(sign, varProvider, game);
				} else if (index == 1) {
					//Second sign is the informational sign
					infoLayout.inflate(sign, varProvider, game);
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
							player = currentIterator.next().getName();
							prefix = ingamePlayers ? ingamePrefix : deadPrefix;
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
						
						sign.setLine(i, prefix + player);
					}
					
					sign.update();
					
					if (breakLoop) {
						return LoopReturn.BREAK;
					}
				}
				
				return LoopReturn.DEFAULT;
			}
		});
	}
	
	protected void clearAll() {
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
	
	private void loopSigns(SignLooper looper) {
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
	
	@Override
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
	
	@Override
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
		} catch (WallValidationException e) {
			throw new RuntimeException(e);
		}
	}
	
	private interface SignLooper {
		
		public LoopReturn loop(int index, Sign sign);
		
		public enum LoopReturn {
			
			DEFAULT,
			CONTINUE,
			BREAK,
			RETURN;
			
		}
		
	}
	
	public enum BlockFace2D {
		
		NORTH(0, -1, BlockFace.NORTH),
		SOUTH(0, 1, BlockFace.SOUTH),
		WEST(-1, 0, BlockFace.WEST),
		EAST(1, 0, BlockFace.EAST);
		
		private int xVec;
		private int zVec;
		private BlockFace blockFace;
		
		private BlockFace2D(int xVec, int zVec, BlockFace blockFace) {
			this.xVec = xVec;
			this.zVec = zVec;
			this.blockFace = blockFace;
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
			
			NOT_IN_LINE, 
			NOT_SAME_Y_AXIS;
			
		}
		
	}
	
}
