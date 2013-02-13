package me.matzefratze123.heavyspleef;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;

import org.bukkit.configuration.file.YamlConfiguration;

public class FileConfig {

	private File configFile;
	private HeavySpleef t;
	
	public FileConfig(HeavySpleef instance) {	
		t = instance;
		final File dataFolder = t.getDataFolder();
		dataFolder.mkdirs();
		configFile = new File(dataFolder, "config.yml");
		if (!configFile.exists()) {
			this.t.getLogger().log(Level.INFO, "Could not find a config file! Creating a new...");
			this.createDefaultConfigFile(configFile);
			this.t.getConfig().setDefaults(YamlConfiguration.loadConfiguration(HeavySpleef.class.getResourceAsStream("/defaultconfig.yml")));
		}
	}

	private void createDefaultConfigFile(File configFile2) {
		 try {
	            configFile.createNewFile();
	            
	            final InputStream configIn = HeavySpleef.class.getResourceAsStream("/defaultconfig.yml");
	            final FileOutputStream configOut = new FileOutputStream(configFile);
	            final byte[] buffer = new byte[1024];
	            int read;
	            
	            while ((read = configIn.read(buffer)) > 0)
	                configOut.write(buffer, 0, read);
	                
	            configIn.close();
	            configOut.close();
	            this.t.getLogger().log(Level.INFO, "Config File successfully created!");
	        } catch (final IOException ioe) {
	            this.t.getLogger().log(Level.WARNING, "Could not create config file! IOException? Using normal values!", ioe);
	            return;
	        } catch (final NullPointerException npe) {
	            this.t.getLogger().log(Level.WARNING, "Could not create config file! NullPointerException? Deleting config file and using normal values!", npe);
	            configFile.delete();
	        }
	}

}
