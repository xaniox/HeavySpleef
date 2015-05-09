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
		public static final String GAME_IS_INGAME = PREFIX + "game-is-ingame"; // $[game]
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
		public static final String REGION_VISUALIZED = PREFIX + "deathzone-not-present";
		
		public static final String DEATHZONE_NOT_PRESENT = PREFIX + "deathzone-not-present";
		public static final String DEATHZONE_REMOVED = PREFIX + "deathzone-removed";
		public static final String DEATHZONE_ADDED = PREFIX + "deathzone-added";
		
		public static final String BLOCK_NOT_A_SIGN = PREFIX + "block-not-a-sign";
		public static final String CLICK_ON_SIGN_TO_ADD_WALL = PREFIX + "click-on-sign-to-add-wall";
		public static final String WALL_ADDED = PREFIX + "wall-added";
		public static final String CLICK_ON_WALL_TO_REMOVE = PREFIX + "click-on-wall-to-remove";
		public static final String WALLS_REMOVED = PREFIX + "walls-removed"; // $[count]
		public static final String NO_WALLS_FOUND = PREFIX + "no-walls-found";
		
		public static final String GAME_INFORMATION = PREFIX + "game-information";
		public static final String NAME = PREFIX + "name";
		public static final String WORLD = PREFIX + "world";
		public static final String GAME_STATE = PREFIX + "game-state";
		public static final String FLAGS = PREFIX + "flags";
		public static final String FLOORS = PREFIX + "floors";
		public static final String DEATH_ZONES = PREFIX + "death-zones";
		public static final String EXTENSIONS = PREFIX + "extensions";
		
		public static final String CUBOID = PREFIX + "cuboid";
		public static final String CYLINDRICAL = PREFIX + "cylindrical";
		public static final String POLYGONAL = PREFIX + "polygonal";

		public static final String ERROR_ON_STATISTIC_LOAD = PREFIX + "error-on-statistic-load";
		public static final String TOP_STATISTICS_HEADER = PREFIX + "top-statistics-header";
		public static final String TOP_STATISTIC_FORMAT = PREFIX + "top-statistics-format";
		public static final String TOP_STATISTICS_FOOTER = PREFIX + "top-statistics-footer";
		public static final String TIME_FORMAT = PREFIX + "time-format";
		
		public static final String STATISTIC_HEADER = PREFIX + "statistic-header";
		public static final String STATISTIC_FORMAT = PREFIX + "statistic-format";
		public static final String STATISTIC_FOOTER = PREFIX + "statistic-footer";

		public static final String STATISTIC_CACHE_CLEARED = PREFIX + "statistic-cache-cleared";
		
		public static final String NO_VOTE_ENABLED = PREFIX + "no-vote-enabled";
		public static final String SUCCESSFULLY_VOTED = PREFIX + "successfully-voted";
		public static final String ALREADY_VOTED = PREFIX + "already-voted";

		public static final String CANNOT_DO_THAT_INGAME = PREFIX + "cannot-do-that-ingame";
		public static final String GAME_DOESNT_ALLOW_SPECTATE = PREFIX + "game-doesnt-allow-spectate";
		
		public static final String SAVING_DATA = PREFIX + "saving-data";
		public static final String ERROR_ON_SAVE = PREFIX + "error-on-save";
		public static final String EVERYTHING_SAVED = PREFIX + "everything-saved";
		public static final String STATISTICS_SAVED = PREFIX + "statistics-saved";
		public static final String GAMES_SAVED = PREFIX + "games-saved";
		
		public static final String HELP_HEADER = PREFIX + "help-header";
		public static final String HELP_RECORD = PREFIX + "help-record";

		public static final String PLUGIN_RELOADED = PREFIX + "plugin-reloaded";
		public static final String GAME_RENAMED = PREFIX + "game-renamed";

		public static final String UPDATER_NOT_FINISHED_YET = PREFIX + "updater-not-finished-yet";
		public static final String NO_UPDATE_AVAILABLE = PREFIX + "no-update-available";
		public static final String STARTING_UPDATE = PREFIX + "starting-update";
		public static final String SUCCESSFULLY_PULLED_UPDATE = PREFIX + "successfully-pulled-update";
		public static final String RESTART_SERVER_TO_UPDATE = PREFIX + "restart-server-to-update";
		public static final String ERROR_ON_UPDATING = PREFIX + "error-on-updating";
		public static final String UPDATING_NOT_ENABLED = PREFIX + "updating-not-enabled";
		
	}
	
	public interface Broadcast {
		
		public static final String PREFIX = "broadcast.";
		
		public static final String PLAYER_LEFT_GAME = PREFIX + "player-left-game"; // $[player]
		public static final String GAME_STARTED = PREFIX + "game-started";
		public static final String GAME_STOPPED = PREFIX + "game-stopped";
		public static final String GAME_COUNTDOWN_MESSAGE = PREFIX + "game-countdown-message"; // $[remaining]
		public static final String PLAYER_JOINED_GAME = PREFIX + "player-joined-game"; // $[player]
		public static final String PLAYER_LOST_GAME = PREFIX + "player-lost-game"; // $[player]; $[killer]
		public static final String PLAYER_LOST_GAME_UNKNOWN_KILLER = PREFIX + "player-lost-game-unknown-killer"; // $[player]
		public static final String PLAYER_WON_GAME = PREFIX + "player-won-game"; // $[player]
		
		public static final String GAME_TIMED_OUT = PREFIX + "game-timeout";
		public static final String GAME_TIMEOUT_COUNTDOWN = PREFIX + "game-timeout-countdown"; // $[minutes]; $[seconds]
		
		public static final String TEAM_IS_OUT = PREFIX + "team-is-out"; // $[color]
		public static final String NEED_FLOORS = PREFIX + "need-floors";
		
	}
	
	public interface Player {
	
		public static final String PREFIX = "player.";
		
		public static final String NOT_A_NUMBER = PREFIX + "not-a-number";
		
		public static final String PLAYER_LEAVE = PREFIX + "player-leave";
		public static final String PLAYER_KICK = PREFIX + "player-kick"; // $[kicker]; $[message]
		public static final String PLAYER_KICK_NO_REASON = PREFIX + "player-kick-no-reason";
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

		public static final String NO_SIGN_AVAILABLE = PREFIX + "no-sign-available"; //$[identifier]
		public static final String SIGN_REMOVED = PREFIX + "sign-removed";
		public static final String ANTICAMPING_WARN = PREFIX + "anticamping-warn";
		public static final String ANTICAMPING_TELEPORT = PREFIX + "anticamping-teleport";

		public static final String JOIN_INV_TITLE = PREFIX + "join-inv-title";
		
	}
	
	public interface Help {
		
		public static final String PREFIX = "help.";
		
		public interface Description {
			
			public static final String PREFIX = Help.PREFIX + "description.";
			
			public static final String CLEARCACHE = PREFIX + "clearcache";
			public static final String CREATE = PREFIX + "create";
			public static final String DELETE = PREFIX + "delete";
			public static final String DISABLE = PREFIX + "disable";
			public static final String ENABLE = PREFIX + "enable";
			public static final String FLAG = PREFIX + "flag";
			public static final String HELP = PREFIX + "help";
			public static final String INFO = PREFIX + "info";
			public static final String JOIN = PREFIX + "join";
			public static final String KICK = PREFIX + "kick";
			public static final String LEAVE = PREFIX + "leave";
			public static final String LIST = PREFIX + "list";
			public static final String SAVE = PREFIX + "save";
			public static final String START = PREFIX + "start";
			public static final String STATS = PREFIX + "stats";
			public static final String STOP = PREFIX + "stop";
			public static final String ADDFLOOR = PREFIX + "addfloor";
			public static final String RELOAD = PREFIX + "reload";
			public static final String REMOVEFLOOR = PREFIX + "removefloor";
			public static final String SHOWFLOOR = PREFIX + "showfloor";
			public static final String ADDWALL = PREFIX + "addwall";
			public static final String REMOVEWALL = PREFIX + "removewall";
			public static final String RENAME = PREFIX + "rename";
			public static final String ADDDEATHZONE = PREFIX + "adddeathzone";
			public static final String REMOVEDEATHZONE = PREFIX + "removedeathzone";
			public static final String SHOWDEATHZONE = PREFIX + "showdeathzone";
			public static final String UPDATE = PREFIX + "update";
			public static final String VOTE = PREFIX + "vote";

			
		}
		
	}
	
	public interface Arrays {
		
		public static final String PREFIX = "arrays";
		
		public static final String TEAM_COLOR_ARRAY = PREFIX + "team-color-array";
		
	}
	
}