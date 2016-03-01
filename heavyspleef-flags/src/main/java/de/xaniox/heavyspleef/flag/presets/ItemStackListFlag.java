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
package de.xaniox.heavyspleef.flag.presets;

import com.google.common.collect.Maps;
import org.bukkit.inventory.ItemStack;
import org.dom4j.Element;

import java.util.Map;

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