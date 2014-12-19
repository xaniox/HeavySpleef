package de.matzefratze123.heavyspleef.commands;

import de.matzefratze123.heavyspleef.commands.internal.Command;
import de.matzefratze123.heavyspleef.commands.internal.CommandContext;
import de.matzefratze123.heavyspleef.commands.internal.CommandException;
import de.matzefratze123.heavyspleef.commands.internal.CommandValidate;
import de.matzefratze123.heavyspleef.commands.internal.PlayerOnly;
import de.matzefratze123.heavyspleef.core.Game;
import de.matzefratze123.heavyspleef.core.GameManager;
import de.matzefratze123.heavyspleef.core.HeavySpleef;
import de.matzefratze123.heavyspleef.core.i18n.Messages;
import de.matzefratze123.heavyspleef.core.player.SpleefPlayer;

public class CommandStart {
	
	@Command(name = "start", usage = "/spleef start [game]",
			description = "Starts the current game or a given game",
			permission = "heavyspleef.admin.start")
	@PlayerOnly
	public void onStartCommand(CommandContext context, HeavySpleef heavySpleef) throws CommandException {
		SpleefPlayer player = heavySpleef.getSpleefPlayer(context.getSender());
		GameManager manager = heavySpleef.getGameManager();
		
		Game game;
		if (context.argsLength() > 0) {
			game = manager.getGame(context.getString(0));
		} else {
			game = manager.getGame(player);
		}
		
		CommandValidate.notNull(game, heavySpleef.getMessage(Messages.Command.GAME_DOESNT_EXIST));
		
		game.countdown();
		player.sendMessage(heavySpleef.getMessage(Messages.Command.GAME_STARTED));
	}

}
