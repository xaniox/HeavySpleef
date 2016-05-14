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
import de.xaniox.heavyspleef.core.event.Subscribe;
import de.xaniox.heavyspleef.core.flag.Flag;
import de.xaniox.heavyspleef.core.flag.InputParseException;
import de.xaniox.heavyspleef.core.game.Game;
import de.xaniox.heavyspleef.core.player.SpleefPlayer;
import de.xaniox.heavyspleef.flag.presets.IntegerFlag;
import org.bukkit.ChatColor;
import org.dom4j.Element;

import java.util.Iterator;
import java.util.List;
import java.util.Random;

@Flag(name = "probability", parent = FlagWinCommand.class)
public class FlagWinCommandProbability extends PairListFlag<Integer, Integer> {

    private final Random random = new Random();

    @Override
    public List<Pair<Integer, Integer>> parseInput(SpleefPlayer player, String input) throws InputParseException {
        if (input.trim().isEmpty()) {
            sendInstructions(player);
            return Lists.newArrayList();
        }

        List<Pair<Integer, Integer>> list;
        if (getValue() == null) {
            list = Lists.newArrayList();
            sendInstructions(player);
        } else {
            list = getValue();
        }

        list.add(FlagSetProbability.parseInputStatically(player, input));
        return list;
    }

    private void sendInstructions(SpleefPlayer player) {
        player.sendMessage(ChatColor.GRAY + "Set probabilities via /spleef flag <game> win-command:probability:set <place> <probability>");
        player.sendMessage(ChatColor.GRAY + "For example: To define the command for the 2nd place you would use:");
        player.sendMessage(ChatColor.GRAY + "/spleef flag <game> win-command:probability:set  say @p won the game on arena @g");
    }

    public void setProbability(Pair<Integer, Integer> pair) {
        removeProbability(pair.getKey());
        getValue().add(pair);
    }

    public boolean removeProbability(int place) {
        Iterator<Pair<Integer, Integer>> iterator = getValue().iterator();
        boolean success = false;

        while (iterator.hasNext()) {
            Pair<Integer, Integer> pair = iterator.next();
            if (pair.getKey() != place) {
                continue;
            }

            iterator.remove();
            success = true;
        }

        return success;
    }

    public float getProbability(int place) {
        for (Pair<Integer, Integer> pair : getValue()) {
            if (pair.getKey() != place) {
                continue;
            }

            return pair.getValue() / 100f;
        }

        return 1f;
    }

    @Subscribe
    public void onWinCommandExecution(FlagWinCommand.WinCommandExecutionEvent event) {
        int place = event.getPlace();
        float probability = getProbability(place);

        double rn = random.nextDouble();
        if (rn < probability) {
            return;
        }

        event.setCancelled(true);
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
    public void marshalValue(Element element, Integer value) {
        element.setText(String.valueOf(value.intValue()));
    }

    @Override
    public Integer unmarshalValue(Element element) {
        return Integer.parseInt(element.getText());
    }

    @Override
    public void getDescription(List<String> description) {
        description.add("Adds a probability for a winner command to execute or not");
    }

    @Flag(name = "set", parent = FlagWinCommandProbability.class)
    public static class FlagSetProbability extends FlagWinCommand.PairFlagDummy<Integer, Integer> {

        @Override
        public void onFlagAdd(Game game) {
            FlagWinCommandProbability parent = game.getFlag(FlagWinCommandProbability.class);
            parent.setProbability(getValue());

            game.removeFlag(getClass());
        }

        @Override
        public void getDescription(List<String> description) {
            description.add("Sets the probability for a command to execute for a specified place");
        }

        @Override
        public Pair<Integer, Integer> parseInput(SpleefPlayer player, String input) throws InputParseException {
            return parseInputStatically(player, input);
        }

        static Pair<Integer, Integer> parseInputStatically(SpleefPlayer player, String input) throws InputParseException {
            String[] args = input.split(" ");
            if (args.length < 2) {
                throw new InputParseException("Please specify a place and a probability (0 - 100)");
            }

            int place;
            try {
                place = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                throw new InputParseException("Specified place must be a number!");
            }

            int probability;
            try {
                probability = Integer.parseInt(args[1]);
            } catch (NumberFormatException nfe) {
                //Try parsing a float
                try {
                    probability = (int) (Float.parseFloat(args[1]) * 100f);
                } catch (NumberFormatException nfe1) {
                    throw new InputParseException("Specified probability must an integer or a decimal");
                }
            }

            return new Pair<>(place, probability);
        }

    }

    @Flag(name = "remove", parent = FlagWinCommandProbability.class)
    public static class FlagRemoveProbability extends IntegerFlag {

        @Override
        public void getDescription(List<String> description) {
            description.add("Removes the probability for a specified place");
        }

        @Override
        public void onFlagAdd(Game game) {
            FlagWinCommandProbability parent = (FlagWinCommandProbability) getParent();
            parent.removeProbability(getValue());

            game.removeFlag(getClass());
        }
    }

}
