package de.matzefratze123.heavyspleef.command;

import org.bukkit.command.CommandSender;

import de.matzefratze123.heavyspleef.command.handler.HSCommand;
import de.matzefratze123.heavyspleef.command.handler.Help;
import de.matzefratze123.heavyspleef.core.Game;
import de.matzefratze123.heavyspleef.core.GameManager;
import de.matzefratze123.heavyspleef.util.Permissions;

public class CommandRegenerate extends HSCommand {

	public CommandRegenerate() {
		setMinArgs(1);
		setPermission(Permissions.RESTORE_FLOORS);
	}
	
	@Override
	public void execute(CommandSender sender, String[] args) {
		if (!GameManager.hasGame(args[0])) {
			sender.sendMessage(_("arenaDoesntExists"));
			return;
		}
		
		Game game = GameManager.getGame(args[0]);
		game.getComponents().regenerateFloors();
		sender.sendMessage(_("floorsRegenerated"));
	}

	@Override
	public Help getHelp(Help help) {
		help.setUsage("/spleef regenerate <game>");
		
		help.addHelp("Restores all floors of a game.");
		
		return help;
	}

}
