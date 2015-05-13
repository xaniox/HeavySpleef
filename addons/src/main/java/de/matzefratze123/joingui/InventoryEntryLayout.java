package de.matzefratze123.joingui;

import java.text.ParseException;
import java.util.List;
import java.util.Set;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import de.matzefratze123.heavyspleef.core.layout.CustomizableLine;
import de.matzefratze123.heavyspleef.core.script.Variable;
import de.matzefratze123.heavyspleef.core.script.VariableSuppliable;

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
		
		String title = this.title.generate(vars);
		List<String> lore = Lists.newArrayList();
		for (CustomizableLine line : this.lore) {
			lore.add(line.generate(vars));
		}
		
		ItemMeta meta = stack.getItemMeta();
		meta.setDisplayName(title);
		meta.setLore(lore);
		
		stack.setItemMeta(meta);
	}

}
