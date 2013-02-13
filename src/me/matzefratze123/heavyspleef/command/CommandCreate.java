package me.matzefratze123.heavyspleef.command;

import me.matzefratze123.heavyspleef.HeavySpleef;
import me.matzefratze123.heavyspleef.core.Game;
import me.matzefratze123.heavyspleef.core.GameManager;
import me.matzefratze123.heavyspleef.selection.SelectionManager;
import me.matzefratze123.heavyspleef.utility.LocationHelper;
import me.matzefratze123.heavyspleef.utility.Permissions;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandCreate extends HSCommand {

	public CommandCreate() {
		setMaxArgs(1);
		setMinArgs(1);
		setOnlyIngame(true);
		setPermission(Permissions.CREATE_GAME.getPerm());
		setUsage("/spleef create <Name>");
	}
	
	@Override
	public void execute(CommandSender sender, String[] args) {
		Player player = (Player)sender;
		if (GameManager.hasGame(args[0].toLowerCase())) {
			player.sendMessage(_("arenaAlreadyExists"));
			return;
		}
		SelectionManager selManager = HeavySpleef.instance.getSelectionManager();
		
		if (!selManager.hasSelection(player) || selManager.getFirstSelection(player) == null || selManager.getSecondSelection(player) == null) {
			player.sendMessage(_("needSelection"));
			return;
		}
		if (selManager.isTroughWorlds(player)) {
			player.sendMessage(_("selectionCantTroughWorlds"));
			return;
		}
		
		for (Game game : GameManager.getGames()) {
			if (LocationHelper.isInsideRegion(selManager.getFirstSelection(player), game.getFirstCorner(), game.getSecondCorner())) {
				player.sendMessage(_("arenaCantBeInsideAnother"));
				return;
			}
			if (LocationHelper.isInsideRegion(selManager.getSecondSelection(player), game.getFirstCorner(), game.getSecondCorner())) {
				player.sendMessage(_("arenaCantBeInsideAnother"));
				return;
			}
		}
		
		GameManager.createGame(args[0].toLowerCase(), selManager.getFirstSelection(player), selManager.getSecondSelection(player), true);
		player.sendMessage(_("gameCreated"));
	}

}
