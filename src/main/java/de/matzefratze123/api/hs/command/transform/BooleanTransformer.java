package de.matzefratze123.api.hs.command.transform;

public class BooleanTransformer implements Transformer<Boolean> {

	@Override
	public Boolean transform(String argument) throws TransformException {
		if (argument.equalsIgnoreCase("on") || argument.equalsIgnoreCase("true")) {
			return true;
		} else if (argument.equalsIgnoreCase("off") || argument.equalsIgnoreCase("false")){
			return false;
		}
		
		throw new TransformException();
	}

}
