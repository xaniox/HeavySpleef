package de.matzefratze123.heavyspleef.core.config;

import org.bukkit.configuration.Configuration;

public abstract class ThrowingConfigurationObject<E extends Exception> extends ConfigurationObject {

	public ThrowingConfigurationObject(Configuration config) {
		super(config);
	}
	
	public ThrowingConfigurationObject(Configuration config, Object[] args) {
		super(config, args);
	}
	
	@Override
	public void inflate(Configuration config, Object... args) {
		try {
			inflateUnsafe(config, args);
		} catch (Exception e) {
			Class<? extends E> clazz = getExceptionClass();
			if (clazz.isInstance(e)) {
				throw new UnsafeException(e);
			} else if (e instanceof RuntimeException) {
				throw (RuntimeException) e;
			}
			
			//This part shouldn't be executed
		}
	}

	public abstract void inflateUnsafe(Configuration config, Object[] args) throws E;
	
	protected abstract Class<? extends E> getExceptionClass();
	
	public static class UnsafeException extends RuntimeException {

		private static final long serialVersionUID = -7780543786839198797L;
		
		public UnsafeException(Throwable cause) {
			super(cause);
		}
		
	}
	
}
