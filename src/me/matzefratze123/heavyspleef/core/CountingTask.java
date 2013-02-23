package me.matzefratze123.heavyspleef.core;

import java.util.Random;

import me.matzefratze123.heavyspleef.HeavySpleef;
import me.matzefratze123.heavyspleef.utility.statistic.StatisticManager;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class CountingTask implements Runnable {

	private int remaining = 10;
	private String id;
	private Game game;
	
	public CountingTask(int start, String gameId) {
		this.remaining = start;
		this.id = gameId;
		this.game = GameManager.getGame(id);
		this.game.setGameState(GameState.COUNTING);
		teleportPlayers();
	}
	
	@Override
	public void run() {
		if (remaining <= 0) {
			start();
		} else if (remaining <= 10){
			if (HeavySpleef.instance.getConfig().getBoolean("sounds.plingSound")) {
				if (remaining <= 5) {
					for (Player p : game.getPlayers()) {
						p.playSound(p.getLocation(), Sound.NOTE_PLING, 4.0F, p.getLocation().getPitch());
					}
				}
			}
			game.tellAll(Game._("gameIsStarting", String.valueOf(remaining)));
			remaining--;
		} else {
			if (remaining % 10 == 0)
				game.tellAll(Game._("gameIsStarting", String.valueOf(remaining)));
			remaining--;
		}
	}
	
	private void teleportPlayers() {
		for (Player p : game.getPlayers()) {
			p.teleport(getRandomSpleefLocation(game)); // Teleport every player to a random location inside the arena at the start of the game
		}
	}
	
	private void start() {
		game.tellAll(Game._("gameHasStarted"));
		game.broadcast(Game._("gameOnArenaHasStarted", game.getName()));
		game.broadcast(Game._("startedGameWith", String.valueOf(game.players.size())));
		game.setGameState(GameState.INGAME);
		int taskID = GameManager.getTaskID(id);
		GameManager.tasks.remove(id);
		Bukkit.getScheduler().cancelTask(taskID);
		
		for (Player p : game.getPlayers())
			StatisticManager.getStatistic(p.getName()).addGame();
	}
	
	//Returns a random location inside a spleef arena
	public static Location getRandomSpleefLocation(Game game) {
		Random random = new Random();
		int minX = Math.min(game.getFirstInnerCorner().getBlockX(), game.getSecondInnerCorner().getBlockX()) + 1; // Add 1 because of walls from the arena
		int minZ = Math.min(game.getFirstInnerCorner().getBlockZ(), game.getSecondInnerCorner().getBlockZ()) + 1;
		
		int maxX = Math.max(game.getFirstInnerCorner().getBlockX(), game.getSecondInnerCorner().getBlockX()) - 1; // Subtract 1 because of walls from the arena
		int maxZ = Math.max(game.getFirstInnerCorner().getBlockZ(), game.getSecondInnerCorner().getBlockZ()) - 1;
		
		int differenceX, differenceZ;
		
		differenceX = minX < maxX ? maxX - minX : minX - maxX; // Difference between corners X
		differenceZ = minZ < maxZ ? maxZ - minZ : minZ - maxZ; // Difference between corners Z
		
		int randomX = minX + random.nextInt(differenceX + 1); // Choose a random X location
		int randomZ = minZ + random.nextInt(differenceZ + 1); // Choose a random Z location
		
		int y = Math.max(game.getHighestFloor().getFirstCorner().getBlockY(), game.getHighestFloor().getSecondCorner().getBlockY()) + 1;
		
		return new Location(game.getFirstCorner().getWorld(), randomX, y, randomZ); // Return the location
	}

}
