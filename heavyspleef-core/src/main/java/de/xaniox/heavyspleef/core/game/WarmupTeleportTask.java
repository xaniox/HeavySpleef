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
package de.xaniox.heavyspleef.core.game;

import de.xaniox.heavyspleef.core.SimpleBasicTask;
import de.xaniox.heavyspleef.core.event.PlayerWarmupTeleportEvent;
import de.xaniox.heavyspleef.core.player.SpleefPlayer;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;

import java.util.Iterator;
import java.util.Map;
import java.util.Random;

public class WarmupTeleportTask extends SimpleBasicTask {

    private final Random random = new Random();
    private Game game;
    private Map<SpleefPlayer, Location> teleportMap;
    private WarmupFinishedListener finishListener;

    public WarmupTeleportTask(Plugin plugin, Game game, Map<SpleefPlayer, Location> teleportMap, long warmupTimeInterval) {
        super(plugin, TaskType.SYNC_REPEATING_TASK, warmupTimeInterval, warmupTimeInterval);

        this.game = game;
        this.teleportMap = teleportMap;
    }

    public void setFinishedListener(WarmupFinishedListener finishListener) {
        this.finishListener = finishListener;
    }

    @Override
    public void run() {
        int ri = random.nextInt(teleportMap.size());
        Iterator<Map.Entry<SpleefPlayer, Location>> iterator = teleportMap.entrySet().iterator();
        Map.Entry<SpleefPlayer, Location> entry = null;

        for (int i = 0; i <= ri; i++) {
            if (i != ri) {
                iterator.next();
                continue;
            }

            entry = iterator.next();
        }

        if (entry == null) {
            return;
        }

        SpleefPlayer player = entry.getKey();
        Location location = entry.getValue();
        teleportMap.remove(player);

        if (!player.isOnline()) {
            checkTriggerWarmupFinished();
            return;
        }

        player.teleport(location);

        PlayerWarmupTeleportEvent event = new PlayerWarmupTeleportEvent(game, player, location);
        game.getEventBus().callEvent(event);

        checkTriggerWarmupFinished();
    }

    private void checkTriggerWarmupFinished() {
        if (teleportMap.size() == 0) {
            cancel();

            if (finishListener != null) {
                finishListener.warmupFinished();
            }
        }
    }

    public interface WarmupFinishedListener {

        void warmupFinished();

    }

}
