package de.matzefratze123.heavyspleef.core;

public enum GameProperty {
	
	INSTANT_BREAK(true),
	PLAY_BLOCK_BREAK(true),
	JOIN_ON_COUNTDOWN(true),
	DISABLE_HUNGER(true),
	DISABLE_PVP(true),
	DISABLE_DAMAGE(true),
	DISABLE_ITEM_PICKUP(true),
	DISABLE_ITEM_DROP(true),
	DISABLE_BUILD(true),
	USE_LIQUID_DEATHZONE(true),
	BROADCAST_RADIUS(30);
	
	private Object defaultValue;
	
	private GameProperty(Object defaultValue) {
		this.defaultValue = defaultValue;
	}
	
	public Object getDefaultValue() {
		return defaultValue;
	}

}
