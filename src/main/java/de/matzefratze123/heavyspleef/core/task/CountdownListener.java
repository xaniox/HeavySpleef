package de.matzefratze123.heavyspleef.core.task;

public interface CountdownListener {
	
	public void onStart();
	
	public void onCancel();
	
	public void onFinish();
	
	public void onTick();
	
	public void onPause();
	
	public void onUnpause();
	
}
