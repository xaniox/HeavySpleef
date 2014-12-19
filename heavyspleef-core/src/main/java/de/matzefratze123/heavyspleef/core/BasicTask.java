package de.matzefratze123.heavyspleef.core;

public interface BasicTask extends Runnable {

	public TaskType getTaskType();
	
	public void start();
	
	public void cancel();
	
	public boolean isSync();
	
	public boolean isRunning();
	
	public enum TaskType {
		
		SYNC_RUN_TASK(true, 0),
		SYNC_DELAYED_TASK(true, 1),
		SYNC_REPEATING_TASK(true, 2),
		ASYNC_RUN_TASK(false, 0),
		ASYNC_DELAYED_TASK(false, 1),
		ASYNC_REPEATING_TASK(false, 2);
		
		private boolean sync;
		private int argsLength;
		
		private TaskType(boolean sync, int argsLength) {
			this.sync = sync;
			this.argsLength = argsLength;
		}
		
		public boolean isSync() {
			return sync;
		}
		
		public int getArgsLength() {
			return argsLength;
		}
		
	}
	
}
