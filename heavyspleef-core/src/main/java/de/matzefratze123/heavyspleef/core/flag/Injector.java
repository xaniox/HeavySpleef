package de.matzefratze123.heavyspleef.core.flag;

import java.lang.reflect.Field;

/**
 * Defines an injector to inject certain values
 * into fields of a class annotated with {@link Inject}
 * 
 * @author matzefratze123
 * @param <T> The type of object this class injects into
 */
public interface Injector<T> {
	
	/**
	 * Called to inject values into fields of a specific class<br><br>
	 * 
	 * <b>Note:</b> The fields given are already accessible so you 
	 * do not have to call {@link Field#setAccessible(boolean)} again
	 * 
	 * @param clazz The class containg the fields
	 * @param injectableFields The fields to be value-injected
	 * @param cookie An optional object given by the caller
	 * 
	 * @throws IllegalArgumentException When the false argument is given
	 * @throws IllegalAccessException When access to the field fails
	 */
	public void inject(T instance, Field[] injectableFields, Object cookie) throws IllegalArgumentException, IllegalAccessException;

}
