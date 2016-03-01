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
package de.xaniox.heavyspleef.core.flag;

import de.xaniox.heavyspleef.core.HeavySpleef;
import de.xaniox.heavyspleef.core.event.SpleefListener;
import de.xaniox.heavyspleef.core.game.Game;
import de.xaniox.heavyspleef.core.game.GameProperty;
import de.xaniox.heavyspleef.core.i18n.I18N;
import de.xaniox.heavyspleef.core.i18n.I18NManager;
import de.xaniox.heavyspleef.core.persistence.XMLMarshallable;
import de.xaniox.heavyspleef.core.player.SpleefPlayer;
import org.bukkit.event.Listener;

import java.util.List;
import java.util.Map;

public abstract class AbstractFlag<T> implements Listener, SpleefListener, XMLMarshallable {
	
	private T value;
	private AbstractFlag<?> parent;
	private HeavySpleef heavySpleef;
	private I18N i18n;
	
	public abstract void getDescription(List<String> description);
	
	public abstract T parseInput(SpleefPlayer player, String input) throws InputParseException;
	
	public abstract String getValueAsString();
	
	public void defineGameProperties(Map<GameProperty, Object> properties) {}
	
	public void onFlagAdd(Game game) {}
	
	public void onFlagRemove(Game game) {}
	
	public void validateInput(T input) throws ValidationException {}
	
	public void validateInput(T input, Game game) throws ValidationException {}
	
	public T getValue() {
		return value;
	}
	
	public void setValue(T value) {
		this.value = value;
	}
	
	public void setI18N(I18N i18n) {
		this.i18n = i18n;
	}
	
	public AbstractFlag<?> getParent() {
		return parent;
	}
	
	public void setParent(AbstractFlag<?> parent) {
		this.parent = parent;
	}
	
	protected HeavySpleef getHeavySpleef() {
		return heavySpleef;
	}
	
	protected void setHeavySpleef(HeavySpleef heavySpleef) {
		this.heavySpleef = heavySpleef;
	}
	
	protected I18N getI18N() {
		if (i18n == null) {
			i18n = I18NManager.getGlobal();
		}
		
		return i18n;
	}

}