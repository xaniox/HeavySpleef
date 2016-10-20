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
package de.xaniox.heavyspleef.flag;

import de.xaniox.heavyspleef.core.HeavySpleef;
import de.xaniox.heavyspleef.core.flag.FlagRegistry;
import de.xaniox.heavyspleef.core.module.SimpleModule;
import de.xaniox.heavyspleef.flag.defaults.*;
import de.xaniox.heavyspleef.flag.defaults.FlagTeam.FlagTeamSpawnpoint;

public class FlagModule extends SimpleModule {
	
	public FlagModule(HeavySpleef heavySpleef) {
		super(heavySpleef);
	}

	@Override
	public void enable() {
		FlagRegistry registry = getHeavySpleef().getFlagRegistry();
		
		registry.registerFlag(FlagAllowSpectateFly.class);
		registry.registerFlag(FlagAntiCamping.class);
		registry.registerFlag(FlagAutoSpectate.class);
		registry.registerFlag(FlagAutostart.class);
		registry.registerFlag(FlagAutoQueue.class);
        registry.registerFlag(FlagBossbar.class);
		registry.registerFlag(FlagBowspleef.class);
		registry.registerFlag(FlagCountdown.class);
		registry.registerFlag(FlagCountdownTitles.class);
		registry.registerFlag(FlagEntryFee.class);
		registry.registerFlag(FlagFireworks.class);
		registry.registerFlag(FlagFireworks.FlagAddFirework.class);
		registry.registerFlag(FlagFireworks.FlagRemoveFirework.class);
		registry.registerFlag(FlagFreeze.class);
		registry.registerFlag(FlagInvisibleSpectate.class);
		registry.registerFlag(FlagItemReward.class);
		registry.registerFlag(FlagItemReward.FlagAddItemReward.class);
		registry.registerFlag(FlagItemReward.FlagRemoveItemReward.class);
		registry.registerFlag(FlagJackpot.class);
		registry.registerFlag(FlagLeaveItem.class);
		registry.registerFlag(FlagLeavepoint.class);
		registry.registerFlag(FlagLobby.class);
		registry.registerFlag(FlagLosePoint.class);
		registry.registerFlag(FlagMaxPlayers.class);
		registry.registerFlag(FlagMaxTeamSize.class);
		registry.registerFlag(FlagMinPlayers.class);
		registry.registerFlag(FlagMinTeamSize.class);
		registry.registerFlag(FlagMultiSpawnpoint.class);
		registry.registerFlag(FlagMultiSpawnpoint.FlagAddSpawnpoint.class);
		registry.registerFlag(FlagMultiSpawnpoint.FlagRemoveSpawnpoint.class);
		registry.registerFlag(FlagQueueLobby.class);
		registry.registerFlag(FlagQueueLobbyLeavePoint.class);
		registry.registerFlag(FlagRegen.class);
		registry.registerFlag(FlagRegenPercentage.class);
		registry.registerFlag(FlagReward.class);
		registry.registerFlag(FlagScoreboard.class);
		registry.registerFlag(FlagShears.class);
		registry.registerFlag(FlagShovels.class);
		registry.registerFlag(FlagShowBarriers.class);
		registry.registerFlag(FlagSnowballs.class);
		registry.registerFlag(FlagSpawnpoint.class);
		registry.registerFlag(FlagSpectate.class);
		registry.registerFlag(FlagSpectateLeavepoint.class);
		registry.registerFlag(FlagSplegg.class);
		registry.registerFlag(FlagTeam.class);
		registry.registerFlag(FlagTeamLeatherArmor.class);
		registry.registerFlag(FlagTeamScoreboard.class);
		registry.registerFlag(FlagTeamSpawnpoint.class);
		registry.registerFlag(FlagTimeout.class);
		registry.registerFlag(FlagTrackingSpectate.class);
		registry.registerFlag(FlagWinPoint.class);
		registry.registerFlag(FlagVote.class);
	}
	
	@Override
	public void reload() {
		
	}

	@Override
	public void disable() {
		
	}

}