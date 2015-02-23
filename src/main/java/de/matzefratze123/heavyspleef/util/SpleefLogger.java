/*
 * HeavySpleef - Advanced spleef plugin for bukkit
 *
 * Copyright (C) 2013-2014 matzefratze123
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.matzefratze123.heavyspleef.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import de.matzefratze123.heavyspleef.HeavySpleef;
import de.matzefratze123.heavyspleef.core.Game;
import de.matzefratze123.heavyspleef.objects.SpleefPlayer;

public class SpleefLogger {

	private static File	logFile;

	static {
		if (logFile == null) {
			logFile = new File(HeavySpleef.getInstance().getDataFolder(), "spleef-log.txt");
			if (!logFile.exists()) {
				try {
					logFile.createNewFile();
				} catch (IOException e) {
					handleException(e);
				}
			}
		}
	}

	public static void log(LogType type, Game game, SpleefPlayer player) {
		String msg = String.format(type.getMessage(), player.getRawName());
		logRaw("Game " + game.getName() + ": " + msg);
	}

	public static void logRaw(String str) {
		BufferedWriter writer = null;

		try {

			FileWriter fw = new FileWriter(logFile, true);
			writer = new BufferedWriter(fw);

			String time = getFormattedTime();

			writer.write(time + " " + str);
			writer.newLine();
			writer.flush();

		} catch (IOException e) {
			handleException(e);
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
					handleException(e);
				}
			}
		}
	}

	private static String getFormattedTime() {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return formatter.format(new Date());
	}

	private static void handleException(Exception e) {
		Logger.severe("Exception: " + e.getMessage());
		e.printStackTrace();
	}

	public static enum LogType {

		WIN("Player %s won the game"), LOSE("Player %s lost the game"), JOIN("Player %s joined the game"), LEAVE("Player %s left the game");

		private String	msg;

		private LogType(String msg) {
			this.msg = msg;
		}

		public String getMessage() {
			return msg;
		}

	}

}
