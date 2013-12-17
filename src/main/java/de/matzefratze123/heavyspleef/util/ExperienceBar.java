package de.matzefratze123.heavyspleef.util;

import org.apache.commons.lang.Validate;
import org.bukkit.entity.Player;

public class ExperienceBar {

	private Player player;
	
	public ExperienceBar(Player player) {
		this.player = player;
	}
	
	public Player getPlayer() {
		return player;
	}
	
	public void setExp(int procent) {
		Validate.isTrue(procent >= 0, "procent is less than 0");
		Validate.isTrue(procent <= 100, "procent is greater than 100");
		float exp = procent / 100.0F;
		player.setExp(exp);
	}
	
}
