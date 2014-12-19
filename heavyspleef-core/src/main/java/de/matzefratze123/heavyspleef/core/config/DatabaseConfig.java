package de.matzefratze123.heavyspleef.core.config;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;

import com.google.common.collect.Lists;

public class DatabaseConfig extends ConfigurationObject {
	
	private boolean statisticsEnabled;
	private PersistenceType gamePersistenceType;
	private List<DatabaseConnection> connections;
	
	public DatabaseConfig(Configuration config) {
		super(config);
	}
	
	@Override
	public void inflate(Configuration config) {
		ConfigurationSection moduleSection = config.getConfigurationSection("database-modules");
		this.statisticsEnabled = moduleSection.getBoolean("statistics.enabled");
		this.gamePersistenceType = PersistenceType.byName(moduleSection.getString("games.persistence-type"));
		
		this.connections = Lists.newArrayList();
		ConfigurationSection connectionsSection = config.getConfigurationSection("persistence-connection");
		connectionsSection.getKeys(false).forEach(
				key -> connections.add(new DatabaseConnection(connectionsSection.getConfigurationSection(key))));
	}
	
	public boolean isStatisticsModuleEnabled() {
		return statisticsEnabled;
	}
	
	public PersistenceType getGamesPersistenceType() {
		return gamePersistenceType;
	}
	
	public List<DatabaseConnection> getDatabaseConnections() {
		return connections;
	}
	
	public enum PersistenceType {
		
		XML,
		SQL;
		
		public static PersistenceType byName(String name) {
			Optional<PersistenceType> foundType = Arrays.stream(values()).filter(type -> type.name().equalsIgnoreCase(name)).findFirst();
			
			return foundType.isPresent() ? foundType.get() : null;
		}
		
	}

}
