package me.matzefratze123.heavyspleef.command;

import me.matzefratze123.heavyspleef.core.Game;
import me.matzefratze123.heavyspleef.core.GameManager;
import me.matzefratze123.heavyspleef.utility.Permissions;

import org.bukkit.command.CommandSender;

public class CommandRename extends HSCommand {

	public CommandRename() {
		setMinArgs(2);
		setMaxArgs(2);
		setUsage("/spleef rename <arena> <newName>");
		setOnlyIngame(true);
		setPermission(Permissions.RENAME);
	}
	
	@Override
	public void execute(CommandSender sender, String[] args) {
		if (!GameManager.hasGame(args[0])) {
			sender.sendMessage(_("arenaDoesntExists"));
			return;
		}
		
		Game game = GameManager.getGame(args[0]);
		
		String message = game.rename(args[1]) ? _("gameRenamed", args[0], args[1]) : _("arenaAlreadyExists");
		sender.sendMessage(message);
	}

}
