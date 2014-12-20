package de.matzefratze123.heavyspleef.persistence;

import de.matzefratze123.heavyspleef.core.HeavySpleef;
import de.matzefratze123.heavyspleef.core.module.SimpleModule;

public class PersistenceModule extends SimpleModule {

	public PersistenceModule(HeavySpleef heavySpleef) {
		super(heavySpleef);
	}

	@Override
	public void enable() {
		HeavySpleef heavySpleef = getHeavySpleef();
		ReadWriteHandler handler = null;
		
		try {
			handler = new CachingReadWriteHandler(heavySpleef, null);
		} catch (Exception e) {
			throw new RuntimeException("Could not enable HeavySpleef persistence module", e);
		}
		
		ForwardingAsyncReadWriteHandler delegateHandler = new ForwardingAsyncReadWriteHandler(handler, heavySpleef.getPlugin(), false);
		heavySpleef.setDatabaseHandler(delegateHandler);
	}

	@Override
	public void disable() {}
	
}
