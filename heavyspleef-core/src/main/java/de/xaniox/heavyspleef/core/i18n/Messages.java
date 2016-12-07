/*
 * This file is part of HeavySpleef.
 * Copyright (c) 2014-2016 Matthias Werning
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
package de.xaniox.heavyspleef.core.i18n;


public interface Messages {
	
	interface Command {
	
		String PREFIX = "command.";
		
		String PLAYER_ONLY = PREFIX + "player-only";
		String NO_PERMISSION = PREFIX + "no-permission";
		String DESCRIPTION_FORMAT = PREFIX + "description-format"; // $[description]
		String USAGE_FORMAT = PREFIX + "usage-format"; // $[usage]
		String UNKNOWN_COMMAND = PREFIX + "unknown-command";
		
		String GAME_CREATED = PREFIX + "game-created"; // $[game]
		String GAME_ALREADY_EXIST = PREFIX + "game-already-exist"; // $[game]
		String GAME_DOESNT_EXIST = PREFIX + "game-doesnt-exist"; // $[game]
		String GAME_DISCARDED = PREFIX + "game-discarded"; // $[game]
		String GAME_ALREADY_DISABLED = PREFIX + "game-already-disabled"; // $[game]
		String GAME_ALREADY_ENABLED = PREFIX + "game-already-enabled"; // $[game]
		String GAME_DISABLED = PREFIX + "game-disabled"; // $[game]
		String GAME_ENABLED = PREFIX + "game-enabled"; // $[game]
		String GAME_JOIN_IS_DISABLED = PREFIX + "game-join-is-disabled"; // $[game]
		String GAME_STARTED = PREFIX + "game-started"; // $[game]
		String GAME_STOPPED = PREFIX + "game-stopped"; // $[game]
		String GAME_IS_INGAME = PREFIX + "game-is-ingame"; // $[game]
		String PLAYER_NOT_FOUND = PREFIX + "player-not-found"; // $[player]
		String PLAYER_NOT_IN_GAME = PREFIX + "player-not-ingame"; //$[player]
		String PLAYER_KICKED = PREFIX + "player-kicked"; // $[player]
		String NOT_INGAME = PREFIX + "not-ingame";
		
		String INVALID_FLAG_INPUT = PREFIX + "invalid-flag-input";
		String FLAG_SET = PREFIX + "flag-set"; // $[flag]
		String PARENT_FLAG_NOT_SET = PREFIX + "parent-flag-not-set"; // $[parent-flag]
		String FLAG_REQUIRES_HOOK = PREFIX + "flag-requires-hook"; // $[hook]
		String FLAG_NOT_PRESENT = PREFIX + "flag-not-present"; // $[flag]
		String FLAG_REMOVED = PREFIX + "flag-removed"; // $[flag]
		
		String CLICK_TO_JOIN = PREFIX + "click-to-join"; // $[game]
		String DEFINE_FULL_WORLDEDIT_REGION = PREFIX + "define-full-worldedit-region";

		String WORLDEDIT_SELECTION_NOT_SUPPORTED = PREFIX +"worldedit-selection-not-supported";
		String FLOOR_ADDED = PREFIX +"floor-added";
		String FLOOR_NOT_PRESENT = PREFIX + "floor-not-present";
		String FLOOR_REMOVED = PREFIX + "floor-removed";
		String REGION_VISUALIZED = PREFIX + "region-visualized";
		
		String DEATHZONE_NOT_PRESENT = PREFIX + "deathzone-not-present";
		String DEATHZONE_REMOVED = PREFIX + "deathzone-removed";
		String DEATHZONE_ADDED = PREFIX + "deathzone-added";
		
		String BLOCK_NOT_A_SIGN = PREFIX + "block-not-a-sign";
		String CLICK_ON_SIGN_TO_ADD_WALL = PREFIX + "click-on-sign-to-add-wall";
		String WALL_ADDED = PREFIX + "wall-added";
		String CLICK_ON_WALL_TO_REMOVE = PREFIX + "click-on-wall-to-remove";
		String WALLS_REMOVED = PREFIX + "walls-removed"; // $[count]
		String NO_WALLS_FOUND = PREFIX + "no-walls-found";
		
		String GAME_INFORMATION = PREFIX + "game-information";
		String NAME = PREFIX + "name";
		String WORLD = PREFIX + "world";
		String GAME_STATE = PREFIX + "game-state";
		String FLAGS = PREFIX + "flags";
		String FLOORS = PREFIX + "floors";
		String DEATH_ZONES = PREFIX + "death-zones";
		String EXTENSIONS = PREFIX + "extensions";
		
		String CUBOID = PREFIX + "cuboid";
		String CYLINDRICAL = PREFIX + "cylindrical";
		String POLYGONAL = PREFIX + "polygonal";

		String ERROR_ON_STATISTIC_LOAD = PREFIX + "error-on-statistic-load";
		String TOP_STATISTICS_HEADER = PREFIX + "top-statistics-header";
		String TOP_STATISTIC_FORMAT = PREFIX + "top-statistics-format";
		String TOP_STATISTICS_FOOTER = PREFIX + "top-statistics-footer";
		String TIME_FORMAT = PREFIX + "time-format";
		
		String STATISTIC_HEADER = PREFIX + "statistic-header";
		String STATISTIC_FORMAT = PREFIX + "statistic-format";
		String STATISTIC_FOOTER = PREFIX + "statistic-footer";

		String STATISTIC_CACHE_CLEARED = PREFIX + "statistic-cache-cleared";
		
		String NO_VOTE_ENABLED = PREFIX + "no-vote-enabled";
		String SUCCESSFULLY_VOTED = PREFIX + "successfully-voted";
		String ALREADY_VOTED = PREFIX + "already-voted";

		String CANNOT_DO_THAT_INGAME = PREFIX + "cannot-do-that-ingame";
		String GAME_DOESNT_ALLOW_SPECTATE = PREFIX + "game-doesnt-allow-spectate";
		
		String SAVING_DATA = PREFIX + "saving-data";
		String ERROR_ON_SAVE = PREFIX + "error-on-save";
		String EVERYTHING_SAVED = PREFIX + "everything-saved";
		String STATISTICS_SAVED = PREFIX + "statistics-saved";
		String GAMES_SAVED = PREFIX + "games-saved";
		
		String HELP_HEADER = PREFIX + "help-header";
		String HELP_RECORD = PREFIX + "help-record";

		String PLUGIN_RELOADED = PREFIX + "plugin-reloaded";
		String GAME_RENAMED = PREFIX + "game-renamed";

		String UPDATER_NOT_FINISHED_YET = PREFIX + "updater-not-finished-yet";
		String NO_UPDATE_AVAILABLE = PREFIX + "no-update-available";
		String STARTING_UPDATE = PREFIX + "starting-update";
		String SUCCESSFULLY_PULLED_UPDATE = PREFIX + "successfully-pulled-update";
		String RESTART_SERVER_TO_UPDATE = PREFIX + "restart-server-to-update";
		String ERROR_ON_UPDATING = PREFIX + "error-on-updating";
		String UPDATING_NOT_ENABLED = PREFIX + "updating-not-enabled";

		String ALREADY_PLAYING = PREFIX + "already-playing";

		String TEAM_COLOR_NOT_FOUND = PREFIX + "team-color-not-found"; // $[color]
		String ADDED_TO_QUEUE = PREFIX + "added-to-queue"; // $[game]
		String REMOVED_FROM_QUEUE = PREFIX + "removed-from-queue"; // $[game]
		String CANNOT_SPECTATE_IN_QUEUE_LOBBY = PREFIX + "cannot-spectate-in-queue";
		String COULD_NOT_ADD_TO_QUEUE = PREFIX + "could-not-add-to-queue";
		String JOIN = PREFIX + "join";
		String ADMIN_INFO = PREFIX + "admin-info";
		String SHOW_ADMIN_INFO = PREFIX + "show-admin-info";
		
		String INVALID_REGEN_INTERVAL = PREFIX + "invalid-regen-interval";
		String INVALID_AUTOSTART = PREFIX + "invalid-autostart";
		String INVALID_COUNTDOWN = PREFIX + "invalid-countdown";
		String INVALID_ENTRY_FEE = PREFIX + "invalid-entry-fee";
		String INVALID_MAX_PLAYERS = PREFIX + "invalid-max-players";
		String INVALID_TEAM_MAX_SIZE = PREFIX + "invalid-team-max-size";
		String INVALID_MIN_PLAYERS = PREFIX + "invalid-min-players";
		String INVALID_TEAM_MIN_SIZE = PREFIX + "invalid-team-min-size";
		String INVALID_SNOWBALL_AMOUNT = PREFIX + "invalid-snowball-amount";
		String INVALID_TIMEOUT = PREFIX + "invalid-timeout";

		String UNSUFFICIENT_FUNDS = PREFIX + "unsufficient-funds";
		String FLAG_DOESNT_EXIST = PREFIX + "flag-doesnt-exist";
		String JOIN_TIMER_STARTED = PREFIX + "join-timer-started";
		String FLAG_CONFLICT = PREFIX + "flag-conflict";
		String AT_LEAST_TWO_TEAMS = PREFIX + "at-least-two-teams";
		String NO_DUPLICATE_TEAMS = PREFIX + "no-duplicate-teams";
		String FUNCTION_ONLY_IN_LOBBY = PREFIX + "function-only-in-lobby";

		String NEED_MC_VERSION_FOR_FLAG = PREFIX + "need-mc-version-for-flag";

		String RATING_ENABLED = PREFIX + "rating-enabled";
		String RATING_DISABLED = PREFIX + "rating-disabled";

		String NO_GAMES_EXIST = PREFIX + "no-games-exist";
		String NEED_STATISTICS_ENABLED = PREFIX + "need-statistics-enabled";

		String ADDON_NOT_ENABLED = PREFIX + "addon-not-enabled";
		String ADDON_ALREADY_ENABLED = PREFIX + "addon-already-enabled";
		String ADDON_UNLOADED = PREFIX + "addon-unloaded";
		String ADDON_LOADED = PREFIX + "addon-loaded";
		String NO_ADDONS_INSTALLED = PREFIX + "no-addons-installed";
		String ADDON_LIST_HEADER = PREFIX + "addon-list-header";
		String ADDON_LIST_ENTRY = PREFIX + "addon-list-entry";
		
		String NEED_AT_LEAST_ONE_NUMBER = PREFIX + "need-at-least-one-number";
		String ALREADY_QUEUED = PREFIX + "already-queued";

		String ADDON_NOT_EXISTING = PREFIX + "addon-not-existing";
		String ADDON_RELOADED = PREFIX + "addon-reloaded";
		
	}
	
	interface Broadcast {
		
		String PREFIX = "broadcast.";
		
		String PLAYER_LEFT_GAME = PREFIX + "player-left-game"; // $[player]
		String GAME_STARTED = PREFIX + "game-started";
		String GAME_STOPPED = PREFIX + "game-stopped";
		String GAME_COUNTDOWN_MESSAGE = PREFIX + "game-countdown-message"; // $[remaining]
		String PLAYER_JOINED_GAME = PREFIX + "player-joined-game"; // $[player]
		String PLAYER_LOST_GAME = PREFIX + "player-lost-game"; // $[player]; $[killer]
		String PLAYER_LOST_GAME_UNKNOWN_KILLER = PREFIX + "player-lost-game-unknown-killer"; // $[player]
		String PLAYER_WON_GAME = PREFIX + "player-won-game"; // $[player]
		
		String GAME_TIMED_OUT = PREFIX + "game-timeout";
		String GAME_TIMEOUT_COUNTDOWN = PREFIX + "game-timeout-countdown"; // $[minutes]; $[seconds]
		
		String TEAM_IS_OUT = PREFIX + "team-is-out"; // $[color]
		String NEED_FLOORS = PREFIX + "need-floors";
		String TOO_FEW_PLAYERS_TEAM = PREFIX + "too-few-players-team"; // $[amount]

		String FLOORS_REGENERATED = PREFIX + "floors-regenerated";
		String NEED_MIN_PLAYERS = PREFIX + "need-min-players";
		String TEAMS_LEFT = PREFIX + "teams-left";
		String TEAM_WON = PREFIX + "team-won";
		String PLAYER_VOTED = PREFIX + "player-voted";
		
		String BROADCAST_GAME_START = PREFIX + "broadcast-game-start";

		String NEED_MORE_PLAYERS = PREFIX + "need-more-players";
        String COUNTDOWN_TITLES_NUMBER_FORMAT = PREFIX + "countdown-titles-number-format";
        String COUNTDOWN_TITLES_GO = PREFIX + "countdown-titles-go";

        String BOSSBAR_PLAYER_JOINED = PREFIX + "bossbar-player-joined";
        String BOSSBAR_PLAYERS_NEEDED = PREFIX + "bossbar-players-needed";
        String BOSSBAR_PLAYER_LEFT = PREFIX + "bossbar-player-left";
        String BOSSBAR_PLAYER_LOST = PREFIX + "bossbar-player-lost";
        String BOSSBAR_COUNTDOWN = PREFIX + "bossbar-countdown";
        String BOSSBAR_PLAYING_ON = PREFIX + "bossbar-playing-on";
        String BOSSBAR_PLAYERS_LEFT = PREFIX + "bossbar-players-left";
        String BOSSBAR_GO = PREFIX + "bossbar-go";

        String WARMUP_STARTED = PREFIX + "warmup-started";

    }
	
	interface Player {
	
		String PREFIX = "player.";
		
		String NOT_A_NUMBER = PREFIX + "not-a-number";
		
		String PLAYER_LEAVE = PREFIX + "player-leave";
		String PLAYER_KICK = PREFIX + "player-kick"; // $[kicker]; $[message]
		String PLAYER_KICK_NO_REASON = PREFIX + "player-kick-no-reason";
		String PLAYER_LOSE = PREFIX + "player-lose";
		String PLAYER_WIN = PREFIX + "player-win";
		String GAME_STOPPED = PREFIX + "game-stopped";
		String ERROR_ON_INVENTORY_LOAD = PREFIX + "error-on-inventory-load";
		String ERROR_NO_LOBBY_POINT_SET = PREFIX + "error-no-lobby-point-set";
		String AVAILABLE_FLAGS = PREFIX + "available-flags"; // $[flags]
		
		String TEAM_MAX_PLAYER_COUNT_REACHED = PREFIX + "team-max-player-count-reached";
		
		String PAID_ENTRY_FEE = PREFIX + "paid-entry-fee"; // $[amount]
		
		String NO_SPECTATE_FLAG = PREFIX + "no-spectate-flag";
		String PLAYER_SPECTATE = PREFIX + "player-spectate";
		String PLAYER_LEAVE_SPECTATE = PREFIX + "player-leave-spectate";
		
		String PLAYER_RECEIVE_JACKPOT = PREFIX + "player-receive-jackpot"; // $[amount]
		
		String MAX_PLAYER_COUNT_REACHED = PREFIX + "max-player-count-reached"; // $[max]

		String TEAM_SELECTOR_TITLE = PREFIX + "team-selector-title";
		String CLICK_TO_JOIN_TEAM = PREFIX + "click-to-join-team";
		String NEED_MIN_PLAYERS = PREFIX + "need-min-players"; // $[amount]

		String NO_SIGN_AVAILABLE = PREFIX + "no-sign-available"; //$[identifier]
		String SIGN_REMOVED = PREFIX + "sign-removed";
		String ANTICAMPING_WARN = PREFIX + "anticamping-warn";
		String ANTICAMPING_TELEPORT = PREFIX + "anticamping-teleport";

		String ITEMREWARD_ITEMS_DROPPED = PREFIX + "itemreward-items-dropped";
		String CANNOT_TELEPORT_IN_QUEUE = PREFIX + "cannot-teleport-in-queue";

		String COMMAND_NOT_ALLOWED = PREFIX + "command-not-allowed";
		String JOIN_CANCELLED_MOVED = PREFIX + "join-cancelled-moved";
		String JOIN_CANCELLED_DAMAGE = PREFIX + "join-cancelled-damage";
		String JOIN_CANCELLED_DEATH = PREFIX + "join-cancelled-death";
		String CANNOT_CHANGE_GAMEMODE = PREFIX + "cannot-change-gamemode";
		String TEAM_CHOOSEN = PREFIX + "team-choosen";

		String LEAVE_SPECTATE_DISPLAYNAME = PREFIX + "leave-spectate-displayname";
		String LEAVE_SPECTATE_LORE = PREFIX + "leave-spectate-lore";

		String LEAVE_QUEUE_DISPLAYNAME = PREFIX + "leave-queue-displayname";
		String LEAVE_QUEUE_LORE = PREFIX + "leave-queue-lore";
		String REMOVED_FROM_QUEUE_DEATH = PREFIX + "removed-from-queue-death";

		String TRACKER = PREFIX + "tracker";
		String TRACKER_LORE = PREFIX + "tracker-lore";
		String TRACKER_INVENTORY_TITLE = PREFIX + "tracker-inventory-title";
		String TRACKER_NOW_TRACKING = PREFIX + "tracker-now-tracking";
		String TRACKER_SKULL_TITLE = PREFIX + "tracker-skull-title";

		String GAINED_RATING = PREFIX + "gained-rating";
		String LOST_RATING = PREFIX + "lost-rating";

		String MAX_PLAYERS_IN_TEAM_REACHED = PREFIX + "max-players-in-team-reached";
		String UPDATE_AVAILABLE = PREFIX + "update-available";
		String RECEIVED_REWARD_PLACE = PREFIX + "received-reward-place";

		String LEAVE_GAME_DISPLAYNAME = PREFIX + "leave-game-displayname";
		String LEAVE_GAME_LORE = PREFIX + "leave-game-lore";

		String SHOVEL = PREFIX + "shovel";
		String SHOVEL_LORE = PREFIX + "shovel-lore";

		String SPLEGG = PREFIX + "splegg";
		String SPLEGG_LORE = PREFIX + "splegg-lore";

		String BOW = PREFIX + "bow";
		String BOW_LORE = PREFIX + "bow-lore";

		String SHEARS = PREFIX + "shears";
		String SHEARS_LORE = PREFIX + "shears-lore";
		
	}
	
	interface Help {
		
		String PREFIX = "help.";
		
		interface Description {
			
			String PREFIX = Help.PREFIX + "description.";
			
			String ADDONS = PREFIX + "addon";
			String CLEARCACHE = PREFIX + "clearcache";
			String CREATE = PREFIX + "create";
			String DELETE = PREFIX + "delete";
			String DISABLE = PREFIX + "disable";
			String DISABLERATING = PREFIX + "disablerating";
			String ENABLE = PREFIX + "enable";
			String ENABLERATING = PREFIX + "enablerating";
			String FLAG = PREFIX + "flag";
			String HELP = PREFIX + "help";
			String INFO = PREFIX + "info";
			String JOIN = PREFIX + "join";
			String KICK = PREFIX + "kick";
			String LEAVE = PREFIX + "leave";
			String LIST = PREFIX + "list";
			String SAVE = PREFIX + "save";
			String START = PREFIX + "start";
			String STATS = PREFIX + "stats";
			String STOP = PREFIX + "stop";
			String ADDFLOOR = PREFIX + "addfloor";
			String RELOAD = PREFIX + "reload";
			String REMOVEFLOOR = PREFIX + "removefloor";
			String SHOWFLOOR = PREFIX + "showfloor";
			String ADDWALL = PREFIX + "addwall";
			String REMOVEWALL = PREFIX + "removewall";
			String RENAME = PREFIX + "rename";
			String ADDDEATHZONE = PREFIX + "adddeathzone";
			String REMOVEDEATHZONE = PREFIX + "removedeathzone";
			String SHOWDEATHZONE = PREFIX + "showdeathzone";
			String UPDATE = PREFIX + "update";
			String VERSION = PREFIX + "version";
			String VOTE = PREFIX + "vote";

			
		}
		
	}
	
	interface Arrays {
		
		String PREFIX = "arrays.";
		
		String TEAM_COLOR_ARRAY = PREFIX + "team-color-array";
		String TIME_UNIT_ARRAY = PREFIX + "time-unit-array";
		String GAME_STATE_ARRAY = PREFIX + "game-state-array";
		String PLACES = PREFIX + "places";
		
	}
	
}
