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

public class Variable {
	
	private String name;
	private Value value;
	
	public Variable(String name, Value value) {
		this.name = name;
		this.value = value;
	}
	
	public Variable(String name, Object value) {
		this(name, new Value(value));
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