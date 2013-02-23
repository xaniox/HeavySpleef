package me.matzefratze123.heavyspleef.listener;

import me.matzefratze123.heavyspleef.core.Game;
import me.matzefratze123.heavyspleef.core.GameManager;
import me.matzefratze123.heavyspleef.utility.Permissions;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class SignListener implements Listener {

	@EventHandler
	public void onSignChange(SignChangeEvent e) {
		Player p = e.getPlayer();
		Block block = e.getBlock();
		
		String line1, line2, line3;
		
		line1 = ChatColor.stripColor(e.getLine(0));
		line2 = ChatColor.stripColor(e.getLine(1));
		line3 = ChatColor.stripColor(e.getLine(2));
		
		if (p == null)
			return;
		if (!line1.equalsIgnoreCase("[Spleef]"))
			return;
		if (!p.hasPermission(Permissions.CREATE_SPLEEF_SIGN.getPerm())) {
			p.sendMessage(Game._("notAllowedToCreateSpleefSigns"));
			block.breakNaturally();
			return;
		}
		if (line2.equalsIgnoreCase("[Join]")) {
			if (!GameManager.hasGame(line3.toLowerCase())) {
				p.sendMessage(Game._("arenaDoesntExists"));
				block.breakNaturally();
				return;
			}
			p.sendMessage(Game._("spleefSignCreated"));
			
			e.setLine(0, ChatColor.DARK_BLUE + "[Spleef]");
			e.setLine(1, ChatColor.RED + "[Join]");
			e.setLine(2, line3);
			e.setLine(3, "");
		} else if (line2.equalsIgnoreCase("[Start]")) {
			if (!GameManager.hasGame(line3.toLowerCase())) {
				p.sendMessage(Game._("arenaDoesntExists"));
				block.breakNaturally();
				return;
			}
			p.sendMessage(Game._("spleefSignCreated"));
			
			e.setLine(0, ChatColor.DARK_BLUE + "[Spleef]");
			e.setLine(1, ChatColor.RED + "[Start]");
			e.setLine(2, line3);
			e.setLine(3, "");
		} else if (line2.equalsIgnoreCase("[Leave]")) {
			p.sendMessage(Game._("spleefSignCreated"));
			
			e.setLine(0, ChatColor.DARK_BLUE + "[Spleef]");
			e.setLine(1, ChatColor.RED + "[Leave]");
			e.setLine(2, line3);
			e.setLine(3, "");
		}
	}
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent e) {
		Player p = e.getPlayer();
		Block block = e.getClickedBlock();
		
		if (block == null)
			return;
		if (p == null)
			return;
		if (e.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;
		
		BlockState state = block.getState();
		if (state.getType() != Material.SIGN_POST && state.getType() != Material.WALL_SIGN)
			return;
		if (!(state instanceof Sign))
			return;
		
		Sign sign = (Sign) state;
		
		String line1, line2, line3;
		
		line1 = ChatColor.stripColor(sign.getLine(0));
		line2 = ChatColor.stripColor(sign.getLine(1));
		line3 = ChatColor.stripColor(sign.getLine(2));
		
		if (line1.equalsIgnoreCase("[Spleef]") && line2.equalsIgnoreCase("[Join]")) {
			if (!p.hasPermission(Permissions.SIGN_JOIN.getPerm())) {
				p.sendMessage(Game._("noPermission"));
				return;
			}
			
			p.performCommand("spleef join " + line3.toLowerCase());
		} else if (line1.equalsIgnoreCase("[Spleef]") && line2.equalsIgnoreCase("[Start]")) {
			if (!p.hasPermission(Permissions.SIGN_START.getPerm())) {
				p.sendMessage(Game._("noPermission"));
				return;
			}
			
			p.performCommand("spleef start " + line3.toLowerCase());
		} else if (line1.equalsIgnoreCase("[Spleef]") && line2.equalsIgnoreCase("[Leave]")) {
			if (!p.hasPermission(Permissions.SIGN_LEAVE.getPerm())) {
				p.sendMessage(Game._("noPermission"));
				return;
			}
			
			p.performCommand("spleef leave");
		}
		
	}
	
}
