package de.matzefratze123.api.hs.command.transform;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DoubleTransformer implements Transformer<Double> {
	
	private static final Pattern FLOATING_NUMBER_PATTERN = Pattern.compile("[\\-\\+]?[0-9]*(\\.[0-9]+)?");
	
	@Override
	public Double transform(String argument) throws TransformException {
		Matcher matcher = FLOATING_NUMBER_PATTERN.matcher(argument);
		
		if (matcher.matches()) {
			return Double.parseDouble(argument);
		} else throw new TransformException();
	}
	
}
