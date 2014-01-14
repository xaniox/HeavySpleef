/*
 *   HeavySpleef - Advanced spleef plugin for bukkit
 *   
 *   Copyright (C) 2013-2014 matzefratze123
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package de.matzefratze123.heavyspleef.api;

import java.util.List;

import org.bukkit.ChatColor;

import de.matzefratze123.heavyspleef.core.ScoreBoard;
import de.matzefratze123.heavyspleef.core.SignWall;
import de.matzefratze123.heavyspleef.core.Team;
import de.matzefratze123.heavyspleef.core.Team.Color;
import de.matzefratze123.heavyspleef.core.region.IFloor;
import de.matzefratze123.heavyspleef.core.region.LoseZone;
import de.matzefratze123.heavyspleef.objects.SpleefPlayer;

public interface IGameComponents {

	public void addFloor(IFloor floor);
	
	public boolean hasFloor(int id);
	
	public void removeFloor(IFloor floor);
	
	public void removeFloor(int id);
	
	public IFloor getFloor(int id);
	
	public List<IFloor> getFloors();
	
	public void regenerateFloors();
	
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
	
	@Deprecated
	public void addTeam(ChatColor color);
	
	public void addTeam(Color color);
	
	public void addTeam(Team team);
	
	@Deprecated
	public Team getTeam(ChatColor color);
	
	public Team getTeam(Color color);
	
	public Team getTeam(SpleefPlayer player);
	
	@Deprecated
	public boolean removeTeam(ChatColor color);
	
	public boolean removeTeam(Color color);
	
	public boolean removeTeam(Team team);
	
	public boolean removePlayerFromTeam(SpleefPlayer player);
	
	@Deprecated
	public boolean hasTeam(ChatColor color);
	
	public boolean hasTeam(Color color);
	
	public List<Team> getTeams();
	
	public List<Team> getActiveTeams();

}
