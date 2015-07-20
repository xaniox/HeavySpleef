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
package de.matzefratze123.heavyspleef.migration;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

public class TemplatedDocument {
	
	private List<String> document;
	
	public TemplatedDocument(Reader templateReader, Map<String, String> variables) throws IOException {
		this.document = Lists.newArrayList();
		
		try (BufferedReader reader = templateReader instanceof BufferedReader ?
				(BufferedReader) templateReader : new BufferedReader(templateReader)) {
			String read;
			
			while ((read = reader.readLine()) != null) {
				for (Entry<String, String> entry : variables.entrySet()) {
					String varName = '%' + entry.getKey() + '%';
					
					if (read.contains(varName)) {
						read = read.replace(varName, entry.getValue());
					}
				}
				
				document.add(read);
			}
		}
	}
	
	public List<String> getDocument() {
		return ImmutableList.copyOf(document);
	}
	
	public void writeDocument(Writer writer) throws IOException {
		try (BufferedWriter bufWriter = writer instanceof BufferedWriter ?
				(BufferedWriter) writer : new BufferedWriter(writer)) {
			for (int i = 0; i < document.size(); i++) {
				bufWriter.write(document.get(i));
				
				if (i + 1 < document.size()) {
					bufWriter.newLine();
				}
			}
			
			bufWriter.flush();
		}
	}
	
}