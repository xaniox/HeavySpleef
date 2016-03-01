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
package de.xaniox.heavyspleef.core.script;

public class Value implements Comparable<Value> {
	
	private Object value;
	
	public Value(Object value) {
		this.value = value;
	}
	
	public Object get() {
		return value;
	}
	
	public boolean isBoolean() {
		return value instanceof Boolean;
	}
	
	public boolean isInt() {
		return value instanceof Integer;
	}
	
	public boolean isDouble() {
		return value instanceof Double;
	}
	
	public boolean isNumber() {
		return isDouble() || isInt();
	}
	
	public boolean isString() {
		return value instanceof String;
	}
	
	public boolean asBoolean() {
		return isBoolean() ? ((Boolean)value).booleanValue() : false;
	}
	
	public int asInt() {
		return isInt() ? ((Integer)value).intValue() : 0;
	}
	
	public double asDouble() {
		return isDouble() ? ((Double)value).doubleValue() : 0.0D;
	}
	
	public String asString() {
		return value.toString();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		
		if (!(obj instanceof Value)) {
			return false;
		}
		
		Value other = (Value) obj;
		if (get().getClass() != other.get().getClass()) {
			return false;
		}
		
		if (isNumber() && other.isNumber()) {
			if (isInt() && other.isInt()) {
				return asInt() == other.asInt();
			} else if (isInt() && other.isDouble()) {
				return asInt() == other.asDouble();
			} else if (isDouble() && other.isInt()) {
				return asDouble() == other.asInt();
			} else if (isDouble() && isDouble()) {
				return asDouble() == other.asDouble();
			}
		}
		
		return get().equals(other.get());
	}

	@Override
	public int compareTo(Value other) {
		if (get().getClass() != other.get().getClass()) {
			return 0;
		}
		
		if (isNumber() && other.isNumber()) {
			if (isInt()) {
				int value = asInt();
				
				if (other.isInt()) {
					int otherValue = other.asInt();
					return value < otherValue ? -1 : value > otherValue ? 1 : 0;
				} else if (other.isDouble()) {
					double otherValue = other.asDouble();
					return value < otherValue ? -1 : value > otherValue ? 1 : 0;
				}
			} else if (isDouble()) {
				double value = asDouble();
				
				if (other.isInt()) {
					int otherValue = other.asInt();
					return value < otherValue ? -1 : value > otherValue ? 1 : 0;
				} else if (other.isDouble()) {
					double otherValue = other.asDouble();
					return value < otherValue ? -1 : value > otherValue ? 1 : 0;
				}
			}
		} else if (isString() && other.isString()) {
			return asString().compareTo(other.asString());
		} else if (isBoolean() && other.isBoolean()) {
			return ((Boolean)get()).compareTo(other.asBoolean());
		}
		
		throw new SyntaxException("Cannot compare " + get().getClass().getSimpleName() + " with " + other.get().getClass().getSimpleName());
	}
	
}