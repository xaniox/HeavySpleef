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
package me.matzefratze123.heavyspleef.utility;

/**
 * An enum containing all the permissions of the plugin...
 * 
 * @author matzefratze123
 */
public enum Permissions {

	// Command
	KICK("heavyspleef.kick"),
	SET_MIN_PLAYERS("heavyspleef.setminplayers"),
	DISABLE("heavyspleef.disable"),
	ENABLE("heavyspleef.enable"),
	HELP_ADMIN("heavyspleef.help.admin"),
	HELP_USER("heavyspleef.help.user"),
	ADD_FLOOR("heavyspleef.addfloor"),
	REMOVE_FLOOR("heavyspleef.removefloor"),
	SET_LOSEPOINT("heavyspleef.setlose"),
	SET_WINPOINT("heavyspleef.setwin"),
	SET_PREGAMEPOINT("heavyspleef.setlobby"),
	ADD_LOSEZONE("heavyspleef.addlose"),
	REMOVE_LOSEZONE("heavyspleef.removelose"),
	JOIN_GAME("heavyspleef.join"),
	LEAVE_GAME("heavyspleef.leave"),
	START_GAME("heavyspleef.start"),
	CREATE_GAME("heavyspleef.create"),
	DELETE_GAME("heavyspleef.delete"),
	SET_JACKPOT("heavyspleef.setjackpot"),
	SET_MONEY("heavyspleef.setmoney"),
	SET_CHANCES("heavyspleef.setchances"),
	SAVE("heavyspleef.save"),
	STOP("heavyspleef.stop"),
	STARTONMINPLAYERS("heavyspleef.startonminplayers"),
	SET_COUNTDOWN("heavyspleef.setcountdown"),
	SET_SHOVEL("heavyspleef.setshovel"),
	STATS("heavyspleef.statistic"),
	STATS_OTHERS("heavyspleef.statistic.others"),
	
	// Bypass
	BUILD_BYPASS("heavyspleef.build.bypass"),
	
	//Selection
	SELECTION("heavyspleef.selection"),
	
	//Sign
	CREATE_SPLEEF_SIGN("heavyspleef.createsign"),
	SIGN_JOIN("heavyspleef.sign.join"),
	SIGN_LEAVE("heavyspleef.sign.leave"),
	SIGN_START("heavyspleef.sign.start");
	
	
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
