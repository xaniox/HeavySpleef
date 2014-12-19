package de.matzefratze123.heavyspleef.commands.internal;

public interface Instantiator {
	
	public <T> T instantiate(Class<T> clazz) throws InstantiationException;
	
}
