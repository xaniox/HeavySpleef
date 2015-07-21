package de.matzefratze123.heavyspleef.addon;

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
	private static final String UNLOAD_ACTION = "load";
	
	private final I18N i18n = I18NManager.getGlobal();
	
	@Command(name = "add-on", descref = Messages.Help.Description.ADDONS,
			usage = "/spleef add-on <load|unload> <add-on>", minArgs = 2,
			permission = Permissions.PERMISSION_ADDON)
	public void onAddOnCommand(CommandContext context, HeavySpleef heavySpleef, AddOnManager manager) throws CommandException {
		CommandSender sender = context.getSender();
		if (sender instanceof Player) {
			sender = heavySpleef.getSpleefPlayer(sender);
		}
		
		String action = context.getString(0);
		String addonName = context.getString(1);
		
		if (action.equalsIgnoreCase(LOAD_ACTION)) {
			CommandValidate.isTrue(!manager.isAddOnEnabled(addonName), i18n.getVarString(Messages.Command.ADDON_ALREADY_ENABLED)
					.setVariable("addon", addonName)
					.toString());
			
			AddOnModule module = (AddOnModule) heavySpleef.getModuleManager().getModule(AddOnModule.class);
			manager.searchAndLoad(module.getBaseDir(), addonName);
			manager.enableAddOn(addonName);
			
			sender.sendMessage(i18n.getVarString(Messages.Command.ADDON_LOADED)
					.setVariable("addon", addonName)
					.toString());
		} else if (action.equalsIgnoreCase(UNLOAD_ACTION)) {
			CommandValidate.isTrue(manager.isAddOnEnabled(addonName), i18n.getVarString(Messages.Command.ADDON_NOT_ENABLED)
					.setVariable("addon", addonName)
					.toString());
			
			manager.disableAddOn(addonName);
			manager.unloadAddOn(addonName);
			
			sender.sendMessage(i18n.getVarString(Messages.Command.ADDON_UNLOADED)
					.setVariable("addon", addonName)
					.toString());
		} else {
			throw new CommandException(context.getCommand().getUsage());
		}
	}
	
}
