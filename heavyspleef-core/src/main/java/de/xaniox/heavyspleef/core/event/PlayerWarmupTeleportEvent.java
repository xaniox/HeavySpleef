package de.xaniox.heavyspleef.core.event;

import de.xaniox.heavyspleef.core.game.Game;
import de.xaniox.heavyspleef.core.player.SpleefPlayer;
import org.bukkit.Location;

public class PlayerWarmupTeleportEvent extends PlayerGameEvent {

    public PlayerWarmupTeleportEvent(Game game, SpleefPlayer player, Location where) {
        super(game, player);
    }

}
