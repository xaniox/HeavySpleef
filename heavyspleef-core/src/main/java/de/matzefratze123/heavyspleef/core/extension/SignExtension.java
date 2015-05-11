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

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Set;

import lombok.Getter;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.PluginManager;
import org.dom4j.Element;

import de.matzefratze123.heavyspleef.core.Game;
import de.matzefratze123.heavyspleef.core.GameManager;
import de.matzefratze123.heavyspleef.core.HeavySpleef;
import de.matzefratze123.heavyspleef.core.i18n.I18N;
import de.matzefratze123.heavyspleef.core.i18n.I18NManager;
import de.matzefratze123.heavyspleef.core.i18n.Messages;
import de.matzefratze123.heavyspleef.core.layout.SignLayout;
import de.matzefratze123.heavyspleef.core.player.SpleefPlayer;

public abstract class SignExtension extends GameExtension {
	
	protected static final String SPLEEF_SIGN_IDENTIFIER = "[spleef]";
	
	@Getter
	private Location location;
	@Getter
	private SignLayout layout;
	
	protected SignExtension() {}
	
	public SignExtension(Location location) {
		this.location = location;
		this.layout = retrieveSignLayout();
	}
	
	@ExtensionInit
	public static void initListener(HeavySpleef heavySpleef) {
		PluginManager pluginManager = Bukkit.getPluginManager();
		pluginManager.registerEvents(new SignChangeListener(heavySpleef), heavySpleef.getPlugin());
	}
	
	public abstract void onSignClick(SpleefPlayer player);
	
	protected abstract SignLayout retrieveSignLayout();
	
	public void updateSign() {
		Block block = location.getWorld().getBlockAt(location);
		if (block.getType() != Material.SIGN_POST && block.getType() != Material.WALL_SIGN) {
			return;
		}
		
		Sign sign = (Sign) block.getState();
		layout.inflate(sign, getGame());
	}
	
	protected String[] generateLines() {
		return layout.generate(getGame());
	}
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		Block clickedBlock = event.getClickedBlock();
		if (clickedBlock == null) {
			return;
		}
		
		if (clickedBlock.getType() != Material.SIGN_POST && clickedBlock.getType() != Material.WALL_SIGN) {
			return;
		}
		
		Location loc = clickedBlock.getLocation();
		if (!loc.equals(location)) {
			return;
		}
		
		SpleefPlayer player = getHeavySpleef().getSpleefPlayer(event.getPlayer());
		onSignClick(player);
	}
	
	@Override
	public void marshal(Element element) {
		Element locationElement = element.addElement("location");
		locationElement.addElement("world").addText(location.getWorld().getName());
		locationElement.addElement("x").addText(String.valueOf(location.getBlockX()));
		locationElement.addElement("y").addText(String.valueOf(location.getBlockY()));
		locationElement.addElement("z").addText(String.valueOf(location.getBlockZ()));
	}

	@Override
	public void unmarshal(Element element) {
		Element locationElement = element.element("location");
		
		World world = Bukkit.getWorld(locationElement.elementText("world"));
		int x = Integer.parseInt(locationElement.elementText("x"));
		int y = Integer.parseInt(locationElement.elementText("y"));
		int z = Integer.parseInt(locationElement.elementText("z"));
		location = new Location(world, x, y, z);
	}
	
	private static class SignChangeListener implements Listener {
		
		private final HeavySpleef heavySpleef;
		private final I18N i18n = I18NManager.getGlobal();
		
		public SignChangeListener(HeavySpleef heavySpleef) {
			this.heavySpleef = heavySpleef;
		}
		
		@EventHandler
		public void onSignChange(SignChangeEvent event) {
			String[] lines = event.getLines();
			
			if (!lines[0].equalsIgnoreCase(SPLEEF_SIGN_IDENTIFIER)) {
				return;
			}
			
			Player player = event.getPlayer();
			
			ExtensionRegistry registry = heavySpleef.getExtensionRegistry();
			Set<Class<? extends SignExtension>> classes = registry.getExtensionsByType(SignExtension.class);
			Class<? extends SignExtension> found = null;
			String identifier = lines[1];
			
			for (Class<? extends SignExtension> clazz : classes) {
				String classIdentifier;
				
				try {
					Field field = clazz.getDeclaredField("IDENTIFIER");
					if ((field.getModifiers() & Modifier.STATIC) == 0) {
						throw new NoSuchFieldException();
					}
					
					boolean accessible = field.isAccessible();
					
					if (!accessible) {
						field.setAccessible(true);
					}
					
					Object obj = field.get(null);
					if (!(obj instanceof String)) {
						throw new NoSuchFieldException();
					}
					
					classIdentifier = (String) obj;
				} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
					continue;
				}
				
				if (identifier.equalsIgnoreCase(classIdentifier) || identifier.equalsIgnoreCase('[' + classIdentifier + ']')) {
					found = clazz;
					break;
				}
			}
			
			if (found == null) {
				player.sendMessage(i18n.getVarString(Messages.Player.NO_SIGN_AVAILABLE)
						.setVariable("identifier", identifier)
						.toString());
				return;
			}
			
			String gameName = lines[2];
			GameManager manager = heavySpleef.getGameManager();
			
			if (!manager.hasGame(gameName)) {
				player.sendMessage(i18n.getVarString(Messages.Command.GAME_DOESNT_EXIST)
						.setVariable("game", gameName)
						.toString());
				return;
			}
			
			Game game = manager.getGame(gameName);
			SignExtension extension;
			
			try {
				Constructor<? extends SignExtension> constructor = found.getConstructor(Location.class);
				extension = constructor.newInstance(event.getBlock().getLocation());
			} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				player.sendMessage(i18n.getVarString(Messages.Player.NO_SIGN_AVAILABLE)
						.setVariable("identifier", identifier)
						.toString());
				return;
			}
			
			game.addExtension(extension);
			extension.updateSign();
			
			String[] result = extension.generateLines();
			for (int i = 0; i < result.length; i++) {
				event.setLine(i, result[i]);
			}
			
			heavySpleef.getDatabaseHandler().saveGame(game, null);
		}
		
		@EventHandler(priority = EventPriority.MONITOR)
		public void onBlockBreak(BlockBreakEvent event) {
			if (event.isCancelled()) {
				return;
			}
			
			GameManager manager = heavySpleef.getGameManager();
			Block block = event.getBlock();
			Location blockLoc = block.getLocation();			
			
			Game gameFound = null;
			SignExtension found = null;
			
			for (Game game : manager.getGames()) {
				for (SignExtension extension : game.getExtensionsByType(SignExtension.class)) {
					Location location = extension.getLocation();
					if (blockLoc.equals(location)) {
						found = extension;
						gameFound = game;
						break;
					}
				}
				
				if (found != null) {
					break;
				}
			}
			
			if (found == null) {
				return;
			}
			
			gameFound.removeExtension(found);
			event.getPlayer().sendMessage(i18n.getVarString(Messages.Player.SIGN_REMOVED)
					.setVariable("game", gameFound.getName())
					.toString());
			
			heavySpleef.getDatabaseHandler().saveGame(gameFound, null);
		}
		
	}

}
