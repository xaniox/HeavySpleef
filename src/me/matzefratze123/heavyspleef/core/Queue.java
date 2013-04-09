package me.matzefratze123.heavyspleef.core;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class Queue {

	private Game game = null;
	private Team team = null;
	private String owner = null;
	
	public Queue(Player owner, Game game) {
		this.game = game;
		this.owner = owner.getName();
	}
	
	public Queue(Player owner, Game game, Team team) {
		this.owner = owner.getName();
		this.team = team;
		this.game = game;
	}
	
	public Game getGame() {
		return this.game;
	}
	
	public Team getTeam() {
		return this.team;
	}
	
	public Player getOwner() {
		return Bukkit.getPlayer(this.owner);
	}
	
}
