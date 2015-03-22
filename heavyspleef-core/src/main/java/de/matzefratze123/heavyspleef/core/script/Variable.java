package de.matzefratze123.heavyspleef.core.script;

public class Variable {
	
	private String name;
	private Value value;
	
	public Variable(String name, Value value) {
		this.name = name;
		this.value = value;
	}
	
	public String getName() {
		return name;
	}
	
	public Value getValue() {
		return value;
	}
	
	public boolean isBoolean() {
		return value.isBoolean();
	}

	public boolean isInt() {
		return value.isInt();
	}

	public boolean isDouble() {
		return value.isDouble();
	}

	public boolean isNumber() {
		return value.isNumber();
	}

	public boolean isString() {
		return value.isString();
	}

	public boolean asBoolean() {
		return value.asBoolean();
	}

	public int asInt() {
		return value.asInt();
	}

	public double asDouble() {
		return value.asDouble();
	}

	public String asString() {
		return value.asString();
	}
	
}
