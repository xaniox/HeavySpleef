/*
 * This file is part of HeavySpleef.
 * Copyright (c) 2014-2015 matzefratze123
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
package de.matzefratze123.heavyspleef.flag.defaults;

import java.util.ArrayList;
import java.util.List;

import net.milkbowl.vault.economy.Economy;

import org.dom4j.Element;

import com.google.common.collect.Lists;

import de.matzefratze123.heavyspleef.core.event.PlayerWinGameEvent;
import de.matzefratze123.heavyspleef.core.event.Subscribe;
import de.matzefratze123.heavyspleef.core.flag.Flag;
import de.matzefratze123.heavyspleef.core.flag.InputParseException;
import de.matzefratze123.heavyspleef.core.flag.ValidationException;
import de.matzefratze123.heavyspleef.core.hook.HookManager;
import de.matzefratze123.heavyspleef.core.hook.HookReference;
import de.matzefratze123.heavyspleef.core.i18n.Messages;
import de.matzefratze123.heavyspleef.core.player.SpleefPlayer;
import de.matzefratze123.heavyspleef.flag.presets.ArgumentParser;
import de.matzefratze123.heavyspleef.flag.presets.DelimiterBasedListParser;
import de.matzefratze123.heavyspleef.flag.presets.ListFlag;
import de.matzefratze123.heavyspleef.flag.presets.ListInputParser;

@Flag(name = "reward", depend = HookReference.VAULT)
public class FlagReward extends ListFlag<Double> {

	private Economy economy;
	
	@Override
	public void getDescription(List<String> description) {
		description.add("Defines a money reward given to all winners");
	}
	
	@Override
	public void marshalListItem(Element element, Double item) {
		element.setText(String.valueOf(item));
	}

	@Override
	public Double unmarshalListItem(Element element) {
		return Double.parseDouble(element.getText());
	}
	
	@Override
	public void unmarshal(Element element) {
		//Add support for older versions of the reward flag
		if (element.elements().size() == 0) {
			if (getValue() == null) {
				setValue(new ArrayList<Double>());
			}
			
			double d = unmarshalListItem(element);
			add(d);
		} else {
			super.unmarshal(element);
		}
	}

	@Override
	public String getListItemAsString(Double item) {
		return String.valueOf(item);
	}

	@Override
	public ListInputParser<Double> createParser() {
		return new DelimiterBasedListParser<Double>(" ", new ArgumentParser<Double>() {

			@Override
			public Double parseArgument(String argument) throws InputParseException {
				try {
					return Double.valueOf(argument);
				} catch (NumberFormatException nfe) {
					throw new InputParseException(getI18N().getString(Messages.Player.NOT_A_NUMBER));
				}
			}
		});
	}
	
	@Override
	public void validateInput(List<Double> input) throws ValidationException {
		if (input.size() == 0) {
			throw new ValidationException(getI18N().getString(Messages.Command.NEED_AT_LEAST_ONE_NUMBER));
		}
	}
	
	@Subscribe
	public void onGameWin(PlayerWinGameEvent event) {
		//Call the method to potentially lazy-initialize the economy plugin
		Economy economy = getEconomy();
		List<Double> rewards = Lists.newArrayList();
		double winnerReward = rewards.get(0);
		String[] places = getI18N().getStringArray(Messages.Arrays.PLACES);
		
		for (SpleefPlayer winner : event.getWinners()) {
			economy.depositPlayer(winner.getBukkitPlayer(), winnerReward);
			winner.sendMessage(getI18N().getVarString(Messages.Player.RECEIVED_REWARD_PLACE)
					.setVariable("amount", economy.format(winnerReward))
					.setVariable("place", places[0])
					.toString());
		}
		
		List<SpleefPlayer> losePlaces = event.getLosePlaces();
		for (int i = 0; i < losePlaces.size() && i + 1 < rewards.size(); i++) {
			double reward = rewards.get(i + 1);
			SpleefPlayer loser = losePlaces.get(i);
			
			String placeString = places.length < i + 1 ? places[i + 1] : String.valueOf(i + 1) + '.';
			
			economy.depositPlayer(loser.getBukkitPlayer(), reward);
			loser.sendMessage(getI18N().getVarString(Messages.Player.RECEIVED_REWARD_PLACE)
					.setVariable("amount", economy.format(reward))
					.setVariable("place", placeString)
					.toString());
		}
	}
	
	private Economy getEconomy() {
		if (economy == null) {
			HookManager manager = getHeavySpleef().getHookManager();
			economy = manager.getHook(HookReference.VAULT).getService(Economy.class);
		}
		
		return economy;
	}

}