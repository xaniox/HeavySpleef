package de.matzefratze123.heavyspleef.core.script;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;

public class VariablizedString {
	
	private List<Object> messageParts;
	
	public VariablizedString(List<Object> parts) {
		this.messageParts = parts;
	}
	
	public VariableHolder[] getVariables() {
		List<VariableHolder> holders = Lists.newArrayList();
		for (Object part : messageParts) {
			if (!(part instanceof VariableHolder)) {
				continue;
			}
			
			holders.add((VariableHolder)part);
		}
		
		return holders.toArray(new VariableHolder[holders.size()]);
	}
	
	public String eval(Set<Variable> vars) {
		StringBuilder builder = new StringBuilder();
		
		for (int i = 0; i < messageParts.size(); i++) {
			Object part = messageParts.get(i);
			
			if (part instanceof String) {
				builder.append((String) part);
			} else if (part instanceof VariableHolder) {
				VariableHolder holder = (VariableHolder) part;
				
				Value val = null;
				for (Variable var : vars) {
					if (holder.getName().equals(var.getName())) {
						val = var.getValue();
					}
				}
				
				if (val != null) {
					builder.append(val.get());
				}
			}
		}
		
		return builder.toString();
	}
	
}
