package me.matzefratze123.heavyspleef.command;

import me.matzefratze123.heavyspleef.HeavySpleef;
import me.matzefratze123.heavyspleef.core.Game;
import me.matzefratze123.heavyspleef.core.GameManager;
import me.matzefratze123.heavyspleef.utility.Permissions;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandSetMoney extends HSCommand {

	public CommandSetMoney() {
		setMaxArgs(2);
		setMinArgs(2);
		setPermission(Permissions.SET_MONEY.getPerm());
		setUsage("/spleef setmoney <Name> <Value>");
		setOnlyIngame(true);
	}
	
	@Override
	public void execute(CommandSender sender, String[] args) {
		Player player = (Player)sender;
		if (!HeavySpleef.hasVault) {
			player.sendMessage(_("noVault"));
			return;
		}
		
		if (!GameManager.hasGame(args[0].toLowerCase())) {
			sender.sendMessage(_("arenaDoesntExists"));
			return;
		}
		Game game = GameManager.getGame(args[0].toLowerCase());
		
		try {
			game.setMoney(Integer.parseInt(args[1]));
			player.sendMessage(_("moneySet"));
		} catch (NumberFormatException e) {
			player.sendMessage(_("notANumber", args[1]));
		}
	}

}
