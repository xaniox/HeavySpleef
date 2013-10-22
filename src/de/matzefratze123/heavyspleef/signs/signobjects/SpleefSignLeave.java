package de.matzefratze123.heavyspleef.signs.signobjects;

import org.bukkit.ChatColor;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.block.SignChangeEvent;

import de.matzefratze123.heavyspleef.command.CommandLeave;
import de.matzefratze123.heavyspleef.core.Game;
import de.matzefratze123.heavyspleef.signs.SpleefSign;
import de.matzefratze123.heavyspleef.util.Permissions;

public class SpleefSignLeave implements SpleefSign {

	@Override
	public void onClick(Player player, Sign sign) {
		CommandLeave.leave(player);
	}

	@Override
	public String getId() {
		return "sign.leave";
	}

	@Override
	public String[] getLines() {
		String[] lines = new String[3];
		
		lines[0] = "[Leave]";
		
		return lines;
	}

	@Override
	public Permissions getPermission() {
		return Permissions.SIGN_LEAVE;
	}

	@Override
	public void onPlace(SignChangeEvent e) {
		e.getPlayer().sendMessage(Game._("spleefSignCreated"));
		
		e.setLine(1, ChatColor.DARK_GRAY + "[" + ChatColor.DARK_RED + ChatColor.BOLD + "Leave" + ChatColor.DARK_GRAY + "]");
	}

}
