package de.matzefratze123.heavyspleef.core.layout;

public class VariableNotAvailableException extends RuntimeException {

	private static final long serialVersionUID = 6860633367196015521L;
	
	private String var;
	
	public VariableNotAvailableException(String var) {
		this.var = var;
	}
	
	@Override
	public String getMessage() {
		return "Variable \"" + var + "\" is not available in this context";
	}

}
