package de.matzefratze123.heavyspleef.flag.defaults;

import java.util.List;

import de.matzefratze123.heavyspleef.core.event.GameCountdownEvent;
import de.matzefratze123.heavyspleef.core.event.GameListener;
import de.matzefratze123.heavyspleef.core.flag.Flag;
import de.matzefratze123.heavyspleef.flag.presets.IntegerFlag;

@Flag(name = "countdown")
public class FlagCountdown extends IntegerFlag {

	@Override
	public void getDescription(List<String> description) {
		description.add("Defines the countdown length");
	}
	
	@GameListener
	public void onGameCountdown(GameCountdownEvent event) {
		event.setCountdownEnabled(true);
		event.setCountdownLength(getValue());
	}

}
