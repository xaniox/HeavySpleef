package de.matzefratze123.heavyspleef.core.script;

import java.util.Set;

public class SimpleCondition implements Condition {

	private Object conditionObj;
	
	public SimpleCondition(Object conditionObj) {
		this.conditionObj = conditionObj;
	}
	
	@Override
	public boolean eval(Set<Variable> vars) {
		if (conditionObj instanceof Boolean) {
			return ((Boolean) conditionObj).booleanValue();
		} else if (conditionObj instanceof VariableHolder) {
			VariableHolder holder = (VariableHolder) conditionObj;
			Variable var = null;
			
			for (Variable v : vars) {
				if (v.getName().equals(holder.getName())) {
					var = v;
					break;
				}
			}
			
			SyntaxValidate.notNull(var, "Cannot assign any value to " + holder.getName() + ": Value not found");
			
			return var.asBoolean();
		}
		
		return false;
	}
	
	@Override
	public String toString() {
		return conditionObj.toString();
	}
	
}
