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

import de.xaniox.heavyspleef.core.Permissions;
import de.xaniox.heavyspleef.core.config.ConfigType;
import de.xaniox.heavyspleef.core.config.SignLayoutConfiguration;
import de.xaniox.heavyspleef.core.game.Game;
import de.xaniox.heavyspleef.core.game.GameManager;
import de.xaniox.heavyspleef.core.i18n.I18N;
import de.xaniox.heavyspleef.core.i18n.I18NManager;
import de.xaniox.heavyspleef.core.i18n.Messages;
import de.xaniox.heavyspleef.core.layout.SignLayout;
import de.xaniox.heavyspleef.core.player.SpleefPlayer;
import org.bukkit.Location;

@Extension(name = "start-sign")
public class StartSignExtension extends SignExtension {
	
	public static final String IDENTIFIER = "start";
	private final I18N i18n = I18NManager.getGlobal();
	
	@SuppressWarnings("unused")
	private StartSignExtension() {}
	
	public StartSignExtension(Location location, PrefixType prefixType) {
		super(location, prefixType);
	}
	
	@Override
	public String[] getPermission() {
		return new String[] {Permissions.PERMISSION_SIGN_START, Permissions.PERMISSION_START};
	}

	@Override
	public void onSignClick(SpleefPlayer player) {
		GameManager manager = getHeavySpleef().getGameManager();
		Game game = manager.getGame(player);
		
		if (game == null) {
			player.sendMessage(i18n.getString(Messages.Command.NOT_INGAME));
			return;
		}
		
		if (game.getGameState().isGameActive()) {
			player.sendMessage(i18n.getString(Messages.Command.GAME_IS_INGAME));
			return;
		}
		
		game.countdown();
	}
	
	@Override
	public SignLayout retrieveSignLayout() {
		SignLayoutConfiguration config = heavySpleef.getConfiguration(ConfigType.START_SIGN_LAYOUT_CONFIG);
		return config.getLayout();
	}
	
}