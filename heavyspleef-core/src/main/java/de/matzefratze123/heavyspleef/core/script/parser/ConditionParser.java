package de.matzefratze123.heavyspleef.core.script.parser;

import java.text.ParseException;

import de.matzefratze123.heavyspleef.core.script.ComparingCondition;
import de.matzefratze123.heavyspleef.core.script.Condition;
import de.matzefratze123.heavyspleef.core.script.SimpleCondition;
import de.matzefratze123.heavyspleef.core.script.VariableHolder;
import de.matzefratze123.heavyspleef.core.script.ComparingCondition.Operator;

public class ConditionParser implements Parser<Condition> {
	
	// $[var] < 0
	// 4>=6
	// $[var]==$[otherVar]
	
	private String condition;
	private State state;
	private int position;
	
	public ConditionParser(String condition) {
		this.condition = condition;
		this.state = State.READ_OPERAND_1; 
	}
	
	@Override
	public Condition parse() throws ParseException {
		int length = condition.length();
		String tmpStr = "";
		int tmpPos = 0;
		
		Object operand1 = null;
		Operator operator = null;
		Object operand2 = null;
		
		for (position = 0; position < length; position++) {
			char c = condition.charAt(position);
			
			if (c == ' ') {
				//Ignore space characters
				continue;
			}
			
			switch (state) {
			case READ_OPERAND_1:
				if (isScriptOperator(c, 0) || position == length - 1) {
					if (position == length - 1) {
						tmpStr += c;
					} else {
						state = State.READ_OPERATOR;
					}
					
					if (tmpStr.isEmpty()) {
						throw new ParseException("Condition must have an operand before defining an operator", position);
					}
					
					if (tmpStr.startsWith("$")) {
						//This is a variable
						VariableHolderParser varParser = new VariableHolderParser(tmpStr);
						VariableHolder var = varParser.parse();
						
						operand1 = var;
					} else {
						operand1 = parseString(tmpStr, position);
					}
					
					tmpStr = "";
					tmpPos = 0;
					
					if (position != length - 1) {
						position -= 1;
					}
				} else {
					tmpStr += c;
				}
				break;
			case READ_OPERATOR:
				if (isScriptOperator(c, tmpPos)) {
					if (position == length - 1) {
						throw new ParseException("Operator must be followed by a second operand", position);
					}
					
					++tmpPos;
					tmpStr += c;
				} else {
					state = State.READ_OPERAND_2;
					
					operator = Operator.byScriptOperator(tmpStr);
					position -= 1;
					tmpStr = "";
					tmpPos = 0;
				}
				break;
			case READ_OPERAND_2:
				if (position == length - 1) {
					tmpStr += c;
					
					if (tmpStr.isEmpty()) {
						throw new ParseException("Operator must be followed by a second operand", position);
					}
					
					if (tmpStr.startsWith("$")) {
						//This is a variable
						VariableHolderParser varParser = new VariableHolderParser(tmpStr);
						VariableHolder var = varParser.parse();
						
						operand2 = var;
					} else {
						operand2 = parseString(tmpStr, position);
					}
				} else {
					tmpStr += c;
				}
				break;
			default:
				break;
			
			}
		}
		
		Condition condition;
		
		if (operator == null || operand2 == null) {
			condition = new SimpleCondition(operand1);
		} else {
			condition = new ComparingCondition(operator, operand1, operand2);
		}
		
		return condition;
	}
	
	private Object parseString(String str, int offset) throws ParseException {
		Object result;
		
		if (isNumberFormat(str)) {
			if (str.contains(".")) {
				result = Double.parseDouble(str);
			} else {
				result = Integer.parseInt(str);
			}
		} else if (str.startsWith("\"") && str.endsWith("\"")) {
			result = str.substring(1, str.length() - 1);
		} else if (str.equalsIgnoreCase("true") || str.equalsIgnoreCase("false")) {
			result = Boolean.valueOf(str);
		} else {
			throw new ParseException(str, offset);
		}
		
		return result;
	}
	
	private boolean isNumberFormat(String str) {
		boolean isNumber = true;
		boolean hadDigit = false;
		
		for (int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);
			
			if (!Character.isDigit(c) && (hadDigit || c != '.')) {
				isNumber = false;
			}
			
			if (c == '.') {
				hadDigit = true;
			}
		}
		
		return isNumber;
	}
	
	private boolean isScriptOperator(char c, int pos) {
		for (ComparingCondition.Operator operator : ComparingCondition.Operator.values()) {
			String scriptOperator = operator.getScriptOperator();
			if (pos < scriptOperator.length() && scriptOperator.charAt(pos) == c) {
				return true;
			}
		}
		
		return false;
	}
	
	private enum State {
		
		READ_OPERAND_1,
		READ_OPERATOR,
		READ_OPERAND_2;
		
	}
	
}
