/*
 * This file is part of HeavySpleef.
 * Copyright (c) 2014-2015 matzefratze123
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package de.matzefratze123.heavyspleef.persistence.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.google.common.collect.Maps;

import de.matzefratze123.heavyspleef.core.Statistic;
import de.matzefratze123.heavyspleef.persistence.sql.SQLAccessor.Field.Type;
import de.matzefratze123.heavyspleef.persistence.sql.SQLDatabaseContext.SQLImplementation;

public class StatisticAccessor extends SQLAccessor<Statistic, UUID> {
	
	@Override
	public Class<Statistic> getObjectClass() {
		return Statistic.class;
	}
	
	@Override
	public String getTableName() {
		return ColumnContract.TABLE_NAME;
	}
	
	@Override
	public Map<String, Field> defineSchema() {
		Map<String, Field> schema = Maps.newLinkedHashMap();
		schema.put(ColumnContract.ID, new Field(Type.INT).primaryKey().autoIncrement());
		schema.put(ColumnContract.UUID, new Field(Type.CHAR).length(36).unique());
		schema.put(ColumnContract.WINS, new Field(Type.INT));
		schema.put(ColumnContract.LOSSES, new Field(Type.INT));
		schema.put(ColumnContract.KNOCKOUTS, new Field(Type.INT));
		schema.put(ColumnContract.GAMES_PLAYED, new Field(Type.INT));
		schema.put(ColumnContract.BLOCKS_BROKEN, new Field(Type.INT));
		schema.put(ColumnContract.TIME_PLAYED, new Field(Type.BIGINT));
		schema.put(ColumnContract.RATING, new Field(Type.DOUBLE));
		
		return schema;
	}

	@Override
	public void write(Statistic object, Connection connection) throws SQLException {
		StringBuilder insertSql = new StringBuilder("INSERT ");
		if (getSqlImplementation() == SQLImplementation.SQLITE) {
			insertSql.append("OR IGNORE ");
		}
		
		insertSql.append("INTO " + ColumnContract.TABLE_NAME + " (");
		addColumnSignature(insertSql);
		
		insertSql.append(") VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
		
		if (getSqlImplementation() == SQLImplementation.MYSQL) {
			insertSql.append(" ON DUPLICATE KEY UPDATE ");
			
			String[] allColumns = ColumnContract.ALL_COLUMNS;
			for (int i = 0; i < allColumns.length; i++) {
				String column = allColumns[i];
				insertSql.append(column + "=?");
				
				if (i + 1 < allColumns.length) {
					insertSql.append(',');
				}
			}
		}
		
		insertSql.append(';');
		try (PreparedStatement insertStatement = connection.prepareStatement(insertSql.toString())) {
			setValues(insertStatement, object, true, 1);
			
			if (getSqlImplementation() == SQLImplementation.MYSQL) {
				setValues(insertStatement, object, true, 9);
			}
			
			insertStatement.executeUpdate();
		}
		
		if (getSqlImplementation() == SQLImplementation.SQLITE) {
			StringBuilder updateSql = new StringBuilder("UPDATE ");
			updateSql.append(ColumnContract.TABLE_NAME).append(" SET ");
			
			String[] allColumns = ColumnContract.ALL_COLUMNS;
			for (int i = 0; i < allColumns.length; i++) {
				String column = allColumns[i];
				updateSql.append(column + "=?");
				
				if (i + 1 < allColumns.length) {
					updateSql.append(',');
				}
			}
			
			updateSql.append(" WHERE " + ColumnContract.UUID + "=?");
			
			try (PreparedStatement updateStatement = connection.prepareStatement(updateSql.toString())) {
				setValues(updateStatement, object, false, 1);

				updateStatement.setString(9, object.getUniqueIdentifier().toString());
				updateStatement.executeUpdate();
			}
		}
	}
	
	private void addColumnSignature(StringBuilder builder) {
		builder.append(ColumnContract.UUID).append(", ");
		builder.append(ColumnContract.WINS).append(", ");
		builder.append(ColumnContract.LOSSES).append(", ");
		builder.append(ColumnContract.KNOCKOUTS).append(", ");
		builder.append(ColumnContract.GAMES_PLAYED).append(", ");
		builder.append(ColumnContract.BLOCKS_BROKEN).append(", ");
		builder.append(ColumnContract.TIME_PLAYED).append(", ");
		builder.append(ColumnContract.RATING);
	}
	
	private void setValues(PreparedStatement statement, Statistic statistic, boolean addUniqueColumns, int indexOffset) throws SQLException {
		int index = indexOffset;
		
		if (addUniqueColumns) {
			statement.setString(index++, statistic.getUniqueIdentifier().toString());
		}
		
		statement.setInt(index++, statistic.getWins());
		statement.setInt(index++, statistic.getLosses());
		statement.setInt(index++, statistic.getKnockouts());
		statement.setInt(index++, statistic.getGamesPlayed());
		statement.setInt(index++, statistic.getBlocksBroken());
		statement.setLong(index++, statistic.getTimePlayed());
		statement.setDouble(index++, statistic.getRating());
	}

	@Override
	public Statistic fetch(UUID key, Connection connection) throws SQLException {
		StringBuilder selectSql = new StringBuilder("SELECT * FROM ");
		selectSql.append(ColumnContract.TABLE_NAME);
		selectSql.append(" WHERE " + ColumnContract.UUID + "=?");
		selectSql.append(";");
		
		Statistic statistic = null;
		
		try (PreparedStatement statement = connection.prepareStatement(selectSql.toString())) {
			statement.setString(1, key.toString());
			
			try (ResultSet result = statement.executeQuery()) {
				if (result.next()) {
					statistic = fetchStatisticFromResult(key, result);
				}
			}
		}
		
		return statistic;
	}
	
	private Statistic fetchStatisticFromResult(UUID uuid, ResultSet result) throws SQLException {
		if (uuid == null) {
			uuid = UUID.fromString(result.getString(ColumnContract.UUID));
		}
		
		Statistic statistic = new Statistic(uuid);
		
		int wins = result.getInt(ColumnContract.WINS);
		int losses = result.getInt(ColumnContract.LOSSES);
		int knockouts = result.getInt(ColumnContract.KNOCKOUTS);
		int gamesPlayed = result.getInt(ColumnContract.GAMES_PLAYED);
		int blocksBroken = result.getInt(ColumnContract.BLOCKS_BROKEN);
		long timePlayed = result.getLong(ColumnContract.TIME_PLAYED);
		int rating = result.getInt(ColumnContract.RATING);
		
		statistic.setWins(wins);
		statistic.setLosses(losses);
		statistic.setKnockouts(knockouts);
		statistic.setGamesPlayed(gamesPlayed);
		statistic.setTimePlayed(timePlayed);
		statistic.setBlocksBroken(blocksBroken);
		statistic.setRating(rating);
		
		return statistic;
	}

	@Override
	public List<Statistic> fetch(SQLQueryOptionsBuilder optionsBuilder, Connection connection) throws SQLException {
		StringBuilder selectSql = new StringBuilder("SELECT * FROM ");
		selectSql.append(ColumnContract.TABLE_NAME);
		
		if (optionsBuilder != null) {
			selectSql.append(' ');
			selectSql.append(optionsBuilder.build());
		}
		
		selectSql.append(";");
		
		List<Statistic> statistics = new ArrayList<Statistic>();
		try (PreparedStatement statement = connection.prepareStatement(selectSql.toString());
			ResultSet result = statement.executeQuery()) {			
			while (result.next()) {
				statistics.add(fetchStatisticFromResult(null, result));
			}
		}
		
		return statistics;
	}
	
	public interface ColumnContract {
		
		public static final String TABLE_NAME = "heavyspleef_statistics";
		
		public static final String ID = "id";
		public static final String UUID = "uuid";
		public static final String WINS = "wins";
		public static final String LOSSES = "losses";
		public static final String KNOCKOUTS = "knockouts";
		public static final String GAMES_PLAYED = "games_played";
		public static final String BLOCKS_BROKEN = "blocks_broken";
		public static final String TIME_PLAYED = "time_played";
		public static final String RATING = "rating";
		
		public static final String[] ALL_COLUMNS = {UUID, WINS, LOSSES, KNOCKOUTS, GAMES_PLAYED, BLOCKS_BROKEN, TIME_PLAYED, RATING};
		
	}

}
