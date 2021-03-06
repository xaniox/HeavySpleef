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
package de.xaniox.heavyspleef.core.flag;

public class InputParseException extends Exception {
	
	private static final long serialVersionUID = -6850001760533574838L;
	private String malformedInput;
	
	public InputParseException(Throwable throwable) {
		super(throwable);
	}
	
	public InputParseException(String message) {
		super(message);
	}

	public InputParseException(String malformedInput, String message, Throwable cause) {
		super(message, cause);
		
		this.malformedInput = malformedInput;
	}

	public InputParseException(String malformedInput, String message) {
		super(message);
		
		this.malformedInput = malformedInput;
	}

	public InputParseException(String malformedInput, Throwable cause) {
		super(cause);
		
		this.malformedInput = malformedInput;
	}
	
	public String getMalformedInput() {
		return malformedInput;
	}
	
}