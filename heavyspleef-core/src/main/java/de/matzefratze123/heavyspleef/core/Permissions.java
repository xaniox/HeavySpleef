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
package de.matzefratze123.heavyspleef.core;

public interface Permissions {
	
	public static final String PREFIX = "heavyspleef.";
	public static final String PLAYER_PREFIX = PREFIX + "player.";
	public static final String ADMIN_PREFIX = PREFIX + "admin.";
	
	public static final String PERMISSION_VIP = PREFIX + "vip";
	public static final String PERMISSION_CLEAR_CACHE = ADMIN_PREFIX + "clearcache";
	public static final String PERMISSION_CREATE = ADMIN_PREFIX + "create";
	public static final String PERMISSION_DELETE = ADMIN_PREFIX + "delete";
	public static final String PERMISSION_DISABLE = ADMIN_PREFIX + "disable";
	public static final String PERMISSION_DISABLE_RATING = ADMIN_PREFIX + "disablerating";
	public static final String PERMISSION_ENABLE = ADMIN_PREFIX + "enable";
	public static final String PERMISSION_ENABLE_RATING = ADMIN_PREFIX + "enablerating";
	public static final String PERMISSION_FLAG = ADMIN_PREFIX + "flag";
	public static final String PERMISSION_HELP = PLAYER_PREFIX + "help";
	public static final String PERMISSION_INFO = ADMIN_PREFIX + "info";
	public static final String PERMISSION_JOIN = PLAYER_PREFIX + "join";
	public static final String PERMISSION_KICK = ADMIN_PREFIX + "kick";
	public static final String PERMISSION_LEAVE = PLAYER_PREFIX + "leave";
	public static final String PERMISSION_LIST = PLAYER_PREFIX + "list";
	public static final String PERMISSION_RELOAD = ADMIN_PREFIX + "reload";
	public static final String PERMISSION_RENAME = ADMIN_PREFIX + "rename";
	public static final String PERMISSION_SAVE = ADMIN_PREFIX + "save";
	public static final String PERMISSION_START = PLAYER_PREFIX + "start";
	public static final String PERMISSION_STATS = PLAYER_PREFIX + "stats";
	public static final String PERMISSION_STOP = ADMIN_PREFIX + "stop";
	public static final String PERMISSION_UPDATE = ADMIN_PREFIX + "update";
	public static final String PERMISSION_ADD_DEATHZONE = ADMIN_PREFIX + "adddeathzone";
	public static final String PERMISSION_REMOVE_DEATHZONE = ADMIN_PREFIX + "removedeathzone";
	public static final String PERMISSION_SHOW_DEATHZONE = ADMIN_PREFIX + "showdeathzone";
	public static final String PERMISSION_ADD_FLOOR = ADMIN_PREFIX + "addfloor";
	public static final String PERMISSION_REMOVE_FLOOR = ADMIN_PREFIX + "removefloor";
	public static final String PERMISSION_SHOW_FLOOR = ADMIN_PREFIX + "showfloor";
	public static final String PERMISSION_ADD_WALL = ADMIN_PREFIX + "addwall";
	public static final String PERMISSION_REMOVE_WALL = ADMIN_PREFIX + "removewall";
	public static final String PERMISSION_CREATE_SIGN = ADMIN_PREFIX + "createsign";
	public static final String PERMISSION_REMOVE_SIGN = ADMIN_PREFIX + "removesign";
	public static final String PERMISSION_SPECTATE = PLAYER_PREFIX + "spectate";
	public static final String PERMISSION_VOTE = PLAYER_PREFIX + "vote";
	

}
