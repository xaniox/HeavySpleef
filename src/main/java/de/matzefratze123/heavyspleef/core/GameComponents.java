package de.matzefratze123.heavyspleef.core;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import de.matzefratze123.heavyspleef.api.IGameComponents;
import de.matzefratze123.heavyspleef.core.flag.FlagType;
import de.matzefratze123.heavyspleef.core.region.IFloor;
import de.matzefratze123.heavyspleef.core.region.LoseZone;
import de.matzefratze123.heavyspleef.util.Util;

public class GameComponents implements IGameComponents {

	private final Game game;
	
	//Physical Game Datas
	protected List<IFloor> floors = new ArrayList<IFloor>();
	protected List<LoseZone> loseZones = new ArrayList<LoseZone>();
	
	protected List<ScoreBoard> scoreBoards = new ArrayList<ScoreBoard>();
	protected List<SignWall> signwalls = new ArrayList<SignWall>();
	
	//Data objects
	protected List<Team> teams = new ArrayList<Team>();
	
	protected GameComponents(Game game) {
		this.game = game;
	}
	
	public Game getGame() {
		return game;
	}
	
	/* Floors start */
	
	@Override
	public void addFloor(IFloor floor) {
		if (hasFloor(floor.getId())) {
			throw new IllegalArgumentException("floor with id " + floor.getId() + " already registered!");
		}
		
		floors.add(floor);
	}
	
	@Override
	public boolean hasFloor(int id) {
		for (IFloor floor : floors) {
			if (floor.getId() == id) {
				return true;
			}
		}
		
		return false;
	}
	
	@Override
	public void removeFloor(int id) {
		IFloor floor = getFloor(id);
		
		if (floor != null) {
			floor.remove();
		}
		
		floors.remove(floor);
	}
	
	@Override
	public IFloor getFloor(int id) {
		for (IFloor floor : floors) {
			if (floor.getId() == id) {
				return floor;
			}
		}
		
		return null;
	}
	
	@Override
	public List<IFloor> getFloors() {
		return floors;
	}
	
	@Override
	public void regenerateFloors() {
		for (IFloor floor : floors) {
			floor.generate();
		}
	}
	
	@Override
	public void regenerateFloor(int id) {
		IFloor floor = getFloor(id);
		
		if (floor == null) {
			return;
		}
		
		floor.generate();
	}
	
	/* Floors end */
	
	/* LoseZones start */
	@Override
	public void addLoseZone(LoseZone loseZone) {
		if (hasLoseZone(loseZone.getId())) {
			throw new IllegalArgumentException("losezone with id " + loseZone.getId() + " already registered!");
		}
		
		loseZones.add(loseZone);
	}
	
	@Override
	public boolean hasLoseZone(int id) {
		for (LoseZone loseZone : loseZones) {
			if (loseZone.getId() == id) {
				return true;
			}
		}
		
		return false;
	}
	
	@Override
	public void removeLoseZone(int id) {
		LoseZone loseZone = getLoseZone(id);
		
		floors.remove(loseZone);
	}
	
	@Override
	public LoseZone getLoseZone(int id) {
		for (LoseZone loseZone : loseZones) {
			if (loseZone.getId() == id) {
				return loseZone;
			}
		}
		
		return null;
	}
	
	@Override
	public List<LoseZone> getLoseZones() {
		return loseZones;
	}
	
	/* LoseZones end*/
	
	/* SignWalls start */
	@Override
	public void addSignWall(SignWall wall) {
		if (hasSignWall(wall.getId())) {
			throw new IllegalArgumentException("signwall with id " + wall.getId() + " already registered!");
		}
		
		signwalls.add(wall);
	}
	
	@Override
	public boolean hasSignWall(int id) {
		for (SignWall wall : signwalls) {
			if (wall.getId() == id) {
				return true;
			}
		}
		
		return false;
	}
	
	@Override
	public void removeSignWall(int id) {
		SignWall wall = getSignWall(id);
		
		signwalls.remove(wall);
	}
	
	@Override
	public SignWall getSignWall(int id) {
		for (SignWall wall : signwalls) {
			if (wall.getId() == id) {
				return wall;
			}
		}
		
		return null;
	}
	
	@Override
	public List<SignWall> getSignWalls() {
		return signwalls;
	}
	
