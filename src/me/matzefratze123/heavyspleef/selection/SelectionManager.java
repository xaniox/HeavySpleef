package me.matzefratze123.heavyspleef.selection;

import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * Contains selections for players
 * and methods to modify them
 * 
 * @author matzefratze123
 */
public class SelectionManager {
	
	public SelectionManager() {}
	
	/** The HashMap containing all locations and selections of players **/
	private HashMap<String, Selection> selections = new HashMap<String, Selection>(); 		
	
	/**
	 * Set's the lower point of a selection of a player
	 * 
	 * @param player Player's selection
	 * @param sel Location that should be insert
	 */
	public void setSecondSelection(Player player, Location sel) {
		selections.get(player.getName()).setSecondSel(sel);
	}
	
	/**
	 * Set's the upper point of a selection of a player
	 * 
	 * @param player Player's selection
	 * @param sel Location that should be insert
	 */
	public void setFirstSelection(Player player, Location sel) {
		selections.get(player.getName()).setFirstSel(sel);
	}
	
	/**
	 * Get's the lower point of a selection
	 * 
	 * @param player Selection of player
	 * @return The lower point of the selection
	 */
	public Location getSecondSelection(Player player) {
		return selections.get(player.getName()).getSecondSel();
	}
	
	/**
	 * Get's the upper point of a selection
	 * 
	 * @param player Selection of player
	 * @return The upper point of the selection
	 */
	public Location getFirstSelection(Player player) {
		return selections.get(player.getName()).getFirstSel();
	}
	
	/**
	 * Add's a selection
	 * 
	 * @param player Selection of player
	 * @param locs Selectionlocations
	 */
	public void addSelection(Player player, Location... locs) {
		if (locs.length < 2)
			return;
		selections.put(player.getName(), new Selection(locs[0], locs[1]));
	}
	
	/**
	 * Removes a selection
	 * 
	 * @param player Selection of player
	 */
	protected void removeSelection(Player player) {
		selections.remove(player.getName());
	}
	
	/**
	 * Checks wether the player has a selection
	 * 
	 * @param player The Player to check
	 * @return True if the player has a selection, otherwise false
	 */
	public boolean hasSelection(Player player) {
		return selections.containsKey(player.getName());
	}
	
	/**
	 * Indicates wether the selection is trough worlds
	 * 
	 * @param player The player to check
	 * @return true if the selection is trough worlds
	 */
	public boolean isTroughWorlds(Player player) {
		return getFirstSelection(player).getWorld() != getSecondSelection(player).getWorld();
	}
	

}