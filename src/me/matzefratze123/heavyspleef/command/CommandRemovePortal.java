package me.matzefratze123.heavyspleef.command;

import me.matzefratze123.heavyspleef.core.GameManager;
import me.matzefratze123.heavyspleef.core.region.HUBPortal;
import me.matzefratze123.heavyspleef.utility.Permissions;
import me.matzefratze123.heavyspleef.utility.Util;

import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandRemovePortal extends HSCommand {
	
	public CommandRemovePortal() {
		setPermission(Permissions.REMOVE_PORTAL);
		setUsage("/spleef removeportal");
		setOnlyIngame(true);
	}
	
	@Override
	public void execute(CommandSender sender, String[] args) {
		Player player = (Player)sender;
		Block targetBlock = player.getTargetBlock(Util.getTransparentMaterials(), 100);
		
		HUBPortal port = null;
		for (HUBPortal portal : GameManager.getPortals()) {
			if (portal.contains(targetBlock)) {
				port = portal;
			}
		}
		
		if (port == null) {
			player.sendMessage(_("notLookingAtPortal"));
			return;
		}
		
		GameManager.removePortal(port);
		player.sendMessage(_("portalRemoved"));
	}

}
