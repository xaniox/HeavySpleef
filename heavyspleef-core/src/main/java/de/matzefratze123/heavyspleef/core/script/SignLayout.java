package de.matzefratze123.heavyspleef.core.script;

import java.util.Map;

import org.bukkit.block.Sign;

public interface SignLayout {
	
	public void prepareVariables(Map<String, Object> vars);
	
	public void applyTo(Sign sign);
	
}
