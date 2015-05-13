package de.matzefratze123.joingui;

import java.util.List;

import de.matzefratze123.heavyspleef.commands.SpleefCommandManager;
import de.matzefratze123.heavyspleef.commands.base.CommandManagerService;
import de.matzefratze123.heavyspleef.commands.base.proxy.ProxyExecution;
import de.matzefratze123.heavyspleef.core.HeavySpleef;
import de.matzefratze123.heavyspleef.core.Unregister;
import de.matzefratze123.heavyspleef.core.flag.FlagInit;
import de.matzefratze123.heavyspleef.core.flag.Inject;

public class FlagJoinItem extends SingleItemStackFlag {

	private static ProxyExecution execution;
	private static JoinInventory inventory;
	private static JoinCommandProxy proxy;
	
	@Inject
	private static JoinGuiAddOn addOn;
	
	@FlagInit
	public static void injectCommandProxy(HeavySpleef heavySpleef) {
		SpleefCommandManager manager = (SpleefCommandManager) heavySpleef.getCommandManager();
		CommandManagerService service = manager.getService();
		
		inventory = new JoinInventory(addOn);
		heavySpleef.getGlobalEventBus().registerGlobalListener(inventory);
		
		proxy = new JoinCommandProxy(inventory);
		
		execution = ProxyExecution.inject(service, "spleef/join");
		execution.attachProxy(proxy);
	}
	
	@Unregister
	public static void unattachCommandProxy(HeavySpleef heavySpleef) {
		execution.unattachProxy(proxy);
		heavySpleef.getGlobalEventBus().unregisterGlobalListener(inventory);
	}
	
	@Override
	public void getDescription(List<String> description) {
		description.add("Sets the item displayed in the join gui for a game");
	}

}
