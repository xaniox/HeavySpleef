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

import de.xaniox.heavyspleef.core.HeavySpleef;
import de.xaniox.heavyspleef.core.event.SpleefListener;
import de.xaniox.heavyspleef.core.game.Game;
import de.xaniox.heavyspleef.core.persistence.XMLMarshallable;
import org.bukkit.event.Listener;

public abstract class GameExtension implements SpleefListener, Listener, XMLMarshallable {
	
	protected HeavySpleef heavySpleef;
	protected Game game;
	
	public HeavySpleef getHeavySpleef() {
		return heavySpleef;
	}
	
	public void setHeavySpleef(HeavySpleef heavySpleef) {
		this.heavySpleef = heavySpleef;
	}
	
	public Game getGame() {
		return game;
	}
	
	public void setGame(Game game) {
		this.game = game;
	}
	
}