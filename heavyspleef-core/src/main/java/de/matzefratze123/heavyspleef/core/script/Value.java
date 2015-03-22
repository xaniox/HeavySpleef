package de.matzefratze123.heavyspleef.core.script;

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
