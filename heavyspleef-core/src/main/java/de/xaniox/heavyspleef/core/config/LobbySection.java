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
package de.xaniox.heavyspleef.core.config;

import org.bukkit.configuration.ConfigurationSection;

/**
 * Created by Matthias on 30.03.2016 - heavyspleef
 */
public class LobbySection {

    private boolean enablePvp;
    private boolean respawnInSpectate;

    public LobbySection(ConfigurationSection section) {
        this.enablePvp = section.getBoolean("enable-pvp", false);
        this.respawnInSpectate = section.getBoolean("respawn-in-lobby", false);
    }

    public boolean isEnablePvp() {
        return enablePvp;
    }

    public boolean isRespawnInSpectate() {
        return respawnInSpectate;
    }

}
