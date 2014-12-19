package de.matzefratze123.heavyspleef.core.event;

public interface Cancellable {
	
	public void setCancelled(boolean cancel);
	
	public boolean isCancelled();
	
}
