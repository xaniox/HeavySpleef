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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SQLQueryOptionsBuilder {
	
	private ExpressionList whereExpressions;
	private String groupBy;
	private String sortBy;
	private String limit;
	
	public static SQLQueryOptionsBuilder newBuilder() {
		return new SQLQueryOptionsBuilder();
	}
	
	private SQLQueryOptionsBuilder() {
		this.whereExpressions = new ExpressionList();
	}
	
	public SQLQueryOptionsBuilder groupBy(String column) {
		this.groupBy = column;
		
		return this;
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
		return build(false);
	}
	
	private String build(boolean prepare) {
		StringBuilder builder = new StringBuilder();
		
		if (!whereExpressions.isEmpty()) {
			builder.append(whereExpressions.build(prepare));
			builder.append(' ');
		}
		
		if (groupBy != null) {
			builder.append("GROUP BY " + (prepare ? '?' : groupBy));
			builder.append(' ');
		}
		
		if (sortBy != null) {
			builder.append("ORDER BY " + (prepare ? '?' : sortBy));
			builder.append(' ');
		}
		
		if (limit != null) {
			builder.append("LIMIT " + (prepare ? limit.contains(",") ? "?,?" : '?' : limit));
		}
		
		return builder.toString();
	}
	
	public PreparedStatement prepareStatement(Connection connection, String queryStart) throws SQLException {
		String statementSkeleton = build(true);
		
		PreparedStatement statement = connection.prepareStatement(queryStart.trim() + " " + statementSkeleton);
		int currentIndex = 1;
		
		if (!whereExpressions.isEmpty()) {
			currentIndex = whereExpressions.apply(statement, currentIndex);
		}
		
		if (groupBy != null) {
			statement.setString(currentIndex++, groupBy);
		}
		if (sortBy != null) {
			statement.setString(currentIndex++, sortBy);
		}
		if (limit != null) {
			String[] components = limit.split(",");
			int offset = 0;
			int limit;
			
			if (components.length > 1) {
				offset = Integer.parseInt(components[0]);
				limit = Integer.parseInt(components[1]);
			} else {
				limit = Integer.parseInt(components[0]);
			}
			
			statement.setInt(currentIndex++, offset);
			statement.setInt(currentIndex++, limit);
		}
		
		return statement;
	}
	
	public class ExpressionList {
		
		private List<Object> expressions;
		
		public ExpressionList() {
			this.expressions = new ArrayList<Object>();
		}
		
		public ExpressionList eq(String property, Object value) {
			validateBooleanOperator();
			expressions.add(new WhereExpression(property, ConditionOperator.EQUALS, value));
			return this;
		}
		
		public ExpressionList like(String property, Object value) {
			validateBooleanOperator();
			expressions.add(new WhereExpression(property, ConditionOperator.LIKE, value));
			return this;
		}
		
		public ExpressionList gt(String property, Object value) {
			validateBooleanOperator();
			expressions.add(new WhereExpression(property, ConditionOperator.GREATER_THAN, value));
			return this;
		}
		
		public ExpressionList st(String property, Object value) {
			validateBooleanOperator();
			expressions.add(new WhereExpression(property, ConditionOperator.SMALLER_THAN, value));
			return this;
		}
		
		public ExpressionList and() {
			validateAddBooleanOperator();
			expressions.add(BooleanOperator.AND);
			return this;
		}
		
		public ExpressionList or() {
			validateAddBooleanOperator();
			expressions.add(BooleanOperator.OR);
			return this;
		}
		
		private void validateBooleanOperator() {
			if (expressions.size() == 0) {
				return;
			}
			
			if (!(expressions.get(expressions.size() - 1) instanceof BooleanOperator)) {
				//There must be an boolean operator before adding the next where expression
				throw new IllegalStateException("Must call and() or or() to continue with next where expression");
			}
		}
		
		private void validateAddBooleanOperator() {
			if (expressions.size() == 0 || expressions.get(expressions.size() - 1) instanceof BooleanOperator) {
				throw new IllegalStateException("Must call eq(), like(), gt() or st() before calling and() or or() again");
			}
		}
		
		public SQLQueryOptionsBuilder back() {
			return SQLQueryOptionsBuilder.this;
		}
		
		private boolean isEmpty() {
			return expressions.isEmpty();
		}
		
		private String build(boolean prepare) {
			if (expressions.size() == 0) {
				return "";
			}
			
			if (expressions.get(expressions.size() - 1) instanceof BooleanOperator) {
				throw new IllegalStateException("Last expression cannot be a BooleanOperator");
			}
			
			StringBuilder builder = new StringBuilder("WHERE ");
			
			Iterator<Object> iterator = expressions.iterator();
			while (iterator.hasNext()) {
				Object obj = iterator.next();
				
				if (obj instanceof WhereExpression) {
					builder.append(prepare ? ((WhereExpression)obj).prepare() : obj.toString());
				} else if (obj instanceof BooleanOperator) {
					builder.append(" ").append(((BooleanOperator)obj).getOperator()).append(" ");
				}
			}
			
			return builder.toString();
		}
		
		private int apply(PreparedStatement statement, int currentIndex) throws SQLException {
			for (Object obj : expressions) {
				if (!(obj instanceof WhereExpression)) {
					continue;
				}
				
				WhereExpression expr = (WhereExpression) obj;
				statement.setObject(currentIndex++, expr.value);
			}
			
			return currentIndex;
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
			
			public String prepare() {
				return property + operator.getOperator() + "?";
			}
			
			@Override
			public String toString() {
				boolean addQuote = true;
				
				if (value instanceof Number) {
					addQuote = false;
				}
				
				return property + operator.getOperator() + (addQuote ? "\"" : "") + value.toString() + (addQuote ? "\"" : "");
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
	
	private enum BooleanOperator {
		
		AND("AND"),
		OR("OR");
		
		private String operator;
		
		private BooleanOperator(String operator) {
			this.operator = operator;
		}
		
		public String getOperator() {
			return operator;
		}
		
	}
	
}
