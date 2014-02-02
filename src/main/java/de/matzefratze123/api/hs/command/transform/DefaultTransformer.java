package de.matzefratze123.api.hs.command.transform;

public class DefaultTransformer implements Transformer<String> {

	@Override
	public String transform(String argument) {
		return argument;
	}

}
