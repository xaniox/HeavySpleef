package me.matzefratze123.heavyspleef.core.task;

public abstract class AbstractCountdown implements Runnable {

	private int remaining;
	
	protected AbstractCountdown(int start) {
		this.remaining = start;
	}

	@Override
	public void run() {
		if (remaining == 0) {
			onFinish();
		} else if (remaining > 0){//Do pre countdown
			onCount();
			remaining--;
		} else if (remaining < 0) {//Call the interrupt method, something is going false...
			onInterrupt();
		}
	}
	
	public void onFinish() {}
	
	public void onCount() {}
	
	public void onInterrupt() {}
	
	public int getTimeRemaining() {
		return this.remaining;
	}
}
