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

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Location;

import de.matzefratze123.heavyspleef.listener.TagListener;
import de.matzefratze123.heavyspleef.objects.SpleefPlayer;
import de.matzefratze123.heavyspleef.util.I18N;
import de.matzefratze123.heavyspleef.util.Util;

public class Team {

	private List<SpleefPlayer>	players		= new ArrayList<SpleefPlayer>();
	private Color				color;

	private int					maxplayers	= -1;
	private int					minplayers	= -1;

	private Location			spawnpoint;

	public Team(Color color) {
		this.color = color;
	}

	public Color getColor() {
		return this.color;
	}

	public void join(SpleefPlayer player) {
		if (players.contains(player)) {
			player.sendMessage(I18N._("alreadyInTeam", color.toMessageColorString()));
			return;
		}

		players.add(player);
		player.sendMessage(I18N._("addedToTeam", color.toMessageColorString()));

		TagListener.setTag(player, color.toChatColor());
	}

	public void leave(SpleefPlayer player) {
		if (!players.contains(player)) {
			player.sendMessage(I18N._("notInThisTeam", color.toMessageColorString()));
			return;
		}

		players.remove(player);
		player.sendMessage(I18N._("removedFromTeam", color.toMessageColorString()));

		TagListener.setTag(player, null);
	}

	public boolean hasPlayer(SpleefPlayer player) {
		return players.contains(player);
	}

	public boolean hasPlayersLeft() {
		return players.size() > 0;
	}

	public List<SpleefPlayer> getPlayers() {
		return players;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null)
			return false;
		if (!(o instanceof Team))
			return false;
		Team team = (Team) o;
		if (team.getColor() != getColor())
			return false;

		return true;
	}

	public void setMaxPlayers(int maxplayers) {
		this.maxplayers = maxplayers;
	}

	public void setMinPlayers(int minplayers) {
		this.minplayers = minplayers;
	}

	public void setSpawnpoint(Location spawnpoint) {
		this.spawnpoint = spawnpoint;
	}

	public int getMinPlayers() {
		return this.minplayers;
	}

	public int getMaxPlayers() {
		return this.maxplayers;
	}

	public Location getSpawnpoint() {
		return spawnpoint;
	}

	public int getCurrentKnockouts() {
		int knockouts = 0;

		for (SpleefPlayer player : players) {
			knockouts += player.getKnockouts();
		}

		return knockouts;
	}

	public enum Color {

		RED('c', 0xE, 0xFF0000), 
		BLUE('9', 0xB, 0x0000FF), 
		YELLOW('e', 0x4, 0xFFFF00), 
		GREEN('a', 0x5, 0x00FF00), 
		GRAY('7', 0x8, 0x808080);

		private char	chatColorChar;
		private int		woolColor;
		private int		rgb;

		private Color(char chatColorChar, int woolColor, int rgb) {
			this.chatColorChar = chatColorChar;
			this.woolColor = woolColor;
			this.rgb = rgb;
		}

		public ChatColor toChatColor() {
			return ChatColor.getByChar(chatColorChar);
		}

		public String toMessageColorString() {
			return toChatColor() + toString();
		}

		public int getWoolColor() {
			return woolColor;
		}

		public int asRGB() {
			return rgb;
		}
		
		@Override
		public String toString() {
			return Util.firstToUpperCase(name());
		}

		public static Color byName(String name) {
			if (name == null) {
				throw new IllegalArgumentException("name cannot be null");
			}

			try {
				return valueOf(name.toUpperCase());
			} catch (Exception e) {
				return null;
			}
		}

		public static Color byChatColor(ChatColor color) {
			for (Color c : values()) {
				if (c.chatColorChar == color.getChar()) {
					return c;
				}
			}

			return null;
		}

		public static Color byWoolColor(int color) {
			for (Color c : values()) {
				if (c.getWoolColor() == color) {
					return c;
				}
			}

			return null;
		}

		public static String toFriendlyList() {
			final Color[] values = values();
			final int values_length = values().length;

			String[] colors = new String[values_length];

			for (int i = 0; i < values_length; i++) {
				colors[i] = values[i].toMessageColorString();
			}

			return Util.toFriendlyString(colors, ", ");
		}

		public static String toFriendlyList(Game game) {
			final Color[] values = values();
			final int values_length = values().length;

			String[] colors = new String[game.getComponents().getTeams().size()];

			for (int i = 0; i < values_length; i++) {
				if (!game.getComponents().hasTeam(values[i])) {
					continue;
				}

				colors[i] = values[i].toMessageColorString();
			}

			return Util.toFriendlyString(colors, ", ");
		}

	}

}
