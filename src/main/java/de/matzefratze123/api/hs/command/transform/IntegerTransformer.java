package de.matzefratze123.api.hs.command.transform;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IntegerTransformer implements Transformer<Integer> {
	
	private static final Pattern INTEGER_PATTERN = Pattern.compile("[+,-]?\\d*");
	
	@Override
	public Integer transform(String argument) throws TransformException {
		Matcher matcher = INTEGER_PATTERN.matcher(argument);
		
		if (matcher.matches()) {
			return Integer.parseInt(argument);
		} else throw new TransformException();
	}

}
