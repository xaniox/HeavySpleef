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
package de.xaniox.heavyspleef.core.extension;

import com.google.common.collect.Sets;
import de.xaniox.heavyspleef.core.HeavySpleef;
import de.xaniox.heavyspleef.core.Permissions;
import de.xaniox.heavyspleef.core.config.ConfigType;
import de.xaniox.heavyspleef.core.config.DefaultConfig;
import de.xaniox.heavyspleef.core.config.SignSection;
import de.xaniox.heavyspleef.core.event.*;
import de.xaniox.heavyspleef.core.game.Game;
import de.xaniox.heavyspleef.core.game.GameManager;
import de.xaniox.heavyspleef.core.i18n.I18N;
import de.xaniox.heavyspleef.core.i18n.I18NManager;
import de.xaniox.heavyspleef.core.i18n.Messages;
import de.xaniox.heavyspleef.core.layout.SignLayout;
import de.xaniox.heavyspleef.core.player.SpleefPlayer;
import de.xaniox.heavyspleef.core.script.Variable;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.PluginManager;
import org.dom4j.Element;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Set;
import java.util.logging.Level;

public abstract class SignExtension extends GameExtension {
	
	protected static final String SPLEEF_SIGN_IDENTIFIER = "[spleef]";
	
	private final I18N i18n = I18NManager.getGlobal();
	private PrefixType prefixType;
	private Location location;
	private SignLayout layout;
	
	protected SignExtension() {}
	
	public SignExtension(Location location, PrefixType prefixType) {
		this.location = location;
		this.prefixType = prefixType;
	}
	
	@ExtensionInit
	public static void initListener(HeavySpleef heavySpleef) {
		PluginManager pluginManager = Bukkit.getPluginManager();
		pluginManager.registerEvents(new SignChangeListener(heavySpleef), heavySpleef.getPlugin());
	}
	
	public abstract void onSignClick(SpleefPlayer player);
	
	protected abstract SignLayout retrieveSignLayout();
	
	public abstract String[] getPermission();
	
	public void updateSign() {
		Block block = location.getWorld().getBlockAt(location);
		if (block.getType() != Material.SIGN_POST && block.getType() != Material.WALL_SIGN) {
			return;
		}
		
		Sign sign = (Sign) block.getState();
		getSignLayout().inflate(sign, getVariables());
	}
	
	protected String[] generateLines() {
		return getSignLayout().generate(getVariables());
	}
	
	private Set<Variable> getVariables() {
		Set<Variable> vars = Sets.newHashSet();
		Set<String> requested = getSignLayout().getRequestedVariables();
		
		DefaultConfig config = getHeavySpleef().getConfiguration(ConfigType.DEFAULT_CONFIG);
		vars.add(new Variable("prefix", prefixType.getConfigString(config)));
		getGame().supply(vars, requested);
		
		return vars;
	}
	
	private SignLayout getSignLayout() {
		if (layout == null) {
			layout = retrieveSignLayout();
		}
		
		return layout;
	}
	
	public Location getLocation() {
		return location;
	}
	
	public SignLayout getLayout() {
		return layout;
	}

	@Subscribe(priority = Subscribe.Priority.MONITOR)
	public void onGameStateChange(GameStateChangeEvent event) {
		Game game = event.getGame();
		updateSign();
	}

	@Subscribe(priority = Subscribe.Priority.MONITOR)
	public void onPlayerJoin(PlayerJoinGameEvent event) {
		Game game = event.getGame();
		updateSign();
	}

	@Subscribe(priority = Subscribe.Priority.MONITOR)
	public void onPlayerLeave(PlayerLeaveGameEvent event) {
		if (event.isCancelled()) {
			return;
		}

		Game game = event.getGame();
		updateSign();
	}

	@Subscribe(priority = Subscribe.Priority.MONITOR)
	public void onGameEnd(GameEndEvent event) {
		Game game = event.getGame();
		updateSign();
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
		
		Action action = event.getAction();
		if (action == Action.LEFT_CLICK_BLOCK && player.getBukkitPlayer().getGameMode() == GameMode.CREATIVE) {
			return;
		}
		
		String[] permissions = getPermission();
		boolean hasPermission = false;
		
		for (String perm : permissions) {
			if (!player.hasPermission(perm)) {
				continue;
			}
			
			hasPermission = true;
		}
		
		if (!hasPermission) {
			player.sendMessage(i18n.getString(Messages.Command.NO_PERMISSION));
			return;
		}
		
		onSignClick(player);
	}
	
	@Override
	public void marshal(Element element) {
		element.addElement("prefix-type").addText(prefixType.name());
		Element locationElement = element.addElement("location");
		locationElement.addElement("world").addText(location.getWorld().getName());
		locationElement.addElement("x").addText(String.valueOf(location.getBlockX()));
		locationElement.addElement("y").addText(String.valueOf(location.getBlockY()));
		locationElement.addElement("z").addText(String.valueOf(location.getBlockZ()));
	}

	@Override
	public void unmarshal(Element element) {
		Element prefixTypeElement = element.element("prefix-type");
		prefixType = prefixTypeElement != null ? PrefixType.valueOf(element.elementText("prefix-type")) : PrefixType.SPLEEF;
		
		Element locationElement = element.element("location");
		
		World world = Bukkit.getWorld(locationElement.elementText("world"));
		int x = Integer.parseInt(locationElement.elementText("x"));
		int y = Integer.parseInt(locationElement.elementText("y"));
		int z = Integer.parseInt(locationElement.elementText("z"));
		location = new Location(world, x, y, z);
	}
	
	protected enum PrefixType {
		
		SPLEEF("[spleef]"),
		SPLEGG("[splegg]");
		
		private String prefixString;
		
		private PrefixType(String prefixString) {
			this.prefixString = prefixString;
		}
		
		public String getPrefixString() {
			return prefixString;
		}
		
		protected String getConfigString(DefaultConfig config) {
			SignSection section = config.getSignSection();
			String configString;
			
			switch (this) {
			case SPLEEF:
				configString = section.getSpleefPrefix();
				break;
			case SPLEGG:
				configString = section.getSpleggPrefix();
				break;
			default:
				configString = section.getSpleefPrefix();
				break;
			}
			
			return configString;
		}
		
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
			
			PrefixType type = null;
			for (PrefixType availableType : PrefixType.values()) {
				if (!lines[0].equalsIgnoreCase(availableType.getPrefixString())) {
					continue;
				}
				
				type = availableType;
			}
			
			if (type == null) {
				return;
			}
			
			SpleefPlayer player = heavySpleef.getSpleefPlayer(event.getPlayer());
			if (!player.hasPermission(Permissions.PERMISSION_CREATE_SIGN)) {
				return;
			}
			
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
				Constructor<? extends SignExtension> constructor = found.getConstructor(Location.class, PrefixType.class);
				extension = constructor.newInstance(event.getBlock().getLocation(), type);
			} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				heavySpleef.getLogger().log(Level.WARNING, "Could not create sign: " + e);
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
			
			SpleefPlayer player = heavySpleef.getSpleefPlayer(event.getPlayer());
			if (!player.hasPermission(Permissions.PERMISSION_REMOVE_SIGN)) {
				return;
			}
			
			gameFound.removeExtension(found);
			player.sendMessage(i18n.getVarString(Messages.Player.SIGN_REMOVED)
					.setVariable("game", gameFound.getName())
					.toString());
			
			heavySpleef.getDatabaseHandler().saveGame(gameFound, null);
		}
		
	}

}