package me.matzefratze123.heavyspleef.command;

import me.matzefratze123.heavyspleef.HeavySpleef;
import me.matzefratze123.heavyspleef.core.Game;
import me.matzefratze123.heavyspleef.core.GameManager;
import me.matzefratze123.heavyspleef.selection.SelectionManager;
import me.matzefratze123.heavyspleef.utility.LocationHelper;
import me.matzefratze123.heavyspleef.utility.Permissions;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandAddFloor extends HSCommand {

	public CommandAddFloor() {
		setMaxArgs(1);
		setMinArgs(0);
		setOnlyIngame(true);
		setPermission(Permissions.ADD_FLOOR.getPerm());
		setUsage("/spleef addfloor [Block-ID[:DATA]|given]");
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
		while (g.hasFloor(id))
			id++;
		if (args.length == 1 && args[0].equalsIgnoreCase("given")) {
			g.addFloor(selManager.getFirstSelection(player), selManager.getSecondSelection(player), id, -1, (byte)0, false, true);
			player.sendMessage(_("floorCreated"));
			return;
		}
		
		if (args.length == 1) {
			
			int blockID = 35;
			byte blockData = 0;
			
			try {
				String[] split = args[0].split(":"); 
				
				blockID = Integer.parseInt(split[0]);
				if (blockID == 0) {
					player.sendMessage(_("cantConsistOfAir"));
					return;
				}
				Material mat = Material.getMaterial(blockID);
				if (mat == null) {
					player.sendMessage(_("invalidBlockID"));
					return;
				}
				
				if (split.length > 1) {
					blockData = Byte.parseByte(split[1]);
					if (blockData > Byte.MAX_VALUE || blockData < Byte.MIN_VALUE) {
						player.sendMessage(_("toBigData"));
						return;
					}
				}
				g.addFloor(loc1, loc2, id, blockID, blockData, false, false);
			} catch (NumberFormatException e) {
				player.sendMessage(_("blockIDIsntNumber"));
				return;
			}
		} else
			g.addFloor(selManager.getFirstSelection(player), selManager.getSecondSelection(player), id, 35, (byte)0, true, false);
		player.sendMessage(_("floorCreated"));
	}

}
