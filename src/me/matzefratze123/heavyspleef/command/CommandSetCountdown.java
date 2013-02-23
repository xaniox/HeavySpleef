package me.matzefratze123.heavyspleef.command;

import me.matzefratze123.heavyspleef.core.Game;
import me.matzefratze123.heavyspleef.core.GameManager;
import me.matzefratze123.heavyspleef.utility.Permissions;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandSetCountdown extends HSCommand {

	public CommandSetCountdown() {
		setMaxArgs(2);
		setMinArgs(2);
		setUsage("/spleef setcountdown <Name> <countdown>");
		setOnlyIngame(true);
		setPermission(Permissions.SET_COUNTDOWN.getPerm());
	}
	
	@Override
	public void execute(CommandSender sender, String[] args) {
		Player p = (Player)sender;
		if (!GameManager.hasGame(args[0].toLowerCase())) {
			p.sendMessage(_("arenaDoesntExists"));
			return;
		}
		
		Game game = GameManager.getGame(args[0].toLowerCase());
		try {
			int countdown = Integer.parseInt(args[1]);
			game.setCountdown(countdown);
			p.sendMessage(_("countdownSet", args[1]));
		} catch (NumberFormatException e) {
			p.sendMessage(_("notANumber", args[1]));
		}
	}

}
