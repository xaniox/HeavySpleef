package de.matzefratze123.heavyspleef.persistence;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.TreeSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;



import org.apache.commons.lang.Validate;
import org.dom4j.DocumentException;



import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;



import de.matzefratze123.heavyspleef.core.Game;
import de.matzefratze123.heavyspleef.core.HeavySpleef;
import de.matzefratze123.heavyspleef.core.Statistic;
import de.matzefratze123.heavyspleef.core.floor.Floor;
import de.matzefratze123.heavyspleef.core.floor.schematic.FloorSchematicCodec;
import de.matzefratze123.heavyspleef.core.floor.schematic.FloorSchematicCodec.FloorEntry;

public class CachingReadWriteHandler implements ReadWriteHandler {
	
	public static final String AVAJE_ORM_DATABASE = "avaje_orm";
	public static final String XML_DATABASE = "xml";
	
	private static final String DEFAULT_GAME_XML_SUB_PATH = "/persistent/games.xml";
	private static final String DEFAULT_SQL_DATABASE_URL = "jdbc:sqlite://%s/persistent/database.db";
	
	private static final long STATISTIC_CACHE_EXPIRE = 10 * 60 * 1000L;
	
	private final File dataFolder;
	private final Logger logger;
	
	private DatabaseController gameDatabaseController;
	private DatabaseController statisticDatabaseController;
	
	private Cache<String, Statistic> statisticCache;
	private final CacheLoader<String, Statistic> statisticCacheLoader = new CacheLoader<String, Statistic>() {
		
		@Override
		public Statistic load(String player) throws Exception {
			if (statisticDatabaseController == null) {
				throw new IllegalStateException("Statistic database has not been initialized");
			}
			
			Statistic statistic = (Statistic) statisticDatabaseController.queryUnique(Statistic.NAME_ATTRIBUTE, player, Statistic.class);
			return statistic;
		}
	};
	
	public CachingReadWriteHandler(HeavySpleef heavySpleef, Properties properties) throws DocumentException, IOException, Exception {
		this.dataFolder = heavySpleef.getDataFolder();
		this.logger = heavySpleef.getLogger();
		
		String gameDatabase = properties.getProperty("game.database", XML_DATABASE);
		String statisticDatabase = properties.getProperty("statistic.database");
		
		Validate.isTrue(!statisticDatabase.equals(XML_DATABASE), "statistics cannot be saved on xml");
		
		if (gameDatabase.equals(XML_DATABASE)) {
			String flatfile = properties.getProperty("file", heavySpleef.getDataFolder().getPath() + DEFAULT_GAME_XML_SUB_PATH);
			File file = new File(flatfile);
			
			gameDatabaseController = new JAXBController(file, "games", heavySpleef.getPersistentBeans());
		} else if (gameDatabase.equalsIgnoreCase(AVAJE_ORM_DATABASE)) {
			gameDatabaseController = createORMController(heavySpleef, properties);
		}
		
		if (statisticDatabase != null && statisticDatabase.equals(AVAJE_ORM_DATABASE)) {
			if (gameDatabaseController instanceof AvajeORMController) {
				//Use the same database for statistics as it's a sql database
				statisticDatabaseController = gameDatabaseController;
			} else {
				//Create a new avaje orm controller
				statisticDatabaseController = createORMController(heavySpleef, properties);
			}
			
			//Create a cache for fast data access
			statisticCache = CacheBuilder.newBuilder()
					.expireAfterAccess(STATISTIC_CACHE_EXPIRE, TimeUnit.MILLISECONDS)
					.build(statisticCacheLoader);
		}
	}
	
	private AvajeORMController createORMController(HeavySpleef heavySpleef, Properties properties) throws Exception {
		//Read all connection details
		String driver = properties.getProperty("driver", AvajeORMController.SQLITE_DRIVER);
		String url = properties.getProperty("url", String.format(DEFAULT_SQL_DATABASE_URL, heavySpleef.getDataFolder().getPath()));
		String user = properties.getProperty("user");
		String password = properties.getProperty("password");
		
		//Connection properties for avaje
		Properties connProperties = new Properties();
		connProperties.put("driver", driver);
		connProperties.put("url", url);
		connProperties.put("user", user);
		connProperties.put("password", password);
		connProperties.put("isolation", "READ_COMMITTED");
		
		//Retrieve the persistent beans
		List<Class<?>> persistentBeans = heavySpleef.getPersistentBeans();
		
		//Create the avaje controller
		return new AvajeORMController(heavySpleef.getPlugin(), persistentBeans, connProperties);
	}
	
