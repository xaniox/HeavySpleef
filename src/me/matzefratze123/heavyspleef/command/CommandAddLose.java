package me.matzefratze123.heavyspleef.command;

import me.matzefratze123.heavyspleef.HeavySpleef;
import me.matzefratze123.heavyspleef.core.Game;
import me.matzefratze123.heavyspleef.core.GameManager;
import me.matzefratze123.heavyspleef.selection.SelectionManager;
import me.matzefratze123.heavyspleef.utility.LocationHelper;
import me.matzefratze123.heavyspleef.utility.Permissions;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandAddLose extends HSCommand {

	public CommandAddLose() {
		setMaxArgs(0);
		setMinArgs(0);
		setPermission(Permissions.ADD_LOSEZONE.getPerm());
		setOnlyIngame(true);
		setUsage("/spleef addlose");
	}
	
	@Override
	public void execute(CommandSender sender, String[] args) {
		Player player = (Player)sender;
		
		SelectionManager selManager = HeavySpleef.instance.getSelectionManager();
		
		if (!selManager.hasSelection(player) || selManager.getFirstSelection(player) == null || selManager.getSecondSelection(player) == null) {
			player.sendMessage(_("needSelection"));
			return;
		}
		if (selManager.isTroughWorlds(player)) {
			player.sendMessage(_("selectionCantTroughWorlds"));
			return;
		}
		
		Game g = null;
		Location loc1 = selManager.getFirstSelection(player);
		Location loc2 = selManager.getSecondSelection(player);
		
		
		for (Game game : GameManager.getGames()) {
			if (LocationHelper.isInsideRegion(loc1, game.getFirstCorner(), game.getSecondCorner()) &&
					LocationHelper.isInsideRegion(loc2, game.getFirstCorner(), game.getSecondCorner()))
				g = game;
		}
		if (g == null) {
			player.sendMessage(_("notInsideArena"));
			return;
		}
		
		int id = 0;
		while (g.hasLoseZone(id))
			id++;
	
		g.addLoseZone(id, selManager.getFirstSelection(player), selManager.getSecondSelection(player));
		player.sendMessage(_("loseZoneCreated", String.valueOf(id), g.getName(), String.valueOf(id)));
	}

}
