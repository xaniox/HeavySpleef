package de.xaniox.heavyspleef.flag.defaults;

import java.util.List;

import org.bukkit.Location;

import de.xaniox.heavyspleef.core.event.Subscribe;
import de.xaniox.heavyspleef.core.flag.Flag;
import de.xaniox.heavyspleef.flag.defaults.FlagSpectate.SpectateLeaveEvent;
import de.xaniox.heavyspleef.flag.presets.LocationFlag;

@Flag(name = "leavepoint", parent = FlagSpectate.class)
public class FlagSpectateLeavepoint extends LocationFlag {

	@Override
	public void getDescription(List<String> description) {
		description.add("Sets the leaving point for spectators");
	}
	
	@Subscribe
	public void onSpectateLeave(SpectateLeaveEvent event) {
		Location val = getValue();
		if (val == null) {
			return;
		}
		
		event.setTeleportationLocation(val);
	}

}
