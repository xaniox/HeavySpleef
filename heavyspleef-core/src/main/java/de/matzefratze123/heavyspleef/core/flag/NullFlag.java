package de.matzefratze123.heavyspleef.core.flag;

import java.util.List;
import java.util.Map;

import org.bukkit.entity.Player;
import org.dom4j.Element;

import de.matzefratze123.heavyspleef.core.GameProperty;

public class NullFlag extends AbstractFlag<Void> {

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
	public void getDescription(List<String> description) {}

	@Override
	public Void parseInput(Player player, String input) throws InputParseException {
		return null;
	}

	@Override
	public void marshal(Element element) {}

	@Override
	public void unmarshal(Element element) {}

}
