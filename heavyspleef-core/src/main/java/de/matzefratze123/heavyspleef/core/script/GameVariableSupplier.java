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
package de.matzefratze123.heavyspleef.core.script;

import java.util.Set;
import java.util.StringTokenizer;

import de.matzefratze123.heavyspleef.core.CountdownTask;
import de.matzefratze123.heavyspleef.core.Game;
import de.matzefratze123.heavyspleef.core.flag.AbstractFlag;

public class GameVariableSupplier implements VariableSupplier<Game> {

	private static final String HAS_FLAG_PREFIX = "has_flag";
	private static final String FLAG_VALUE_PREFIX = "flag_value";
	
	@Override
	public void provide(Set<Variable> vars, Set<String> requested, Game game) {
		String gameState = game.getGameState().name().toLowerCase();
		gameState = Character.toUpperCase(gameState.charAt(0)) + gameState.substring(1);
		
		CountdownTask task = game.getCountdownTask();
		
		vars.add(new Variable("name", game.getName()));
		vars.add(new Variable("state", gameState));
		vars.add(new Variable("players", game.getPlayers().size()));
		vars.add(new Variable("dead", game.getDeadPlayers().size()));
		vars.add(new Variable("countdown", task != null ? task.getRemaining() : 0));
		vars.add(new Variable("is_countdown", task != null));
		
		for (String req : requested) {
			StringTokenizer tokenizer = new StringTokenizer(req, ":");
			
			String primaryReq = tokenizer.nextToken();
			
			boolean hasFlagRequest = primaryReq.equals(HAS_FLAG_PREFIX);
			boolean flagValueRequest = primaryReq.equals(FLAG_VALUE_PREFIX);
			
			if (hasFlagRequest || flagValueRequest) {
				if (!tokenizer.hasMoreTokens()) {
					throw new IllegalStateException("Requested variable '" + req + "' must be followed by a flag name ('<request>:<flagpath>')");
				}
				
				String flagPath = tokenizer.nextToken();
				
				if (hasFlagRequest) {
					vars.add(new Variable(req, game.isFlagPresent(flagPath)));
				} else if (flagValueRequest) {
					AbstractFlag<?> flag = game.getFlag(flagPath);
					
					vars.add(new Variable(req, flag != null ? flag.getValue() : null));
				}
			}
		}
	}

}
