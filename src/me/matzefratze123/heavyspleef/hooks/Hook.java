package me.matzefratze123.heavyspleef.hooks;

public interface Hook<T> {

	public void hook();
	
	public T getHook();
	
	public boolean hasHook();
	
}
