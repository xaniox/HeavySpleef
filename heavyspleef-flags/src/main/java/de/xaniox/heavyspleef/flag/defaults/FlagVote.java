/*
 * This file is part of HeavySpleef.
 * Copyright (c) 2014-2016 Matthias Werning
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
package de.xaniox.heavyspleef.flag.defaults;

import com.google.common.collect.Sets;
import de.xaniox.heavyspleef.commands.base.*;
import de.xaniox.heavyspleef.core.HeavySpleef;
import de.xaniox.heavyspleef.core.Permissions;
import de.xaniox.heavyspleef.core.Unregister;
import de.xaniox.heavyspleef.core.config.ConfigType;
import de.xaniox.heavyspleef.core.config.DefaultConfig;
import de.xaniox.heavyspleef.core.config.FlagSection;
import de.xaniox.heavyspleef.core.config.SignLayoutConfiguration;
import de.xaniox.heavyspleef.core.event.GameCountdownEvent;
import de.xaniox.heavyspleef.core.event.PlayerJoinGameEvent;
import de.xaniox.heavyspleef.core.event.PlayerLeaveGameEvent;
import de.xaniox.heavyspleef.core.event.Subscribe;
import de.xaniox.heavyspleef.core.extension.Extension;
import de.xaniox.heavyspleef.core.extension.ExtensionRegistry;
import de.xaniox.heavyspleef.core.extension.SignExtension;
import de.xaniox.heavyspleef.core.flag.Flag;
import de.xaniox.heavyspleef.core.flag.FlagInit;
import de.xaniox.heavyspleef.core.game.Game;
import de.xaniox.heavyspleef.core.game.GameManager;
import de.xaniox.heavyspleef.core.game.GameState;
import de.xaniox.heavyspleef.core.i18n.I18N;
import de.xaniox.heavyspleef.core.i18n.I18NManager;
import de.xaniox.heavyspleef.core.i18n.Messages;
import de.xaniox.heavyspleef.core.layout.SignLayout;
import de.xaniox.heavyspleef.core.player.SpleefPlayer;
import de.xaniox.heavyspleef.flag.presets.BaseFlag;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.material.MaterialData;

import java.util.List;
import java.util.Set;

@Flag(name = FlagVote.FLAG_NAME, hasCommands = true)
public class FlagVote extends BaseFlag {

	protected static final String FLAG_NAME = "vote";
	private static Listener listener;
	
	private Set<SpleefPlayer> voted;
	
	public FlagVote() {
		voted = Sets.newHashSet();
	}
	
	@Command(name = "vote", permission = Permissions.PERMISSION_VOTE,
			usage = "/spleef vote", descref = Messages.Help.Description.VOTE)
	@PlayerOnly
	public static void onVoteCommand(CommandContext context, HeavySpleef heavySpleef) throws CommandException {
		Player bukkitPlayer = context.getSender();
		SpleefPlayer player = heavySpleef.getSpleefPlayer(bukkitPlayer);
		
		GameManager manager = heavySpleef.getGameManager();
		Game game = manager.getGame(player);
		
		final I18N i18n = I18NManager.getGlobal();
		
		CommandValidate.notNull(game, i18n.getString(Messages.Command.NOT_INGAME));
		CommandValidate.isTrue(game.isFlagPresent(FLAG_NAME), i18n.getString(Messages.Command.NO_VOTE_ENABLED));
		
		if (game.getGameState() != GameState.LOBBY) {
			player.sendMessage(i18n.getString(Messages.Command.FUNCTION_ONLY_IN_LOBBY));
			return;
		}
		
		FlagVote flag = game.getFlag(FlagVote.class);
		boolean success = flag.vote(player, game);
		player.sendMessage(i18n.getString(success ? Messages.Command.SUCCESSFULLY_VOTED : Messages.Command.ALREADY_VOTED));
	}
	
	@FlagInit
	public static void initFlag(HeavySpleef heavySpleef) {
		ExtensionRegistry registry = heavySpleef.getExtensionRegistry();
		registry.registerExtension(VoteSignExtension.class);
		
		listener = new BlockVoteListener(heavySpleef);
		Bukkit.getPluginManager().registerEvents(listener, heavySpleef.getPlugin());
	}
	
	@Unregister
	public static void deinitFlag(HeavySpleef heavySpleef) {
		ExtensionRegistry registry = heavySpleef.getExtensionRegistry();
		registry.unregister(VoteSignExtension.class);
		
		HandlerList.unregisterAll(listener);
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
	
	@Subscribe(priority = Subscribe.Priority.MONITOR)
	public void onPlayerJoin(PlayerJoinGameEvent event) {
		checkVotes(event.getGame());
	}
	
	@Subscribe(priority = Subscribe.Priority.MONITOR)
	public void onGameCountdown(GameCountdownEvent event) {
		if (event.isCancelled()) {
			return;
		}
		
		voted.clear();
	}
	
	public boolean vote(SpleefPlayer player, Game game) {
		if (voted.contains(player) || game.getGameState() != GameState.LOBBY) {
			return false;
		}
		
		voted.add(player);
		game.broadcast(getI18N().getVarString(Messages.Broadcast.PLAYER_VOTED)
				.setVariable("player", player.getDisplayName())
				.toString());
		
		checkVotes(game);
		return true;
	}
	
	private void checkVotes(Game game) {
		DefaultConfig config = getHeavySpleef().getConfiguration(ConfigType.DEFAULT_CONFIG);
		FlagSection section = config.getFlagSection();
		
		Set<SpleefPlayer> players = game.getPlayers();
		
		int autostartVote = section.getAutostartVote();
		double percentageVoted = (double)voted.size() / players.size();
		
		if (percentageVoted * 100 >= autostartVote && players.size() >= 2 && !game.getGameState().isGameActive()) {
			game.countdown();
		}
	}
	
	@Extension(name = "vote-sign")
	public static class VoteSignExtension extends SignExtension {

		public static final String IDENTIFIER = "vote";
		private final I18N i18n = I18NManager.getGlobal();
		
		@SuppressWarnings("unused")
		private VoteSignExtension() {}
		
		public VoteSignExtension(Location location, PrefixType prefixType) {
			super(location, prefixType);
		}
		
		@Override
		public String[] getPermission() {
			return new String[] {Permissions.PERMISSION_SIGN_VOTE, Permissions.PERMISSION_VOTE};
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
			
			if (game.getGameState() != GameState.LOBBY) {
				player.sendMessage(i18n.getString(Messages.Command.FUNCTION_ONLY_IN_LOBBY));
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
	
	private static class BlockVoteListener implements Listener {
		
		private I18N i18n = I18NManager.getGlobal();
		private HeavySpleef heavySpleef;
		
		public BlockVoteListener(HeavySpleef heavySpleef) {
			this.heavySpleef = heavySpleef;
		}
		
		@SuppressWarnings("deprecation")
		@EventHandler
		public void onPlayerInteract(PlayerInteractEvent event) {
			SpleefPlayer player = heavySpleef.getSpleefPlayer(event.getPlayer());
			GameManager manager = heavySpleef.getGameManager();
			
			Game game = manager.getGame(player);
			if (game == null) {
				return;
			}
			
			if (!game.isFlagPresent(FlagVote.class)) {
				return;
			}
			
			DefaultConfig config = heavySpleef.getConfiguration(ConfigType.DEFAULT_CONFIG);
			FlagSection section = config.getFlagSection();
			MaterialData readyBlock = section.getReadyBlock();
			
			Block clickedBlock = event.getClickedBlock();
			if (clickedBlock == null || clickedBlock.getType() != readyBlock.getItemType() || clickedBlock.getData() != readyBlock.getData()) {
				return;
			}
			
			if (game.getGameState() != GameState.LOBBY) {
				player.sendMessage(i18n.getString(Messages.Command.FUNCTION_ONLY_IN_LOBBY));
				return;
			}
			
			FlagVote flag = game.getFlag(FlagVote.class);
			boolean success = flag.vote(player, game);
			player.sendMessage(i18n.getString(success ? Messages.Command.SUCCESSFULLY_VOTED : Messages.Command.ALREADY_VOTED));
		}
		
	}

}