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

public class CommandLeave {

	@Command(name = "leave", usage = "/spleef leave",
			description = "Leaves a game",
			permission = "heavyspleef.leave")
	@PlayerOnly
	public void onLeaveCommand(CommandContext context, HeavySpleef heavySpleef) throws CommandException {
		SpleefPlayer player = heavySpleef.getSpleefPlayer(context.getSender());
		
		GameManager manager = heavySpleef.getGameManager();
		Game game = manager.getGame(player);
		
		CommandValidate.notNull(game, heavySpleef.getMessage(Messages.Command.NOT_INGAME));
		
		game.leave(player);
	}
	
}
