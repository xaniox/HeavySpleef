package me.matzefratze123.heavyspleef.command;

import me.matzefratze123.heavyspleef.core.Game;
import me.matzefratze123.heavyspleef.core.GameManager;
import me.matzefratze123.heavyspleef.utility.Permissions;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandRemoveLose extends HSCommand {

	public CommandRemoveLose() {
		setMinArgs(2);
		setMaxArgs(2);
		setOnlyIngame(true);
		setPermission(Permissions.REMOVE_LOSEZONE.getPerm());
		setUsage("/spleef removelose <Name> <ID>");
	}
	
	@Override
	public void execute(CommandSender sender, String[] args) {
		Player player = (Player)sender;
		
		int id;
		try {
			id = Integer.parseInt(args[1]);
		} catch (NumberFormatException e) {
			player.sendMessage(_("notANumber", args[1]));
			return;
		}
		if (!GameManager.hasGame(args[0].toLowerCase())) {
			player.sendMessage(_("arenaDoesntExists"));
			return;
		}
		Game game = GameManager.getGame(args[0].toLowerCase());
		if (!game.hasLoseZone(id)) {
			player.sendMessage(_("loseZoneWithIDDoesntExists"));
			return;
		}
		if (game.isIngame() || game.isCounting()) {
			player.sendMessage(_("cantRemoveLoseWhileRunning"));
			return;
		}
		game.removeLoseZone(id);
		player.sendMessage(_("loseZoneRemoved", String.valueOf(id)));
		return;
				
		
	}

}
