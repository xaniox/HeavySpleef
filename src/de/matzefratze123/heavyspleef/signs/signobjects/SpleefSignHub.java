package de.matzefratze123.heavyspleef.signs.signobjects;

import org.bukkit.ChatColor;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.block.SignChangeEvent;

import de.matzefratze123.heavyspleef.command.CommandHub;
import de.matzefratze123.heavyspleef.core.Game;
import de.matzefratze123.heavyspleef.signs.SpleefSign;
import de.matzefratze123.heavyspleef.util.Permissions;

public class SpleefSignHub implements SpleefSign {

	@Override
	public void onClick(Player player, Sign sign) {
		CommandHub.tpToHub(player);
	}

	@Override
	public String getId() {
		return "sign.hub";
	}

	@Override
	public String[] getLines() {
		String[] lines = new String[3];
		
		lines[0] = "[HUB]";
		
		return lines;
	}

	@Override
	public Permissions getPermission() {
		return Permissions.SIGN_HUB;
	}

	@Override
	public void onPlace(SignChangeEvent e) {
		e.getPlayer().sendMessage(Game._("spleefSignCreated"));
		
		e.setLine(1, ChatColor.DARK_GRAY + "[" + ChatColor.RESET + ChatColor.BOLD + "HUB" + ChatColor.DARK_GRAY + "]");
	}
	
}
