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
package de.xaniox.heavyspleef.core.game;

public enum GameProperty {
	
	INSTANT_BREAK(true),
	PLAY_BLOCK_BREAK(true),
	JOIN_ON_COUNTDOWN(true),
	DISABLE_HUNGER(true),
	DISABLE_PVP(true),
	DISABLE_DAMAGE(true),
	DISABLE_ITEM_PICKUP(true),
	DISABLE_ITEM_DROP(true),
	DISABLE_BUILD(true),
	USE_LIQUID_DEATHZONE(true),
	DISABLE_FLOOR_BREAK(false),
	BLOCK_COMMANDS(true),
	BROADCAST_RADIUS(30);
	
	private Object defaultValue;
	
	private GameProperty(Object defaultValue) {
		this.defaultValue = defaultValue;
	}
	
	public Object getDefaultValue() {
		return defaultValue;
	}
	
	public static GameProperty forName(String name) {
		for (GameProperty property : values()) {
			if (property.name().equalsIgnoreCase(name)) {
				return property;
			}
		}
		
		return null;
	}

}