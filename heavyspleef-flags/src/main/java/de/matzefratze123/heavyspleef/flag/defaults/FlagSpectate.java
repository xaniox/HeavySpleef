package de.matzefratze123.heavyspleef.flag.defaults;

import java.util.List;
import java.util.Set;

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
import de.matzefratze123.heavyspleef.core.i18n.Messages;
import de.matzefratze123.heavyspleef.core.player.PlayerStateHolder;
import de.matzefratze123.heavyspleef.core.player.SpleefPlayer;
import de.matzefratze123.heavyspleef.flag.presets.LocationFlag;

public class FlagSpectate extends LocationFlag {
	
	private Set<SpleefPlayer> spectators;
	
	@Command(name = "spectate", description = "Spectates a spleef game", minArgs = 1, 
			usage = "/spleef spectate <game>", permission = "heavyspleef.spectate")
	@PlayerOnly
	public static void onSpectateCommand(CommandContext context, HeavySpleef heavySpleef) throws CommandException {
		Player player = context.getSender();
		SpleefPlayer spleefPlayer = heavySpleef.getSpleefPlayer(player);
		String gameName = context.getString(0);
		
		GameManager manager = heavySpleef.getGameManager();
		CommandValidate.isTrue(manager.hasGame(gameName), heavySpleef.getVarMessage(Messages.Command.GAME_DOESNT_EXIST)
				.setVariable("game", gameName)
				.toString());
		
		Game game = manager.getGame(gameName);
		FlagSpectate spectateFlag = game.getFlag(FlagSpectate.class);
		
		CommandValidate.isTrue(spectateFlag != null, null); //TODO: Add message
		
		if (spectateFlag.isSpectating(spleefPlayer)) {			
			spectateFlag.spectate(spleefPlayer, game);
			spleefPlayer.sendMessage(null); //TODO: Add message
		} else {
			spectateFlag.leave(spleefPlayer);
			spleefPlayer.sendMessage(null); //TODO: Add message
		}
	}
	
	public FlagSpectate() {
		this.spectators = Sets.newHashSet();
	}
	
	@Override
	public void getDescription(List<String> description) {
		description.add("Enables the spectate mode for Spleef");
	}
	
	public void spectate(SpleefPlayer player, Game game) {
		player.savePlayerState(this);
		PlayerStateHolder.applyDefaultState(player.getBukkitPlayer());
		
		player.teleport(getValue());
		FlagAllowSpectateFly allowFlyFlag = getChildFlag(FlagAllowSpectateFly.class, game);
		if (allowFlyFlag != null) {
			allowFlyFlag.onSpectateEnter(player);
		}		
	}
	
	public void leave(SpleefPlayer player) {
		PlayerStateHolder state = player.getPlayerState(this);
		state.apply(player.getBukkitPlayer(), false);
		player.sendMessage(null); //TODO: Add message
	}
	
	public boolean isSpectating(SpleefPlayer player) {
		return spectators.contains(player);
	}
	
}