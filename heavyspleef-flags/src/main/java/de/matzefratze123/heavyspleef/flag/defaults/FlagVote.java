/*
 * This file is part of HeavySpleef.
 * Copyright (c) 2014-2015 matzefratze123
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package de.matzefratze123.heavyspleef.flag.defaults;

import java.util.List;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.google.common.collect.Sets;

import de.matzefratze123.heavyspleef.commands.base.Command;
import de.matzefratze123.heavyspleef.commands.base.CommandContext;
import de.matzefratze123.heavyspleef.commands.base.CommandException;
import de.matzefratze123.heavyspleef.commands.base.CommandValidate;
import de.matzefratze123.heavyspleef.commands.base.PlayerOnly;
import de.matzefratze123.heavyspleef.core.Game;
import de.matzefratze123.heavyspleef.core.GameManager;
import de.matzefratze123.heavyspleef.core.HeavySpleef;
import de.matzefratze123.heavyspleef.core.Unregister;
import de.matzefratze123.heavyspleef.core.config.ConfigType;
import de.matzefratze123.heavyspleef.core.config.DefaultConfig;
import de.matzefratze123.heavyspleef.core.config.FlagSection;
import de.matzefratze123.heavyspleef.core.config.SignLayoutConfiguration;
import de.matzefratze123.heavyspleef.core.event.Subscribe;
import de.matzefratze123.heavyspleef.core.event.PlayerGameEvent;
import de.matzefratze123.heavyspleef.core.event.PlayerLeaveGameEvent;
import de.matzefratze123.heavyspleef.core.extension.Extension;
import de.matzefratze123.heavyspleef.core.extension.ExtensionRegistry;
import de.matzefratze123.heavyspleef.core.extension.SignExtension;
import de.matzefratze123.heavyspleef.core.flag.Flag;
import de.matzefratze123.heavyspleef.core.flag.FlagInit;
import de.matzefratze123.heavyspleef.core.i18n.I18N;
import de.matzefratze123.heavyspleef.core.i18n.Messages;
import de.matzefratze123.heavyspleef.core.layout.SignLayout;
import de.matzefratze123.heavyspleef.core.player.SpleefPlayer;
import de.matzefratze123.heavyspleef.flag.presets.BooleanFlag;

@Flag(name = FlagVote.FLAG_NAME, hasCommands = true)
public class FlagVote extends BooleanFlag {

	protected static final String FLAG_NAME = "vote";
	private Set<SpleefPlayer> voted;
	
	public FlagVote() {
		voted = Sets.newHashSet();
	}
	
	@Command(name = "vote", permission = "heavyspleef.vote", 
			usage = "/spleef vote", descref = Messages.Help.Description.VOTE)
	@PlayerOnly
	public static void onVoteCommand(CommandContext context, HeavySpleef heavySpleef) throws CommandException {
		Player bukkitPlayer = context.getSender();
		SpleefPlayer player = heavySpleef.getSpleefPlayer(bukkitPlayer);
		
		GameManager manager = heavySpleef.getGameManager();
		Game game = manager.getGame(player);
		
		I18N i18n = I18N.getInstance();
		
		CommandValidate.notNull(game, i18n.getString(Messages.Command.NOT_INGAME));
		CommandValidate.isTrue(game.isFlagPresent(FLAG_NAME), i18n.getString(Messages.Command.NO_VOTE_ENABLED));
		
		FlagVote flag = game.getFlag(FlagVote.class);
		boolean success = flag.vote(player, game);
		player.sendMessage(i18n.getString(success ? Messages.Command.SUCCESSFULLY_VOTED : Messages.Command.ALREADY_VOTED));
	}
	
	@FlagInit
	public static void initVoteSign(HeavySpleef heavySpleef) {
		ExtensionRegistry registry = heavySpleef.getExtensionRegistry();
		registry.registerExtension(VoteSignExtension.class);
	}
	
	@Unregister
	public static void unregisterVoteSign(HeavySpleef heavySpleef) {
		ExtensionRegistry registry = heavySpleef.getExtensionRegistry();
		registry.unregister(VoteSignExtension.class);
	}

	@Override
	public void getDescription(List<String> description) {
		description.add("Enables the ability to vote to start the game");
	}
	
	@Subscribe
	public void onPlayerLeaveGameEvent(PlayerLeaveGameEvent event) {
		SpleefPlayer player = event.getPlayer();
		
		if (voted.contains(player)) {
			voted.remove(player);
		}
	}
	
	@Subscribe
	public void onGameEnd(PlayerGameEvent event) {
		voted.clear();
	}
	
	public boolean vote(SpleefPlayer player, Game game) {
		if (voted.contains(player)) {
			return false;
		}
		
		voted.add(player);
		checkVotes(game);
		return true;
	}
	
	private void checkVotes(Game game) {
		DefaultConfig config = getHeavySpleef().getConfiguration(ConfigType.DEFAULT_CONFIG);
		FlagSection section = config.getFlagSection();
		
		int autostartVote = section.getAutostartVote();
		double percentageVoted = (double)voted.size() / game.getPlayers().size();
		
		if (percentageVoted * 100 >= autostartVote) {
			game.countdown();
		}
	}
	
	@Extension(name = "vote-sign")
	public static class VoteSignExtension extends SignExtension {

		public static final String IDENTIFIER = "vote";
		private final I18N i18n = I18N.getInstance();
		
		@SuppressWarnings("unused")
		private VoteSignExtension() {}
		
		public VoteSignExtension(Location location) {
			super(location);
		}
		
		@Override
		public void onSignClick(SpleefPlayer player) {
			GameManager manager = getHeavySpleef().getGameManager();
			
			if (manager.getGame(player) == null) {
				player.sendMessage(i18n.getString(Messages.Command.NOT_INGAME));
				return;
			}
			
			Game game = getGame();
			if (!game.isFlagPresent(FlagVote.class)) {
				player.sendMessage(i18n.getVarString(Messages.Command.NO_VOTE_ENABLED)
						.setVariable("game", game.getName())
						.toString());
				return;
			}
			
			FlagVote flag = game.getFlag(FlagVote.class);
			boolean success = flag.vote(player, game);
			player.sendMessage(i18n.getString(success ? Messages.Command.SUCCESSFULLY_VOTED : Messages.Command.ALREADY_VOTED));
		}
		
		@Override
		public SignLayout retrieveSignLayout() {
			SignLayoutConfiguration config = heavySpleef.getConfiguration(ConfigType.VOTE_SIGN_LAYOUT_CONFIG);
			return config.getLayout();
		}
		
	}

}