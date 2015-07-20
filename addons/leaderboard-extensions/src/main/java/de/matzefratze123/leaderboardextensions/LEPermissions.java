/*
 * This file is part of addons.
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
package de.matzefratze123.leaderboardextensions;

import de.matzefratze123.heavyspleef.core.Permissions;

public class LEPermissions {
	
	private static final String ADMIN_PREFIX = Permissions.ADMIN_PREFIX;
	
	public static final String PERMISSION_ADD_PODIUM = ADMIN_PREFIX + "addpodium";
	public static final String PERMISSION_REMOVE_PODIUM = ADMIN_PREFIX + "removepodium";
	public static final String PERMISSION_ADD_LEADERBOARD_WALL = ADMIN_PREFIX + "addleaderboardwall";
	public static final String PERMISSION_REMOVE_LEADERBOARD_WALL = ADMIN_PREFIX + "removeleaderboardwall";
	public static final String PERMISSION_ADD_LEADERBOARD_ROW = ADMIN_PREFIX + "addleaderboardrow";
	public static final String PERMISSION_REMOVE_LEADERBOARD_ROW = ADMIN_PREFIX + "removeleaderboardrow";
	
}
