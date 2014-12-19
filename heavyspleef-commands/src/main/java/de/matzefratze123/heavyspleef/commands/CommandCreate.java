package de.matzefratze123.heavyspleef.commands;

import org.bukkit.command.CommandSender;

import de.matzefratze123.heavyspleef.commands.internal.Command;
import de.matzefratze123.heavyspleef.commands.internal.CommandContext;
import de.matzefratze123.heavyspleef.commands.internal.CommandException;
import de.matzefratze123.heavyspleef.commands.internal.CommandValidate;
import de.matzefratze123.heavyspleef.commands.internal.PlayerOnly;
import de.matzefratze123.heavyspleef.core.Game;
import de.matzefratze123.heavyspleef.core.GameManager;
import de.matzefratze123.heavyspleef.core.HeavySpleef;
import de.matzefratze123.heavyspleef.core.i18n.Messages;

public class CommandCreate {

	@Command(name = "create", minArgs = 1, usage = "/spleef create <game>", 
			description = "Creates a new spleef game with the given name and selection",
			permission = "heavyspleef.admin.create")
	@PlayerOnly
	public void onCreateCommand(CommandContext context, HeavySpleef heavySpleef) throws CommandException {
		CommandSender sender = context.getSender();
		
		String gameName = context.getString(0);
		GameManager manager = heavySpleef.getGameManager();
		
		CommandValidate.isTrue(!manager.hasGame(gameName), heavySpleef.getMessage(Messages.Command.GAME_ALREADY_EXIST));
		
		Game game = new Game(heavySpleef, gameName);
		manager.addGame(game);
		sender.sendMessage(heavySpleef.getMessage(heavySpleef.getMessage(Messages.Command.GAME_CREATED)));
	}

}
