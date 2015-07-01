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
package de.matzefratze123.heavyspleef.core;

import de.matzefratze123.heavyspleef.core.i18n.I18N;
import de.matzefratze123.heavyspleef.core.i18n.I18NManager;
import de.matzefratze123.heavyspleef.core.i18n.Messages;

public enum GameState {
	
	DISABLED(false, false),
	WAITING(false, true),
	LOBBY(false, true),
	STARTING(true, true),
	INGAME(true, true);
	
	private boolean gameActive;
	private boolean gameEnabled;
	
	private GameState(boolean gameActive, boolean gameEnabled) {
		this.gameActive = gameActive;
		this.gameEnabled = gameEnabled;
	}
	
	public boolean isGameActive() {
		return gameActive;
	}
	
	public boolean isGameEnabled() {
		return gameEnabled;
	}
	
	public String getLocalizedName() {
		I18N i18n = I18NManager.getGlobal();
		String[] array = i18n.getStringArray(Messages.Arrays.GAME_STATE_ARRAY);
		return array[ordinal()];
	}
	
}
