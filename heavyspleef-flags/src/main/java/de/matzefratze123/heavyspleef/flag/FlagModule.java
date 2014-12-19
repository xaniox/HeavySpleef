package de.matzefratze123.heavyspleef.flag;

import de.matzefratze123.heavyspleef.core.HeavySpleef;
import de.matzefratze123.heavyspleef.core.flag.FlagRegistry;
import de.matzefratze123.heavyspleef.core.module.SimpleModule;
import de.matzefratze123.heavyspleef.flag.defaults.ShovelsFlag;

public class FlagModule extends SimpleModule {
	
	public FlagModule(HeavySpleef heavySpleef) {
		super(heavySpleef);
	}

	@Override
	public void enable() {
		FlagRegistry registry = getHeavySpleef().getFlagRegistry();
		
		registry.registerFlag(ShovelsFlag.class);
	}

	@Override
	public void disable() {
		// TODO Auto-generated method stub
		
	}

}
