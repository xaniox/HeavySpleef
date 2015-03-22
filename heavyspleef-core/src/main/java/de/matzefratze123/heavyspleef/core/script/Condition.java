package de.matzefratze123.heavyspleef.core.script;

import java.util.Set;

public interface Condition {
	
	public boolean eval(Set<Variable> vars);
	
}
