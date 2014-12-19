package de.matzefratze123.heavyspleef.core.module;

import de.matzefratze123.heavyspleef.core.HeavySpleef;

public abstract class SimpleModule implements Module {

	private HeavySpleef heavySpleef;
	
	public SimpleModule(HeavySpleef heavySpleef) {
		this.heavySpleef = heavySpleef;
	}
	
	@Override
	public HeavySpleef getHeavySpleef() {
		return heavySpleef;
	}

}
