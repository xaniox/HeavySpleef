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
package de.matzefratze123.heavyspleef.addon;

import java.util.Set;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.matzefratze123.heavyspleef.commands.base.Command;
import de.matzefratze123.heavyspleef.commands.base.CommandContext;
import de.matzefratze123.heavyspleef.commands.base.CommandException;
import de.matzefratze123.heavyspleef.commands.base.CommandValidate;
import de.matzefratze123.heavyspleef.core.HeavySpleef;
import de.matzefratze123.heavyspleef.core.Permissions;
import de.matzefratze123.heavyspleef.core.i18n.I18N;
import de.matzefratze123.heavyspleef.core.i18n.I18NManager;
import de.matzefratze123.heavyspleef.core.i18n.Messages;

public class AddOnCommands {
	
	private static final String LOAD_ACTION = "load";
	private static final String UNLOAD_ACTION = "unload";
	private static final String LIST_ACTION = "list";
	
	private final I18N i18n = I18NManager.getGlobal();
	
	@Command(name = "add-on", descref = Messages.Help.Description.ADDONS,
			usage = "/spleef add-on <[load|unload> <add-on>]|list>", minArgs = 1,
			permission = Permissions.PERMISSION_ADDON)
	public void onAddOnCommand(CommandContext context, HeavySpleef heavySpleef, AddOnManager manager) throws CommandException {
		CommandSender sender = context.getSender();
		if (sender instanceof Player) {
			sender = heavySpleef.getSpleefPlayer(sender);
		}
		
		String action = context.getString(0);
		String addonName = context.getStringSafely(1);
		
		if (action.equalsIgnoreCase(LOAD_ACTION)) {
			AddOn existingAddon = manager.getAddOn(addonName);
			
			CommandValidate.isTrue(existingAddon == null, i18n.getVarString(Messages.Command.ADDON_ALREADY_ENABLED)
					.setVariable("addon", existingAddon.getName())
					.toString());
			
			AddOnModule module = (AddOnModule) heavySpleef.getModuleManager().getModule(AddOnModule.class);
			
			AddOn addon = manager.searchAndLoad(module.getBaseDir(), addonName, false);
			if (addon != null) {
				manager.enableAddOn(addon.getName());
				
				sender.sendMessage(i18n.getVarString(Messages.Command.ADDON_LOADED)
						.setVariable("addon", addon.getName())
						.toString());
			} else {
				throw new CommandException(i18n.getVarString(Messages.Command.ADDON_NOT_EXISTING)
						.setVariable("addon", addonName)
						.toString());
			}
		} else if (action.equalsIgnoreCase(UNLOAD_ACTION)) {
			AddOn addon = manager.getAddOn(addonName);
			
			CommandValidate.isTrue(addon != null, i18n.getVarString(Messages.Command.ADDON_NOT_ENABLED)
					.setVariable("addon", addonName)
					.toString());
			
			manager.disableAddOn(addon.getName());
			manager.unloadAddOn(addon.getName());
			
			sender.sendMessage(i18n.getVarString(Messages.Command.ADDON_UNLOADED)
					.setVariable("addon", addon.getName())
					.toString());
		} else if (action.equalsIgnoreCase(LIST_ACTION)) {
			Set<AddOn> addOns = manager.getAddOns();
			
			if (addOns.isEmpty()) {
				sender.sendMessage(i18n.getString(Messages.Command.NO_ADDONS_INSTALLED));
			} else {
				sender.sendMessage(i18n.getVarString(Messages.Command.ADDON_LIST_HEADER)
						.setVariable("amount", String.valueOf(addOns.size()))
						.toString());
				
				for (AddOn addOn : addOns) {
					AddOnProperties properties = addOn.getProperties();
					
					sender.sendMessage(i18n.getVarString(Messages.Command.ADDON_LIST_ENTRY)
							.setVariable("addon", addOn.getName())
							.setVariable("version", properties.getVersion())
							.setVariable("author", properties.getAuthor())
							.toString());
				}
			}
		} else {
			throw new CommandException(context.getCommand().getUsage());
		}
	}
	
}
