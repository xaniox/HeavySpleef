/*
 * This file is part of HeavySpleef.
 * Copyright (c) 2014-2015 matzefratze123
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package de.matzefratze123.heavyspleef.core.i18n;

public interface Messages {
	
	public interface Command {
	
		public static final String PREFIX = "command.";
		
		public static final String PLAYER_ONLY = PREFIX + "player-only";
		public static final String NO_PERMISSION = PREFIX + "no-permission";
		public static final String DESCRIPTION_FORMAT = PREFIX + "description-format"; // $[description]
		public static final String USAGE_FORMAT = PREFIX + "usage-format"; // $[usage]
		public static final String UNKNOWN_COMMAND = PREFIX + "unknown-command";
		
		public static final String GAME_CREATED = PREFIX + "game-created"; // $[game] TODO
		public static final String GAME_ALREADY_EXIST = PREFIX + "game-already-exist"; // $[game] TODO
		public static final String GAME_DOESNT_EXIST = PREFIX + "game-doesnt-exist"; // $[game] TODO
		public static final String GAME_DISCARDED = PREFIX + "game-discarded"; // $[game] TODO
		public static final String GAME_ALREADY_DISABLED = PREFIX + "game-already-disabled"; // $[game] TODO 
		public static final String GAME_ALREADY_ENABLED = PREFIX + "game-already-enabled"; // $[game] TODO
		public static final String GAME_DISABLED = PREFIX + "game-disabled"; // $[game] TODO
		public static final String GAME_ENABLED = PREFIX + "game-enabled"; // $[game] TODO
		public static final String GAME_JOIN_IS_DISABLED = PREFIX + "game-join-is-disabled"; // $[game] TODO
		public static final String GAME_STARTED = PREFIX + "game-started"; // $[game] TODO
		public static final String GAME_STOPPED = PREFIX + "game-stopped"; // $[game] TODO
		public static final String PLAYER_NOT_FOUND = PREFIX + "player-not-found"; // $[player]
		public static final String PLAYER_NOT_IN_GAME = PREFIX + "player-not-ingame"; //$[player]
		public static final String PLAYER_KICKED = PREFIX + "player-kicked"; // $[player] TODO
		public static final String NOT_INGAME = PREFIX + "not-ingame";
		
	}
	
	public interface Broadcast {
		
		public static final String PREFIX = "broadcast.";
		
		public static final String PLAYER_LEFT_GAME = PREFIX + "player-left-game"; // $[player]
		public static final String GAME_STARTED = PREFIX + "game-started";
		public static final String GAME_STOPPED = PREFIX + "game-stopped";
		public static final String GAME_COUNTDOWN_MESSAGE = PREFIX + "game-countdown-message"; // $[remaining]
		public static final String PLAYER_JOINED_GAME = PREFIX + "player-joined-game"; // $[player]
		public static final String PLAYER_LOST_GAME = PREFIX + "player-lost-game"; // $[player]; $[killer]
		public static final String PLAYER_WON_GAME = PREFIX + "player-won-game"; // $[player]
		
	}
	
	public interface Player {
	
		public static final String PREFIX = "player.";
		
		public static final String PLAYER_LEAVE = PREFIX + "player-leave";
		public static final String PLAYER_KICK = PREFIX + "player-kick"; // $[kicker]; $[message]
		public static final String PLAYER_LOSE = PREFIX + "player-lose";
		public static final String PLAYER_WIN = PREFIX + "player-win";
		public static final String GAME_STOPPED = PREFIX + "game-stopped";
		public static final String ERROR_ON_INVENTORY_LOAD = PREFIX + "error-on-inventory-load";
		public static final String ERROR_NO_LOBBY_POINT_SET = PREFIX + "error-no-lobby-point-set";
		
	}
	
}
