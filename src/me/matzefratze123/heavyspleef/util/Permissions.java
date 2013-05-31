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
package me.matzefratze123.heavyspleef.util;

/**
 * An enum containing all the permissions of the plugin...
 * 
 * @author matzefratze123
 */
public enum Permissions {

	// Command
	KICK("heavyspleef.kick"),
	TELEPORT_HUB("heavyspleef.teleporthub"),
	SET_FLAG("heavyspleef.flag"),
	SET_TEAMFLAG("heavyspleef.setteamflag"),
	DISABLE("heavyspleef.disable"),
	ENABLE("heavyspleef.enable"),
	HELP_ADMIN("heavyspleef.help.admin"),
	HELP_USER("heavyspleef.help.user"),
	ADD_FLOOR("heavyspleef.addfloor"),
	REMOVE_FLOOR("heavyspleef.removefloor"),
	ADD_LOSEZONE("heavyspleef.addlose"),
	REMOVE_LOSEZONE("heavyspleef.removelose"),
	JOIN_GAME("heavyspleef.join"),
	JOIN_GAME_OTHERS("heavyspleef.join.target"),
	JOIN_GAME_INV("heavyspleef.join.inventory"),
	LEAVE_GAME("heavyspleef.leave"),
	START_GAME("heavyspleef.start"),
	CREATE_GAME("heavyspleef.create"),
	DELETE_GAME("heavyspleef.delete"),
	RENAME("heavyspleef.rename"),
	SAVE("heavyspleef.save"),
	STOP("heavyspleef.stop"),
	COMMAND_WHITELISTED("heavyspleef.whitelistcommand"),
	STATS("heavyspleef.statistic"),
	STATS_OTHERS("heavyspleef.statistic.others"),
	SET_HUB("heavyspleef.sethub"),
	ADD_SCOREBOARD("heavyspleef.addscoreboard"),
	REMOVE_SCOREBOARD("heavyspleef.removescoreboard"),
	UPDATE_SCOREBOARD("heavyspleef.updatescoreboard"),
	UPDATE_PLUGIN("heavyspleef.updateplugin"),
	ADD_WALL("heavyspleef.addwall"),
	REMOVE_WALL("heavyspleef.removewall"),
	ADD_TEAM("heavyspleef.addteam"),
	REMOVE_TEAM("heavyspleef.removeteam"),
	LIST("heavyspleef.list"),
	RELOAD("heavyspleef.reload"),
	INFO("heavyspleef.info"),
	VOTE("heavyspleef.vote"),
	USE_PORTAL("heavyspleef.useportal"),
	ADD_PORTAL("heavyspleef.addportal"),
	REMOVE_PORTAL("heavyspleef.removeportal"),
	
	// Bypass
	BUILD_BYPASS("heavyspleef.build.bypass"),
	
	//Selection
	SELECTION("heavyspleef.selection"),
	
	//Sign
	CREATE_SPLEEF_SIGN("heavyspleef.createsign"),
	SIGN_JOIN("heavyspleef.sign.join"),
	SIGN_LEAVE("heavyspleef.sign.leave"),
	SIGN_START("heavyspleef.sign.start"),
	SIGN_HUB("heavyspleef.sign.hub"),
	SIGN_VOTE("heavyspleef.sign.vote");
	
	
	private String perm;
	private Permissions(String perm) {
		this.perm = perm;
	}
	
	/**
	 * Get's the permissions string
	 * 
	 * @return The permissions string
	 */
	public String getPerm() {
		return perm;
	}
}
