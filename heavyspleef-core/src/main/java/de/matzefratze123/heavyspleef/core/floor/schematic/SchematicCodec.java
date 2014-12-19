package de.matzefratze123.heavyspleef.core.floor.schematic;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import de.schlichtherle.io.FileInputStream;
import de.schlichtherle.io.FileOutputStream;

public interface SchematicCodec<T> {
	
	public T load(InputStream inputStream) throws CodecException, IOException;
	
	public default T load(File file) throws CodecException, IOException {
		try (InputStream inputStream = new FileInputStream(file)) {
			return load(inputStream);			
		}
	}
	
	public void save(T obj, OutputStream outputStream) throws CodecException, IOException;
	
	public default void save(T obj, File file) throws CodecException, IOException {
		try (OutputStream outputStream = new FileOutputStream(file)) {
			save(obj, outputStream);
		}
	}
	
}
