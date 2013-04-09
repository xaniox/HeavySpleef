package me.matzefratze123.heavyspleef.listener;

import java.util.HashMap;
import java.util.Map;

import me.matzefratze123.heavyspleef.core.Game;
import me.matzefratze123.heavyspleef.core.GameManager;
import me.matzefratze123.heavyspleef.core.Team;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.kitteh.tag.PlayerReceiveNameTagEvent;

public class TagListener implements Listener {

	private Map<String, String> previousTag = new HashMap<String, String>();
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onNameTag(PlayerReceiveNameTagEvent e) {
		Player player = e.getNamedPlayer();
		
		if (GameManager.isInAnyGame(player)) {
		
			Game game = GameManager.fromPlayer(player);
			Team team = game.getTeam(player);
			
			if (team == null)
				return;
			
			if (!previousTag.containsKey(player.getName()))
				previousTag.put(player.getName(), e.getTag());
			e.setTag(team.getColor() + player.getName());
		} else {
			e.setTag(previousTag.get(player.getName()));
			previousTag.remove(player.getName());
		}
	}
	
}
