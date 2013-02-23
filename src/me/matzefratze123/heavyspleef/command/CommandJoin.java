package me.matzefratze123.heavyspleef.command;

import me.matzefratze123.heavyspleef.HeavySpleef;
import me.matzefratze123.heavyspleef.core.CountingTask;
import me.matzefratze123.heavyspleef.core.Game;
import me.matzefratze123.heavyspleef.core.GameManager;
import me.matzefratze123.heavyspleef.utility.Permissions;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandJoin extends HSCommand {

	public CommandJoin() {
		setMaxArgs(2);
		setMinArgs(1);
		setOnlyIngame(true);
		setPermission(Permissions.JOIN_GAME.getPerm());
		setUsage("/spleef join <Arena>");
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		Player player = (Player)sender;
		
		if (!GameManager.hasGame(args[0].toLowerCase())) {
			player.sendMessage(_("arenaDoesntExists"));
			return;
		}
		Game game = GameManager.getGame(args[0].toLowerCase());
		
		if (game.isDisabled()) {
			player.sendMessage(_("gameIsDisabled"));
			return;
		}
		if (!game.isFinal()) {
			player.sendMessage(_("isntReadyToPlay"));
			return;
		}
		
		if (args.length == 1) {
			if (HeavySpleef.hasVault) {
				if (HeavySpleef.econ.getBalance(player.getName()) < game.getMoney()) {
					player.sendMessage(_("notEnoughMoneyToJoin"));
					return;
				}
			}
			
			join(player, game);
		} else if (args.length == 2) {
			Player target = Bukkit.getPlayer(args[1]);
			if (target == null) {
				player.sendMessage(_("playerNotOnline"));
				return;
			}
			if (HeavySpleef.hasVault) {
				if (HeavySpleef.econ.getBalance(target.getName()) < game.getMoney()) {
					player.sendMessage(_("targetHasntEnoughMoney"));
					return;
				}
			}
			
			boolean join = join(target, game);
			if (join)
				player.sendMessage(_("targetHasJoined", target.getName(), game.getName()));
			else
				player.sendMessage(_("targetAddedToQueue", target.getName()));
		}
	}
	
	private boolean join(Player player, Game game) {
		if (game.isIngame()) {
			player.sendMessage(_("gameAlreadyRunning"));
			GameManager.addQueue(player, game.getName());
			return false;
		}
		if (GameManager.isInAnyGame(player)) {
			player.sendMessage(_("cantJoinMultipleGames"));
			return false;
		}
		if (game.isCounting() && plugin.getConfig().getBoolean("general.joinAtCountdown")) {
			player.teleport(CountingTask.getRandomSpleefLocation(game));
			player.sendMessage(_("playerJoinedToPlayer", game.getName()));
			game.addPlayer(player);
			return true;
		} else if (game.isCounting() && !plugin.getConfig().getBoolean("general.joinAtCountdown")){
			player.sendMessage(_("gameAlreadyRunning"));
			GameManager.addQueue(player, game.getName());
			return false;
		}
		
		player.teleport(game.getPreGamePoint());
		player.sendMessage(_("playerJoinedToPlayer", game.getName()));
		game.addPlayer(player);
		if (GameManager.queues.containsKey(player.getName()))
			GameManager.queues.remove(player.getName());
		return true;
	}

}
