package de.matzefratze123.heavyspleef.core.script;

public class VariableHolder {

	private String name;
	
	public VariableHolder(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	@Override
	public String toString() {
		return name;
	}
	
}
