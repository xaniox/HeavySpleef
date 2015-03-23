package de.matzefratze123.heavyspleef.core.layout;

import java.text.ParseException;
import java.util.List;
import java.util.Set;

import org.bukkit.block.Sign;

import com.google.common.collect.Lists;

import de.matzefratze123.heavyspleef.core.script.Variable;

public class SignLayout {
	
	public static final int LINE_COUNT = 4;
	
	private List<SignLine> lines;
	
	public SignLayout(List<String> lines) throws ParseException {
		this.lines = Lists.newArrayList();
		
		for (int i = 0; i < lines.size(); i++) {
			SignLine line = new SignLine(lines.get(i));
			
			this.lines.add(line);
		}
	}
	
	public void inflate(Sign sign, Set<Variable> variables) {
		for (int i = 0; i < lines.size() && i < LINE_COUNT; i++) {
			SignLine line = lines.get(i);
			String lineString = line.generate(variables);
			
			sign.setLine(i, lineString);
		}
	}
	
}
