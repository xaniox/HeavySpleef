package de.matzefratze123.joingui;

import org.bukkit.entity.Player;

import de.matzefratze123.heavyspleef.commands.base.proxy.Filter;
import de.matzefratze123.heavyspleef.commands.base.proxy.Proxy;
import de.matzefratze123.heavyspleef.commands.base.proxy.ProxyContext;
import de.matzefratze123.heavyspleef.commands.base.proxy.ProxyPriority;
import de.matzefratze123.heavyspleef.commands.base.proxy.ProxyPriority.Priority;
import de.matzefratze123.heavyspleef.commands.base.proxy.Redirection;

@Filter("spleef/join")
@ProxyPriority(Priority.HIGH)
public class JoinCommandProxy implements Proxy {

	private JoinInventory inventory;
	
	public JoinCommandProxy(JoinInventory inventory) {
		this.inventory = inventory;
	}
	
	public void execute(ProxyContext context, Object[] executionArgs) {
		//Activate the GUI when the args length is 0
		if (context.argsLength() == 0 && context.getSender() instanceof Player) {
			Player player = context.getSender();
			inventory.open(player);
			
			context.redirect(Redirection.CANCEL);
		}
	}

}
