package de.xaniox.heavyspleef.core.event;

import de.xaniox.heavyspleef.core.game.Game;
import de.xaniox.heavyspleef.core.player.SpleefPlayer;

public class UpdateLobbyItemsEvent extends PlayerGameEvent {

    public UpdateLobbyItemsEvent(Game game, SpleefPlayer player) {
        super(game, player);
    }

}
