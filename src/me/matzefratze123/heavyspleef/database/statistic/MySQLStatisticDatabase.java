package me.matzefratze123.heavyspleef.database.statistic;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import me.matzefratze123.heavyspleef.HeavySpleef;
import me.matzefratze123.heavyspleef.utility.statistic.Statistic;
import me.matzefratze123.heavyspleef.utility.statistic.StatisticManager;

import org.bukkit.Bukkit;

public class MySQLStatisticDatabase implements IStatisticDatabase {

	private String dbHost;
	private String dbPort;
	private String databaseName;
	private String dbUser;
	private String dbPassword;
	
	private HeavySpleef plugin;
	private Connection conn;
	
	public MySQLStatisticDatabase() {
		this.plugin = HeavySpleef.instance;
		this.dbHost = plugin.getConfig().getString("statistic.host");
		this.dbPort = plugin.getConfig().getString("statistic.port");
		this.databaseName = plugin.getConfig().getString("statistic.databaseName");
		this.dbUser = plugin.getConfig().getString("statistic.user");
		this.dbPassword = plugin.getConfig().getString("statistic.password");
	}
	
	private Connection getInstance() {
		try {
			if (conn == null || conn.isClosed())
				createConnection();
			return conn;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return conn;
	}
	
	private void createConnection() {
		try {
			Class.forName("com.mysql.jdbc.Driver");
	        
	        conn = DriverManager.getConnection("jdbc:mysql://" + dbHost + ":"
	                + dbPort + "/" + databaseName + "?" + "user="
	        		+ dbUser + "&" + "password=" + dbPassword);
	    } catch (ClassNotFoundException e) {
	        Bukkit.getLogger().severe("No drivers found for MySQL statistic database! Changing to YAML!");
	        plugin.statisticDatabase = new YamlStatisticDatabase();
	        plugin.statisticDatabase.load();
	    } catch (SQLException e) {
	        Bukkit.getLogger().severe("Could not connect to MySQL database! Bad username or password?");
	        Bukkit.getLogger().severe("Using YAML Database!");
	        plugin.statisticDatabase = new YamlStatisticDatabase();
	        plugin.statisticDatabase.load();
	    }
	}
	
	public ResultSet executeQuery(String sql) throws SQLException {
		conn = getInstance();
		Statement statement = conn.createStatement();
		return statement.executeQuery(sql);
	}
	
	public void executeUpdate(String sql) throws SQLException {
		conn = getInstance();
		Statement statement = conn.createStatement();
		statement.executeUpdate(sql);
	}

	@Override
	public void save() {
		for (Statistic stat : StatisticManager.getStatistics()) {
			int wins = stat.getWins();
		}
	}

	@Override
	public void load() {
		
	}

}
