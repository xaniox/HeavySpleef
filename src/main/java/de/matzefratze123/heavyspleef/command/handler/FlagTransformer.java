package de.matzefratze123.heavyspleef.command.handler;

import de.matzefratze123.api.command.transform.TransformException;
import de.matzefratze123.api.command.transform.Transformer;
import de.matzefratze123.heavyspleef.core.flag.Flag;
import de.matzefratze123.heavyspleef.core.flag.FlagType;

@SuppressWarnings("rawtypes")
public class FlagTransformer implements Transformer<Flag> {

	@Override
	public Flag<?> transform(String argument) throws TransformException {
		Flag<?> flag = null;
		
		for (Flag<?> f : FlagType.getFlagList()) {
			if (flag != null) {
				break;
			}
			
			if (f.getName().equalsIgnoreCase(argument)) {
				flag = f;
				break;
			} else {
				//Check the aliases
				String[] aliases = f.getAliases();
				if (aliases == null) //Aliases null, continue
					continue;
				
				for (String alias : aliases) {
					if (alias.equalsIgnoreCase(argument)) {
						flag = f;
						break;
					}
				}
			}
		}
		
		if (flag == null) {
			throw new TransformException();
		}
		
		return flag;
	}

}
