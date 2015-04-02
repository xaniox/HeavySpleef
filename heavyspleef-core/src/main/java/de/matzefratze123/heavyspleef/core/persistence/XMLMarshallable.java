package de.matzefratze123.heavyspleef.core.persistence;

import org.dom4j.Element;

public interface XMLMarshallable {
	
	public void marshal(Element element);
	
	public void unmarshal(Element element);

}