	@Override
	public void saveGames(Iterable<Game> iterable) {
		gameDatabaseController.update(iterable, Game.class);
		
		iterable.forEach(this::writeFloors);
	}

	@Override
	public void saveGame(Game game) {
		gameDatabaseController.update(game, Game.class);
		
		writeFloors(game);
	}
	
	private void writeFloors(Game game) {
		final FloorSchematicCodec codec = FloorSchematicCodec.getInstance();
		
		game.getFloors().forEach(floor -> {
			FloorEntry entry = new FloorEntry(game.getName(), floor);
			
			File schematicDir = new File(dataFolder, "persistent/floor-schematics/" + game.getName());
			schematicDir.mkdirs();
			
			File schematicFile = new File(schematicDir, "r." + floor.getName() + ".floor");
			
			try {
				if (!schematicFile.exists()) {
					schematicFile.createNewFile();
				}
			
				codec.save(entry, schematicFile);
			} catch (IOException e) {
				logger.log(Level.SEVERE, "Could not save floor schematic " + floor.getName(), e);
			}
		});
	}

	@Override
	public Game getGame(String name) {
		Game game = (Game) gameDatabaseController.queryUnique(Game.NAME_ATTRIBUTE, name, Game.class);
		
		loadAndInjectFloors(game);
		
		return game;
	}
	
	@Override
	public List<Game> getGames() {
		List<Game> games = gameDatabaseController.query(null, null, Game.class, null, DatabaseController.NO_LIMIT)
				.stream()
				.filter(o -> o instanceof Game)
				.map(o -> ((Game)o))
				.collect(Collectors.toList());
		
		games.forEach(this::loadAndInjectFloors);
		
		return games;
	}
	
	@Override
	public void deleteGame(Game game) {
		gameDatabaseController.delete(game);
	}
	
	private void loadAndInjectFloors(Game game) {
		File schematicDir = new File(dataFolder, "persistent/floor-schematics/" + game.getName());
		File[] childs = schematicDir.listFiles(FloorSchematicCodec.FILENAME_FILTER);
		
		final FloorSchematicCodec codec = FloorSchematicCodec.getInstance();
		Arrays.stream(childs).forEach(child -> {
			try {
				FloorEntry entry = codec.load(child);
				
				Floor floor = entry.getFloor();
				game.addFloor(floor);
			} catch (IOException e) {
				logger.log(Level.SEVERE, "Could not load back floor schematic file " + child.getPath(), e);
			}
		});
	}

	@Override
	public void saveStatistics(Iterable<Statistic> iterable) {
		validateStatisticDatabaseSetup();
		
		gameDatabaseController.update(iterable, Statistic.class);
	}

	@Override
	public void saveStatistic(Statistic statistic) {
		validateStatisticDatabaseSetup();
		
		gameDatabaseController.update(statistic, Statistic.class);
	}

	@Override
	public Statistic getStatistic(String player) {
		validateStatisticDatabaseSetup();
		
		Statistic statistic;
		
		try {
			statistic = statisticCache.get(player);
		} catch (ExecutionException e) {
			throw new RuntimeException(e.getCause());
		}
		
		return statistic;
	}

	@Override
	public TreeSet<Statistic> getTopStatistics(int limit) {
		validateStatisticDatabaseSetup();
		
		List<? extends Object> result = statisticDatabaseController.query(null, null, Statistic.class, Statistic.RATING_ATTRIBUTE, limit);
		TreeSet<Statistic> statistics = result.stream().filter(obj -> obj instanceof Statistic).map(obj -> (Statistic)obj).collect(Collectors.toCollection(TreeSet::new));
		
		return statistics;
	}
	
	private void validateStatisticDatabaseSetup() {
		if (statisticDatabaseController == null) {
			throw new IllegalStateException("No statistic-database has been setup");
		}
	}

}
