/*
 * This file is part of addons.
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
package de.xaniox.leaderboardextensions;

import de.xaniox.heavyspleef.core.extension.ExtensionRegistry;
import de.xaniox.heavyspleef.core.extension.GameExtension;
import de.xaniox.heavyspleef.persistence.xml.XMLAccessor;
import de.xaniox.heavyspleef.persistence.xml.XMLContext;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import java.io.*;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Set;

public class ExtensionXmlHandler {
	
	private static final Charset UTF8 = Charset.forName("UTF-8");
	
	private final OutputFormat format = OutputFormat.createPrettyPrint();
	private final XMLAccessor<?> podiumAccessor = new PodiumAccessor();
	private final XMLAccessor<?> wallAccessor = new WallAccessor();
	private final SAXReader saxReader = new SAXReader();
	private final XMLContext context;
	private final ExtensionRegistry registry;
	private File xmlFile;
	
	public ExtensionXmlHandler(File xmlFile, ExtensionRegistry registry) {
		this.xmlFile = xmlFile;
		this.context = new XMLContext(podiumAccessor, wallAccessor);
		this.registry = registry;
	}
	
	public void saveExtensions(Set<GameExtension> extensions) throws IOException {
		if (!xmlFile.exists()) {
			xmlFile.createNewFile();
		}
		
		Document document = DocumentHelper.createDocument();
		Element rootElement = document.addElement("extensions");
		
		for (GameExtension extension : extensions) {
			Element extensionElement = rootElement.addElement("extension");
			String name = registry.getExtensionName(extension.getClass());
			
			extensionElement.addAttribute("name", name);
			context.write(extension, extensionElement);
		}
		
		XMLWriter xmlWriter = null;
		
		try (OutputStream out = new FileOutputStream(xmlFile);
				Writer writer = new OutputStreamWriter(out, UTF8)) {
			xmlWriter = new XMLWriter(writer, format);
			xmlWriter.write(document);
			xmlWriter.flush();
		} finally {
			if (xmlWriter != null) {
				xmlWriter.close();
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public void loadExtensions(Set<GameExtension> set) throws IOException, DocumentException {
		if (!xmlFile.exists()) {
			return;
		}
		
		try (InputStream in = new FileInputStream(xmlFile);
				Reader reader = new InputStreamReader(in, UTF8)) {
			Document document = saxReader.read(reader);
			Element rootElement = document.getRootElement();
			
			List<Element> extensionElements = rootElement.elements("extension");
			for (Element extensionElement : extensionElements) {
				String name = extensionElement.attributeValue("name");
				Class<? extends GameExtension> expected = registry.getExtensionClass(name);
				
				GameExtension extension = context.read(extensionElement, expected);
				set.add(extension);
			}
		}
	}

}