package me.matzefratze123.heavyspleef.command;

import me.matzefratze123.heavyspleef.core.Cuboid;
import me.matzefratze123.heavyspleef.core.Game;
import me.matzefratze123.heavyspleef.core.GameManager;
import me.matzefratze123.heavyspleef.utility.LocationHelper;
import me.matzefratze123.heavyspleef.utility.Permissions;

import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandRemoveFloor extends HSCommand {

	public CommandRemoveFloor() {
		setMaxArgs(0);
		setMinArgs(0);
		setOnlyIngame(true);
		setPermission(Permissions.REMOVE_FLOOR.getPerm());
		setUsage("/spleef removefloor");
	}
	
	@Override
	public void execute(CommandSender sender, String[] args) {
		Player player = (Player)sender;
		Block block = player.getTargetBlock(null, 50);
		if (block == null) {
			player.sendMessage(_("notLookingAtABlock"));
			return;
		}
		for (Game game : GameManager.getGames()) {
			for (Cuboid floor : game.getFloors()) {
				if (LocationHelper.isInsideRegion(block, floor.getFirstCorner(), floor.getSecondCorner())) {
					if (game.isIngame() || game.isCounting()) {
						player.sendMessage(_("cantRemoveFloorWhileRunning"));
						return;
					}
					floor.remove();
					int id = floor.getId();
					game.removeFloor(id);
					player.sendMessage(_("floorRemoved"));
					return;
				}
			}
		}
	}

}
