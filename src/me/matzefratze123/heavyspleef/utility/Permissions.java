package me.matzefratze123.heavyspleef.utility;

/**
 * An enum containing all the permissions of the plugin...
 * 
 * @author matzefratze123
 */
public enum Permissions {

	KICK("heavyspleef.kick"),
	SET_MIN_PLAYERS("heavyspleef.setminplayers"),
	BUILD_BYPASS("heavyspleef.build.bypass"),
	SELECTION("heavyspleef.selection"),
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
	CREATE_SPLEEF_SIGN("heavyspleef.createsign"),
	SET_MONEY("heavyspleef.setmoney"),
	SAVE("heavyspleef.save"),
	STOP("heavyspleef.stop");
	
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
