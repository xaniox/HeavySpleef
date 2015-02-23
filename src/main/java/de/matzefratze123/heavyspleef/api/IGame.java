/*
 * HeavySpleef - Advanced spleef plugin for bukkit
 *
 * Copyright (C) 2013-2014 matzefratze123
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.matzefratze123.heavyspleef.api;

import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;

import de.matzefratze123.heavyspleef.core.BroadcastType;
import de.matzefratze123.heavyspleef.core.GameState;
import de.matzefratze123.heavyspleef.core.GameType;
import de.matzefratze123.heavyspleef.core.LoseCause;
import de.matzefratze123.heavyspleef.core.StopCause;
import de.matzefratze123.heavyspleef.core.flag.Flag;
import de.matzefratze123.heavyspleef.core.queue.GameQueue;
import de.matzefratze123.heavyspleef.objects.SpleefPlayer;

public interface IGame {

	/**
	 * Gets the name of this game
	 */
	public String getName();

	/**
	 * Gets the type of this game
	 */
	public GameType getType();

	/**
	 * Gets the world of this game
	 */
	public World getWorld();

	/**
	 * Renames this game
	 * 
	 * @param newName
	 *            The new name of the game
	 */
	public void rename(String newName);

	/**
	 * Gets the components of this game
	 */
	public IGameComponents getComponents();

	/**
	 * Checks if the game contains a block
	 */
	public boolean contains(Location location);

	/**
	 * Starts this game
	 */
	public void start();

	/**
	 * Countdowns this game
	 */
	public void countdown();

	/**
	 * Stops this game
	 */
	public void stop();

	/**
	 * Stops the game with the given cause
	 */
	public void stop(StopCause cause);

	/**
	 * Enables this game
	 */
	public void enable();

	/**
	 * Disables this game
	 */
	public void disable();

	/**
	 * Joins a game
	 * 
	 * @param player
	 *            The player to join
	 */
	public void join(SpleefPlayer player);

	/**
	 * Leaves a game
	 * 
	 * @param player
	 *            The player to leave
	 */
	public void leave(SpleefPlayer player);

	/**
	 * Leaves a game with the given cause
	 * 
	 * @param player
	 *            The player to leave
	 * @param cause
	 *            The cause of the leave
	 */
	public void leave(SpleefPlayer player, LoseCause cause);

	/**
	 * Checks if a player is ingame
	 */
	public boolean hasPlayer(SpleefPlayer player);

	/**
	 * Gets all ingame players
	 */
	public List<SpleefPlayer> getIngamePlayers();

	/**
	 * Gets all out players
	 */
	public List<OfflinePlayer> getOutPlayers();

	/**
	 * Gets a flag
	 * 
	 * @param flag
	 *            The flag to get
	 * @return The value of the flag
	 */
	public <T extends Flag<V>, V> V getFlag(T flag);

	/**
	 * Sets a flag
	 * 
	 * @param flag
	 *            The flag to set
	 * @param value
	 *            The flags generic value (e.g. a boolean, when a boolean flag
	 *            is given)
	 */
	public <T extends Flag<V>, V> void setFlag(T flag, V value);

	/**
	 * Checks if this game has a flag
	 * 
	 * @param flag
	 *            The flag to check
	 */
	public boolean hasFlag(Flag<?> flag);

	/**
	 * Gets all flags
	 */
	public Map<Flag<?>, Object> getFlags();

	/**
	 * Checks if the player can spleef at the given location
	 * 
	 * @param player
	 *            The player to check
	 * @param location
	 *            The location to check
	 */
	public boolean canSpleef(SpleefPlayer player, Location location);

	/**
	 * Broadscasts a message to the game
	 * 
	 * @param message
	 *            The message to send
	 * @param type
	 *            The BroadcastType of this broadcast
	 */
	public void broadcast(String message, BroadcastType type);

	/**
	 * Broadscasts a message to the game
	 * 
	 * @param message
	 */
	public void broadcast(String message);

	/**
	 * Gets the state of this game
	 */
	public GameState getGameState();

	/**
	 * Sets the state of this game
	 * 
	 * @param state
	 */
	public void setGameState(GameState state);

	/**
	 * Gets the queue of this game
	 */
	public GameQueue getQueue();

}
