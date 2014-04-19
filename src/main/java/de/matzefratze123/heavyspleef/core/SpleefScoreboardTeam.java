package de.matzefratze123.heavyspleef.core;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import com.google.common.collect.ImmutableList;

import de.matzefratze123.heavyspleef.objects.SpleefPlayer;

public class SpleefScoreboardTeam implements SpleefScoreboard, SpleefPlayerGameListener {

	private Game		game;
	private Scoreboard	scoreboard;
	private Objective	objective;
	
	private List<Team> lastIngameTeams;

	public SpleefScoreboardTeam(Game game) {
		this.game = game;
		this.lastIngameTeams = ImmutableList.copyOf(game.getComponents().getActiveTeams());
		
		game.registerGameListener(this);
		scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();

		objective = scoreboard.registerNewObjective("heavyspleefScore", SCOREBOARD_CRITERIA);

		objective.setDisplaySlot(DisplaySlot.SIDEBAR);
		objective.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + "Spleef Knockouts");
	}

	@Override
	public void updateScoreboard() {
		for (Team team : game.getComponents().getTeams()) {
			OfflinePlayer fakePlayer = getFakeOfflinePlayer(team, team.hasPlayersLeft());
			
			if (team.hasPlayersLeft()) {
				objective.getScore(fakePlayer).setScore(team.getCurrentKnockouts());
			} else {
				if (!lastIngameTeams.contains(team)) {
					return;
				}
				
				scoreboard.resetScores(getFakeOfflinePlayer(team, true));
				objective.getScore(fakePlayer).setScore(team.getCurrentKnockouts());
			}
		}
		
		lastIngameTeams = ImmutableList.copyOf(game.getComponents().getActiveTeams());
	}
	
	private OfflinePlayer getFakeOfflinePlayer(Team team, boolean hasPlayersLeft) {
		return Bukkit.getOfflinePlayer((hasPlayersLeft ? team.getColor().toChatColor() : ChatColor.GRAY) + team.getColor().toString());
	}

	@Override
	public void removeScoreboard() {
		for (SpleefPlayer player : game.getIngamePlayers()) {
			Scoreboard last = player.getState().getBoard();

			if (last == null) {
				last = Bukkit.getScoreboardManager().getMainScoreboard();
			}

			player.getBukkitPlayer().setScoreboard(last);
		}
		
		game.unregisterGameListener(this);
	}

	@Override
	public void show() {
		for (SpleefPlayer player : game.getIngamePlayers()) {
			player.getBukkitPlayer().setScoreboard(scoreboard);
		}
		
		lastIngameTeams = ImmutableList.copyOf(game.getComponents().getActiveTeams());
	}

	@Override
	public void playerJoin(SpleefPlayer player) {
		updateScoreboard();
	}

	@Override
	public void playerLeave(SpleefPlayer player) {
		updateScoreboard();

		Scoreboard last = player.getState().getBoard();

		if (last == null) {
			last = Bukkit.getScoreboardManager().getMainScoreboard();
		}

		player.getBukkitPlayer().setScoreboard(last);
	}

	@Override
	public void playerKnockout(SpleefPlayer player) {
		updateScoreboard();
	}

	@Override
	public void playerWin(SpleefPlayer player) {}

}
