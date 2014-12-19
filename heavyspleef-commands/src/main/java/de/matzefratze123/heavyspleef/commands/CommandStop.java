package de.matzefratze123.heavyspleef.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.matzefratze123.heavyspleef.commands.internal.Command;
import de.matzefratze123.heavyspleef.commands.internal.CommandContext;
import de.matzefratze123.heavyspleef.commands.internal.CommandException;
import de.matzefratze123.heavyspleef.commands.internal.CommandValidate;
import de.matzefratze123.heavyspleef.core.Game;
import de.matzefratze123.heavyspleef.core.GameManager;
import de.matzefratze123.heavyspleef.core.HeavySpleef;
import de.matzefratze123.heavyspleef.core.i18n.Messages;
import de.matzefratze123.heavyspleef.core.player.SpleefPlayer;

public class CommandStop {
	
	@Command(name = "stop", usage = "/spleef stop [game]",
			description = "Stops the current game or the given game",
			permission = "heavyspleef.admin.stop")
	public void onStopCommand(CommandContext context, HeavySpleef heavySpleef) throws CommandException {
		CommandSender sender = context.getSender();
		GameManager manager = heavySpleef.getGameManager();
		
		Game game;
		if (context.argsLength() > 0) {
			game = manager.getGame(context.getString(0));
		} else {
			CommandValidate.isTrue(sender instanceof Player, heavySpleef.getMessage(Messages.Command.MUST_BE_A_PLAYER));
			SpleefPlayer player = heavySpleef.getSpleefPlayer(sender);
			
			game = manager.getGame(player);
		}
		
		CommandValidate.notNull(game, heavySpleef.getMessage(Messages.Command.GAME_DOESNT_EXIST));
		game.stop();
		
		sender.sendMessage(heavySpleef.getMessage(Messages.Command.GAME_STOPPED));
	}
	
}
