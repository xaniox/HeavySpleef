package de.matzefratze123.heavyspleef.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.plugin.PluginDescriptionFile;

import de.matzefratze123.heavyspleef.commands.base.Command;
import de.matzefratze123.heavyspleef.commands.base.CommandContext;
import de.matzefratze123.heavyspleef.core.HeavySpleef;
import de.matzefratze123.heavyspleef.core.i18n.I18N;
import de.matzefratze123.heavyspleef.core.i18n.Messages;

public class CommandReload {

	private I18N i18n = I18N.getInstance();
	
	@Command(name = "reload", permission = "heavyspleef.admin.reload",
			descref = Messages.Help.Description.RELOAD, usage = "/spleef reload")
	public void onReloadCommand(CommandContext context, HeavySpleef heavySpleef) {
		CommandSender sender = context.getSender();
		
		long timeBefore = System.currentTimeMillis();
		heavySpleef.reload();
		long timeDif = System.currentTimeMillis() - timeBefore;
		
		PluginDescriptionFile pdf = heavySpleef.getPlugin().getDescription();
		
		sender.sendMessage(i18n.getVarString(Messages.Command.PLUGIN_RELOADED)
				.setVariable("time-dif-ms", String.valueOf(timeDif))
				.setVariable("time-dif-s", String.valueOf(timeDif / 1000D))
				.setVariable("version", pdf.getVersion())
				.toString());
	}
	
}
