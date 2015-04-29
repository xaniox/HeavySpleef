package de.matzefratze123.heavyspleef.core.config;

import org.bukkit.configuration.ConfigurationSection;

import lombok.Getter;

@Getter
public class UpdateSection {
	
	private boolean updateChecking;
	private boolean updateCommandEnabled;
	
	public UpdateSection(ConfigurationSection section) {
		this.updateChecking = section.getBoolean("enable-update-check", true);
		this.updateCommandEnabled = section.getBoolean("enable-update-command", true);
	}

}
