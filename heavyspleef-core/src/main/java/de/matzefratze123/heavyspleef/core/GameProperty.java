package de.matzefratze123.heavyspleef.core;

public enum GameProperty {
	
	INSTANT_BREAK(true),
	PLAY_BLOCK_BREAK(true),
	JOIN_ON_COUNTDOWN(true),
	DISABLE_HUNDER(true),
	DISABLE_PVP(true),
	SUPPRESS_ENTITY_SPAWNING(true),
	DISABLE_ITEM_PICKUP(true),
	DISABLE_ITEM_DROP(true),
	DISABLE_BUILD(true),
	USE_LIQUID_DEATHZONE(true);
	
	private Object defaultValue;
	
	private GameProperty(Object defaultValue) {
		this.defaultValue = defaultValue;
	}
	
	public Object getDefaultValue() {
		return defaultValue;
	}

}
