package de.matzefratze123.heavyspleef.commands;

import org.bukkit.command.CommandSender;

import de.matzefratze123.heavyspleef.commands.internal.Command;
import de.matzefratze123.heavyspleef.commands.internal.CommandContext;
import de.matzefratze123.heavyspleef.commands.internal.CommandException;
import de.matzefratze123.heavyspleef.commands.internal.CommandValidate;
import de.matzefratze123.heavyspleef.core.Game;
import de.matzefratze123.heavyspleef.core.GameManager;
import de.matzefratze123.heavyspleef.core.GameState;
import de.matzefratze123.heavyspleef.core.HeavySpleef;
import de.matzefratze123.heavyspleef.core.i18n.Messages;

public class CommandDisable {

	@Command(name = "disable", minArgs = 1, usage = "/spleef disable <game>",
			description = "Disables a game with the given name",
			permission = "heavyspleef.admin.disable")
	public void onDisableCommand(CommandContext context, HeavySpleef heavySpleef) throws CommandException {
		CommandSender sender = context.getSender();
		
		String gameName = context.getString(0);
		GameManager manager = heavySpleef.getGameManager();
		
		CommandValidate.isTrue(manager.hasGame(gameName), heavySpleef.getMessage(Messages.Command.GAME_DOESNT_EXIST));
		Game game = manager.getGame(gameName);
		
		CommandValidate.isTrue(game.getGameState() != GameState.DISABLED, heavySpleef.getMessage(Messages.Command.GAME_ALREADY_DISABLED));
		game.disable();
		
		sender.sendMessage(heavySpleef.getMessage(Messages.Command.GAME_DISABLED));
	}
	
}
