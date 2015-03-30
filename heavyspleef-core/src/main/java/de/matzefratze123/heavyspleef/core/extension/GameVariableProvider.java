package de.matzefratze123.heavyspleef.core.extension;

import java.util.Set;
import java.util.StringTokenizer;

import de.matzefratze123.heavyspleef.core.CountdownTask;
import de.matzefratze123.heavyspleef.core.Game;
import de.matzefratze123.heavyspleef.core.flag.AbstractFlag;
import de.matzefratze123.heavyspleef.core.layout.VariableProvider;
import de.matzefratze123.heavyspleef.core.script.Variable;

public class GameVariableProvider implements VariableProvider<Game> {

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
					if (!game.isFlagPresent(flagPath)) {
						throw new IllegalArgumentException("Requested flag " + flagPath + " for variable '" + req
								+ "' not available (Consider using a if check of $[has_flag:" + flagPath + " before trying to get its value");
					}
					
					vars.add(new Variable(req, flag.getValue()));
				}
			}
		}
	}

}
