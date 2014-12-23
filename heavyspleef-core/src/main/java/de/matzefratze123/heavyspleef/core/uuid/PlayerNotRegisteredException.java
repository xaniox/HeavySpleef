package de.matzefratze123.heavyspleef.core.uuid;

public class PlayerNotRegisteredException extends Exception {
	
	private static final long serialVersionUID = 4558758661972329819L;
	
	private String name;
	
	public PlayerNotRegisteredException(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}

}
