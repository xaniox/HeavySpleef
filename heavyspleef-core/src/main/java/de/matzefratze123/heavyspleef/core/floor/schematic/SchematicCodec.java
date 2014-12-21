package de.matzefratze123.heavyspleef.core.floor.schematic;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface SchematicCodec<T> {
	
	public T load(InputStream inputStream) throws CodecException, IOException;
	
	public T load(File file) throws CodecException, IOException;
	
	public void save(T obj, OutputStream outputStream) throws CodecException, IOException;
	
	public void save(T obj, File file) throws CodecException, IOException;
	
}
