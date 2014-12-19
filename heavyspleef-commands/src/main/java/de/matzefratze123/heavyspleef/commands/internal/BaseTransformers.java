package de.matzefratze123.heavyspleef.commands.internal;

import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class BaseTransformers {

	public static final Map<Class<?>, Transformer<?>> BASE_TRANSFORMERS;
	
	public static final Transformer<Integer> INTEGER_TRANSFORMER = new Transformer<Integer>() {

		@Override
		public Integer transform(String arg) throws TransformException {
			int result;
			
			try {
				result = Integer.parseInt(arg);
			} catch (NumberFormatException nfe) {
				throw new TransformException(nfe);
			}
			
			return result;
		}
	};
	
	public static final Transformer<Double> DOUBLE_TRANSFORMER = new Transformer<Double>() {

		@Override
		public Double transform(String arg) throws TransformException {
			double result;
			
			try {
				result = Double.parseDouble(arg);
			} catch (NumberFormatException nfe) {
				throw new TransformException(nfe);
			}
			
			return result;
		}
	};
	
	public static final Transformer<Boolean> BOOLEAN_TRANSFORMER = new Transformer<Boolean>() {
		
		private Set<String> falseKeywords = Sets.newHashSet("false", "no", "0");
		private Set<String> trueKeywords = Sets.newHashSet("true", "yes", "1");
		
		@Override
		public Boolean transform(String arg) throws TransformException {
			arg = arg.toLowerCase();
			
			if (falseKeywords.contains(arg)) {
				return false;
			} else if (trueKeywords.contains(arg)) {
				return true;
			} else throw new TransformException("argument " + arg + " not matching to any boolean expression");
		}
	};

	public static final Transformer<Player> PLAYER_TRANSFORMER = new Transformer<Player>() {
		
		@SuppressWarnings("deprecation")
		@Override
		public Player transform(String arg) throws TransformException {
			Player player = Bukkit.getPlayerExact(arg);
			
			return player;
		}
	};
	
	static {
		BASE_TRANSFORMERS = Maps.newHashMap();
		BASE_TRANSFORMERS.put(Integer.class, INTEGER_TRANSFORMER);
		BASE_TRANSFORMERS.put(Double.class, DOUBLE_TRANSFORMER);
		BASE_TRANSFORMERS.put(Boolean.class, BOOLEAN_TRANSFORMER);
		BASE_TRANSFORMERS.put(Player.class, PLAYER_TRANSFORMER);
	}
	
}