	@Override
	public void updateWalls() {
		for (SignWall wall : signwalls) {
			wall.update();
		}
	}
	
	/* SignWalls end*/
	
	/* Scoreboards start */
	@Override
	public void addScoreBoard(ScoreBoard scoreboard) {
		if (hasScoreBoard(scoreboard.getId())) {
			throw new IllegalArgumentException("scoreboard with id " + scoreboard.getId() + " already registered!");
		}
		
		scoreBoards.add(scoreboard);
	}
	
	@Override
	public boolean hasScoreBoard(int id) {
		for (ScoreBoard scoreboard : scoreBoards) {
			if (scoreboard.getId() == id) {
				return true;
			}
		}
		
		return false;
	}
	
	@Override
	public void removeScoreBoard(int id) {
		ScoreBoard scoreboard = getScoreBoard(id);
		
		scoreBoards.remove(scoreboard);
	}
	
	@Override
	public ScoreBoard getScoreBoard(int id) {
		for (ScoreBoard scoreboard : scoreBoards) {
			if (scoreboard.getId() == id) {
				return scoreboard;
			}
		}
		
		return null;
	}
	
	@Override
	public List<ScoreBoard> getScoreBoards() {
		return scoreBoards;
	}
	
	@Override
	public void updateScoreBoards() {
		for (ScoreBoard board : scoreBoards) {
			if (!game.getFlag(FlagType.ONEVSONE)) {
				board.generate('0', '0', '0', '0');
			} else {
				board.generate(getWins());
			}
		}
	}
	
	/**
	 * Internal method.</br></br>
	 * 
	 * Gets an character array containing the 1vs1 wins of the current game
	 */
	private char[] getWins() {
		int[] digits = new int[4];
		
		for (int i = 1; i <= 2; i++) {
			if (i > game.getIngamePlayers().size()) {
				continue;
			}
			
			int wins = game.getIngamePlayers().get(i - 1).getWins();
			int lastDigit = i * 2;
			
			if (wins > 0) {
				while (wins > 0) {
					digits[--lastDigit] = wins % 10;
					wins /= 10;
				}
			}
		}
		
		char[] chars = new char[digits.length];
		
		for (int i = 0; i < digits.length; i++) {
			chars[i] = asCharDigit(digits[i]);
		}
		
		return chars;
	}
	
	private char asCharDigit(int i) {
		return (char)(i + '0');
	}
	
	/* Scoreboards end */
	
	/* Teams start */
	@Override
	public void addTeam(ChatColor color) {
		for (Team team : teams) {
			if (team.getColor() == color) {
				teams.remove(team);
				break;
			}
		}
		
		Team team = new Team(color, game);
		teams.add(team);
	}
	@Override
	public void addTeam(Team team) {
		addTeam(team.getColor());
	}
	
	@Override
	public Team getTeam(ChatColor color) {
		for (Team team : teams) {
			if (team.getColor() == color)
				return team;
		}
		
		return null;
	}
	
	@Override
	public Team getTeam(Player player) {
		for (Team team : teams) {
			for (Player p : team.getPlayers()) {
				if (player.getName().equalsIgnoreCase(p.getName())) {
					return team;
				}
			}
		}
		
		return null;
	}
	
	@Override
	public boolean removeTeam(ChatColor color) {
		for (Team team : teams) {
			if (team.getColor() == color) {
				teams.remove(team);
				return true;
			}
		}
		
		return false;
	}
	
	@Override
	public boolean removePlayerFromTeam(Player player) {
		boolean removed = false;
		
		for (Team team : teams) {
			if (team.hasPlayer(player)) {
				team.leave(player);
				removed = true;
			}
		}
		
		return removed;
	}
	
	@Override
	public boolean hasTeam(ChatColor color) {
		for (Team team : teams) {
			if (team.getColor() == color)
				return true;
		}
		
		return false;
	}
	
	@Override
	public List<Team> getTeams() {
		return teams;
	}
	
	public Set<String> getTeamColors() {
		Set<String> set = new HashSet<String>();
		for (Team team : teams)
			set.add(team.getColor() + Util.formatMaterialName(team.getColor().name()));
		
		return set;
	}
	
	public void resetTeams() {
		for (Team team : teams) {
			team.resetKnockouts();
		}
	}
	
	/* Teams end */
}
