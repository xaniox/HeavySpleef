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
package de.matzefratze123.heavyspleef.commands.base.proxy;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.Validate;

import com.google.common.collect.Lists;

import de.matzefratze123.heavyspleef.commands.base.CommandContainer;
import de.matzefratze123.heavyspleef.commands.base.CommandContext;
import de.matzefratze123.heavyspleef.commands.base.CommandExecution;
import de.matzefratze123.heavyspleef.commands.base.CommandManagerService;
import de.matzefratze123.heavyspleef.commands.base.MessageBundle;
import de.matzefratze123.heavyspleef.commands.base.PermissionChecker;
import de.matzefratze123.heavyspleef.commands.base.proxy.ProxyPriority.Priority;

public class ProxyExecution implements CommandExecution {

	private CommandExecution delegate;
	private List<ProxyHolder> proxies;
	
	public static ProxyExecution decorate(CommandExecution execution) {
		ProxyExecution proxyExecution = new ProxyExecution(execution);
		return proxyExecution;
	}
	
	public static ProxyExecution inject(CommandManagerService service, String path) {
		CommandContainer container = service.containerOf(path);
		Validate.notNull(container, "Non-existent command path '" + path + "'");
		
		CommandExecution execution = container.getExecution();
		if (!(execution instanceof ProxyExecution)) {
			execution = decorate(execution);
		}
		
		container.setExecution(execution);
		return (ProxyExecution) execution;
	}
	
	protected ProxyExecution(CommandExecution delegate) {
		this.delegate = delegate;
		this.proxies = Lists.newArrayList();
	}
	
	@Override
	public void execute(CommandContext context, MessageBundle bundle, PermissionChecker checker, Object[] executionArgs) {
		Redirection redirection = Redirection.EXECUTE;
		
		//Execute proxies
		for (ProxyHolder holder : proxies) {
			if (!applyFilter(context, holder)) {
				//This filter is not appliable to this execution
				continue;
			}
			
			Proxy proxy = holder.proxy;
			ProxyContext proxyContext = new ProxyContext(context);
			
			proxy.execute(proxyContext, executionArgs);	
			redirection = proxyContext.redirection();
		}
		
		if (redirection == Redirection.EXECUTE) {
			delegate.execute(context, bundle, checker, executionArgs);
		}
	}
	
	private boolean applyFilter(CommandContext context, ProxyHolder holder) {
		String[] filter = holder.filter;
		if (filter == null) {
			return true;
		}
		
		String fullName = context.getCommand().getFullyQualifiedName();
		for (String filterStr : filter) {
			if (fullName.equals(filterStr)) {
				return true;
			}
		}
		
		return false;
	}
	
	public void attachProxy(Proxy proxy) {
		Validate.isTrue(!isProxyAttached(proxy), "Proxy already attached");
		Class<? extends Proxy> clazz = proxy.getClass();
		
		Priority priority = Priority.NORMAL;
		if (clazz.isAnnotationPresent(ProxyPriority.class)) {
			ProxyPriority priorityAnnotation = clazz.getAnnotation(ProxyPriority.class);
			priority = priorityAnnotation.value();
		}
		
		String[] filter = null;
		if (clazz.isAnnotationPresent(Filter.class)) {
			Filter filterAnnotation = clazz.getAnnotation(Filter.class);
			filter = filterAnnotation.value();
		}
		
		ProxyHolder holder = new ProxyHolder();
		holder.proxy = proxy;
		holder.priority = priority;
		holder.filter = filter;
		
		proxies.add(holder);
		//Finally sort the list to get an appropriate order
		Collections.sort(proxies);
	}
	
	public void unattachProxy(Proxy proxy) {
		Validate.isTrue(isProxyAttached(proxy), "Proxy is not attached");
		
		Iterator<ProxyHolder> iterator = proxies.iterator();
		while (iterator.hasNext()) {
			ProxyHolder holder = iterator.next();
			if (holder.proxy != proxy) {
				continue;
			}
			
			iterator.remove();
			break;
		}
	}
	
	private boolean isProxyAttached(Proxy proxy) {
		for (ProxyHolder holder : proxies) {
			if (holder.proxy.equals(proxy)) {
				return true;
			}
		}
		
		return false;
	}
	
	private class ProxyHolder implements Comparable<ProxyHolder> {
		
		private Proxy proxy;
		private String[] filter;
		private ProxyPriority.Priority priority;
		
		@Override
		public int compareTo(ProxyHolder o) {
			return Integer.valueOf(priority.getPriorityInt()).compareTo(o.priority.getPriorityInt());
		}
		
	}

}
