package me.matzefratze123.heavyspleef.core.flag;

import java.util.Arrays;
import java.util.List;

public class FlagType {

	public static final LocationFlag WIN = new LocationFlag("win");
	public static final LocationFlag LOSE = new LocationFlag("lose");
	public static final LocationFlag LOBBY = new LocationFlag("lobby");
	public static final LocationFlag QUEUELOBBY = new LocationFlag("queuelobby");
	public static final LocationFlag SPAWNPOINT1 = new LocationFlag("spawnpoint1");
	public static final LocationFlag SPAWNPOINT2 = new LocationFlag("spawnpoint2");
	
	public static final IntegerFlag MINPLAYERS = new IntegerFlag("minplayers");
	public static final IntegerFlag MAXPLAYERS = new IntegerFlag("maxplayers");
	public static final IntegerFlag AUTOSTART = new IntegerFlag("autostart");
	public static final IntegerFlag COUNTDOWN = new IntegerFlag("countdown");
	public static final IntegerFlag JACKPOTAMOUNT = new IntegerFlag("jackpotamount");
	public static final IntegerFlag REWARD = new IntegerFlag("reward");
	public static final IntegerFlag CHANCES = new IntegerFlag("chances");
	public static final IntegerFlag TIMEOUT = new IntegerFlag("timeout");
	public static final IntegerFlag ROUNDS = new IntegerFlag("rounds");
	
	public static final BooleanFlag ONEVSONE = new BooleanFlag("1vs1");
	public static final BooleanFlag SHOVELS = new BooleanFlag("shovels");
	
	public static final Flag<?>[] flagList = new Flag<?>[] {WIN, LOSE, LOBBY, QUEUELOBBY, SPAWNPOINT1, SPAWNPOINT2,
															MINPLAYERS, MAXPLAYERS, AUTOSTART, COUNTDOWN, JACKPOTAMOUNT,
															REWARD, CHANCES, TIMEOUT, ROUNDS, ONEVSONE, SHOVELS};
	
	public static List<Flag<?>> getFlagList() {
		return Arrays.asList(flagList);
	}
}
