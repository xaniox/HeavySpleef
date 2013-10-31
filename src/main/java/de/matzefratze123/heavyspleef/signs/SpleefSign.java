package de.matzefratze123.heavyspleef.signs;

import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.block.SignChangeEvent;

import de.matzefratze123.heavyspleef.util.Permissions;

public interface SpleefSign {
	
	public void onClick(Player player, Sign sign);
	
	public void onPlace(SignChangeEvent event);
	
	public String getId();
	
	public String[] getLines();
	
	public Permissions getPermission();
	
}
