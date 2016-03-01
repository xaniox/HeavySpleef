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
package de.xaniox.heavyspleef.core.i18n;

import java.io.File;
import java.util.Locale;
import java.util.logging.Logger;

/**
 * An simple builder for building I18N objects
 * 
 * @author matzefratze123
 */
public final class I18NBuilder {

	private Locale locale;
	private ClassLoader classLoader;
	private Logger logger;
	private I18N.LoadingMode loadingMode;
	private File fileSystemFolder;
	private String classpathFolder;

	public static I18NBuilder builder() {
		return new I18NBuilder();
	}
	
	private I18NBuilder() {}
	
	public I18N build() {
		ClassLoader loader = classLoader;
		if (loader == null) {
			loader = getClass().getClassLoader();
		}
		
		I18N i18n = new I18N(locale, loadingMode, fileSystemFolder, classpathFolder, loader, logger);
		return i18n;
	}

	public I18NBuilder setLocale(Locale locale) {
		this.locale = locale;
		return this;
	}

	public I18NBuilder setClassLoader(ClassLoader classLoader) {
		this.classLoader = classLoader;
		return this;
	}

	public I18NBuilder setLogger(Logger logger) {
		this.logger = logger;
		return this;
	}

	public I18NBuilder setLoadingMode(I18N.LoadingMode loadingMode) {
		this.loadingMode = loadingMode;
		return this;
	}

	public I18NBuilder setFileSystemFolder(File fileSystemFolder) {
		this.fileSystemFolder = fileSystemFolder;
		return this;
	}

	public I18NBuilder setClasspathFolder(String classpathFolder) {
		this.classpathFolder = classpathFolder;
		return this;
	}

}