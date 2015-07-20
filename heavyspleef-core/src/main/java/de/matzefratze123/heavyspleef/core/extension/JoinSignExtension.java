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

import de.matzefratze123.heavyspleef.core.Permissions;
import de.matzefratze123.heavyspleef.core.config.ConfigType;
import de.matzefratze123.heavyspleef.core.config.SignLayoutConfiguration;
import de.matzefratze123.heavyspleef.core.game.Game;
import de.matzefratze123.heavyspleef.core.game.JoinRequester;
import de.matzefratze123.heavyspleef.core.game.JoinRequester.JoinValidationException;
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
	
	public JoinSignExtension(Location location, PrefixType prefixType) {
		super(location, prefixType);
	}
	
	@Override
	public String[] getPermission() {
		return new String[] {Permissions.PERMISSION_SIGN_JOIN, Permissions.PERMISSION_JOIN};
	}
	
	@Override
	public void onSignClick(SpleefPlayer player) {
		Game game = getGame();
		
		try {
			long timer = game.getJoinRequester().request(player, JoinRequester.QUEUE_PLAYER_CALLBACK);
			if (timer > 0) {
				player.sendMessage(i18n.getVarString(Messages.Command.JOIN_TIMER_STARTED)
						.setVariable("timer", String.valueOf(timer))
						.toString());
			}
		} catch (JoinValidationException e) {
			player.sendMessage(e.getMessage());
			JoinRequester.QUEUE_PLAYER_CALLBACK.onJoin(player, game, e.getResult());
		}		
	}
	
	@Override
	public SignLayout retrieveSignLayout() {
		SignLayoutConfiguration config = heavySpleef.getConfiguration(ConfigType.JOIN_SIGN_LAYOUT_CONFIG);
		return config.getLayout();
	}

}
