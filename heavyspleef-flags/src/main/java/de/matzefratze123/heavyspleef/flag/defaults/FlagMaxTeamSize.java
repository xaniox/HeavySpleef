package de.matzefratze123.heavyspleef.flag.defaults;

import java.util.List;
import java.util.Map;

import de.matzefratze123.heavyspleef.core.GameProperty;
import de.matzefratze123.heavyspleef.core.flag.Flag;
import de.matzefratze123.heavyspleef.flag.presets.IntegerFlag;

@Flag(name = "max-players", parent = FlagTeam.class)
public class FlagMaxTeamSize extends IntegerFlag {

	@Override
	public void defineGameProperties(Map<GameProperty, Object> properties) {}

	@Override
	public boolean hasGameProperties() {
		return false;
	}

	@Override
	public boolean hasBukkitListenerMethods() {
		return false;
	}

	@Override
	public void getDescription(List<String> description) {
		description.add("Sets the count of maximum players which are allowed to join a team");
	}

}
