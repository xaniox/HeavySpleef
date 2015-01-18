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
package de.matzefratze123.heavyspleef.persistence.sql;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SQLQueryOptionsBuilder {
	
	private ExpressionList whereExpressions;
	private String sortBy;
	private String limit;
	
	public static SQLQueryOptionsBuilder newBuilder() {
		return new SQLQueryOptionsBuilder();
	}
	
	private SQLQueryOptionsBuilder() {
		this.whereExpressions = new ExpressionList();
	}
	
	public SQLQueryOptionsBuilder sortBy(String column) {
		this.sortBy = column;
		
		return this;
	}
	
	public SQLQueryOptionsBuilder limit(String limit) {
		this.limit = limit;
		
		return this;
	}
	
	public SQLQueryOptionsBuilder limit(int limit) {
		this.limit = String.valueOf(limit);
		
		return this;
	}
	
	public ExpressionList where() {
		return whereExpressions;
	}
	
	public String build() {
		StringBuilder builder = new StringBuilder();
		
		if (!whereExpressions.isEmpty()) {
			builder.append(whereExpressions.build());
			builder.append(' ');
		}
		
		if (sortBy != null) {
			builder.append("ORDER BY " + sortBy);
			builder.append(' ');
		}
		
		if (limit != null) {
			builder.append("LIMIT " + limit);
		}
		
		return builder.toString();
	}
	
	public class ExpressionList {
		
		private List<WhereExpression> expressions;
		
		public ExpressionList() {
			this.expressions = new ArrayList<WhereExpression>();
		}
		
		public ExpressionList eq(String property, Object value) {
			expressions.add(new WhereExpression(property, ConditionOperator.EQUALS, value));
			return this;
		}
		
		public ExpressionList like(String property, Object value) {
			expressions.add(new WhereExpression(property, ConditionOperator.LIKE, value));
			return this;
		}
		
		public ExpressionList gt(String property, Object value) {
			expressions.add(new WhereExpression(property, ConditionOperator.GREATER_THAN, value));
			return this;
		}
		
		public ExpressionList st(String property, Object value) {
			expressions.add(new WhereExpression(property, ConditionOperator.SMALLER_THAN, value));
			return this;
		}
		
		public SQLQueryOptionsBuilder back() {
			return SQLQueryOptionsBuilder.this;
		}
		
		private boolean isEmpty() {
			return expressions.isEmpty();
		}
		
		private String build() {
			StringBuilder builder = new StringBuilder("WHERE ");
			
			Iterator<WhereExpression> iterator = expressions.iterator();
			while (iterator.hasNext()) {
				WhereExpression expression = iterator.next();
				
				builder.append(expression.toString());
				
				if (iterator.hasNext()) {
					builder.append(" AND ");
				}
			}
			
			return builder.toString();
		}
		
		private class WhereExpression {
			
			private String property;
			private ConditionOperator operator;
			private Object value;
			
			public WhereExpression(String property, ConditionOperator operator, Object value) {
				this.property = property;
				this.operator = operator;
				this.value = value;
			}
			
			public String toString() {
				return property + operator.getOperator() + value.toString();
			}
			
		}
		
	}
	
	private enum ConditionOperator {
		
		EQUALS("="),
		LIKE("LIKE"),
		GREATER_THAN(">"),
		SMALLER_THAN("<");
		
		private String operator;
		
		private ConditionOperator(String operator) {
			this.operator = operator;
		}
		
		public String getOperator() {
			return operator;
		}
		
	}
	
}
