package me.matzefratze123.heavyspleef.command;

import me.matzefratze123.heavyspleef.core.Game;
import me.matzefratze123.heavyspleef.core.GameManager;
import me.matzefratze123.heavyspleef.utility.Permissions;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandSetChances extends HSCommand {

	public CommandSetChances() {
		setMaxArgs(2);
		setMinArgs(2);
		setOnlyIngame(true);
		setPermission(Permissions.SET_CHANCES.getPerm());
		setUsage("/spleef setchances <name> <amount>");
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		Player player = (Player)sender;
		
		if (!GameManager.hasGame(args[0])) {
			sender.sendMessage(_("arenaDoesntExists"));
			return;
		}
		Game game = GameManager.getGame(args[0]);
		
		try {
			int amount = Integer.parseInt(args[1]) + 1;
			
			game.setChances(amount);
			player.sendMessage(_("chancesSet", String.valueOf(amount - 1)));
		} catch (NumberFormatException e) {
			player.sendMessage(_("notANumber"));
		}
	}

}
