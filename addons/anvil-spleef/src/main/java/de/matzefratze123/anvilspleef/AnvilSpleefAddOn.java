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

import de.matzefratze123.heavyspleef.addon.java.BasicAddOn;
import de.matzefratze123.heavyspleef.commands.SpleefCommandManager;
import de.matzefratze123.heavyspleef.commands.base.CommandManagerService;
import de.matzefratze123.heavyspleef.commands.base.proxy.ProxyExecution;

public class AnvilSpleefAddOn extends BasicAddOn {

	private AddFloorCommandProxy proxy;
	private ProxyExecution execution;
	
	@Override
	public void enable() {
		SpleefCommandManager manager = (SpleefCommandManager) getHeavySpleef().getCommandManager();
		CommandManagerService service = manager.getService();
		
		proxy = new AddFloorCommandProxy(this);
		
		execution = ProxyExecution.inject(service, "spleef/addfloor");
		execution.attachProxy(proxy);
	}
	
	@Override
	public void disable() {
		if (execution != null) {
			execution.unattachProxy(proxy);
		}
	}
	
}
