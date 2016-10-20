package me.zylem.polynomialparser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Function {

	public static final String[] OPERATORS = { "+", "-", "*", "/", "^" };
	public static final String DECIMAL_POINT = ".";

	private final String function;
	private String[] parameters;

	private Function(String function) {
		this.function = function;
	}

	@Override
	public String toString() {
		double answer = evaluate(new double[getParameters().length]);
		if (answer % 1 == 0)
			return String.valueOf((int) answer);
		return String.valueOf(answer);
	}

	public String toStringFormatted() {
		String formatted = function.replaceAll("\\+", " + ").replaceAll("\\-", " - ").trim();
		if (formatted.startsWith("+ "))
			formatted = formatted.replaceFirst("\\+ ", "+");
		if (formatted.startsWith("- "))
			formatted = formatted.replaceFirst("\\- ", "-");
		return formatted;
	}

	public String[] getParameters() {
		if (parameters != null)
			return parameters;
		List<String> parameters = new ArrayList<>();
		for (int i = 0; i < function.length(); i++) {
			String character = function.substring(i, i + 1);
			if (isParameter(character) && !parameters.contains(character))
				parameters.add(character);
		}
		return this.parameters = parameters.toArray(new String[parameters.size()]);
	}

	public int getIndex(String parameter) {
		int index = Arrays.asList(parameter).indexOf(parameter);
		if (index == -1)
			throw new IllegalArgumentException(parameter + " is not a parameter");
		return index;
	}

	public double evaluate(double... parameter) {
		if (parameter.length != getParameters().length)
			throw new IllegalArgumentException("Function " + function + " requires " + getParameters().length + " parameter" + (getParameters().length != 1 ? "s" : "") + ", you have supplied " + parameter.length);
		double answer = 0;
		String function = this.function;
		for (int i = 0; i < function.length(); i++) {
			String character = function.substring(i, i + 1);
			if (isParameter(character) && (i == 0 || !isNumber(function.substring(i - 1, i))))
				function = function.substring(0, i) + "1" + function.substring(i, function.length());
		}
		if (isNumber(function.substring(0, 1)) || isDecimalPoint(function.substring(0, 1)))
			function = "+" + function;
		for (int i = 0; i < function.length(); i++) {
			String character = function.substring(i, i + 1);
			if (isParameter(character)) {
				function = function.substring(0, i) + "*" + parameter[getIndex(character)]
						+ (i + 2 < function.length() && (isNumber(function.substring(i + 1, i + 2)) || isDecimalPoint(function.substring(i + 1, i + 2))) ? "_*_" : "") + function.substring(i + 1, function.length());
			}
		}
		for (int i = 0; i < function.length(); i++) {
			String stage = "";
			String sign = function.substring(i, i + 1);
			String character = function.substring(i + 1, i + 2);
			master: while (isNumber(character) || isDecimalPoint(character) || (isOperator(character) && !isSign(character))) {
				if (isOperator(character) && !isSign(character)) {
					String operator = character;
					i++;
					character = function.substring(i + 1, i + 2);
					String lastStage = stage;
					stage = "";
					while (isNumber(character) || isDecimalPoint(character)) {
						stage += character;
						i++;
						if (i + 2 > function.length()) {
							if (operator.equals("*"))
								stage = String.valueOf(Double.valueOf(lastStage) * Double.valueOf(stage));
							else if (operator.equals("/"))
								stage = String.valueOf(Double.valueOf(lastStage) / Double.valueOf(stage));
							break master;
						}
						character = function.substring(i + 1, i + 2);
					}
					if (operator.equals("*"))
						stage = String.valueOf(Double.valueOf(lastStage) * Double.valueOf(stage));
					else if (operator.equals("/"))
						stage = String.valueOf(Double.valueOf(lastStage) / Double.valueOf(stage));
					continue;
				}
				stage += character;
				i++;
				if (i + 2 > function.length())
					break;
				character = function.substring(i + 1, i + 2);
			}
			if (sign.equals("+"))
				answer += Double.parseDouble(stage);
			else if (sign.equals("-"))
				answer -= Double.parseDouble(stage);
		}
		return answer;
	}

	public static Function parse(String function) {
		if (function == null || function.length() == 0)
			return null;
		function = function.toLowerCase().replaceAll(" ", "");
		String lastCharacter = null;
		for (int i = 0; i < function.length(); i++) {
			String character = function.substring(i, i + 1);
			if (!isOperator(character) && !isNumber(character) && !isParameter(character) && !isDecimalPoint(character))
				throw new IllegalArgumentException("Invalid character '" + character + "'");
			if (i == 0 && isOperator(character) && !isSign(character))
				throw new IllegalArgumentException("Functions can not start with '" + character + "'");
			if (lastCharacter != null)
				if (isOperator(lastCharacter) && isOperator(character))
					throw new IllegalArgumentException("Successive operators '" + lastCharacter + character + "' not allowed");
			lastCharacter = character;
		}
		for (int i = 0; i < function.length() - 1; i++) {
			String stage = "";
			String character = function.substring(i + 1, i + 2);
			while (isNumber(character) || isDecimalPoint(character)) {
				if (isDecimalPoint(character) && stage.contains(DECIMAL_POINT))
					throw new IllegalArgumentException("Number '" + (stage + character) + "' contains two decimal places");
				stage += character;
				i++;
				if (i + 2 > function.length())
					break;
				character = function.substring(i + 1, i + 2);
			}
		}
		return new Function(function);
	}

	public static boolean isOperator(String operator) {
		return Arrays.asList(OPERATORS).contains(operator);
	}

	public static boolean isNumber(String number) {
		try {
			Double.parseDouble(number);
			return true;
		} catch (NumberFormatException ignored) {
		}
		return false;
	}

	public static boolean isDecimalPoint(String decimalPoint) {
		return decimalPoint.equals(DECIMAL_POINT);
	}

	public static boolean isParameter(String parameter) {
		if (parameter == null || parameter.length() != 1)
			return false;
		return "abcdefghijklmnopqrstuvwxyz".contains(parameter);
	}

	public static boolean isSign(String sign) {
		return sign.equals("+") || sign.equals("-");
	}

}