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
package de.xaniox.wincommand;

import com.google.common.collect.Lists;
import de.xaniox.heavyspleef.core.event.PlayerWinGameEvent;
import de.xaniox.heavyspleef.core.event.Subscribe;
import de.xaniox.heavyspleef.core.flag.AbstractFlag;
import de.xaniox.heavyspleef.core.flag.Flag;
import de.xaniox.heavyspleef.core.flag.InputParseException;
import de.xaniox.heavyspleef.core.game.Game;
import de.xaniox.heavyspleef.core.player.SpleefPlayer;
import de.xaniox.heavyspleef.flag.presets.IntegerFlag;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.dom4j.Element;

import java.util.Iterator;
import java.util.List;

@Flag(name = "win-command")
public class FlagWinCommand extends PairListFlag<Integer, String> {

    private static final String PLAYER_VARIABLE_1 = "@p";
    private static final String PLAYER_VARIABLE_2 = "%player";
    private static final String GAME_VARIABLE = "@g";

    @Override
    public List<Pair<Integer, String>> parseInput(SpleefPlayer player, String input) throws InputParseException {
        if (input.trim().isEmpty()) {
            sendInstructions(player);
            return Lists.newArrayList();
        }

        List<Pair<Integer, String>> list = Lists.newArrayList();
        list.add(FlagAddWinCommand.parseInputStatically(player, input));
        sendInstructions(player);
        return list;
    }

    private void sendInstructions(SpleefPlayer player) {
        player.sendMessage(ChatColor.GRAY + "Set commands via /spleef flag <game> win-command:set <place> <command>");
        player.sendMessage(ChatColor.GRAY + "You may use @p for a player and @g for a game reference");
        player.sendMessage(ChatColor.GRAY + "For example: To define the command for the 2nd place you use:");
        player.sendMessage(ChatColor.GRAY + "/spleef flag <game> win-command:set 2 say @p won the game on arena @g");
    }

    @Override
    public void getDescription(List<String> description) {
        description.add("Sets a command which will be executed by the console for specific winners");
    }

    @Override
    public void marshalKey(Element element, Integer key) {
        element.setText(String.valueOf(key.intValue()));
    }

    @Override
    public Integer unmarshalKey(Element element) {
        return Integer.parseInt(element.getText());
    }

    @Override
    public void marshalValue(Element element, String value) {
        element.setText(value);
    }

    @Override
    public String unmarshalValue(Element element) {
        return element.getText();
    }

    public void setCommand(Pair<Integer, String> pair) {
        List<Pair<Integer, String>> pairs = getValue();
        Iterator<Pair<Integer, String>> iterator = pairs.iterator();

        while (iterator.hasNext()) {
            Pair<Integer, String> listPair = iterator.next();

            if (listPair.getKey().intValue() == pair.getKey().intValue()) {
                iterator.remove();
            }
        }

        pairs.add(pair);
    }

    public void removeCommand(int place) {
        List<Pair<Integer, String>> pairs = getValue();
        Iterator<Pair<Integer, String>> iterator = pairs.iterator();

        while (iterator.hasNext()) {
            Pair<Integer, String> listPair = iterator.next();

            if (listPair.getKey() == place) {
                iterator.remove();
            }
        }
    }

    public String getCommand(int place) {
        for (Pair<Integer, String> pair : getValue()) {
            if (pair.getKey() == place) {
                return pair.getValue();
            }
        }

        return null;
    }

    @Subscribe
    public void onPlayerWinGame(PlayerWinGameEvent event) {
        ConsoleCommandSender console = Bukkit.getConsoleSender();
        Game game = event.getGame();

        String winnerCommand = getCommand(1);
        if (winnerCommand != null) {
            for (SpleefPlayer winner : event.getWinners()) {
                String inflated = inflateCommand(winnerCommand, winner, game);
                Bukkit.dispatchCommand(console, inflated);
            }
        }

        List<SpleefPlayer> losers = event.getLosePlaces();
        for (int i = 0; i < losers.size(); i++) {
            SpleefPlayer player = losers.get(i);
            int place = i + 2;

            String command = getCommand(place);
            if (command == null) {
                continue;
            }

            String inflated = inflateCommand(command, player, game);
            Bukkit.dispatchCommand(console, inflated);
        }
    }

    private String inflateCommand(String commandLine, SpleefPlayer player, Game game) {
        commandLine = commandLine.replace(PLAYER_VARIABLE_1, player.getName());
        commandLine = commandLine.replace(PLAYER_VARIABLE_2, player.getName());
        commandLine = commandLine.replace(GAME_VARIABLE, game.getName());

        commandLine = ChatColor.translateAlternateColorCodes('&', commandLine);
        return commandLine;
    }

    @Flag(name = "set", parent = FlagWinCommand.class)
    public static class FlagAddWinCommand extends PairFlagDummy<Integer, String> {

        @Override
        public void onFlagAdd(Game game) {
            FlagWinCommand parent = game.getFlag(FlagWinCommand.class);
            parent.setCommand(getValue());

            game.removeFlag(getClass());
        }

        @Override
        public void getDescription(List<String> description) {
            description.add("Sets the command to be run for a player occupying the specified place");
        }

        @Override
        public Pair<Integer, String> parseInput(SpleefPlayer player, String input) throws InputParseException {
            return parseInputStatically(player, input);
        }

        static Pair<Integer, String> parseInputStatically(SpleefPlayer player, String input) throws InputParseException {
            String[] parts = input.split(" ", 2);
            String placeStr = parts[0];
            int place;

            try {
                place = Integer.parseInt(placeStr);
            } catch (NumberFormatException nfe) {
                throw new InputParseException("Place must be a number!");
            }

            if (parts.length < 2) {
                throw new InputParseException("Please provide a command to be run");
            }

            String command = parts[1];
            return new Pair<>(place, command);
        }

    }

    @Flag(name = "remove", parent = FlagWinCommand.class)
    public static class FlagRemoveWinCommand extends IntegerFlag {

        @Override
        public void getDescription(List<String> description) {
            description.add("Removes the command for a specified place");
        }

        @Override
        public void onFlagAdd(Game game) {
            FlagWinCommand parent = game.getFlag(FlagWinCommand.class);
            parent.removeCommand(getValue());

            game.removeFlag(getClass());
        }

    }

    private abstract static class PairFlagDummy<K, V> extends AbstractFlag<Pair<K, V>> {

        @Override
        public String getValueAsString() {
            return "[key: " + getValue().getKey() + ", value: " + getValue().getValue() + "]";
        }

        @Override
        public void marshal(Element element) {
            throw new UnsupportedOperationException("Operation not supported");
        }

        @Override
        public void unmarshal(Element element) {
            throw new UnsupportedOperationException("Operation not supported");
        }
    }

}
