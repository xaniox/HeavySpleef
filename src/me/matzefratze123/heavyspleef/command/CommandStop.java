package me.matzefratze123.heavyspleef.command;

import me.matzefratze123.heavyspleef.core.Game;
import me.matzefratze123.heavyspleef.core.GameManager;
import me.matzefratze123.heavyspleef.utility.Permissions;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandStop extends HSCommand {

	public CommandStop() {
		setMinArgs(1);
		setMaxArgs(1);
		setPermission(Permissions.STOP.getPerm());
		setUsage("/spleef stop <Name>");
		setOnlyIngame(true);
	}
	
	@Override
	public void execute(CommandSender sender, String[] args) {
		Player player = (Player)sender;
		if (!GameManager.hasGame(args[0].toLowerCase())){
			player.sendMessage(_("arenaDoesntExists"));
			return;
		}
		
		Game game = GameManager.getGame(args[0].toLowerCase());
		if (!game.isIngame() && !game.isCounting()) {
			player.sendMessage(_("noGameRunning"));
			return;
		}
		game.stop(false);
	}

}
