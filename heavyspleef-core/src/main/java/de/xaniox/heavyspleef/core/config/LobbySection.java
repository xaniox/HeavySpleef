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
