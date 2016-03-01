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
package de.xaniox.heavyspleef.migration;

import com.google.common.collect.Maps;
import com.sk89q.worldedit.regions.Region;
import de.xaniox.heavyspleef.core.HeavySpleef;
import de.xaniox.heavyspleef.core.event.Event;
import de.xaniox.heavyspleef.core.event.EventBus;
import de.xaniox.heavyspleef.core.event.SpleefListener;
import de.xaniox.heavyspleef.core.extension.ExtensionManager;
import de.xaniox.heavyspleef.core.flag.FlagManager;
import de.xaniox.heavyspleef.core.floor.Floor;
import de.xaniox.heavyspleef.core.game.Game;
import de.xaniox.heavyspleef.core.game.GameState;
import org.bukkit.World;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

public class SafeGameCreator {
	
	/* Unsafe related variables */
	private Object theUnsafe;
	private Method allocateInstanceMethod;
	
	/* Game class related variables */
	private Field nameField;
	private Field worldField;
	private Field heavySpleefField;
	private Field gameStateField;
	private Field flagManagerField;
	private Field extensionManagerField;
	private Field floorsField;
	private Field deathzonesField;
	private Field eventBusField;
	
	private final HeavySpleef heavySpleef;
	
	public SafeGameCreator(HeavySpleef heavySpleef) {
		this.heavySpleef = heavySpleef;
		
		try {
			Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
			Field theUnsafeField = unsafeClass.getDeclaredField("theUnsafe");
			theUnsafeField.setAccessible(true);
			theUnsafe = theUnsafeField.get(null);
			allocateInstanceMethod = unsafeClass.getDeclaredMethod("allocateInstance", Class.class);
			allocateInstanceMethod.setAccessible(true);
		} catch (ClassNotFoundException | NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException
				| NoSuchMethodException e) {
			throw new RuntimeException("Could not initialize Unsafe instance", e);
		}
		
		try {
			Class<Game> gameClass = Game.class;
			nameField = gameClass.getDeclaredField("name");
			nameField.setAccessible(true);
			
			worldField = gameClass.getDeclaredField("world");
			worldField.setAccessible(true);
			
			heavySpleefField = gameClass.getDeclaredField("heavySpleef");
			heavySpleefField.setAccessible(true);
			
			gameStateField = gameClass.getDeclaredField("gameState");
			gameStateField.setAccessible(true);
			
			flagManagerField = gameClass.getDeclaredField("flagManager");
			flagManagerField.setAccessible(true);
			
			extensionManagerField = gameClass.getDeclaredField("extensionManager");
			extensionManagerField.setAccessible(true);
			
			floorsField = gameClass.getDeclaredField("floors");
			floorsField.setAccessible(true);
			
			deathzonesField = gameClass.getDeclaredField("deathzones");
			deathzonesField.setAccessible(true);
			
			eventBusField = gameClass.getDeclaredField("eventBus");
			eventBusField.setAccessible(true);
		} catch (NoSuchFieldException | SecurityException e) {
			throw new RuntimeException("Could not initialize game field variables. Did the field names change?", e);
		}
	}
	
	public Game createSafeGame(String name, World world) {
		Game game;
		
		try {
			game = (Game) allocateInstanceMethod.invoke(theUnsafe, Game.class);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new RuntimeException("Could not allocate a safe instance of Game", e);
		}
		
		try {
			nameField.set(game, name);
			worldField.set(game, world);
			heavySpleefField.set(game, heavySpleef);
			gameStateField.set(game, GameState.WAITING);
			
			Map<String, Floor> floorMap = Maps.newHashMap();
			Map<String, Region> deathzoneMap = Maps.newHashMap();
			
			floorsField.set(game, floorMap);
			deathzonesField.set(game, deathzoneMap);
			
			FlagManager flagManager = new FlagManager(heavySpleef.getPlugin(), null, true);
			flagManagerField.set(game, flagManager);
			
			EventBus eventBus = new NullEventBus();
			ExtensionManager extensionManager = new AccessibleExtensionManager(heavySpleef, eventBus);
			extensionManagerField.set(game, extensionManager);
			
			eventBusField.set(game, eventBus);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			throw new RuntimeException("Could not create safe game", e);
		}
		
		return game;
	}
	
	private static class AccessibleExtensionManager extends ExtensionManager {

		public AccessibleExtensionManager(HeavySpleef heavySpleef, EventBus eventManager) {
			super(heavySpleef, eventManager, true);
		}
		
	}
	
	private static class NullEventBus extends EventBus {
		
		protected NullEventBus() {
			super(null);
		}

		@Override
		public void registerListener(SpleefListener listener) {}
		
		@Override
		public void unregister(SpleefListener listener) {}
		
		@Override
		public void callEvent(Event event) {}
		
	}

}