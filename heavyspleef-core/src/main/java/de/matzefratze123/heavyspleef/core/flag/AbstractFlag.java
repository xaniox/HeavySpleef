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
package de.matzefratze123.heavyspleef.core.flag;

import java.util.List;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.dom4j.Element;

import de.matzefratze123.heavyspleef.core.Game;
import de.matzefratze123.heavyspleef.core.GameProperty;
import de.matzefratze123.heavyspleef.core.HeavySpleef;
import de.matzefratze123.heavyspleef.core.event.SpleefListener;

public abstract class AbstractFlag<T> implements Listener, SpleefListener {
	
	private T value;
	private HeavySpleef heavySpleef;
	
	public abstract void defineGameProperties(Map<GameProperty, Object> properties);
	
	public abstract boolean hasGameProperties();
	
	public abstract boolean hasBukkitListenerMethods();
	
	public abstract void getDescription(List<String> description);
	
	public abstract T parseInput(Player player, String input) throws InputParseException;
	
	public abstract void marshal(Element element);
	
	public abstract void unmarshal(Element element);
	
	public T getValue() {
		return value;
	}
	
	public void setValue(T value) {
		this.value = value;
	}
	
	protected void setHeavySpleef(HeavySpleef heavySpleef) {
		this.heavySpleef = heavySpleef;
	}
	
	protected HeavySpleef getHeavySpleef() {
		return heavySpleef;
	}
	
	@SuppressWarnings("unchecked")
	protected <F extends AbstractFlag<?>> F getChildFlag(Class<F> childFlagClass, Game game) {
		Class<? extends AbstractFlag<?>> thisClass = (Class<? extends AbstractFlag<?>>) getClass();
		
		FlagRegistry registry = heavySpleef.getFlagRegistry();
		if (!registry.isChildFlag(thisClass, childFlagClass)) {
			return null;
		}
		
		return game.getFlag(childFlagClass);
	}

}
