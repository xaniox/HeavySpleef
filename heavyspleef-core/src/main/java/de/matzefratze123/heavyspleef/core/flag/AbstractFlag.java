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

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import org.bukkit.event.Listener;

import de.matzefratze123.heavyspleef.core.Game;
import de.matzefratze123.heavyspleef.core.GameProperty;
import de.matzefratze123.heavyspleef.core.HeavySpleef;
import de.matzefratze123.heavyspleef.core.event.SpleefListener;
import de.matzefratze123.heavyspleef.core.i18n.I18N;
import de.matzefratze123.heavyspleef.core.i18n.I18NManager;
import de.matzefratze123.heavyspleef.core.persistence.XMLMarshallable;
import de.matzefratze123.heavyspleef.core.player.SpleefPlayer;

public abstract class AbstractFlag<T> implements Listener, SpleefListener, XMLMarshallable {
	
	private @Getter @Setter T value;
	private @Getter @Setter AbstractFlag<?> parent;
	@Getter(value = AccessLevel.PROTECTED) @Setter(value = AccessLevel.PROTECTED)
	private HeavySpleef heavySpleef;
	private I18N i18n;
	
	public abstract void getDescription(List<String> description);
	
	public abstract T parseInput(SpleefPlayer player, String input) throws InputParseException;
	
	public abstract String getValueAsString();
	
	public void defineGameProperties(Map<GameProperty, Object> properties) {}
	
	public void onFlagAdd(Game game) {}
	
	public void onFlagRemove(Game game) {}
	
	public void validateInput(T input) throws ValidationException {}
	
	public void setI18N(I18N i18n) {
		this.i18n = i18n;
	}
	
	protected I18N getI18N() {
		if (i18n == null) {
			i18n = I18NManager.getGlobal();
		}
		
		return i18n;
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