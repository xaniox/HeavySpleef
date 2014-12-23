package de.matzefratze123.heavyspleef.core.flag;

import java.util.List;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import de.matzefratze123.heavyspleef.core.GameProperty;
import de.matzefratze123.heavyspleef.core.event.SpleefListener;

public abstract class AbstractFlag<T> implements Listener, SpleefListener {
	
	private T value;
	
	public abstract void defineGameProperties(Map<GameProperty, Object> properties);
	
	public abstract boolean hasGameProperties();
	
	public abstract boolean hasBukkitListenerMethods();
	
	public abstract void getDescription(List<String> description);
	
	public abstract T parseInput(Player player, String input) throws InputParseException;
	
	public T getValue() {
		return value;
	}
	
	public void setValue(T value) {
		this.value = value;
	}

}
