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
package de.xaniox.heavyspleef.core.floor;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

public class FloorRegeneratorFactory {

    private static final String FAWE_PLUGIN_NAME = "FastAsyncWorldEdit";

    private final FloorRegenerator defaultRegenerator = new DefaultFloorRegenerator();
    private final FloorRegenerator faweRegenerator = new FAWEFloorRegenerator();

    public FloorRegenerator retrieveRegeneratorInstance() {
        //If FastAsyncWorldEdit is installed, use a special fawe designed
        //generator for compatibility
        PluginManager pluginManager = Bukkit.getPluginManager();
        Plugin fawePlugin = pluginManager.getPlugin(FAWE_PLUGIN_NAME);
        if (fawePlugin != null && fawePlugin.isEnabled()) {
            return faweRegenerator;
        }

        return defaultRegenerator;
    }

}
