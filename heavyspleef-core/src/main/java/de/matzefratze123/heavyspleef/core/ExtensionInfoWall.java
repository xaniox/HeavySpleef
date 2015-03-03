package de.matzefratze123.heavyspleef.core;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import de.matzefratze123.heavyspleef.commands.base.Command;
import de.matzefratze123.heavyspleef.commands.base.CommandContext;
import de.matzefratze123.heavyspleef.commands.base.CommandException;
import de.matzefratze123.heavyspleef.commands.base.CommandValidate;
import de.matzefratze123.heavyspleef.commands.base.PlayerOnly;
import de.matzefratze123.heavyspleef.core.PlayerPostActionHandler.PostActionCallback;
import de.matzefratze123.heavyspleef.core.PlayerPostActionHandler.PostActionType;
import de.matzefratze123.heavyspleef.core.i18n.I18N;
import de.matzefratze123.heavyspleef.core.i18n.Messages;
import de.matzefratze123.heavyspleef.core.player.SpleefPlayer;

public class ExtensionInfoWall implements GameExtension {

	@Command(name = "addwall", permission = "heavyspleef.addwall", 
			description = "Adds a wall consisting of signs to show the status of a particular game", 
			usage = "/spleef addwall <game>", minArgs = 1)
	@PlayerOnly
	public static void onAddWallCommand(CommandContext context, HeavySpleef heavySpleef) throws CommandException {
		SpleefPlayer player = heavySpleef.getSpleefPlayer(context.getSender());
		GameManager manager = heavySpleef.getGameManager();
		final I18N i18n = I18N.getInstance();
		
		String gameName = context.getString(0);
		CommandValidate.isTrue(manager.hasGame(gameName), i18n.getVarString(Messages.Command.GAME_DOESNT_EXIST)
				.setVariable("game", gameName)
				.toString());
		
		Game game = manager.getGame(gameName);
		heavySpleef.getPostActionHandler().addPostAction(player, PostActionType.PLAYER_INTERACT, new PostActionCallback() {

			@Override
			public void onPostAction(SpleefPlayer player, Object cookie) {
				//TODO: Add wall
			}
			
		}, game);
	}
	
	@Command(name = "removewall", permission = "heavyspleef.removewall",
			description = "Removes a wall by right-clicking it", usage = "/spleef removewall")
	@PlayerOnly
	public static void onRemoveWallCommand(CommandContext context, HeavySpleef heavySpleef) throws CommandException {
		
	}
	
	private Location start;
	private Location end;
	private BaseDirection direction;
	
	public ExtensionInfoWall(Location start, Location end) {
		this.start = start;
		this.end = end;
	}
	
	public enum BaseDirection {
		
		NORTH,
		SOUTH,
		WEST,
		EAST;
		
	}
	
}
