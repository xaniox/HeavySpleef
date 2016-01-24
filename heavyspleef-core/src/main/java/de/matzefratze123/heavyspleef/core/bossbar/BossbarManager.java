package de.matzefratze123.heavyspleef.core.bossbar;

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

    public BossbarSession newSession() {
        return null;
    }

}
