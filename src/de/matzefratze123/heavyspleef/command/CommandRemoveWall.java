package de.matzefratze123.heavyspleef.command;


import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.matzefratze123.heavyspleef.core.Game;
import de.matzefratze123.heavyspleef.core.GameManager;
import de.matzefratze123.heavyspleef.core.SignWall;
import de.matzefratze123.heavyspleef.util.Permissions;

public class CommandRemoveWall extends HSCommand {

	public CommandRemoveWall() {
		setMaxArgs(1);
		setMinArgs(0);
		setOnlyIngame(true);
		setPermission(Permissions.REMOVE_WALL);
		setUsage("/spleef removewall");
	}
	
	@Override
	public void execute(CommandSender sender, String[] args) {
		Player p = (Player)sender;
		
		Block blockLocation = p.getTargetBlock(null, 50);
		for (Game game : GameManager.getGames()) {
			for (SignWall wall : game.getWalls()) {
				if (!wall.contains(blockLocation.getLocation()))
					continue;
				game.removeWall(wall.getId());
				p.sendMessage(_("wallRemoved"));
				return;
			}
		}
			
		p.sendMessage(_("notLookingAtWall"));
	}

	
	
}
