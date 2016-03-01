/*
 * This file is part of HeavySpleef.
 * Copyright (c) 2014-2016 Matthias Werning
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package de.xaniox.heavyspleef.flag.defaults;

import de.xaniox.heavyspleef.core.event.Subscribe;
import de.xaniox.heavyspleef.core.flag.Flag;
import de.xaniox.heavyspleef.core.flag.ValidationException;
import de.xaniox.heavyspleef.core.i18n.Messages;
import de.xaniox.heavyspleef.flag.presets.IntegerFlag;

import java.util.List;

@Flag(name = "min-players", parent = FlagTeam.class)
public class FlagMinTeamSize extends IntegerFlag {

	@Override
	public void getDescription(List<String> description) {
		description.add("Sets the count of minimum players which are needed to start the game");
	}
	
	@Override
	public void validateInput(Integer input) throws ValidationException {
		if (input <= 1) {
			throw new ValidationException(getI18N().getString(Messages.Command.INVALID_TEAM_MIN_SIZE));
		}
	}
	
	@Subscribe
	public void validateTeams(FlagTeam.ValidateTeamsEvent event) {
		int minSize = getValue();
		
		for (FlagTeam.TeamSizeHolder holder : event.getTeams()) {
			if (holder.getSize() < minSize) {
				//Too few players
				event.setCancelled(true);
				event.setErrorMessage(getI18N().getVarString(Messages.Broadcast.TOO_FEW_PLAYERS_TEAM)
						.setVariable("amount", String.valueOf(minSize))
						.toString());
				return;
			}
		}
	}

}