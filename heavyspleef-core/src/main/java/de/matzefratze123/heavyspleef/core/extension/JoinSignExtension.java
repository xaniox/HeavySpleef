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
package de.matzefratze123.heavyspleef.core.extension;

import org.bukkit.Location;

import de.matzefratze123.heavyspleef.core.Game;
import de.matzefratze123.heavyspleef.core.config.ConfigType;
import de.matzefratze123.heavyspleef.core.config.SignLayoutConfiguration;
import de.matzefratze123.heavyspleef.core.i18n.I18N;
import de.matzefratze123.heavyspleef.core.i18n.I18NManager;
import de.matzefratze123.heavyspleef.core.i18n.Messages;
import de.matzefratze123.heavyspleef.core.layout.SignLayout;
import de.matzefratze123.heavyspleef.core.player.SpleefPlayer;

@Extension(name = "join-sign")
public class JoinSignExtension extends SignExtension {

	public static final String IDENTIFIER = "join";
	private final I18N i18n = I18NManager.getGlobal();
	
	@SuppressWarnings("unused")
	private JoinSignExtension() {}
	
	public JoinSignExtension(Location location) {
		super(location);
	}
	
	@Override
	public void onSignClick(SpleefPlayer player) {
		Game game = getGame();
		
		if (!game.getGameState().isGameEnabled()) { 
			player.sendMessage(i18n.getVarString(Messages.Command.GAME_JOIN_IS_DISABLED)
				.setVariable("game", game.getName())
				.toString());
			return;
		}
		
		if (game.getGameState().isGameActive()){
			player.sendMessage(i18n.getVarString(Messages.Command.GAME_IS_INGAME)
				.setVariable("game", game.getName())
				.toString());
			return;
		}
		
		if (getHeavySpleef().getGameManager().getGame(player) != null) {
			player.sendMessage(i18n.getString(Messages.Command.ALREADY_PLAYING));
			return;
		}
		
		game.join(player);
	}
	
	@Override
	public SignLayout retrieveSignLayout() {
		SignLayoutConfiguration config = heavySpleef.getConfiguration(ConfigType.JOIN_SIGN_LAYOUT_CONFIG);
		return config.getLayout();
	}

}
