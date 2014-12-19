package de.matzefratze123.heavyspleef.core.config;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.bukkit.configuration.Configuration;

public enum ConfigType {
	
	DEFAULT_CONFIG("config.yml", "/config.yml", DefaultConfig.class),
	DATABASE_CONFIG("database-config.yml", "/database-config.yml", DatabaseConfig.class);

	private String destinationFileName;
	private String classpathResourceName;
	private Class<? extends ConfigurationObject> configClass;
	
	private ConfigType(String destinationFileName, String classpathResourceName, Class<? extends ConfigurationObject> configClass) {
		this.destinationFileName = destinationFileName;
		this.classpathResourceName = classpathResourceName;
		this.configClass = configClass;
	}
	
	public String getDestinationFileName() {
		return destinationFileName;
	}
	
	public String getClasspathResourceName() {
		return classpathResourceName;
	}
	
	public ConfigurationObject newConfigInstance(Configuration configuration) {
		boolean fallback = false;
		Constructor<? extends ConfigurationObject> constructor;
		
		try {
			constructor = configClass.getConstructor(Configuration.class);
		} catch (NoSuchMethodException nsme) {
			try {
				constructor = configClass.getConstructor();
				fallback = true;
			} catch (NoSuchMethodException nsme2) {
				//Give up
				throw new IllegalStateException("Class " + configClass.getCanonicalName() + " does must define an empty or an "
						+ Configuration.class.getCanonicalName() + " constructor");
			}
		}
		
		Object[] args = fallback ? new Object[0] : new Object[] { configuration };
		ConfigurationObject obj;
		
		try {
			obj = constructor.newInstance(args);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}
		
		if (fallback) {
			obj.inflate(configuration);
		}
		
		return obj;
	}
	
}
