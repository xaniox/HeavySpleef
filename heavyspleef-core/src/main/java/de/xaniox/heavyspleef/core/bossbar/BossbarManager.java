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
package de.xaniox.heavyspleef.core.bossbar;

import org.bukkit.Bukkit;

import java.lang.reflect.Field;

public class BossbarManager {

    private static int NON_INITIALIZED = -1;
    private int bossEntityId = NON_INITIALIZED;

    public BossbarManager() {
        initializeEntityId();
    }

    private void initializeEntityId() {
        String packageVersionPart = Bukkit.getServer().getClass().getName().split("\\.")[3];
        Field field = null;
        boolean accessible = false;

        try {
            Class<?> entityClass = Class.forName("net.minecraft.server." + packageVersionPart + ".Entity");
            field = entityClass.getDeclaredField("entityCount");
            accessible = field.isAccessible();

            field.setAccessible(true);
            bossEntityId = field.getInt(null);
            field.set(null, bossEntityId + 1);
        } catch (ReflectiveOperationException e) {

        } finally {
            if (field != null) {
                field.setAccessible(accessible);
            }
        }
    }

    /*public BossbarSession newSession() {
        return null;
    }*/

}