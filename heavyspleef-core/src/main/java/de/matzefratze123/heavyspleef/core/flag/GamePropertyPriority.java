package de.matzefratze123.heavyspleef.core.flag;

public @interface GamePropertyPriority {

	Priority value() default Priority.NORMAL;
	
	public enum Priority {
		
		REQUESTED(0),
		LOWEST(1),
		LOW(2),
		NORMAL(3),
		HIGH(4),
		HIGHEST(5);
		
		private int sortInt;
		
		private Priority(int sortInt) {
			this.sortInt = sortInt;
		}
		
		public int getSortInt() {
			return sortInt;
		}
		
	}
	
}
