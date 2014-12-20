package de.matzefratze123.heavyspleef.core.i18n;

public interface Messages {
	
	public interface Command {
	
		public static final String PREFIX = "command.";
		
		public static final String PLAYER_ONLY = "player-only";
		public static final String NO_PERMISSION = "no-permission";
		public static final String DESCRIPTION_FORMAT = "description-format"; // $[description]
		public static final String USAGE_FORMAT = "usage-format"; // $[usage]
		
		public static final String GAME_CREATED = PREFIX + "game-created";
		public static final String GAME_ALREADY_EXIST = PREFIX + "game-already-exist";
		public static final String GAME_DOESNT_EXIST = PREFIX + "game-doesnt-exist";
		public static final String GAME_DISCARDED = PREFIX + "game-discarded";
		public static final String GAME_ALREADY_DISABLED = PREFIX + "game-already-disabled";
		public static final String GAME_ALREADY_ENABLED = PREFIX + "game-already-enabled";
		public static final String GAME_DISABLED = PREFIX + "game-disabled";
		public static final String GAME_ENABLED = PREFIX + "game-enabled";
		public static final String GAME_JOIN_IS_DISABLED = PREFIX + "game-join-is-disabled";
		public static final String GAME_STARTED = "game-started";
		public static final String GAME_STOPPED = "game-stopped";
		public static final String PLAYER_NOT_FOUND = PREFIX + "player-not-found"; // $[player]
		public static final String PLAYER_NOT_IN_GAME = PREFIX + "player-not-in-game"; //$[player]
		public static final String PLAYER_KICKED = PREFIX + "player-kicked";
		public static final String NOT_INGAME = "not-ingame";
		
	}
	
	public interface Broadcast {
		
		public static final String PREFIX = "broadcast.";
		
		public static final String PLAYER_LEFT_GAME = PREFIX + "player-left-game"; // $[player]
		public static final String GAME_STARTED = PREFIX + PREFIX + "game-started";
		public static final String GAME_COUNTDOWN_MESSAGE = PREFIX + "game-countdown-message";
		public static final String PLAYER_JOINED_GAME = PREFIX + "player-joined-game"; // $[player]
		
	}
	
	public interface Player {
	
		public static final String PREFIX = "player.";
		
		public static final String PLAYER_LEAVE = PREFIX + "player-leave";
		public static final String PLAYER_KICK = PREFIX + "player-kick"; // $[kicker]; $[message]
		
	}
	
}
