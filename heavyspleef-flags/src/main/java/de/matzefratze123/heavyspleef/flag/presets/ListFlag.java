package de.matzefratze123.heavyspleef.flag.presets;

import java.util.List;

import org.bukkit.entity.Player;

import de.matzefratze123.heavyspleef.core.flag.AbstractFlag;
import de.matzefratze123.heavyspleef.core.flag.InputParseException;

public abstract class ListFlag<T> extends AbstractFlag<List<T>>{

	@Override
	public List<T> parseInput(Player player, String input) throws InputParseException {
		// TODO Auto-generated method stub
		return null;
	}
	
	public void add(T e) {
		getValue().add(e);
	}
	
	public void remove(T e) {
		getValue().remove(e);
	}
	
	public boolean contains(Object obj) {
		return getValue().contains(obj);
	}
	
	public int size() {
		return getValue().size();
	}
	
}
