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
package de.xaniox.heavyspleef.persistence.schematic;

import de.xaniox.heavyspleef.persistence.DatabaseContext;

import java.io.*;
import java.util.Set;

public class SchematicContext extends DatabaseContext<SchematicAccessor<?>> {
	
	public SchematicContext(SchematicAccessor<?>... accessors) {
		super(accessors);
	}
	
	public SchematicContext(Set<SchematicAccessor<?>> accessors) {
		super(accessors);
	}
	
	public <T> void write(File file, T object) throws IOException, CodecException {
		if (!file.exists()) {
			throw new IllegalArgumentException("file does not exist");
		}

        ByteArrayOutputStream out = new ByteArrayOutputStream();
		write(out, object);
        try (OutputStream fileOutputStream = new FileOutputStream(file)) {
            fileOutputStream.write(out.toByteArray());
        }
	}
	
	@SuppressWarnings("unchecked")
	public <T> void write(OutputStream out, T object) throws IOException, CodecException {
		Class<T> clazz = (Class<T>) object.getClass();
		SchematicAccessor<T> accessor = (SchematicAccessor<T>) searchAccessor(clazz);
		
		accessor.write(out, object);
	}
	
	public <T> T read(File file, Class<T> clazz) throws IOException, CodecException {
		InputStream in = new FileInputStream(file);
		
		return read(in, clazz);
	}
	
	@SuppressWarnings("unchecked")
	public <T> T read(InputStream in, Class<T> clazz) throws IOException, CodecException {
		SchematicAccessor<T> accessor = (SchematicAccessor<T>) searchAccessor(clazz);
		
		return accessor.read(in);
	}
	
}