/*
 * This file is part of addons.
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
package de.matzefratze123.anvilspleef;

import de.matzefratze123.heavyspleef.commands.base.CommandException;
import de.matzefratze123.heavyspleef.commands.base.proxy.Filter;
import de.matzefratze123.heavyspleef.commands.base.proxy.Proxy;
import de.matzefratze123.heavyspleef.commands.base.proxy.ProxyContext;
import de.matzefratze123.heavyspleef.commands.base.proxy.ProxyPriority;
import de.matzefratze123.heavyspleef.commands.base.proxy.Redirection;
import de.matzefratze123.heavyspleef.commands.base.proxy.ProxyPriority.Priority;
import de.matzefratze123.heavyspleef.core.HeavySpleef;
import de.matzefratze123.heavyspleef.core.game.Game;
import de.matzefratze123.heavyspleef.core.game.GameManager;

@Filter("spleef/addfloor")
@ProxyPriority(Priority.HIGH)
public class AddFloorCommandProxy implements Proxy {

	private AnvilSpleefAddOn addon;
	
	public AddFloorCommandProxy(AnvilSpleefAddOn addon) {
		this.addon = addon;
	}
	
	@Override
	public void execute(ProxyContext context, Object[] executionArgs) throws CommandException {
		HeavySpleef heavySpleef = getFirstArg(executionArgs, HeavySpleef.class);
		String gameName = context.getString(0);
		
		GameManager gameManager = heavySpleef.getGameManager();
		if (!gameManager.hasGame(gameName)) {
			return;
		}
		
		Game game = gameManager.getGame(gameName);
		if (!game.isFlagPresent(FlagAnvilSpleef.class)) {
			return;
		}
		
		if (game.getFloors().size() == 0) {
			return;
		}
		
		context.redirect(Redirection.CANCEL);
		context.getSender().sendMessage(addon.getI18n().getString(ASMessages.ANVIL_SPLEEF_GAME_FLOOR_LIMITED));
	}
	
	@SuppressWarnings("unchecked")
	private <T> T getFirstArg(Object[] args, Class<T> expected) {
		for (Object arg : args) {
			if (!expected.isInstance(arg)) {
				continue;
			}
			
			return (T) arg;
		}
		
		return null;
	}

}
