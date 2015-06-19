package de.matzefratze123.heavyspleef.core;

import de.matzefratze123.heavyspleef.core.Game.JoinResult;
import de.matzefratze123.heavyspleef.core.i18n.I18N;
import de.matzefratze123.heavyspleef.core.i18n.I18NManager;
import de.matzefratze123.heavyspleef.core.i18n.Messages;
import de.matzefratze123.heavyspleef.core.player.SpleefPlayer;

public class JoinRequester {
	
	private final I18N i18n = I18NManager.getGlobal();
	private Game game;
	
	protected JoinRequester(Game game) {
		this.game = game;
	}
	
	public JoinResult request(SpleefPlayer player) throws JoinValidationException {
		if (!game.getGameState().isGameEnabled()) {
			throw new JoinValidationException(i18n.getVarString(Messages.Command.GAME_JOIN_IS_DISABLED)
				.setVariable("game", game.getName())
				.toString());
		}
		
		if (game.getGameState().isGameActive()) {
			throw new JoinValidationException(i18n.getVarString(Messages.Command.GAME_IS_INGAME)
					.setVariable("game", game.getName())
					.toString());
		}
		
		GameManager manager = game.getHeavySpleef().getGameManager();
		
		if (manager.getGame(player) != null) {
			throw new JoinValidationException(i18n.getString(Messages.Command.ALREADY_PLAYING));
		}
		
		return game.join(player);
	}
	
	public static class JoinValidationException extends Exception {

		private static final long serialVersionUID = -2124169192955966100L;

		public JoinValidationException(String message) {
			super(message);
		}
		
	}

}