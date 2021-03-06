/*
 * This file is part of addons.
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
package de.xaniox.joingui;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import de.xaniox.heavyspleef.core.layout.CustomizableLine;
import de.xaniox.heavyspleef.core.script.Variable;
import de.xaniox.heavyspleef.core.script.VariableSuppliable;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.text.ParseException;
import java.util.List;
import java.util.Set;

public class InventoryEntryLayout {
	
	private CustomizableLine title;
	private List<CustomizableLine> lore;
	
	public InventoryEntryLayout(String title, List<String> lore) throws ParseException {
		this.title = new CustomizableLine(title);
		this.lore = Lists.newArrayList();
		
		for (int i = 0; i < lore.size(); i++) {
			String loreLine = lore.get(i);
			this.lore.add(new CustomizableLine(loreLine));
		}
	}
	
	public void inflate(ItemStack stack, VariableSuppliable suppliable) {
		Set<Variable> vars = Sets.newHashSet();
		Set<String> requestedVars = Sets.newHashSet();
		
		title.getRequestedVariables(requestedVars);
		for (CustomizableLine line : lore) {
			line.getRequestedVariables(requestedVars);
		}
		
		suppliable.supply(vars, requestedVars);
		
		ItemMeta meta = stack.getItemMeta();

		if (!meta.hasDisplayName()) {
			//Don't overwrite display names that have been explicitly set
			String title = this.title.generate(vars);
			meta.setDisplayName(title);
		}

		if (!meta.hasLore()) {
			//Don't overwrite lores that have been explicitly set
			List<String> lore = Lists.newArrayList();
			for (CustomizableLine line : this.lore) {
				lore.add(line.generate(vars));
			}
			meta.setLore(lore);
		}
		
		stack.setItemMeta(meta);
	}

}