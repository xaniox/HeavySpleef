package de.matzefratze123.heavyspleef.flag.defaults;

import java.util.List;

import com.google.common.collect.Lists;

import de.matzefratze123.heavyspleef.core.event.GameCountdownEvent;
import de.matzefratze123.heavyspleef.core.event.GameListener;
import de.matzefratze123.heavyspleef.core.flag.Flag;
import de.matzefratze123.heavyspleef.flag.presets.LocationFlag;

@Flag(name = "spawnpoint")
public class FlagSpawnpoint extends LocationFlag {

	@Override
	public void getDescription(List<String> description) {
		description.add("Defines the spawnpoint for players when a game starts to countdown");
	}
	
	@GameListener
	public void onGameCountdown(GameCountdownEvent event) {
		if (getValue() == null) {
			return;
		}
		
		event.setSpawnLocations(Lists.newArrayList(getValue()));
	}

}
