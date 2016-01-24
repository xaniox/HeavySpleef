package de.matzefratze123.heavyspleef.core.bossbar;

/**
 * Created by Matthias on 17.01.2016.
 */
public class BossbarSession {

    private BossbarManager manager;

    protected BossbarSession(BossbarManager manager) {
        this.manager = manager;
    }

    protected BossbarManager getBossbarManager() {
        return manager;
    }

    public void release() {

    }

}
