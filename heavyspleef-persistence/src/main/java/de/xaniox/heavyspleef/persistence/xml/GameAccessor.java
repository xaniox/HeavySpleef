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
package de.xaniox.heavyspleef.persistence.xml;

import com.google.common.collect.Maps;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.CylinderRegion;
import com.sk89q.worldedit.regions.Polygonal2DRegion;
import com.sk89q.worldedit.regions.Region;
import de.xaniox.heavyspleef.core.HeavySpleef;
import de.xaniox.heavyspleef.core.extension.Extension;
import de.xaniox.heavyspleef.core.extension.ExtensionRegistry;
import de.xaniox.heavyspleef.core.extension.GameExtension;
import de.xaniox.heavyspleef.core.flag.*;
import de.xaniox.heavyspleef.core.floor.Floor;
import de.xaniox.heavyspleef.core.game.Game;
import de.xaniox.heavyspleef.core.game.GameState;
import de.xaniox.heavyspleef.core.hook.HookManager;
import de.xaniox.heavyspleef.core.hook.HookReference;
import de.xaniox.heavyspleef.core.stats.StatisticRecorder;
import de.xaniox.heavyspleef.persistence.RegionType;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.PluginManager;
import org.dom4j.Attribute;
import org.dom4j.Element;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;

public class GameAccessor extends XMLAccessor<Game> {

	private static final Map<Class<? extends Region>, XMLRegionMetadataCodec<?>> METADATA_CODECS;
	
	static {
		METADATA_CODECS = Maps.newConcurrentMap();
		METADATA_CODECS.put(CuboidRegion.class, new CuboidRegionXMLCodec());
		METADATA_CODECS.put(CylinderRegion.class, new CylinderRegionXMLCodec());
		METADATA_CODECS.put(Polygonal2DRegion.class, new Polygonal2DRegionXMLCodec());
	}
	
	private HeavySpleef heavySpleef;
	private FlagRegistry flagRegistry;
	
	private ReentrantLock worldLock = new ReentrantLock();
	private ReadWriteLock rwl = new ReentrantReadWriteLock();
	private Lock wl = rwl.writeLock();
	private Lock rl = rwl.readLock();
	
	public GameAccessor(HeavySpleef heavySpleef) {
		this.heavySpleef = heavySpleef;
		this.flagRegistry = heavySpleef.getFlagRegistry();
	}
	
