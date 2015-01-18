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
package de.matzefratze123.heavyspleef.persistence.schematic;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import de.matzefratze123.heavyspleef.persistence.ObjectDatabaseAccessor;

public abstract class SchematicAccessor<T> implements ObjectDatabaseAccessor<T, Void> {

	public abstract void write(OutputStream out, T object) throws IOException, CodecException;
	
	public abstract T read(InputStream in) throws IOException, CodecException;

}
