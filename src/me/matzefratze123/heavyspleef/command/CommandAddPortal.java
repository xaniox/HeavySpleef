package me.matzefratze123.heavyspleef.command;

import me.matzefratze123.heavyspleef.HeavySpleef;
import me.matzefratze123.heavyspleef.core.GameManager;
import me.matzefratze123.heavyspleef.core.region.HUBPortal;
import me.matzefratze123.heavyspleef.selection.Selection;
import me.matzefratze123.heavyspleef.utility.Permissions;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandAddPortal extends HSCommand {
	
	public CommandAddPortal() {
		setPermission(Permissions.ADD_PORTAL);
		setUsage("/spleef addportal");
		setOnlyIngame(true);
	}
	
	@Override
	public void execute(CommandSender sender, String[] args) {
		Player player = (Player)sender;
		
		Selection s = HeavySpleef.instance.getSelectionManager().getSelection(player);
		Location loc1 = s.getFirst();
		Location loc2 = s.getSecond();
		
		if (!s.has()) {
			player.sendMessage(_("needSelection"));
			return;
		}
		if (s.isTroughWorlds()) {
			player.sendMessage(_("selectionCantTroughWorlds"));
			return;
		}
		
		HUBPortal portal = new HUBPortal(loc1, loc2);
		GameManager.addPortal(portal);
		player.sendMessage(_("portalAdded"));
	}

}
