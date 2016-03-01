/*
 * This file is part of HeavySpleef.
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
package de.xaniox.heavyspleef.commands.base;

import com.google.common.collect.Maps;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.Map;
import java.util.Map.Entry;

public class MessageBundle {
	
	private final Yaml yaml = new Yaml();
	private MessageProvider provider;
	private Map<String, String> defaultMessages;
	
	public MessageBundle(MessageProvider provider, InputStream defaultMessagesStream) {
		this.provider = provider;
		
		readDefaultMessages(defaultMessagesStream);
	}
	
	@SuppressWarnings("unchecked")
	private void readDefaultMessages(InputStream in) {
		Map<String, Object> messages = (Map<String, Object>) yaml.load(in);
		this.defaultMessages = Maps.newHashMap();
		
		for (Entry<String, Object> entry : messages.entrySet()) {
			defaultMessages.put(entry.getKey(), entry.getValue().toString());
		}
	}
	
	public String getMessage(String key, String... args) {
		String message = provider.provide(key, args);
		
		if (message == null) {
			message = defaultMessages.get(key);
			
			if (message == null) {
				//Give up
				throw new IllegalArgumentException("No message provided for key '" + key + "'");
			} else {
				message = String.format(message, (Object[]) args);
			}
		}
		
		return message;
	}
	
	public static interface MessageProvider {
		
		public String provide(String key, String[] args);
		
	}
	
}