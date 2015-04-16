/*
 * This file is part of HeavySpleef.
 * Copyright (c) 2014-2015 matzefratze123
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
package de.matzefratze123.heavyspleef.core;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.google.common.collect.Lists;

public interface Instantiator<T> {
	
	public T newInstance(Class<T> clazz, InstantitatorArgs args) throws InstantiationException;
	
	public static class InstantitatorArgs {
		
		private List<Object> args; 
		
		public static InstantitatorArgs createArguments(Object... initArgs) {
			InstantitatorArgs args = new InstantitatorArgs();
			args.addArguments(initArgs);
			
			return args;
		}
		
		private InstantitatorArgs() {
			args = Lists.newArrayList();
		}
		
		public InstantitatorArgs addArgument(Object arg) {
			args.add(arg);
			return this;
		}
		
		public InstantitatorArgs addArguments(Object... args) {
			return addArguments(Arrays.asList(args));
		}
		
		public InstantitatorArgs addArguments(Collection<Object> args) {
			args.addAll(args);
			return this;
		}
		
		@SuppressWarnings("unchecked")
		public <T> T get(int i, Class<T> expected) {
			Object result = args.get(i);
			if (!expected.isInstance(result)) {
				throw new IllegalArgumentException("Expected class does not match args output: " + result.getClass().getCanonicalName());
			}
			
			return (T) result;
		}
		
		@SuppressWarnings("unchecked")
		public <T> T getFirst(Class<T> expected) {
			for (int i = 0; i < args.size(); i++) {
				Object result = args.get(i);
				
				if (!expected.isInstance(result)) {
					continue;
				}
				
				return (T) result;
			}
			
			return null;
		}
		
	}

}
