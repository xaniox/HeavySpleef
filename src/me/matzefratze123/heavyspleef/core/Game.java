package me.matzefratze123.heavyspleef.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.matzefratze123.heavyspleef.HeavySpleef;
import me.matzefratze123.heavyspleef.utility.LanguageHandler;
import me.matzefratze123.heavyspleef.utility.LocationHelper;
import me.matzefratze123.heavyspleef.utility.PlayerStateManager;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public class Game {

	private Location firstCorner;
	private Location secondCorner;
	
	private Map<Integer, Floor> floors = new HashMap<Integer, Floor>();
	private Map<Integer, Cuboid> loseZones = new HashMap<Integer, Cuboid>();
	private Map<String, Integer> knockouts = new HashMap<String, Integer>();
	public  Map<String, List<Block>> brokenBlocks = new HashMap<String, List<Block>>();
	
	private Location firstInnerCorner;
	private Location secondInnerCorner;
	
	private GameState state;
	
	public List<String> players = new ArrayList<String>();
	public List<String> wereOffline = new ArrayList<String>();
	
	private Location winPoint;
	private Location losePoint;
	private Location preLobbyPoint;
	
	private int jackpot = 0;
	private int money;
	private int neededPlayers;
	
	private ConfigurationSection gameSection;
	private String name;
	
	public Game(Location firstCorner, Location secondCorner, String name) {
		this.firstCorner = firstCorner;
		this.secondCorner = secondCorner;
		this.name = name;
		this.state = GameState.NOT_INGAME;
		this.gameSection = HeavySpleef.instance.database.getConfigurationSection(name);
		this.neededPlayers = HeavySpleef.instance.getConfig().getInt("general.neededPlayers");
		this.money = HeavySpleef.instance.getConfig().getInt("general.defaultToPay");
		
		calculateInnerCorners();
	}

	private void calculateInnerCorners() {
		
		int firstCornerX = Math.min(getFirstCorner().getBlockX(), getSecondCorner().getBlockX()) + 1;
		int firstCornerY = Math.min(getFirstCorner().getBlockY(), getSecondCorner().getBlockY()) + 1;
		int firstCornerZ = Math.min(getFirstCorner().getBlockZ(), getSecondCorner().getBlockZ()) + 1;
		
		int secondCornerX = Math.max(getFirstCorner().getBlockX(), getSecondCorner().getBlockX()) - 1;
		int secondCornerY = Math.max(getFirstCorner().getBlockY(), getSecondCorner().getBlockY()) - 1;
		int secondCornerZ = Math.max(getFirstCorner().getBlockZ(), getSecondCorner().getBlockZ()) - 1;
		
		Location corner1 = new Location(getFirstCorner().getWorld(), firstCornerX, firstCornerY, firstCornerZ);
		Location corner2 = new Location(getSecondCorner().getWorld(), secondCornerX, secondCornerY, secondCornerZ);
		
		this.firstInnerCorner = corner1;
		this.secondInnerCorner = corner2;
	}

	public Location getFirstCorner() {
		return firstCorner;
	}

	public void setFirstCorner(Location firstCorner) {
		this.firstCorner = firstCorner;
	}

	public Location getSecondCorner() {
		return secondCorner;
	}

	public void setSecondCorner(Location secondCorner) {
		this.secondCorner = secondCorner;
	}

	public void setGameState(GameState state) {
		this.state = state;
	}
	
	public GameState getGameState() {
		return state;
	}
	
	public boolean isIngame() {
		return state == GameState.INGAME;
	}
	
	public boolean isCounting() {
		return state == GameState.COUNTING;
	}
	
	public boolean isNotIngame() {
		return state == GameState.NOT_INGAME;
	}
	
	public boolean isPreLobby() {
		return state == GameState.PRE_LOBBY;
	}
	
	public boolean isDisabled() {
		return state == GameState.DISABLED;
	}
	
	public void stop(boolean disable) {
		if (GameManager.tasks.containsKey(getName()))
			Bukkit.getScheduler().cancelTask(GameManager.tasks.get(getName()));
		for (String playerName : players) {
			Player player = Bukkit.getPlayer(playerName);
			if (player == null)
				continue;
			player.teleport(getLosePoint());
			if (HeavySpleef.instance.getConfig().getBoolean("general.savePlayerState"))
				PlayerStateManager.restorePlayerState(player);
		}
		broadcast(_("gameStopped"));
		setGameState(GameState.NOT_INGAME);
		players.clear();
		if (!disable)
			addPlayersFromQueue();
	}

	public Location getWinPoint() {
		return winPoint;
	}

	public void setWinPoint(Location winPoint) {
		this.winPoint = winPoint;
	}

	public Location getLosePoint() {
		return losePoint;
	}

	public void setLosePoint(Location losePoint) {
		this.losePoint = losePoint;
	}
	
	public boolean addFloor(Location loc1, Location loc2, int id, int blockID, byte blockData, boolean wool) {
		Floor floor = new Floor(id, loc1, loc2, blockID, blockData, wool);
		if (floors.containsKey(id))
			return false;
		floors.put(id, floor);
		floor.create();
		return true;
	}
	
	public boolean addFloor(Floor floor, boolean create) {
		if (floors.containsKey(floor.getId()))
			return false;
		floors.put(floor.getId(), floor);
		if (create)
			floor.create();
		return true;
	}
 	
	public void removeFloor(int id) {
		floors.remove(id);
	}
	
	public boolean hasFloor(int id) {
		return floors.containsKey(id);
	}
	
	public int getFloorSize() {
		return floors.size();
	}
	
	public int getLoseZoneSize() {
		return loseZones.size();
	}
	
	public boolean addLoseZone(int id, Location loc1, Location loc2) {
		Cuboid loseZone = new LoseZone(loc1, loc2, id);
		if (loseZones.containsKey(id))
			return false;
		loseZones.put(id, loseZone);
		return true;	
	}
	
	public boolean addLoseZone(LoseZone loseZone) {
		if (loseZones.containsKey(loseZone.getId()))
			return false;
		loseZones.put(loseZone.getId(), loseZone);
		return true;
	}
	
	public void removeLoseZone(int id) {
		loseZones.remove(id);
	}
	
	public boolean hasLoseZone(int id) {
		return loseZones.containsKey(id);
	}
	
	public Collection<Floor> getFloors() {
		return floors.values();
	}
	
	public Collection<Cuboid> getLoseZones() {
		return loseZones.values();
	}
	
	public void addPlayer(Player player, boolean atCountdown) {
		players.add(player.getName());
		if (HeavySpleef.instance.getConfig().getBoolean("general.savePlayerState"))
			PlayerStateManager.savePlayerState(player);
		if (HeavySpleef.instance.getConfig().getBoolean("sounds.plingSound")) {
			for (Player p : getPlayers())
				p.playSound(p.getLocation(), Sound.NOTE_PLING, 4.0F, p.getLocation().getPitch());
		}
		tellAll(_("playerJoinedGame", player.getName()));
		if (HeavySpleef.hasVault && getMoney() > 0 && atCountdown) {
			HeavySpleef.econ.withdrawPlayer(player.getName(), getMoney());
			player.sendMessage(_("paidIntoJackpot", HeavySpleef.econ.format(getMoney())));
			this.jackpot += getMoney();
		}
	}
	
	protected void tellAll(String msg) {
		for (Player p : getPlayers()) {
			p.sendMessage(msg);
		}
	}

	public void start() {
		this.jackpot = 0;
		if (HeavySpleef.hasVault) {
			if (getMoney() > 0) {
				for (Player p : getPlayers()) {
					HeavySpleef.econ.withdrawPlayer(p.getName(), getMoney());
					p.sendMessage(_("paidIntoJackpot", HeavySpleef.econ.format(getMoney())));
					this.jackpot += getMoney();
				}
			}
		}
		int countdown = HeavySpleef.instance.getConfig().getInt("general.countdownFrom");
		int taskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(HeavySpleef.instance, new CountingTask(countdown, this.name), 20L, 20L);
		GameManager.tasks.put(this.name, taskID);
	}
	
	public void removePlayer(Player player, LoseCause cause) {
		if (!players.contains(player.getName()))
			return;
		players.remove(player.getName());
		player.sendMessage(_("yourKnockOuts", String.valueOf(getKnockouts(player))));
		broadcast(getLoseMessage(cause, player));
		player.teleport(getLosePoint());
		player.setFireTicks(0);
		if (HeavySpleef.instance.getConfig().getBoolean("general.savePlayerState"))
			PlayerStateManager.restorePlayerState(player);
		if (players.size() <= 0)
			setGameState(GameState.NOT_INGAME);
		if (state == GameState.INGAME || state == GameState.COUNTING) {
			if (players.size() != 1)
				return;
			
			for (int i = 0; i < players.size(); i++) {
				Player winner = Bukkit.getPlayer(players.get(i));
				this.win(winner);
			}
		}
	}
	
	private String getLoseMessage(LoseCause cause, Player player) {
		switch(cause) {
		case KICK:
			return _("loseCause_kick", player.getName());
		case LEAVE:
			return _("loseCause_leave", player.getName(), this.getName());
		case LOSE:
			return _("loseCause_lose", player.getName(), getKiller(player, true));
		case QUIT:
			return _("loseCause_quit", player.getName());
		case UNKNOWN:
			return _("loseCause_unknown", player.getName());
		default:
			return "null";
		}
	}
	
	public String getKiller(Player player, boolean addKnockout) {
		Floor lowerMost = getLowermostFloor();
		
		for (String name : brokenBlocks.keySet()) {
			List<Block> blocks = brokenBlocks.get(name);
			for (Block block : blocks) {
				if (block.getY() != lowerMost.getFirstCorner().getBlockY())
					continue;
				
				int differenceX = block.getX() < player.getLocation().getBlockX() ? player.getLocation().getBlockX() - block.getX() : block.getX() - player.getLocation().getBlockX();
				int differenceZ = block.getZ() < player.getLocation().getBlockZ() ? player.getLocation().getBlockZ() - block.getZ() : block.getZ() - player.getLocation().getBlockZ();
				
				if (differenceX == 0 && differenceZ == 0) {
					if (addKnockout)
						addKnockout(name);
					return name;
				}
				
			}
		}
		return "AntiCamping";
	}
	
	public void addKnockout(String player) {
		if (knockouts.containsKey(player))
			knockouts.put(player, knockouts.get(player) + 1);
		else
			knockouts.put(player, 1);
	}
	
	public int getKnockouts(Player player) {
		if (knockouts.containsKey(player.getName()))
			return knockouts.get(player.getName());
		return 0;
	}
	
	public Floor getLowermostFloor() {
		Map<Integer, Floor> floorsWithY = new HashMap<Integer, Floor>();
		
		for (Floor f : getFloors()) {
			int minY = Math.min(f.getFirstCorner().getBlockY(), f.getSecondCorner().getBlockY());
			floorsWithY.put(minY, f);
		}
		
		Integer[] keySet = floorsWithY.keySet().toArray(new Integer[floorsWithY.size()]);
		Arrays.sort(keySet);
		return floorsWithY.get(keySet[0]);
	}

	private void win(Player p) {
		if (p == null)
			return;
		
		p.teleport(getWinPoint());
		setGameState(GameState.NOT_INGAME);
		setupFloors();
		players.clear();
		
		if (HeavySpleef.instance.getConfig().getBoolean("general.savePlayerState"))
			PlayerStateManager.restorePlayerState(p);
		
		broadcast(_("hasWon", p.getName(), this.getName()));
		p.sendMessage(_("win"));
		p.sendMessage(_("yourKnockOuts", String.valueOf(getKnockouts(p))));
		this.brokenBlocks.clear();
		this.knockouts.clear();
		if (HeavySpleef.instance.getConfig().getBoolean("sounds.levelUp"))
			p.playSound(p.getLocation(), Sound.LEVEL_UP, 4.0F, p.getLocation().getPitch());
		addPlayersFromQueue();
		if (HeavySpleef.hasVault) {
			if (this.jackpot == 0)
				return;
			HeavySpleef.econ.depositPlayer(p.getName(), this.jackpot);
			p.sendMessage(_("jackpotReceived", HeavySpleef.econ.format(this.jackpot)));
			this.jackpot = 0;
		}
	}
	
	private void addPlayersFromQueue() {
		for (String name : GameManager.queues.keySet()) {
			if (GameManager.queues.get(name).equalsIgnoreCase(getName())) {
				Player currentPlayer = Bukkit.getPlayer(name);
				if (currentPlayer == null)
					continue;
				currentPlayer.teleport(getPreGamePoint());
				addPlayer(currentPlayer, false);
				currentPlayer.sendMessage(_("noLongerInQueue"));
				GameManager.queues.remove(currentPlayer.getName());
			}
		}
	}

	public static String _(String... key) {
		return ChatColor.RED + "[" + ChatColor.GOLD + "HeavySpleef" + ChatColor.RED + "] " + ChatColor.RESET + LanguageHandler._(key);
	}
	
	public static String __(String... key) {
		return LanguageHandler._(key);
	}
	
	public Player[] getPlayers() {
		String[] playersAsString = players.toArray(new String[players.size()]);
		ArrayList<Player> pList = new ArrayList<Player>(); 
		for (String player : playersAsString) {
			Player p = Bukkit.getPlayer(player);
			if (p == null)
				continue;
			pList.add(p);
		}
		return pList.toArray(new Player[pList.size()]);
	}

	public Location getPreGamePoint() {
		return preLobbyPoint;
	}

	public void setPreGamePoint(Location preLobbyPoint) {
		this.preLobbyPoint = preLobbyPoint;
	}
	
	public void setupFloors() {
		for (Cuboid floor : floors.values()) {
			floor.create();
		}
	}

	public boolean isFinal() {
		return winPoint != null && losePoint != null && preLobbyPoint != null;
	}

	public Location getSecondInnerCorner() {
		return secondInnerCorner;
	}
	

	public Location getFirstInnerCorner() {
		return firstInnerCorner;
	}
	
	public Location[] get4Points() {
		Location[] locs = new Location[4];
	
		int y = getFirstCorner().getBlockY();
		
		locs[0] = new Location(getFirstCorner().getWorld(), Math.min(getFirstCorner().getBlockX(), getSecondCorner().getBlockX()), y, Math.min(getFirstCorner().getBlockZ(), getSecondCorner().getBlockZ()));
		locs[1] = new Location(getFirstCorner().getWorld(), Math.min(getFirstCorner().getBlockX(), getSecondCorner().getBlockX()), y, Math.max(getFirstCorner().getBlockZ(), getSecondCorner().getBlockZ()));
		locs[2] = new Location(getFirstCorner().getWorld(), Math.max(getFirstCorner().getBlockX(), getSecondCorner().getBlockX()), y, Math.min(getFirstCorner().getBlockZ(), getSecondCorner().getBlockZ()));
		locs[3] = new Location(getFirstCorner().getWorld(), Math.max(getFirstCorner().getBlockX(), getSecondCorner().getBlockX()), y, Math.max(getFirstCorner().getBlockZ(), getSecondCorner().getBlockZ()));
		
		return locs;
	}
	
	public void broadcast(String msg) {
		if (HeavySpleef.instance.getConfig().getBoolean("general.globalBroadcast")) {
			Bukkit.broadcastMessage(msg);
		} else {
			int radius = HeavySpleef.instance.getConfig().getInt("general.broadcast-radius");
			int radiusSqared = radius * radius;
			Location[] corners = get4Points();
			
			for (Player p : Bukkit.getOnlinePlayers()) {
				Location playerLocation = p.getLocation();
				
				if (LocationHelper.getDistance2D(corners[0], p.getLocation()) != -1.0D &&
					   (LocationHelper.getDistance2D(corners[0], playerLocation) <= radiusSqared ||
						LocationHelper.getDistance2D(corners[1], playerLocation) <= radiusSqared ||
						LocationHelper.getDistance2D(corners[2], playerLocation) <= radiusSqared ||
						LocationHelper.getDistance2D(corners[3], playerLocation) <= radiusSqared ||
						this.players.contains(p.getName()))) {
					p.sendMessage(msg);
				}
			}
		}
	}

	public String getName() {
		return name;
	}

	public ConfigurationSection getGameSection() {
		return gameSection;
	}

	public int getNeededPlayers() {
		return neededPlayers;
	}

	public void setNeededPlayers(int neededPlayers) {
		this.neededPlayers = neededPlayers;
	}

	public int getMoney() {
		return money;
	}

	public void setMoney(int money) {
		this.money = money;
	}
	
	public void addBrokenBlock(Player p, Block b) {
		if (brokenBlocks.containsKey(p.getName()))
			brokenBlocks.get(p.getName()).add(b);
		else {
			List<Block> blocks = new ArrayList<Block>();
			blocks.add(b);
			brokenBlocks.put(p.getName(), blocks);
		}
	}
}
