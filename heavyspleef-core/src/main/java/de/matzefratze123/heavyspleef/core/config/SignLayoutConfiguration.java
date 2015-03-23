package de.matzefratze123.heavyspleef.core.config;

import java.text.ParseException;
import java.util.List;

import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;

import com.google.common.collect.Lists;

import de.matzefratze123.heavyspleef.core.layout.SignLayout;

public class SignLayoutConfiguration extends ThrowingConfigurationObject<ParseException> {
	
	private SignLayout layout;
	
	public SignLayoutConfiguration(Configuration config) {
		super(config);
	}

	@Override
	public void inflateUnsafe(Configuration config, Object[] args) throws ParseException {
		List<String> lines = Lists.newArrayList();
		
		ConfigurationSection layoutSection = config.getConfigurationSection("layout");
		
		for (int i = 0; i < SignLayout.LINE_COUNT; i++) {
			String line = layoutSection.getString(String.valueOf(i));
			lines.add(line);
		}
		
		layout = new SignLayout(lines);
	}
	
	public SignLayout getLayout() {
		return layout;
	}

	@Override
	protected Class<? extends ParseException> getExceptionClass() {
		return ParseException.class;
	}

}
