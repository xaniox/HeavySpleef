package de.matzefratze123.heavyspleef.signs;

import org.bukkit.block.Sign;
import org.bukkit.event.block.SignChangeEvent;

import de.matzefratze123.heavyspleef.objects.SpleefPlayer;
import de.matzefratze123.heavyspleef.util.Permissions;

public interface SpleefSign {
	
	public void onClick(SpleefPlayer player, Sign sign);
	
	public void onPlace(SignChangeEvent event);
	
	public String getId();
	
	public String[] getLines();
	
	public Permissions getPermission();
	
}
