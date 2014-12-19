package de.matzefratze123.heavyspleef.core.flag;

import java.io.File;
import java.io.FilenameFilter;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.Validate;

import com.google.common.collect.Maps;

public class FlagRegistry {
	
	private static final FilenameFilter CLASS_FILE_FILTER = (dir, name) -> name.toLowerCase().endsWith(".class");
	
	private File customFlagFolder;
	private Logger logger;
	private ClassLoader classLoader;
	
	private Map<String, Class<? extends AbstractFlag<?>>> availableFlags;
	
	public FlagRegistry(File customFlagFolder, Logger logger) {
		this.customFlagFolder = customFlagFolder;
		this.logger = logger;
		this.availableFlags = Maps.newHashMap();
		
		URL url;
		
		try {
			url = customFlagFolder.toURI().toURL();
		} catch (MalformedURLException mue) {
			throw new RuntimeException(mue);
		}
		
		this.classLoader = new URLClassLoader(new URL[] { url });
		
		loadClasses();
	}
	
	@SuppressWarnings("unchecked")
	private void loadClasses() {
		File[] classFiles = customFlagFolder.listFiles(CLASS_FILE_FILTER);
		
		for (File classFile : classFiles) {
			String className = cutExtension(classFile);
			
			Class<?> clazz;
			
			try {
				clazz = classLoader.loadClass(className);
			} catch (ClassNotFoundException e) {
				logger.log(Level.SEVERE, "Failed to load flag class " + className, e);
				continue;
			}
			
			if (!(AbstractFlag.class.isAssignableFrom(clazz))) {
				logger.warning("Could not load flag class " + className + " as it is not a subclass of " + AbstractFlag.class.getName());
				continue;
			}
			
			Class<? extends AbstractFlag<?>> flagClazz = (Class<? extends AbstractFlag<?>>) clazz;
			registerFlag(flagClazz);
		}
	}
	
	private String cutExtension(File file) {
		String fileName = file.getName();
		int lastDotIndex = fileName.length() - 1;
		
		while (fileName.charAt(lastDotIndex) != '.' && lastDotIndex > 0) {
			--lastDotIndex;
		}
		
		return fileName.substring(0, lastDotIndex);
	}
	
	public File getCustomFlagFolder() {
		return customFlagFolder;
	}
	
	public void registerFlag(Class<? extends AbstractFlag<?>> clazz) {
		Validate.notNull(clazz, "clazz cannot be null");
		
		/* Check if the class provides the required Flag annotation */
		Validate.isTrue(clazz.isAnnotationPresent(Flag.class), "clazz must be annotated with the @Flag annotation");
		
		Flag flagAnnotation = clazz.getAnnotation(Flag.class);
		String flagName = flagAnnotation.name();
		
		Validate.isTrue(!flagName.isEmpty(), "name() in annotation Flag for class " + clazz.getCanonicalName() + "cannot be empty");
		
		/* Check if the class can be instantiated */
		try {
			Constructor<? extends AbstractFlag<?>> constructor = clazz.getDeclaredConstructor();
			
			//Make the constructor accessible for future uses
			constructor.setAccessible(true);
		} catch (NoSuchMethodException | SecurityException e) {
			throw new IllegalArgumentException("clazz must provide an empty constructor");
		}
		
		availableFlags.put(flagName, clazz);
	}
	
	public Class<? extends AbstractFlag<?>> getFlagClass(String name) {
		return availableFlags.get(name);
	}
	
	public AbstractFlag<?> newFlagInstance(String name) {
		Class<? extends AbstractFlag<?>> clazz = availableFlags.get(name);
		
		try {
			return clazz.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			//This should not happen as we made the constructor
			//accessible while the class was registered
			
			//But to be sure throw a RuntimeException
			throw new RuntimeException(e);
		}
	}
	
}
