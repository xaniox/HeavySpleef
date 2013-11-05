package de.matzefratze123.heavyspleef.api;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import de.matzefratze123.heavyspleef.core.ScoreBoard;
import de.matzefratze123.heavyspleef.core.SignWall;
import de.matzefratze123.heavyspleef.core.Team;
import de.matzefratze123.heavyspleef.core.region.IFloor;
import de.matzefratze123.heavyspleef.core.region.LoseZone;

public interface IGameComponents {

	public void addFloor(IFloor floor);
	
	public boolean hasFloor(int id);
	
	public void removeFloor(int id);
	
	public IFloor getFloor(int id);
	
	public List<IFloor> getFloors();
	
	public void regenerateFloors();
	
	public void regenerateFloor(int id);
	
	public void addLoseZone(LoseZone loseZone);
	
	public boolean hasLoseZone(int id);
	
	public void removeLoseZone(int id);
	
	public LoseZone getLoseZone(int id);
	
	public List<LoseZone> getLoseZones();

	public void addSignWall(SignWall wall);
	
	public boolean hasSignWall(int id);
	
	public void removeSignWall(int id);
	
	public SignWall getSignWall(int id);
	
	public List<SignWall> getSignWalls();
	
	public void updateWalls();
	
	public void addScoreBoard(ScoreBoard scoreboard);
	
	public boolean hasScoreBoard(int id);
	
	public void removeScoreBoard(int id);
	
	public ScoreBoard getScoreBoard(int id);
	
	public List<ScoreBoard> getScoreBoards();
	
	public void updateScoreBoards();
	
	public void addTeam(ChatColor color);
	
	public void addTeam(Team team);
	
	public Team getTeam(ChatColor color);
	
	public Team getTeam(Player player);
	
	public boolean removeTeam(ChatColor color);
	
	public boolean removePlayerFromTeam(Player player);
	
	public boolean hasTeam(ChatColor color);
	
	public List<Team> getTeams();

}
