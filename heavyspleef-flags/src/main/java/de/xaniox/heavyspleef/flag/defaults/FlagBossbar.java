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
package de.xaniox.heavyspleef.flag.defaults;

import de.xaniox.heavyspleef.core.BasicTask;
import de.xaniox.heavyspleef.core.MinecraftVersion;
import de.xaniox.heavyspleef.core.SimpleBasicTask;
import de.xaniox.heavyspleef.core.event.*;
import de.xaniox.heavyspleef.core.flag.Flag;
import de.xaniox.heavyspleef.core.flag.Inject;
import de.xaniox.heavyspleef.core.game.CountdownTask;
import de.xaniox.heavyspleef.core.game.Game;
import de.xaniox.heavyspleef.core.game.GameState;
import de.xaniox.heavyspleef.core.game.QuitCause;
import de.xaniox.heavyspleef.core.i18n.Messages;
import de.xaniox.heavyspleef.core.player.SpleefPlayer;
import de.xaniox.heavyspleef.flag.presets.BaseFlag;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;

import java.util.List;

@Flag(name = "bossbar", requiresVersion = MinecraftVersion.V1_9_ID)
public class FlagBossbar extends BaseFlag {

    private static final double DEFAULT_PROGRESS = 1.0d;

    @Inject
    private Game game;
    private SafeContainer<BossBar> bossBar;
    private BasicTask lastMessageResetTask;

    private String baseTitle;
    private SafeContainer<BarColor> baseColor;

    @Override
    public void getDescription(List<String> description) {
        description.add("Displays a bossbar with game information");
    }

    @Override
    public void onFlagAdd(Game game) {
        baseTitle = getI18N().getVarString(Messages.Broadcast.BOSSBAR_PLAYING_ON)
                .setVariable("game", game.getName())
                .toString();
        baseColor = new SafeContainer<>(BarColor.GREEN);

        bossBar = new SafeContainer<>(Bukkit.getServer().createBossBar(baseTitle, baseColor.value, BarStyle.SOLID));
        bossBar.value.setProgress(DEFAULT_PROGRESS);
    }

    @Override
    public void onFlagRemove(Game game) {
        //Nullify that reference
        bossBar = null;
    }

    private void requestBaseUpdate() {
        requestBaseUpdate(false);
    }

    private void requestBaseUpdate(boolean force) {
        if (lastMessageResetTask == null && !force) {
            bossBar.value.setTitle(baseTitle);
            bossBar.value.setColor(baseColor.value);
        }
    }

    @Subscribe(priority = Subscribe.Priority.HIGHEST)
    public void onReceiveEvent(GameEvent event) {
        if (event instanceof BossbarUpdateEvent) {
            //Prevent StackOverflowException
            return;
        }

        //Trigger a bossbar update event
        BossbarUpdateEvent bossbarUpdateEvent = new BossbarUpdateEvent(game, event);
        game.getEventBus().callEvent(bossbarUpdateEvent);

        BossbarUpdateEvent.Message message = bossbarUpdateEvent.getMessage();
        if (message != null) {
            bossBar.value.setTitle(message.getTitle());
            bossBar.value.setColor(message.getColor());
            bossBar.value.setProgress(message.getProgress());

            BasicTask resetTask = new ResetBossbarTask(message.getDuration());
            resetTask.start();

            if (lastMessageResetTask != null && lastMessageResetTask.isRunning()) {
                lastMessageResetTask.cancel();
            }

            lastMessageResetTask = resetTask;
        }

        String permMessage = bossbarUpdateEvent.getPermMessage();
        if (permMessage != null) {
            baseTitle = permMessage;
            requestBaseUpdate();
        }
    }

