package de.matzefratze123.heavyspleef.flag.presets;

import org.dom4j.Element;

public abstract class EnumListFlag<T extends Enum<T>> extends ListFlag<T> {

	public abstract Class<T> getEnumType();
	
	@Override
	public void marshalListItem(Element element, T item) {
		element.addText(item.name());
	}

	@Override
	public T unmarshalListItem(Element element) {
		return Enum.valueOf(getEnumType(), element.getText());
	}

}
