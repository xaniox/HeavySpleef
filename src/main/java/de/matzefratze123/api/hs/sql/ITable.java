package de.matzefratze123.api.hs.sql;

import java.sql.SQLException;
import java.util.Map;

public interface ITable {
	
	public SQLResult select(String selection) throws SQLException;
	
	public SQLResult select(String selection, Map<String, Object> where) throws SQLException;
	
	public SQLResult selectAll() throws SQLException;
	
	public SQLResult selectAll(Map<String, Object> where) throws SQLException;
	
	public int insertOrUpdate(Map<String, Object> values, Map<String, Object> where) throws SQLException;
	
	public boolean hasRow(Map<String, Object> where) throws SQLException;
	
	public boolean hasColumn(String column) throws SQLException;
	
	public int addColumn(String column, Field field) throws SQLException;
	
	public String getName();
	
	public AbstractDatabase getDatabase();

}