    /* Default bossbar event */
    @Subscribe
    public void onBossbarUpdate(BossbarUpdateEvent event) {
        Event trigger = event.getTrigger();

        if (trigger instanceof PlayerJoinGameEvent) {
            PlayerJoinGameEvent joinEvent = (PlayerJoinGameEvent) trigger;

            BossbarUpdateEvent.Message message = new BossbarUpdateEvent.Message(
                    getI18N().getVarString(Messages.Broadcast.BOSSBAR_PLAYER_JOINED)
                            .setVariable("player", joinEvent.getPlayer().getDisplayName()).toString(),
                    BarColor.GREEN,
                    BossbarUpdateEvent.Message.DEFAULT_DURATION
            );

            event.setMessage(message);
        } else if (trigger instanceof PlayerLeaveGameEvent) {
            PlayerLeaveGameEvent leaveEvent = (PlayerLeaveGameEvent) trigger;
            if (leaveEvent.getCause() == QuitCause.WIN) {
                return;
            }

            BossbarUpdateEvent.Message message = new BossbarUpdateEvent.Message(
                    getI18N().getVarString(leaveEvent.getCause() == QuitCause.SELF ? Messages.Broadcast.BOSSBAR_PLAYER_LEFT :
                            Messages.Broadcast.BOSSBAR_PLAYER_LOST).setVariable("player",
                            leaveEvent.getPlayer().getDisplayName()).toString(),
                    BarColor.RED,
                    BossbarUpdateEvent.Message.DEFAULT_DURATION
            );

            event.setMessage(message);

            if (game.getGameState() == GameState.INGAME) {
                baseTitle = getI18N().getVarString(Messages.Broadcast.BOSSBAR_PLAYERS_LEFT)
                        .setVariable("left", String.valueOf(game.getPlayers().size() - 1))
                        .toString();
            }

            requestBaseUpdate();
        } else if (trigger instanceof GameCountdownChangeEvent) {
            GameCountdownChangeEvent countdownEvent = (GameCountdownChangeEvent) trigger;
            CountdownTask countdown = countdownEvent.getCountdown();

            BossbarUpdateEvent.Message message = new BossbarUpdateEvent.Message(
                    getI18N().getVarString(Messages.Broadcast.BOSSBAR_COUNTDOWN)
                            .setVariable("left", String.valueOf(countdown.getRemaining())).toString(),
                    BarColor.YELLOW,
                    20L + 5L,
                    countdown.getRemaining() / (double) countdown.getLength()
            );

            event.setMessage(message);
        } else if (trigger instanceof GameStartEvent) {
            baseTitle = getI18N().getVarString(Messages.Broadcast.BOSSBAR_PLAYERS_LEFT)
                    .setVariable("left", String.valueOf(game.getPlayers().size()))
                    .toString();

            requestBaseUpdate(true);

            BossbarUpdateEvent.Message message = new BossbarUpdateEvent.Message(
                    getI18N().getString(Messages.Broadcast.BOSSBAR_GO),
                    BarColor.YELLOW,
                    2 * 20L
            );

            event.setMessage(message);
        } else if (trigger instanceof GameEndEvent) {
            baseTitle = getI18N().getVarString(Messages.Broadcast.BOSSBAR_PLAYING_ON)
                    .setVariable("game", game.getName())
                    .toString();

            requestBaseUpdate();
        }
    }

    @Subscribe
    public void onPlayerJoinGame(PlayerJoinGameEvent event) {
        addToBossbar(event.getPlayer());
    }

    @Subscribe
    public void onPlayerLeaveGame(PlayerLeaveGameEvent event) {
        removeFromBossbar(event.getPlayer());
    }

    @Subscribe
    public void onSpectateEnter(FlagSpectate.SpectateEnterEvent event) {
        addToBossbar(event.getPlayer());
    }

    @Subscribe
    public void onSpectateLeave(FlagSpectate.SpectateLeaveEvent event) {
        removeFromBossbar(event.getPlayer());
    }

    public void addToBossbar(SpleefPlayer player) {
        bossBar.value.addPlayer(player.getBukkitPlayer());
    }

    public void removeFromBossbar(SpleefPlayer player) {
        bossBar.value.removePlayer(player.getBukkitPlayer());
    }

    public static class BossbarUpdateEvent extends GameEvent {

        private Event triggeredBy;
        private String permMessage;
        private Message message;

        public BossbarUpdateEvent(Game game, Event triggeredBy) {
            super(game);

            this.triggeredBy = triggeredBy;
        }

        public Event getTrigger() {
            return triggeredBy;
        }

        public String getPermMessage() {
            return permMessage;
        }

        public void setPermMessage(String permMessage) {
            this.permMessage = permMessage;
        }

        public Message getMessage() {
            return message;
        }

        public void setMessage(Message message) {
            this.message = message;
        }

        public static class Message {

            //Default temp message duration is 3 seconds
            public static final long DEFAULT_DURATION = 3 * 20L;

            private String title;
            private SafeContainer<BarColor> color;
            private double progress;
            private long duration;

            public Message(String title, BarColor color, long duration) {
                this(title, color, duration, DEFAULT_PROGRESS);
            }

            public Message(String title, BarColor color, long duration, double progress) {
                this.title = title;
                this.color = new SafeContainer<>(color);
                this.progress = progress;
                this.duration = duration;
            }

            public String getTitle() {
                return title;
            }

            public BarColor getColor() {
                return color.value;
            }

            public long getDuration() {
                return duration;
            }

            public double getProgress() {
                return progress;
            }
        }

    }

    private class ResetBossbarTask extends SimpleBasicTask {

        public ResetBossbarTask(long duration) {
            super(getHeavySpleef().getPlugin(), TaskType.SYNC_DELAYED_TASK, duration);
        }

        @Override
        public void run() {
            //Reset to base title
            bossBar.value.setColor(baseColor.value);
            bossBar.value.setTitle(baseTitle);
            double baseProgress = DEFAULT_PROGRESS;
            bossBar.value.setProgress(baseProgress);

            lastMessageResetTask = null;
        }

    }

    private static class SafeContainer<T> {

        private T value;

        public SafeContainer(T value) {
            this.value = value;
        }

    }

}
