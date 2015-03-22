package de.matzefratze123.heavyspleef.core.script;

import java.util.Set;

public class IfStatement {
	
	private Condition condition;
	private String ifString;
	private String elseString;
	
	public IfStatement(Condition condition, String ifString, String elseString) {
		this.condition = condition;
		this.ifString = ifString;
		this.elseString = elseString;
	}
	
	public String eval(Set<Variable> vars) {
		boolean result = condition.eval(vars);
		
		return result ? ifString : elseString;
	}
	
	@Override
	public String toString() {
		return ifString + " " + elseString + " " + condition.toString();
	}
	
}
