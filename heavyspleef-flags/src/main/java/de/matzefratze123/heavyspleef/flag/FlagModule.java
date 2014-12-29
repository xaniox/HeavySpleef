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
package de.matzefratze123.heavyspleef.flag;

import de.matzefratze123.heavyspleef.core.HeavySpleef;
import de.matzefratze123.heavyspleef.core.flag.FlagRegistry;
import de.matzefratze123.heavyspleef.core.module.SimpleModule;
import de.matzefratze123.heavyspleef.flag.defaults.ShovelsFlag;

public class FlagModule extends SimpleModule {
	
	public FlagModule(HeavySpleef heavySpleef) {
		super(heavySpleef);
	}

	@Override
	public void enable() {
		FlagRegistry registry = getHeavySpleef().getFlagRegistry();
		
		registry.registerFlag(ShovelsFlag.class);
	}

	@Override
	public void disable() {
		// TODO Auto-generated method stub
		
	}

}
