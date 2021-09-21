package uk.co.lukestevens.services;

import java.io.IOException;
import java.nio.file.Path;
import java.sql.ResultSet;
import java.sql.SQLException;
import uk.co.lukestevens.DatabaseChangeException;
import uk.co.lukestevens.DatabaseSchemaChange;
import uk.co.lukestevens.db.Database;
import uk.co.lukestevens.db.DatabaseResult;
import uk.co.lukestevens.interfaces.FileParser;

public class ConfiguredDatabaseMigrator extends AbstractDatabaseMigrator {
	
	private final String databaseName="core.version";
	private final String columnName="version";
	
	private final Database db;
	
	public ConfiguredDatabaseMigrator(Path path, FileParser<DatabaseSchemaChange> parser, Database db) {
		super(path, parser);
		this.db = db;
	}
	
	void deploy(DatabaseSchemaChange dsb) {
		try {
			db.update(dsb.getDeploySql());
			db.update("UPDATE " + databaseName +" SET " + columnName + " = ?", dsb.getVersion());
		} catch (SQLException e) {
			throw new DatabaseChangeException(e);
		}
	}
	
	void rollback(DatabaseSchemaChange dsb) {
		try {
			db.update(dsb.getRollbackSql());
			db.update("UPDATE " + databaseName +" SET " + columnName + " = ?", dsb.getVersion()-1);
		} catch (SQLException e) {
			throw new DatabaseChangeException(e);
		}
	}
	
	@Override
	public int getCurrentDatabaseVersion() {
		this.setupDatabase();
		try(DatabaseResult dbr = db.query("SELECT " + columnName + " FROM " + databaseName + ";")){
			ResultSet rs = dbr.getResultSet();
			if(rs.next()) {
				return rs.getInt("version");
			}
			else {
				throw new DatabaseChangeException("No version information available in database");
			}
		} catch (IOException | SQLException e) {
			throw new DatabaseChangeException(e);
		}
	}
	
	void setupDatabase() {
		try {
			db.update("create schema if not exists core; " + 
					"CREATE TABLE if not exists core.version(version INT PRIMARY KEY); " + 
					"INSERT INTO core.version(version) SELECT 0 WHERE NOT EXISTS (SELECT * FROM core.version);");
		} catch (SQLException e) {
			throw new DatabaseChangeException(e);
		}
	}

}
