/*
 * HeavySpleef - Advanced spleef plugin for bukkit
 *
 * Copyright (C) 2013-2014 matzefratze123
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.matzefratze123.heavyspleef.core;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import de.matzefratze123.heavyspleef.HeavySpleef;
import de.matzefratze123.heavyspleef.api.IGameComponents;
import de.matzefratze123.heavyspleef.core.Team.Color;
import de.matzefratze123.heavyspleef.core.flag.FlagType;
import de.matzefratze123.heavyspleef.core.region.IFloor;
import de.matzefratze123.heavyspleef.core.region.LoseZone;
import de.matzefratze123.heavyspleef.core.task.Rollback;
import de.matzefratze123.heavyspleef.core.task.SaveSchematic;
import de.matzefratze123.heavyspleef.objects.SpleefPlayer;

public class GameComponents implements IGameComponents {

	private final Game			game;

	// Physical Game Datas
	protected List<IFloor>		floors		= new ArrayList<IFloor>();
	protected List<LoseZone>	loseZones	= new ArrayList<LoseZone>();

	protected List<ScoreBoard>	scoreBoards	= new ArrayList<ScoreBoard>();
	protected List<SignWall>	signwalls	= new ArrayList<SignWall>();

	// Data objects
	protected List<Team>		teams		= new ArrayList<Team>();

	protected GameComponents(Game game) {
		this.game = game;
	}

	public Game getGame() {
		return game;
	}

	/* Floors start */

	public void addFloor(IFloor floor, boolean saveAsSchematic) {
		if (hasFloor(floor.getId())) {
			throw new IllegalArgumentException("Floor with id " + floor.getId() + " already registered!");
		}

		if (saveAsSchematic) {
			// Save the floor to the disk
			SaveSchematic saver = new SaveSchematic(floor);
			Bukkit.getScheduler().runTask(HeavySpleef.getInstance(), saver);
		}

		floors.add(floor);
	}

	@Override
	public void addFloor(IFloor floor) {
		addFloor(floor, true);
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
	public void removeFloor(IFloor floor) {
		if (!hasFloor(floor.getId())) {
			return;
		}

		floor.delete();
	}

	@Override
	public void removeFloor(int id) {
		IFloor floor = getFloor(id);

		if (floor != null) {
			floors.remove(floor);
			floor.delete();
		}
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
		Rollback rollback = new Rollback(game);
		rollback.rollback();
	}

	public File getFloorFolder() {
		File file = new File(HeavySpleef.getInstance().getDataFolder(), "games/" + game.getName());
		file.mkdirs();

		return file;
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

	/* LoseZones end */

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
			wall.drawWall(game);
		}
	}

	/* SignWalls end */

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
	 * Internal method.</br></br> Gets an character array containing the 1vs1
	 * wins of the current game
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
		return (char) (i + '0');
	}

	/* Scoreboards end */

	/* Teams start */
	@Override
	public void addTeam(ChatColor color) {
		addTeam(Color.byChatColor(color));
	}

	@Override
	public void addTeam(Color color) {
		if (color == null) {
			return;
		}

		for (Team team : teams) {
			if (team.getColor() == color) {
				teams.remove(team);
				break;
			}
		}

		Team team = new Team(color);
		teams.add(team);
	}

	@Override
	public void addTeam(Team team) {
		addTeam(team.getColor());
	}

	@Override
	public Team getTeam(ChatColor color) {
		return getTeam(Color.byChatColor(color));
	}

	@Override
	public Team getTeam(Color color) {
		if (color == null) {
			return null;
		}

		for (Team team : teams) {
			if (team.getColor() == color)
				return team;
		}

		return null;
	}

	@Override
	public Team getTeam(SpleefPlayer player) {
		for (Team team : teams) {
			for (SpleefPlayer p : team.getPlayers()) {
				if (player.getRawName().equalsIgnoreCase(p.getRawName())) {
					return team;
				}
			}
		}

		return null;
	}

	public Team getBestAvailableTeam() {
		Team team = null;
		int min = -1;

		for (Team t : teams) {
			int players = t.getPlayers().size();

			if (t.getMaxPlayers() > 0 && players >= t.getMaxPlayers()) {
				continue;
			}

			if (min == -1 || players < min) {
				min = players;
				team = t;
			}
		}

		return team;
	}

	@Override
	public boolean removeTeam(ChatColor color) {
		return removeTeam(Color.byChatColor(color));
	}

	@Override
	public boolean removeTeam(Color color) {
		return removeTeam(getTeam(color));
	}

	@Override
	public boolean removeTeam(Team team) {
		if (team == null) {
			return false;
		}

		teams.remove(team);
		return true;
	}

	@Override
	public boolean removePlayerFromTeam(SpleefPlayer player) {
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
		return hasTeam(Color.byChatColor(color));
	}

	@Override
	public boolean hasTeam(Color color) {
		for (Team team : teams) {
			if (team.getColor() == color)
				return true;
		}

		return false;
	}

	public boolean hasTeam(Team team) {
		return teams.contains(team);
	}

	@Override
	public List<Team> getTeams() {
		return teams;
	}

	@Override
	public List<Team> getActiveTeams() {
		List<Team> active = new ArrayList<Team>();

		for (Team team : teams) {
			if (team.hasPlayersLeft()) {
				active.add(team);
			}
		}

		return active;
	}

	/* Teams end */
}
