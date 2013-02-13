package me.matzefratze123.heavyspleef.command;

import me.matzefratze123.heavyspleef.core.Game;
import me.matzefratze123.heavyspleef.core.GameManager;
import me.matzefratze123.heavyspleef.core.LoseCause;
import me.matzefratze123.heavyspleef.utility.Permissions;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandKick extends HSCommand {

	public CommandKick() {
		setMinArgs(1);
		setOnlyIngame(true);
		setPermission(Permissions.KICK.getPerm());
		setUsage("/spleef kick <Player> [Reason]");
	}
	
	@Override
	public void execute(CommandSender sender, String[] args) {
		Player player = (Player)sender;
		Player target = Bukkit.getPlayer(args[0]);
		
		if (target == null) {
			player.sendMessage(_("playerNotOnline"));
			return;
		}
		
		if (!GameManager.isInAnyGame(target)) {
			player.sendMessage(_("playerIsntInAnyGame"));
			return;
		}
		
		String reasonMessage = args.length > 1 ? " for " : "";
		StringBuilder reasonBuilder = new StringBuilder();
		for (int i = 1; i < args.length; i++)
			reasonBuilder.append(args[i]).append(" ");
		reasonMessage += reasonBuilder.toString();
		
		Game game = GameManager.getGameFromPlayer(target);
		game.removePlayer(target, LoseCause.KICK);
		target.teleport(game.getLosePoint());
		target.sendMessage(_("kickedOfToPlayer", player.getName(), reasonMessage));
		player.sendMessage(_("kickedOfToKicker", target.getName(), game.getName(), reasonMessage));
	}

}
