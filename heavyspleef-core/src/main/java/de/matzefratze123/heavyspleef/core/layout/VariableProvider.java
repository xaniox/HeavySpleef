package de.matzefratze123.heavyspleef.core.layout;

import java.util.Set;

import de.matzefratze123.heavyspleef.core.script.Variable;

public interface VariableProvider<T> {

	public void provide(Set<Variable> vars, Set<String> requested, T t);

}
