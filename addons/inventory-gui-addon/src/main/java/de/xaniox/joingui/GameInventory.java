/*
 * This file is part of addons.
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
package de.xaniox.joingui;

import com.google.common.collect.Lists;
import de.matzefratze123.inventoryguilib.GuiInventory;
import de.matzefratze123.inventoryguilib.GuiInventorySlot;
import de.xaniox.heavyspleef.addon.java.BasicAddOn;
import de.xaniox.heavyspleef.core.HeavySpleef;
import de.xaniox.heavyspleef.core.event.*;
import de.xaniox.heavyspleef.core.game.Game;
import de.xaniox.heavyspleef.core.game.GameManager;
import de.xaniox.heavyspleef.core.i18n.I18N;
import de.xaniox.heavyspleef.core.player.SpleefPlayer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public abstract class GameInventory extends GuiInventory implements SpleefListener {

    private static final GameNameComparator COMPARATOR = new GameNameComparator();
    private final HeavySpleef heavySpleef;
    private final I18N i18n;
    private final GameManager gameManager;
    private List<Game> recentRegisteredGames;
    private InventoryEntryConfig config;

    public GameInventory(BasicAddOn addOn, InventoryEntryConfig invConfig) {
        super(addOn.getHeavySpleef().getPlugin());

        this.heavySpleef = addOn.getHeavySpleef();
        this.i18n = addOn.getI18n();
        this.gameManager = addOn.getHeavySpleef().getGameManager();
        this.config = invConfig;

        init(null);
    }

    public void init(List<Game> games) {
        if (games == null) {
            games = Lists.newArrayList(gameManager.getGames());
            Collections.sort(games, COMPARATOR);
        }

        setTitle(i18n.getString(Messages.INVENTORY_TITLE));
        int lines = (int) Math.ceil(games.size() / 9d);
        setLines(lines == 0 ? 1 : lines);

        int x = 0;
        int y = 0;
        Iterator<Game> iterator = games.iterator();

        while (iterator.hasNext()) {
            Game game = iterator.next();
            if (!canPlaceGame(game)) {
                continue;
            }

            GuiInventorySlot slot = getSlot(x++, y);
            placeGame(slot, game);

            if (x >= SLOTS_PER_LINE) {
                x = 0;
                ++y;
            }
        }
    }

    protected abstract void onGameClicked(Game game, SpleefPlayer player);

    protected boolean canPlaceGame(Game game) {
        return true;
    }

    protected HeavySpleef getHeavySpleef() {
        return heavySpleef;
    }

    protected I18N getI18n() {
        return i18n;
    }

    private void placeGame(GuiInventorySlot slot, Game game) {
        slot.setValue(game);
        updateSlot(slot, game);
    }

    public void update() {
        List<Game> games = Lists.newArrayList(gameManager.getGames());
        Collections.sort(games, COMPARATOR);

        if (!games.equals(recentRegisteredGames)) {
            int lines = (int) Math.ceil(games.size() / 9d);
            if (lines != getLines()) {
                setLines(lines == 0 ? 1 : lines);
            }

            //Games have been added or deleted
            //Clear and wipe the entire inventory
            clearInventory();
            //Re-initialize it
            init(games);

            recentRegisteredGames = games;
        } else {
            //Nothing has been changed, just update already existent slots
            for (int x = 0; x < SLOTS_PER_LINE; x++) {
                for (int y = 0; y < getLines(); y++) {
                    GuiInventorySlot slot = getSlot(x, y);

                    Object value = slot.getValue();
                    if (value == null || !(value instanceof Game)) {
                        continue;
                    }

                    Game game = (Game) value;

                    //Actually update the itemstack with the game data
                    updateSlot(slot, game);
                }
            }
        }

        updateViews();
    }

    @Override
    public void open(Player player) {
        update();

        super.open(player);
    }

    private void updateSlot(GuiInventorySlot slot, Game game) {
        ItemStack stack;

        if (game.isFlagPresent(FlagJoinItem.class)) {
            FlagJoinItem flag = game.getFlag(FlagJoinItem.class);

            stack = flag.getValue().clone();
        } else {
            stack = new ItemStack(Material.DIAMOND_SPADE);
        }

        slot.setItem(stack);

        InventoryEntryLayout layout = config.getLayout();
        layout.inflate(stack, game);
    }

    private void clearInventory() {
        for (int x = 0; x < SLOTS_PER_LINE; x++) {
            for (int y = 0; y < getLines(); y++) {
                GuiInventorySlot slot = getSlot(x, y);
                clearSlot(slot);
            }
        }
    }

    private void clearSlot(GuiInventorySlot slot) {
        slot.setItem((ItemStack) null);
        slot.setValue(null);
    }

    @Subscribe(priority = Subscribe.Priority.MONITOR)
    public void onGameRename(GameRenameEvent event) {
        update();
    }

    @Subscribe(priority = Subscribe.Priority.MONITOR)
    public void onGameStateChange(GameStateChangeEvent event) {
        update();
    }

    @Subscribe(priority = Subscribe.Priority.MONITOR)
    public void onPlayerJoinGame(PlayerJoinGameEvent event) {
        update();
    }

    @Subscribe(priority = Subscribe.Priority.MONITOR)
    public void onPlayerLeaveGame(PlayerLeaveGameEvent event) {
        update();
    }

    @Subscribe(priority = Subscribe.Priority.MONITOR)
    public void onCountdownChange(GameCountdownChangeEvent event) {
        update();
    }

    @Override
    public void onClick(GuiClickEvent event) {
        event.setCancelled(true);

        GuiInventorySlot slot = event.getSlot();
        Object val = slot.getValue();

        if (!(val instanceof Game)) {
            return;
        }

        Game game = (Game) val;
        SpleefPlayer player = heavySpleef.getSpleefPlayer(event.getPlayer());

        onGameClicked(game, player);
    }

    private static class GameNameComparator implements Comparator<Game> {

        @Override
        public int compare(Game o1, Game o2) {
            return o1.getName().compareTo(o2.getName());
        }

    }

}