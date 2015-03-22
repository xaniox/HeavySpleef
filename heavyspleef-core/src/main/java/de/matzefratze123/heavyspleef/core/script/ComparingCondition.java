package de.matzefratze123.heavyspleef.core.script;

import java.util.Set;

public class ComparingCondition implements Condition {
	
	private Operator operator;
	private Object operand1;
	private Object operand2;
	
	public ComparingCondition(Operator type, Object operand1, Object operand2) {
		this.operator = type;
		this.operand1 = operand1;
		this.operand2 = operand2;
	}
	
	@Override
	public boolean eval(Set<Variable> vars) {
		return operator.eval(vars, this);
	}
	
	@Override
	public String toString() {
		return operand1 + " " + operator.getScriptOperator() + " " + operand2;
	}
	
	public enum Operator {
		
		GREATER_THAN(">") {
			@Override
			public boolean eval(Set<Variable> vars, ComparingCondition condition) {
				return compareNumbers(vars, condition.operand1, condition.operand2) > 0;
			}
		},
		SMALLER_THAN("<") {
			@Override
			public boolean eval(Set<Variable> vars, ComparingCondition condition) {
				return compareNumbers(vars, condition.operand1, condition.operand2) < 0;
			}
		},
		GREATER_SAME_THAN(">=") {
			@Override
			public boolean eval(Set<Variable> vars, ComparingCondition condition) {
				int result = compareNumbers(vars, condition.operand1, condition.operand2);
				
				return result > 0 || result == 0;
			}
		},
		SMALLER_SAME_THAN("<=") {
			@Override
			public boolean eval(Set<Variable> vars, ComparingCondition condition) {
				int result = compareNumbers(vars, condition.operand1, condition.operand2);
				
				return result < 0 || result == 0;
			}
		},
		SAME("==") {
			@Override
			public boolean eval(Set<Variable> vars, ComparingCondition condition) {
				Value operand1 = null;
				Value operand2 = null;
				
				if (condition.operand1 instanceof VariableHolder) {
					operand1 = assign((VariableHolder)condition.operand1, vars).getValue();
				} else {
					operand1 = new Value(condition.operand1);
				}
				
				if (condition.operand2 instanceof VariableHolder) {
					operand2 = assign((VariableHolder)condition.operand2, vars).getValue();
				} else {
					operand2 = new Value(condition.operand2);
				}
				
				return operand1.equals(operand2);
			}
		};
		
		private String scriptOperator;
		
		private Operator(String scriptOperator) {
			this.scriptOperator = scriptOperator;
		}
		
		public abstract boolean eval(Set<Variable> vars, ComparingCondition condition);
		
		public String getScriptOperator() {
			return scriptOperator;
		}
		
		private static Variable assign(VariableHolder holder, Set<Variable> vars) {
			for (Variable var : vars) {
				if (var.getName().equals(holder.getName())) {
					return var;
				}
			}
			
			return null;
		}
		
		private static int compareNumbers(Set<Variable> vars, Object operand1, Object operand2) {
			Value operand1Value = null;
			Value operand2Value = null;
			
			operand1Value = validateAndGetNumber(operand1, vars);
			operand2Value = validateAndGetNumber(operand2, vars);
			
			if (operand1Value == null || operand2Value == null) {
				throw new NullPointerException();
			}
			
			return operand1Value.compareTo(operand2Value);
		}
		
		private static Value validateAndGetNumber(Object operand, Set<Variable> vars) {
			Value resultOperand = null;
			
			if (operand instanceof VariableHolder) {
				VariableHolder holder = (VariableHolder) operand;
				
				Variable var = assign(holder, vars);
				SyntaxValidate.notNull(var, "Variable " + holder.getName() + " cannot be assigned to a value: No value found");
				SyntaxValidate.isTrue(var.isInt() || var.isDouble(), "Variable must be either an int or a double to perform a greater than (>)");
				
				resultOperand = var.getValue();
			} else if (operand instanceof Number) {
				resultOperand = new Value(operand);
			}
			
			return resultOperand;
		}

		public static Operator byScriptOperator(String tmpStr) {
			for (Operator operator : values()) {
				if (operator.getScriptOperator().equals(tmpStr)) {
					return operator;
				}
			}
			
			return null;
		}
		
	}

}
