package de.matzefratze123.heavyspleef.core;

public enum GameProperty {
	
	INSTANT_BREAK(true),
	JOIN_ON_COUNTDOWN(true);
	
	private Object defaultValue;
	
	private GameProperty(Object defaultValue) {
		this.defaultValue = defaultValue;
	}
	
	public Object getDefaultValue() {
		return defaultValue;
	}

}
