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
package de.matzefratze123.heavyspleef.persistence.xml;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.dom4j.Element;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.regions.CuboidRegion;

import de.matzefratze123.heavyspleef.core.FlagManager;
import de.matzefratze123.heavyspleef.core.FlagManager.GamePropertyBundle;
import de.matzefratze123.heavyspleef.core.Game;
import de.matzefratze123.heavyspleef.core.GameProperty;
import de.matzefratze123.heavyspleef.core.HeavySpleef;
import de.matzefratze123.heavyspleef.core.flag.AbstractFlag;
import de.matzefratze123.heavyspleef.core.flag.FlagRegistry;
import de.matzefratze123.heavyspleef.core.floor.Floor;

public class GameAccessor extends XMLAccessor<Game> {

	private HeavySpleef heavySpleef;
	private FlagRegistry flagRegistry;
	
	public GameAccessor(HeavySpleef heavySpleef) {
		this.heavySpleef = heavySpleef;
		this.flagRegistry = heavySpleef.getFlagRegistry();
	}
	
	@Override
	public Class<Game> getObjectClass() {
		return Game.class;
	}

	@Override
	public void write(Game game, Element element) {
		element.addAttribute("name", game.getName());
		element.addAttribute("world", game.getWorld().getName());
		
		FlagManager flagManager = game.getFlagManager();
		Map<String, AbstractFlag<?>> flags = flagManager.getPresentFlags();
		
		Element flagsElement = element.addElement("flags");
		for (Entry<String, AbstractFlag<?>> entry : flags.entrySet()) {
			Element flagElement = flagsElement.addElement("flag");
			flagElement.addAttribute("name", entry.getKey());
			entry.getValue().marshal(flagElement);
		}
		
		GamePropertyBundle defaultBundle = flagManager.getDefaultPropertyBundle();
		Element propertiesElement = element.addElement("properties");
		
		for (Entry<GameProperty, Object> propertyEntry : defaultBundle.entrySet()) {
			Element propertyElement = propertiesElement.addElement("property");
			propertyElement.addAttribute("key", propertyEntry.getKey().name().toLowerCase());
			propertyElement.addAttribute("class", propertyEntry.getValue().getClass().getName());
			propertyElement.addText(propertyEntry.getValue().toString());
		}
		
		Collection<Floor> floors = game.getFloors();
		Element floorsElement = element.addElement("floors");
		for (Floor floor : floors) {
			Element floorElement = floorsElement.addElement("floor");
			floorElement.addAttribute("name", floor.getName());
		}
		
		Set<CuboidRegion> deathzones = game.getDeathzones();
		Element deathzonesElement = element.addElement("deathzones");
		for (CuboidRegion deathzone : deathzones) {
			Element deathzoneElement = deathzonesElement.addElement("deathzone");
			addCoodinateSet(deathzone.getPos1(), deathzoneElement);
			addCoodinateSet(deathzone.getPos2(), deathzoneElement);
		}
	}
	
	private void addCoodinateSet(Vector vector, Element element) {
		Element set = element.addElement("coordinateSet");
		
		Element xElement = set.addElement("x");
		xElement.addText(String.valueOf(vector.getBlockX()));
		
		Element yElement = set.addElement("y");
		yElement.addText(String.valueOf(vector.getBlockY()));
		
		Element zElement = set.addElement("z");
		zElement.addText(String.valueOf(vector.getBlockZ()));
	}
	
	private Vector getCoordinateSet(Element element) {
		int x = Integer.parseInt(element.elementText("x"));
		int y = Integer.parseInt(element.elementText("y"));
		int z = Integer.parseInt(element.elementText("z"));
		
		return new Vector(x, y, z);
	}
	
	private Object getPropertyValue(String type, String valueString) {
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
	}

	@SuppressWarnings("unchecked")
	@Override
	public Game fetch(Element element) {
		String name = element.attributeValue("name");
		String worldName = element.attributeValue("world");
		
		if (name == null) {
			throw new RuntimeException("Name of game cannot be null");
		}
		
		// Not at all thread safe but it is the only way to get a world instance
		// Bukkit#getWorlds() returns an new arraylist instance, so iterating is safe
		// but the internal ArrayList::new(Collection) isn't
		World world = null;
		List<World> worlds = Bukkit.getWorlds();
		for (World w : worlds) {
			if (w.getName().equals(worldName)) {
				world = w;
			}
		}
		
		if (world == null) {
			throw new RuntimeException("World \"" + worldName + " does not exist (game: " + name + ")");
		}
		
		Game game = new Game(heavySpleef, name, world);
		
		Element flagsElement = element.element("flags");
		List<Element> flagElementsList = flagsElement.elements("flag");
		
		for (Element flagElement : flagElementsList) {
			String flagName = flagElement.attributeValue("name");
			
			AbstractFlag<?> flag = flagRegistry.newFlagInstance(flagName, AbstractFlag.class);
			flag.unmarshal(flagElement);
			
			game.addFlag(flag);
		}
		
		Element propertiesElement = element.element("properties");
		List<Element> propertiesElementList = propertiesElement.elements("property");
		
		for (Element propertyElement : propertiesElementList) {
			String key = propertyElement.attributeValue("key");
			String className = propertyElement.attributeValue("class");
			
			GameProperty property = GameProperty.forName(key);
			Object value = getPropertyValue(className, propertyElement.getText());
			
			game.requestProperty(property, value);
		}
		
		Element deathzonesElement = element.element("deathzones");
		List<Element> deathzoneElementList = deathzonesElement.elements("deathzone");
		
		for (Element deathzoneElement : deathzoneElementList) {
			List<Element> coodinateSetElements = deathzoneElement.elements("coordinateSet");
			if (coodinateSetElements.size() < 2) {
				throw new RuntimeException("deathzone must contain at least two coordinate sets");
			}
			
			Vector pos1 = getCoordinateSet(coodinateSetElements.get(0));
			Vector pos2 = getCoordinateSet(coodinateSetElements.get(1));
			
			CuboidRegion region = new CuboidRegion(pos1, pos2);
			game.addDeathzone(region);
		}
		
		return game;
	}

}