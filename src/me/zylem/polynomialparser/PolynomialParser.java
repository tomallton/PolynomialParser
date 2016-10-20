package me.zylem.polynomialparser;

public class PolynomialParser {

	public static void main(String[] args) {
		System.out.println(Function.parse("1/2 + .3 + x").evaluate(3));
	}

}