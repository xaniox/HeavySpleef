package de.matzefratze123.api.command.transform;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class PlayerTransformer implements Transformer<Player> {

	@Override
	public Player transform(String argument) throws TransformException {
		Player player = Bukkit.getPlayer(argument);
		
		if (player == null) {
			throw new TransformException();
		}
		
		return player;
	}

}
