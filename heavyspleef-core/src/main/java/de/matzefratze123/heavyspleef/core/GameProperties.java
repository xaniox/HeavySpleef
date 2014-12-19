package de.matzefratze123.heavyspleef.core;

import java.util.Map;

import com.google.common.collect.Maps;

public class GameProperties {
	
	private Map<GameProperty, Object> properties;
	
	public GameProperties() {
		this.properties = Maps.newHashMap();
		
		setDefaults();
	}
	
	public void setDefaults() {
		properties.put(GameProperty.INSTANT_BREAK, true);
	}
	
}