	@Override
	public Class<Game> getObjectClass() {
		return Game.class;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void write(Game game, Element element) {
		wl.lock();
		
		try {
			element.addAttribute("name", game.getName());
			element.addAttribute("world", game.getWorld().getName());
			
			boolean enableRating = true;
			StatisticRecorder recorder = game.getStatisticRecorder();
			if (recorder != null) {
				enableRating = recorder.isEnableRating();
			}
			
			element.addAttribute("enable-rating", String.valueOf(enableRating));
			if (game.getGameState() == GameState.DISABLED) {
				element.addAttribute("disabled", String.valueOf(true));
			}
			
			FlagManager flagManager = game.getFlagManager();
			Map<String, AbstractFlag<?>> flags = flagManager.getPresentFlags();
			
			Element flagsElement = element.addElement("flags");
			for (Entry<String, AbstractFlag<?>> entry : flags.entrySet()) {
				Element flagElement = flagsElement.addElement("flag");
				flagElement.addAttribute("name", entry.getKey());
				entry.getValue().marshal(flagElement);
			}
			
			/*GamePropertyBundle defaultBundle = flagManager.getDefaultPropertyBundle();
			Element propertiesElement = element.addElement("properties");
			
			for (Entry<GameProperty, Object> propertyEntry : defaultBundle.entrySet()) {
				Element propertyElement = propertiesElement.addElement("property");
				propertyElement.addAttribute("key", propertyEntry.getKey().name().toLowerCase());
				propertyElement.addAttribute("class", propertyEntry.getValue().getClass().getName());
				propertyElement.addText(propertyEntry.getValue().toString());
			}*/
			
			Collection<Floor> floors = game.getFloors();
			Element floorsElement = element.addElement("floors");
			for (Floor floor : floors) {
				Element floorElement = floorsElement.addElement("floor");
				floorElement.addAttribute("name", floor.getName());
			}
			
			Collection<GameExtension> extensions = game.getExtensions();
			Element extensionsElement = element.addElement("extensions");
			for (GameExtension extension : extensions) {
				Element extensionElement = extensionsElement.addElement("extension");
				Extension extensionAnnotation = extension.getClass().getAnnotation(Extension.class);
				
				extensionElement.addAttribute("name", extensionAnnotation.name());
				extension.marshal(extensionElement);
			}
			
			Map<String, Region> deathzones = game.getDeathzones();
			Element deathzonesElement = element.addElement("deathzones");
			for (Entry<String, Region> entry : deathzones.entrySet()) {
				String name = entry.getKey();
				Region deathzone = entry.getValue();
				RegionType type = RegionType.byRegionType(deathzone.getClass());
				
				Element deathzoneElement = deathzonesElement.addElement("deathzone");
				deathzoneElement.addAttribute("name", name);
				deathzoneElement.addAttribute("regiontype", type.getPersistenceName());
				
				XMLRegionMetadataCodec<Region> metadataCodec = (XMLRegionMetadataCodec<Region>) METADATA_CODECS.get(deathzone.getClass());
				metadataCodec.apply(deathzoneElement, deathzone);
			}
		} finally {
			wl.unlock();
		}
	}
	
	/*private static Object getPropertyValue(String type, String valueString) {
		Class<?> clazz;
		
		try {
			clazz = Class.forName(type);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("Could not find class " + type + " for property value \"" + valueString + "\"");
		}
		
		if (clazz == Boolean.class) {
			return Boolean.parseBoolean(valueString);
		} else if (clazz == Integer.class) {
			return Integer.parseInt(valueString);
		} else if (clazz == Double.class) {
			return Double.parseDouble(valueString);
		}
		
		return valueString;
	}*/

	@SuppressWarnings("unchecked")
	@Override
	public Game fetch(Element element) {
		Game game;
		rl.lock();
		
		try {
			String name = element.attributeValue("name");
			String worldName = element.attributeValue("world");
			
			if (name == null) {
				throw new RuntimeException("Name of game cannot be null");
			}
			
			// Not at all thread safe, but it is the only way to get a world instance.
			// Bukkit#getWorlds() returns an new arraylist instance, so iterating is safe
			// but the internal ArrayList::new(Collection) isn't
			World world = null;
			worldLock.lock();
			try {
				List<World> worlds = Bukkit.getWorlds();
				for (World w : worlds) {
					if (w.getName().equals(worldName)) {
						world = w;
					}
				}
			} finally {
				worldLock.unlock();
			}
			
			if (world == null) {
				throw new WorldNotFoundException("World \"" + worldName + "\" does not exist (game: " + name + ")"
                        , name, worldName);
			}
			
			game = new Game(heavySpleef, name, world);
			
			Attribute disabledAttribute = element.attribute("disabled");
			if (disabledAttribute != null) {
				game.setGameState(GameState.DISABLED);
			}
			
			Attribute enableRatingAttribute = element.attribute("enable-rating");
			if (enableRatingAttribute != null) {
				boolean enabled = Boolean.parseBoolean(enableRatingAttribute.getValue());
				game.getStatisticRecorder().setEnableRating(enabled);
			}
			
			Element flagsElement = element.element("flags");
			List<Element> flagElementsList = flagsElement.elements("flag");
			
			for (Element flagElement : flagElementsList) {
				String flagName = flagElement.attributeValue("name");
				AbstractFlag<?> flag = null;
				
				boolean loadUnloaded = false;
				if (flagRegistry.isFlagPresent(flagName)) {
					Class<? extends AbstractFlag<?>> clazz = flagRegistry.getFlagClass(flagName);
					Flag data = flagRegistry.getFlagData(clazz);
					HookReference[] refs = data.depend();
					if (refs.length != 0) {
						HookManager hookManager = heavySpleef.getHookManager();
						
						for (HookReference ref : refs) {
							if (hookManager.getHook(ref).isProvided()) {
								continue;
							}
							
							loadUnloaded = true;
							break;
						}
					}
					
					String[] pluginDepends = data.pluginDepend();
					if (pluginDepends.length != 0) {
						PluginManager manager = Bukkit.getPluginManager();
						for (String depend : pluginDepends) {
							if (manager.isPluginEnabled(depend)) {
								continue;
							}
							
							loadUnloaded = true;
							break;
						}
					}
					
					if (!loadUnloaded) {
						flag = flagRegistry.newFlagInstance(flagName, AbstractFlag.class, game);
						flag.unmarshal(flagElement);
					}
				}
				
				if (loadUnloaded) {
					//This flag class has not been registered yet
					UnloadedFlag unloaded = new UnloadedFlag();
					unloaded.setXmlElement(flagElement);
					flag = unloaded;
				}
				
				game.addFlag(flag, false);
			}
			
			for (AbstractFlag<?> flag : game.getFlagManager().getFlags()) {
				flag.onFlagAdd(game);
			}
			
			FlagManager flagManager = game.getFlagManager();
			flagManager.revalidateParents();
			
			ExtensionRegistry extRegistry = heavySpleef.getExtensionRegistry();
			Element extensionsElement = element.element("extensions");
			List<Element> extensionElementsList = extensionsElement.elements("extension");
			
			for (Element extensionElement : extensionElementsList) {
				String extName = extensionElement.attributeValue("name");
				Class<? extends GameExtension> clazz = extRegistry.getExtensionClass(extName);
				
				if (clazz == null) {
					heavySpleef.getLogger().log(Level.SEVERE,
							"Could not load extension with name \"" + extName + "\"): No corresponding class found for extension name");
					continue;
				}
				
				GameExtension extension;
				
				try {
					Constructor<? extends GameExtension> constructor = clazz.getDeclaredConstructor();
					if (!constructor.isAccessible()) {
						constructor.setAccessible(true);
					}
					
					extension = constructor.newInstance();
				} catch (InstantiationException | IllegalAccessException | NoSuchMethodException | SecurityException | IllegalArgumentException
						| InvocationTargetException e) {
					heavySpleef.getLogger().log(Level.SEVERE, "Could not load extension for class \"" + clazz.getName() + "\" (name = \"" + extName + "\"): ", e);
					continue;
				}
				
				extension.setHeavySpleef(heavySpleef);
				extension.setGame(game);
				extension.unmarshal(extensionElement);
				game.addExtension(extension);
			}
			
			/*Element propertiesElement = element.element("properties");
			List<Element> propertiesElementList = propertiesElement.elements("property");
			
			for (Element propertyElement : propertiesElementList) {
				String key = propertyElement.attributeValue("key");
				String className = propertyElement.attributeValue("class");
				
				GameProperty property = GameProperty.forName(key);
				Object value = getPropertyValue(className, propertyElement.getText());
				
				game.requestProperty(property, value);
			}*/
			
			Element deathzonesElement = element.element("deathzones");
			List<Element> deathzoneElementList = deathzonesElement.elements("deathzone");
			
			for (Element deathzoneElement : deathzoneElementList) {
				String deathzoneName = deathzoneElement.attributeValue("name");
				String persistenceName = deathzoneElement.attributeValue("regiontype");
				RegionType regionType = RegionType.byPersistenceName(persistenceName);
				
				XMLRegionMetadataCodec<Region> metadataCodec = (XMLRegionMetadataCodec<Region>) METADATA_CODECS.get(regionType.getRegionClass());
				Region region = metadataCodec.asRegion(deathzoneElement);
				
				game.addDeathzone(deathzoneName, region);
			}
		} finally {
            rl.unlock();
        }
		
		return game;
	}

	public static class WorldNotFoundException extends RuntimeException {

        private String game;
        private String worldName;

        public WorldNotFoundException(String message, String game, String worldName) {
            super(message);

            this.game = game;
        }

        public WorldNotFoundException(String message, String game, String worldName, Throwable cause) {
            super(message, cause);

            this.game = game;
        }

        public WorldNotFoundException(String game, String worldName, Throwable cause) {
            super(cause);

            this.game = game;
        }

        public String getGameName() {
            return game;
        }

        public String getWorldName() {
            return worldName;
        }
    }

}