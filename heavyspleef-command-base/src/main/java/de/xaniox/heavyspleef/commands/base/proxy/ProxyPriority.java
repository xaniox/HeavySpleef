/*
 * This file is part of HeavySpleef.
 * Copyright (c) 2014-2016 Matthias Werning
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package de.xaniox.heavyspleef.commands.base.proxy;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Comparator;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ProxyPriority {
	
	public Priority value() default Priority.NORMAL;
	
	public enum Priority {

		LOWEST(0),
		LOW(1),
		NORMAL(2),
		HIGH(3),
		HIGHEST(4),
		MONITOR(5);
		
		private int priorityInt;
		
		private Priority(int priorityInt) {
			this.priorityInt = priorityInt;
		}
		
		int getPriorityInt() {
			return priorityInt;
		}
		
		public static class PriorityComparator implements Comparator<Priority> {

			@Override
			public int compare(Priority o1, Priority o2) {
				return Integer.valueOf(o1.priorityInt).compareTo(o2.priorityInt);
			}
			
		}
		
	}
	
}