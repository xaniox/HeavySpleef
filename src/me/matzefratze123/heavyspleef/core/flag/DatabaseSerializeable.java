package me.matzefratze123.heavyspleef.core.flag;

public interface DatabaseSerializeable<T> {

	public String serialize(Object object);
	
	public T deserialize(String str);
	
}
