package me.matzefratze123.heavyspleef.command;

import me.matzefratze123.heavyspleef.core.Game;
import me.matzefratze123.heavyspleef.core.GameManager;
import me.matzefratze123.heavyspleef.core.LoseCause;
import me.matzefratze123.heavyspleef.utility.Permissions;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandLeave extends HSCommand {

	public CommandLeave() {
		setMaxArgs(0);
		setMinArgs(0);
		setOnlyIngame(true);
		setPermission(Permissions.LEAVE_GAME.getPerm());
		setUsage("/spleef leave");
	}
	
	@Override
	public void execute(CommandSender sender, String[] args) {
		Player player = (Player)sender;
		if (!GameManager.isInAnyGame(player)) {
			player.sendMessage(_("playerIsntInAnyGameToPlayer"));
			return;
		}
		Game game = GameManager.getGameFromPlayer(player);
		
		game.removePlayer(player, LoseCause.LEAVE);
		player.teleport(game.getLosePoint());
		player.sendMessage(_("left"));
	}

}
