/**
 *   HeavySpleef - The simple spleef plugin for bukkit
 *   
 *   Copyright (C) 2013 matzefratze123
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package de.matzefratze123.heavyspleef.api;

import java.util.Map;

import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import de.matzefratze123.heavyspleef.core.BroadcastType;
import de.matzefratze123.heavyspleef.core.Game;
import de.matzefratze123.heavyspleef.core.GameManager;
import de.matzefratze123.heavyspleef.core.GameType;
import de.matzefratze123.heavyspleef.core.LoseCause;
import de.matzefratze123.heavyspleef.core.Team;
import de.matzefratze123.heavyspleef.core.flag.Flag;
import de.matzefratze123.heavyspleef.core.region.Floor;
import de.matzefratze123.heavyspleef.core.region.FloorType;


/**
 * Represents a game with the specified data
 * 
 * @author matzefratze123
 */
public class GameData {

	private Game game;
	
	public GameData(Game game) {
		this.game = game;
	}
	
	//Only accessable for classes in this package...
	//Use GameAPI for access to a gamedata
	protected GameData(String name) {
		this.game = GameManager.getGame(name);
	}
	
	public Game getHandle() {
		return this.game;
	}
	
	/**
	 * Gets the name of this game
	 */
	public String getName() {
		return game.getName();
	}
	
	/**
	 * Gets the type of this game
	 */
	public GameType getType() {
		return game.getType();
	}
	
	/**
	 * Checks if the game contains a location
	 * 
	 * @param location The location to check
	 * @return True if the game contains this location
	 */
	public boolean contains(Location location) {
		return game.contains(location);
	}
	
	/**
	 * Checks if the game contains a block
	 * @see #contains(Location)
	 * 
	 * @param block Block to check
	 * @return True if the game contains the block
	 */
	public boolean contains(Block block) {
		return game.contains(block);
	}
	
	/**
	 * Broadcasts a message to this game
	 * 
	 * @param msg The message to send
	 * @param type The announcement type of this game
	 * 
	 * @see de.matzefratze123.heavyspleef.core.BroadcastType
	 */
	public void broadcast(String msg, BroadcastType type) {
		game.broadcast(msg, type);
	}
	
	/**
	 * Adds a randomwool floor to this game
	 * If the game is cuboid there are two locations needed
	 * If the game is cylinder, one location is needed
	 * @see #getType()
	 * 
	 * @param locations
	 * @return The floor that was created. If the game is ingame
	 *         or the locations length was to low it returns null
	 *         and doesn't adds the floor!
	 */
	public Floor addRandomWoolFloor(Location... locations) {
		if (locations.length < 1 || (getType() == GameType.CYLINDER && locations.length < 2))
			return null;
		
		return game.getFloor(game.addFloor(0, (byte)0, FloorType.RANDOMWOOL, locations));
	}
	
	/**
	 * Adds a specified floor with a blockid and data to this game
	 * If the game is cuboid there are two locations needed
	 * If the game is cylinder, one location is needed
	 * @see #getType()
	 * 
	 * @param locations
	 * @return The floor that was created. If the game is ingame
	 *         or the locations length was to low it returns null
	 *         and doesn't adds the floor!
	 */
	public Floor addSpecifiedFloor(Material material, byte data, Location... locations) {
		if (locations.length < 1 || (getType() == GameType.CYLINDER && locations.length < 2))
			return null;
		
		return game.getFloor(game.addFloor(material.getId(), data, FloorType.SPECIFIEDID, locations));
	}
	
	/**
	 * Adds a specified floor with a blockid and data to this game
	 * If the game is cuboid there are two locations needed
	 * If the game is cylinder, one location is needed
	 * @see #getType()
	 * 
	 * @param locations
	 * @return The floor that was created. If the game is ingame
	 *         or the locations length was to low it returns null
	 *         and doesn't adds the floor!
	 */
	public Floor addManuallyBuildedFloor(Location... locations) {
		if (locations.length < 1 || (getType() == GameType.CYLINDER && locations.length < 2))
			return null;
		
		return game.getFloor(game.addFloor(0, (byte)0, FloorType.GIVENFLOOR, locations));
	}
	
	/**
	 * Checks if this game has a floor with the given id
	 * 
	 * @param id The ID to check
	 */
	public boolean hasFloor(int id) {
		return game.hasFloor(id);
	}
	
	/**
	 * Removes a floor with the given id
	 * 
	 * @param id The floor ID
	 * @return True if the floor was successfully removed
	 */
	public boolean removeFloor(int id) {
		if (!hasFloor(id))
			return false;
		game.removeFloor(id);
		return true;
	}
	
	/**
	 * Removes all floors at the given location
	 * 
	 * @return True if the floor was successfully removed
	 */
	public boolean removeFloor(Location location) {
		boolean removed = false;
		
		for (Floor floor : game.getFloors()) {
			if (floor.contains(location)) {
				game.removeFloor(floor.getId());
				removed = true;
			}
		}
		
		return removed;
	}
	
