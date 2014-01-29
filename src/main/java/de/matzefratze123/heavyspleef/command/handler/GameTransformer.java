package de.matzefratze123.heavyspleef.command.handler;

import de.matzefratze123.api.command.transform.TransformException;
import de.matzefratze123.api.command.transform.Transformer;
import de.matzefratze123.heavyspleef.core.Game;
import de.matzefratze123.heavyspleef.core.GameManager;

public class GameTransformer implements Transformer<Game> {

	@Override
	public Game transform(String argument) throws TransformException {
		if (!GameManager.hasGame(argument)) {
			throw new TransformException();
		}
		
		return GameManager.getGame(argument);
	}

}
