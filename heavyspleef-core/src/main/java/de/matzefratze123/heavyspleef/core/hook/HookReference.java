package de.matzefratze123.heavyspleef.core.hook;

public enum HookReference {
	
	VAULT("Vault", VaultHook.class);
	
	private String pluginName;
	private Class<? extends Hook> hookClass;
	
	private HookReference(String pluginName, Class<? extends Hook> hookClass) {
		this.pluginName = pluginName;
		this.hookClass = hookClass;
	}
	
	public String getPluginName() {
		return pluginName;
	}
	
	public Hook newHookInstance() {
		try {
			return hookClass.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

}