	/**
	 * Joins the game with the given player
	 * If you want to use the team parameter
	 * be sure to use an game's team instance
	 * 
	 * getTeam(ChatColor color)
	 * 
	 * @see de.matzefratze123.heavyspleef.core.Game#getTeam(ChatColor)
	 * 
	 * @param player The player to join
	 * @param team The team to join
	 */
	public void join(Player player, Team team) {
		Validate.notNull(player, "Player cannot be null");
		game.join(player, team);
	}
	
	/**
	 * Joins the game with the given player
	 * 
	 * @param player The player to join
	 */
	public void join(Player player) {
		Validate.notNull(player, "Player cannot be null");
		game.join(player);
	}
	
	/**
	 * Checks if a player is in a game
	 * 
	 * @param player The player to check
	 * @return True if the player is in the arena (lobby, counting or ingame)
	 */
	public boolean isActive(Player player) {
		Validate.notNull(player, "Player cannot be null");
		return game.hasPlayer(player);
	}
	
	/**
	 * Kicks a player from the game
	 * 
	 * @param player The player that should be kicked
	 */
	public void kick(Player player) {
		Validate.notNull(player, "Player cannot be null");
		game.leave(player, LoseCause.KICK);
	}
	
	/**
	 * Removes a player with the given cause
	 * 
	 * @param player The player to leave
	 */
	public void leave(Player player, LoseCause losecause) {
		Validate.notNull(player, "Player cannot be null");
		if (!isIngame() && !isCounting())
			throw new IllegalStateException("Player " + player.getName() + " cannot lose while game isn't ingame");
		if (!isActive(player))
			throw new IllegalStateException("Player " + player.getName() + " isn't ingame");
		game.leave(player, losecause);
	}
	
	/**
	 * Stops the game with the given StopCause
	 * @see StopCause
	 */
	public void stop() {
		if (!game.isIngame() && !game.isCounting())
			return;
		game.stop();
	}
	
	/**
	 * Starts the countdown of this game
	 */
	public void countdown() {
		if (game.isIngame() || game.isCounting())
			return;
		game.countdown();
	}
	
	/**
	 * Starts the game instantly without a countdown
	 */
	public void start() {
		if (game.isIngame())
			return;
		game.start();
	}
	
	/**
	 * Disables this game
	 */
	public void disable() {
		game.disable(null);
	}
	
	/**
	 * Enables this game
	 */
	public void enable() {
		game.enable(null);
	}
	
	/**
	 * Indicates if this player can spleef at the given location
	 * 
	 * @param player The player to check
	 * @param location The location to check
	 */
	public boolean canSpleef(Player player, Location location) {
		return game.canSpleef(location, player);
	}
	
	/**
	 * Regenerates all floors of this game
	 */
	public void regenerate() {
		game.regen();
	}
	
	/**
	 * Checks if this game is ingame
	 */
	public boolean isIngame() {
		return game.isIngame();
	}
	
	/**
	 * Checks if this game is counting
	 */
	public boolean isCounting() {
		return game.isCounting();
	}
	
	/**
	 * Checks if this game is in the lobby mode
	 */
	public boolean isLobby() {
		return game.isPreLobby();
	}
	
	/**
	 * Gets the value of the given flag</br>
	 * The return depends on the flagtype</br></br>
	 * 
	 * <b>Example:</b>
	 * <BLOCKQUOTE>getFlag(FlagType.COUNTDOWN);</br>
	 * This would get the countdown value</BLOCKQUOTE>
	 * 
	 * @param flag The flag to get...
	 * @return The value of the flag (depends on the flag)
	 * 
	 */
	public <T extends Flag<V>, V> V getFlag(T flag) {
		return game.getFlag(flag);
	}
	
	/**
	 * Sets a gameflag with the given value</br></br>
	 * 
	 * <b>Example:</b>
	 * <BLOCKQUOTE>setFlag(FlagType.COUNTDOWN, 15);</br>
	 * This would set the countdown of the game to 15 seconds</BLOCKQUOTE>
	 * 
	 * @param flag The flag to set
	 * @param value The value of the flag
	 */
	public <T extends Flag<V>, V> void setFlag(T flag, V value) {
		game.setFlag(flag, value);
	}
	
	/**
	 * Sets all flags of this game
	 * 
	 * @param flags A map with flags and the values
	 */
	public void setFlags(Map<Flag<?>, Object> flags) {
		game.setFlags(flags);
	}
	
	/**
	 * Gets all flags of this game
	 * 
	 * @return A map, containing flags with their specified value
	 */
	public Map<Flag<?>, Object> getFlags() {
		return game.getFlags();
	}
	
}
