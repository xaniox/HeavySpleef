package de.xaniox.heavyspleef.core.config;

import org.bukkit.configuration.ConfigurationSection;

public class SpectateSection {

    private boolean enablePvp;
    private boolean respawnInSpectate;
    private boolean showScoreboard;

    public SpectateSection(ConfigurationSection section) {
        this.enablePvp = section.getBoolean("enable-pvp", false);
        this.respawnInSpectate = section.getBoolean("respawn-in-spectate", false);
        this.showScoreboard = section.getBoolean("show-scoreboard", true);
    }

    public boolean isEnablePvp() {
        return enablePvp;
    }

    public boolean isRespawnInSpectate() {
        return respawnInSpectate;
    }

    public boolean isShowScoreboard() {
        return showScoreboard;
    }

}
