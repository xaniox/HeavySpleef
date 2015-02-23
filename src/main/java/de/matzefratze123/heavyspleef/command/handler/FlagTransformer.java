/*
 * HeavySpleef - Advanced spleef plugin for bukkit
 *
 * Copyright (C) 2013-2014 matzefratze123
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.matzefratze123.heavyspleef.command.handler;

import de.matzefratze123.api.hs.command.transform.TransformException;
import de.matzefratze123.api.hs.command.transform.Transformer;
import de.matzefratze123.heavyspleef.core.flag.Flag;
import de.matzefratze123.heavyspleef.core.flag.FlagType;

@SuppressWarnings("rawtypes")
public class FlagTransformer implements Transformer<Flag> {

	@Override
	public Flag<?> transform(String argument) throws TransformException {
		Flag<?> flag = null;

		for (Flag<?> f : FlagType.getFlagList()) {
			if (flag != null) {
				break;
			}

			if (f.getName().equalsIgnoreCase(argument)) {
				flag = f;
				break;
			} else {
				// Check the aliases
				String[] aliases = f.getAliases();
				if (aliases == null) // Aliases null, continue
					continue;

				for (String alias : aliases) {
					if (alias.equalsIgnoreCase(argument)) {
						flag = f;
						break;
					}
				}
			}
		}

		if (flag == null) {
			throw new TransformException();
		}

		return flag;
	}

}
