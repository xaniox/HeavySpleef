package de.matzefratze123.heavyspleef.flag.defaults;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.google.common.collect.Lists;

import de.matzefratze123.heavyspleef.core.event.GameCountdownEvent;
import de.matzefratze123.heavyspleef.core.event.GameListener;
import de.matzefratze123.heavyspleef.core.flag.Flag;
import de.matzefratze123.heavyspleef.core.flag.InputParseException;
import de.matzefratze123.heavyspleef.core.flag.NullFlag;
import de.matzefratze123.heavyspleef.flag.presets.LocationFlag;
import de.matzefratze123.heavyspleef.flag.presets.LocationListFlag;

@Flag(name = "multi-spawn", ignoreParseException = true)
public class FlagMultiSpawnpoint extends LocationListFlag {

	public FlagMultiSpawnpoint() {
		List<Location> list = Lists.newArrayList();
		setValue(list);
	}
	
	@Override
	public List<Location> parseInput(Player player, String input) throws InputParseException {
		throw new InputParseException("Use multi-spawn:add to add a spawnpoint and multi-spawn:remove to remove the recent spawnpoint");
	}
	
	@Override
	public void getDescription(List<String> description) {
		description.add("Defines multiple spawnpoints for players");
	}
	
	@GameListener
	public void onGameCountdown(GameCountdownEvent event) {
		List<Location> list = getValue();
		event.setSpawnLocations(list);
	}
	
	@Flag(name = "add", parent = FlagMultiSpawnpoint.class)
	public static class FlagAddSpawnpoint extends LocationFlag {
		
		@Override
		public void setValue(Location value) {
			FlagMultiSpawnpoint parent = (FlagMultiSpawnpoint) getParent();
			parent.add(value);
		}

		@Override
		public void getDescription(List<String> description) {
			description.add("Adds a spawnpoint to the list of spawnpoints");
		}
		
	}
	
	@Flag(name = "remove", parent = FlagMultiSpawnpoint.class)
	public static class FlagRemoveSpawnpoint extends NullFlag {
		
		@Override
		public void setValue(Void value) {
			FlagMultiSpawnpoint parent = (FlagMultiSpawnpoint) getParent();
			int lastIndex = parent.size() - 1;
			
			parent.remove(lastIndex);
		}
		
		@Override
		public void getDescription(List<String> description) {
			description.add("Removes the recent spawnpoint");
		}
		
	}
	
}