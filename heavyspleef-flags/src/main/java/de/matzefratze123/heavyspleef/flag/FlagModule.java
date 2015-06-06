/*
 * This file is part of HeavySpleef.
 * Copyright (c) 2014-2015 matzefratze123
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
package de.matzefratze123.heavyspleef.flag;

import de.matzefratze123.heavyspleef.core.HeavySpleef;
import de.matzefratze123.heavyspleef.core.flag.FlagRegistry;
import de.matzefratze123.heavyspleef.core.module.SimpleModule;
import de.matzefratze123.heavyspleef.flag.defaults.FlagAllowSpectateFly;
import de.matzefratze123.heavyspleef.flag.defaults.FlagAutoRegen;
import de.matzefratze123.heavyspleef.flag.defaults.FlagAutostart;
import de.matzefratze123.heavyspleef.flag.defaults.FlagBowspleef;
import de.matzefratze123.heavyspleef.flag.defaults.FlagCountdown;
import de.matzefratze123.heavyspleef.flag.defaults.FlagEntryFee;
import de.matzefratze123.heavyspleef.flag.defaults.FlagFreeze;
import de.matzefratze123.heavyspleef.flag.defaults.FlagItemReward;
import de.matzefratze123.heavyspleef.flag.defaults.FlagJackpot;
import de.matzefratze123.heavyspleef.flag.defaults.FlagLeavepoint;
import de.matzefratze123.heavyspleef.flag.defaults.FlagLobby;
import de.matzefratze123.heavyspleef.flag.defaults.FlagLosePoint;
import de.matzefratze123.heavyspleef.flag.defaults.FlagMaxPlayers;
import de.matzefratze123.heavyspleef.flag.defaults.FlagMaxTeamSize;
import de.matzefratze123.heavyspleef.flag.defaults.FlagMinPlayers;
import de.matzefratze123.heavyspleef.flag.defaults.FlagMultiSpawnpoint;
import de.matzefratze123.heavyspleef.flag.defaults.FlagQueueLobby;
import de.matzefratze123.heavyspleef.flag.defaults.FlagRegen;
import de.matzefratze123.heavyspleef.flag.defaults.FlagReward;
import de.matzefratze123.heavyspleef.flag.defaults.FlagScoreboard;
import de.matzefratze123.heavyspleef.flag.defaults.FlagShears;
import de.matzefratze123.heavyspleef.flag.defaults.FlagShovels;
import de.matzefratze123.heavyspleef.flag.defaults.FlagSnowballs;
import de.matzefratze123.heavyspleef.flag.defaults.FlagSpawnpoint;
import de.matzefratze123.heavyspleef.flag.defaults.FlagSpectate;
import de.matzefratze123.heavyspleef.flag.defaults.FlagSplegg;
import de.matzefratze123.heavyspleef.flag.defaults.FlagTeam;
import de.matzefratze123.heavyspleef.flag.defaults.FlagTimeout;
import de.matzefratze123.heavyspleef.flag.defaults.FlagWinPoint;

public class FlagModule extends SimpleModule {
	
	public FlagModule(HeavySpleef heavySpleef) {
		super(heavySpleef);
	}

	@Override
	public void enable() {
		FlagRegistry registry = getHeavySpleef().getFlagRegistry();
		
		registry.registerFlag(FlagAllowSpectateFly.class);
		registry.registerFlag(FlagAutoRegen.class);
		registry.registerFlag(FlagAutostart.class);
		registry.registerFlag(FlagBowspleef.class);
		registry.registerFlag(FlagCountdown.class);
		registry.registerFlag(FlagEntryFee.class);
		registry.registerFlag(FlagFreeze.class);
		registry.registerFlag(FlagItemReward.class);
		registry.registerFlag(FlagItemReward.FlagAddItemReward.class);
		registry.registerFlag(FlagItemReward.FlagRemoveItemReward.class);
		registry.registerFlag(FlagJackpot.class);
		registry.registerFlag(FlagLeavepoint.class);
		registry.registerFlag(FlagLobby.class);
		registry.registerFlag(FlagLosePoint.class);
		registry.registerFlag(FlagMaxPlayers.class);
		registry.registerFlag(FlagMaxTeamSize.class);
		registry.registerFlag(FlagMinPlayers.class);
		registry.registerFlag(FlagMultiSpawnpoint.class);
		registry.registerFlag(FlagMultiSpawnpoint.FlagAddSpawnpoint.class);
		registry.registerFlag(FlagMultiSpawnpoint.FlagRemoveSpawnpoint.class);
		registry.registerFlag(FlagQueueLobby.class);
		registry.registerFlag(FlagRegen.class);
		registry.registerFlag(FlagReward.class);
		registry.registerFlag(FlagScoreboard.class);
		registry.registerFlag(FlagShears.class);
		registry.registerFlag(FlagShovels.class);
		registry.registerFlag(FlagSnowballs.class);
		registry.registerFlag(FlagSpawnpoint.class);
		registry.registerFlag(FlagSpectate.class);
		registry.registerFlag(FlagSplegg.class);
		registry.registerFlag(FlagTeam.class);
		registry.registerFlag(FlagTimeout.class);
		registry.registerFlag(FlagWinPoint.class);
	}
	
	@Override
	public void reload() {
		
	}

	@Override
	public void disable() {
		
	}

}
