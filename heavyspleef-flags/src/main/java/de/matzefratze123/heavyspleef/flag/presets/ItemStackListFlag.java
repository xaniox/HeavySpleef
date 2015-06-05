package de.matzefratze123.heavyspleef.flag.presets;

import java.util.Map;

import org.bukkit.inventory.ItemStack;
import org.dom4j.Element;

import com.google.common.collect.Maps;

public abstract class ItemStackListFlag extends ListFlag<ItemStack> {
	
	@Override
	public void marshalListItem(Element element, ItemStack item) {
		ItemStackFlag.marshalSerializeable(element, item);
	}

	@Override
	public ItemStack unmarshalListItem(Element element) {
		Map<String, Object> serializedMap = Maps.newHashMap();
		ItemStackFlag.unmarshalElement(element, serializedMap);
		
		ItemStack stack = ItemStack.deserialize(serializedMap);
		return stack;
	}

	@Override
	public String getListItemAsString(ItemStack item) {
		return item.toString();
	}

	@Override
	public ListInputParser<ItemStack> createParser() {
		//Multiple itemstacks must be added seperately
		return null;
	}

}
