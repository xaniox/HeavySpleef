package de.matzefratze123.heavyspleef.objects;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import de.matzefratze123.api.gui.GuiInventory;
import de.matzefratze123.api.gui.GuiInventorySlot;
import de.matzefratze123.heavyspleef.HeavySpleef;
import de.matzefratze123.heavyspleef.command.CommandJoin;
import de.matzefratze123.heavyspleef.core.Game;
import de.matzefratze123.heavyspleef.core.GameManager;
import de.matzefratze123.heavyspleef.core.GameState;
import de.matzefratze123.heavyspleef.core.flag.FlagType;
import de.matzefratze123.heavyspleef.util.I18N;
import de.matzefratze123.heavyspleef.util.Util;

public class InventoryJoinGUI extends GuiInventory {

	private static final char INFINITY_CHAR = '\u221E';
	private static final int LORE_SIZE = 2;
	
	public InventoryJoinGUI() {
		super(HeavySpleef.getInstance());
		
		refresh();
	}
	
	public void refresh() {
		List<Game> games = GameManager.getGames();
		
		int gamesCount = games.size();
		int lines = gamesCount % SLOTS_PER_LINE == 0 ? gamesCount / SLOTS_PER_LINE : gamesCount / SLOTS_PER_LINE + 1;
		
		setLines(lines);
		setTitle(I18N.__("inventory"));
		
		int xIndex = 0, yIndex = 0;
		
		for (Game game : games) {
			GuiInventorySlot slot = getSlot(xIndex, yIndex);
			
			//Store an instance of the game object in the slot
			//so we can easily re-access it
			slot.setValue(game);
			
			ItemStack icon = game.getFlag(FlagType.ICON);
			if (icon == null) {
				icon = new ItemStack(Material.DIAMOND_SPADE);
			}
			
			ChatColor color = game.getGameState() == GameState.JOINABLE || game.getGameState() == GameState.LOBBY ? ChatColor.GREEN : ChatColor.RED;
			String maxPlayers = String.valueOf(game.getFlag(FlagType.MAXPLAYERS) < 2 ? String.valueOf(INFINITY_CHAR) : game.getFlag(FlagType.MAXPLAYERS));
			
			String[] lore = new String[LORE_SIZE];
			
			lore[0] = color + "" + game.getIngamePlayers().size() + ChatColor.DARK_GRAY + ChatColor.BOLD + " / " + ChatColor.RED + maxPlayers;
			lore[1] = ChatColor.AQUA + "" + ChatColor.BOLD + Util.formatMaterialName(game.getGameState().name());
			
			slot.setItem(icon.getType(), icon.getAmount(), icon.getData().getData(),
					color + "" + ChatColor.BOLD + "Join " + ChatColor.GRAY + game.getName(), lore);
			
			if ((xIndex + 1) % SLOTS_PER_LINE == 0) {
				xIndex = 0;
				yIndex++;
			} else {
				xIndex++;
			}
		}
	}

	@Override
	public void onClick(GuiClickEvent event) {
		event.setCancelled(true);
		
		Player player = event.getPlayer();
		SpleefPlayer spleefPlayer = HeavySpleef.getInstance().getSpleefPlayer(player);
		
		GuiInventorySlot slot = event.getSlot();
		
		Object value = slot.getValue();
		if (value == null || !(value instanceof Game)) {
			return;
		}
		
		Game game = (Game) value;
		close(player);
		
		CommandJoin.doFurtherChecks(game, spleefPlayer, null);
	}
	
}
