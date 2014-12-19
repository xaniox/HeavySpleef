package de.matzefratze123.heavyspleef.commands.internal;

public interface Transformer<T> {
	
	public T transform(String arg) throws TransformException;
	
}
