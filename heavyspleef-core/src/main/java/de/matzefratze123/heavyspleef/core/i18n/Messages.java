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
		
		public static final String GAME_CREATED = PREFIX + "game-created"; // $[game]
		public static final String GAME_ALREADY_EXIST = PREFIX + "game-already-exist"; // $[game]
		public static final String GAME_DOESNT_EXIST = PREFIX + "game-doesnt-exist"; // $[game]
		public static final String GAME_DISCARDED = PREFIX + "game-discarded"; // $[game]
		public static final String GAME_ALREADY_DISABLED = PREFIX + "game-already-disabled"; // $[game] 
		public static final String GAME_ALREADY_ENABLED = PREFIX + "game-already-enabled"; // $[game]
		public static final String GAME_DISABLED = PREFIX + "game-disabled"; // $[game]
		public static final String GAME_ENABLED = PREFIX + "game-enabled"; // $[game]
		public static final String GAME_JOIN_IS_DISABLED = PREFIX + "game-join-is-disabled"; // $[game]
		public static final String GAME_STARTED = PREFIX + "game-started"; // $[game]
		public static final String GAME_STOPPED = PREFIX + "game-stopped"; // $[game]
		public static final String PLAYER_NOT_FOUND = PREFIX + "player-not-found"; // $[player]
		public static final String PLAYER_NOT_IN_GAME = PREFIX + "player-not-ingame"; //$[player]
		public static final String PLAYER_KICKED = PREFIX + "player-kicked"; // $[player]
		public static final String NOT_INGAME = PREFIX + "not-ingame";
		
		public static final String INVALID_FLAG_INPUT = PREFIX + "invalid-flag-input";
		public static final String FLAG_SET = PREFIX + "flag-set"; // $[flag]
		public static final String PARENT_FLAG_NOT_SET = PREFIX + "parent-flag-not-set"; // $[parent-flag]
		public static final String FLAG_REQUIRES_HOOK = PREFIX + "flag-requires-hook"; // $[hook]
		public static final String FLAG_NOT_PRESENT = PREFIX + "flag-not-present"; // $[flag]
		public static final String FLAG_REMOVED = PREFIX + "flag-removed"; // $[flag]
		
		public static final String CLICK_TO_JOIN = PREFIX + "click-to-join"; // $[game]
		public static final String DEFINE_FULL_WORLDEDIT_REGION = PREFIX + "define-full-worldedit-region";

		public static final String WORLDEDIT_SELECTION_NOT_SUPPORTED = PREFIX +"worldedit-selection-not-supported";
		public static final String FLOOR_ADDED = PREFIX +"floor-added";
		public static final String FLOOR_NOT_PRESENT = PREFIX + "floor-not-present";
		public static final String FLOOR_REMOVED = PREFIX + "floor-removed";
		public static final String FLOOR_VISUALIZED = PREFIX + "floor-visualized";
		
	}
	
	public interface Broadcast {
		
		public static final String PREFIX = "broadcast.";
		
		public static final String PLAYER_LEFT_GAME = PREFIX + "player-left-game"; // $[player]
		public static final String GAME_STARTED = PREFIX + "game-started";
		public static final String GAME_STOPPED = PREFIX + "game-stopped";
		public static final String GAME_COUNTDOWN_MESSAGE = PREFIX + "game-countdown-message"; // $[remaining]
		public static final String PLAYER_JOINED_GAME = PREFIX + "player-joined-game"; // $[player]
		public static final String PLAYER_LOST_GAME = PREFIX + "player-lost-game"; // $[player]; $[killer]
		public static final String PLAYER_LOST_GAME_UNKNOWN_KILLER = "player-lost-game-unknown-player"; // $[player]
		public static final String PLAYER_WON_GAME = PREFIX + "player-won-game"; // $[player]
		
		public static final String GAME_TIMED_OUT = PREFIX + "game-timeout";
		public static final String GAME_TIMEOUT_COUNTDOWN = PREFIX + "game-timeout-countdown"; // $[minutes]; $[seconds]
		
		public static final String TEAM_IS_OUT = PREFIX + "team-is-out"; // $[color]

		public static final String NEED_FLOORS = "need-floors";

		
	}
	
	public interface Player {
	
		public static final String PREFIX = "player.";
		
		public static final String NOT_A_NUMBER = PREFIX + "not-a-number";
		
		public static final String PLAYER_LEAVE = PREFIX + "player-leave";
		public static final String PLAYER_KICK = PREFIX + "player-kick"; // $[kicker]; $[message]
		public static final String PLAYER_LOSE = PREFIX + "player-lose";
		public static final String PLAYER_WIN = PREFIX + "player-win";
		public static final String GAME_STOPPED = PREFIX + "game-stopped";
		public static final String ERROR_ON_INVENTORY_LOAD = PREFIX + "error-on-inventory-load";
		public static final String ERROR_NO_LOBBY_POINT_SET = PREFIX + "error-no-lobby-point-set";
		public static final String AVAILABLE_FLAGS = PREFIX + "available-flags"; // $[flags]
		
		public static final String TEAM_MAX_PLAYER_COUNT_REACHED = PREFIX + "team-max-player-count-reached";
		
		public static final String PAID_ENTRY_FEE = PREFIX + "paid-entry-fee"; // $[amount]
		
		public static final String NO_SPECTATE_FLAG = PREFIX + "no-spectate-flag";
		public static final String PLAYER_SPECTATE = PREFIX + "player-spectate";
		public static final String PLAYER_LEAVE_SPECTATE = PREFIX + "player-leave-spectate";
		
		public static final String PLAYER_PAY_FEE = PREFIX + "player-pay-fee"; // $[amount]
		public static final String PLAYER_RECEIVE_JACKPOT = PREFIX + "player-receive-jackpot"; // $[amount]
		
		public static final String MAX_PLAYER_COUNT_REACHED = PREFIX + "max-player-count-reached"; // $[max]

		public static final String TEAM_SELECTOR_TITLE = PREFIX + "team-selector-title";
		public static final String CLICK_TO_JOIN_TEAM = PREFIX + "click-to-join-team";
		public static final String NEED_MIN_PLAYERS = PREFIX + "need-min-players"; // $[amount]
		
	}
	
	public interface Arrays {
		
		public static final String PREFIX = "arrays";
		
		public static final String TEAM_COLOR_ARRAY = PREFIX + "team-color-array";
		
	}
	
}