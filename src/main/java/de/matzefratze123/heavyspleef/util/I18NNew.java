/*
 *   HeavySpleef - Advanced spleef plugin for bukkit
 *   
 *   Copyright (C) 2013-2014 matzefratze123
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package de.matzefratze123.heavyspleef.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.matzefratze123.heavyspleef.HeavySpleef;

public class I18NNew {
	
	private static final String         ARGUMENT_TARGET = "%a";     
	
	private static final String         MESSAGES_FILE   = "messages.xml";
	private static final String         MESSAGES_TAG    = "messages";
	private static final String         MESSAGE_ENTRY   = "message";
	private static final String         ENTRY_ENTRY     = "entry";
	private static final String         ENTRY_NAME      = "name";
	private static final String         ID_ATTRIBUTE    = "id";
	
	private static final char           TRANSLATE_CHAR  = '&';
	
	private static boolean              loaded          = false;
	private static Map<String, String>  messages;
	
	static {
		if (messages == null) {
			messages = new HashMap<String, String>();
		}
		
		File dataFolder = HeavySpleef.getInstance().getDataFolder();
		dataFolder.mkdir();
		
		File destFile = new File(dataFolder, MESSAGES_FILE);
		
		if (!destFile.exists()) {
			copyLanguageXml(destFile);
		}
		
		if (!loaded) {
			loaded = true;
			loadMessages();
		}
	}
	
	public static String getMessage(String path, String... args) {
		String message = messages.get(path);
		
		if (message == null) {
			return null;
		}
		
		for (String arg : args) {
			message = replaceFirst(message, ARGUMENT_TARGET, arg);
		}
		
		message = ChatColor.translateAlternateColorCodes(TRANSLATE_CHAR, message);
		
		return message;
	}
	
	private static void loadMessages() {
		InputStream inStream = null;
		
		try {
			File file = new File(HeavySpleef.getInstance().getDataFolder(), MESSAGES_FILE);
			if (file.exists()) {
				inStream = new FileInputStream(file);
			} else {
				inStream = I18NNew.class.getResourceAsStream('/' + MESSAGES_FILE);
			}
			
			if (inStream == null) {
				throw new IOException("null");
			}
			
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			
			Document xml = builder.parse(inStream);
			NodeList nodes = xml.getChildNodes();
			
			for (int i = 0; i < nodes.getLength(); i++) {
				Node node = nodes.item(i);
				
				if (node.getNodeName().equalsIgnoreCase(MESSAGES_TAG)) {
					//Messages entry!
					readEntry(node, "");
				}
			}
		} catch (Exception e) {
			Logger.severe("Failed to load language messages: " + e.getMessage() + ". EXPECT ERRORS!");
			e.printStackTrace();
		} finally {
			try {
				if (inStream != null) {
					inStream.close();
				}
			} catch (Exception e) {}
		}
	}
	
	private static void readEntry(Node node, String parentEntry) {
		NodeList list = node.getChildNodes();
		
		for (int i = 0; i < list.getLength(); i++) {
			Node childNode = list.item(i);
			String nodeName = childNode.getNodeName();
			
			if (nodeName.equalsIgnoreCase(MESSAGE_ENTRY)) {
				NamedNodeMap attributes = childNode.getAttributes();
				Node idNode = attributes.getNamedItem(ID_ATTRIBUTE);
				if (idNode == null) {
					Logger.warning("Warning: No id for message in " + MESSAGES_FILE + ". Ignoring message...");
					continue;
				}
				
				String id = idNode.getNodeValue();
				String value = childNode.getTextContent();
				
				messages.put(parentEntry + id, value);
			} else if (nodeName.equalsIgnoreCase(ENTRY_ENTRY)) {
				NamedNodeMap attributes = childNode.getAttributes();
				Node nameNode = attributes.getNamedItem(ENTRY_NAME);
				String entryName = nameNode.getNodeValue();
				
				readEntry(childNode, parentEntry + entryName + ".");
			}
		}
	}
	
	private static void copyLanguageXml(File destFile) {
		InputStream inStream = null;
		OutputStream outStream = null;
		
		try {
			destFile.createNewFile();
			
			inStream = I18NNew.class.getResourceAsStream('/' + MESSAGES_FILE);
			outStream = new FileOutputStream(destFile);
			
			final byte[] BUFFER = new byte[1024];
			int read;
			
			while ((read = inStream.read(BUFFER)) > 0) {
				outStream.write(BUFFER, 0, read);
			}
		} catch (IOException e) {
			Logger.severe("Failed to copy messages file! Using default messages...");
			e.printStackTrace();
		} finally {
			try {
				if (inStream != null) {
					inStream.close();
				}
				
				if (outStream != null) {
					outStream.flush();
					outStream.close();
				}
			} catch (Exception e) {}
		}
	}
	
	/**
	 * Replaces the first given target in the given string with the given replacement.</br>
	 * This version of replaceFirst ignores regexes which is important for replacing arguments.
	 */
	private static String replaceFirst(String string, String target, String replacement) {
		Validate.notNull(string);
		Validate.notNull(target);
		Validate.notNull(replacement);
		
		if (target.isEmpty()) {
			return string;
		}
		
		char[] chars = string.toCharArray();
		char[] targetChars = target.toCharArray();
		
		int index = 0;
		int start = -1;
		
		for (int i = 0; i < chars.length; i++) {
			if (chars[i] == targetChars[index]) {
				if (index == 0) {
					start = i;
				}
				
				index++;
				
				if (index >= targetChars.length) {
					String firstPart = string.substring(0, start);
					String secondPart = string.substring(i + 1, chars.length);
					
					return firstPart + replacement + secondPart;
				}
			} else {
				index = 0;
			}
		}
		
		return string;
	}
	
}