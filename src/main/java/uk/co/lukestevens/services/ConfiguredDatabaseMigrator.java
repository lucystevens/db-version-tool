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

	final String updateVersionSql="UPDATE " + schemaName + "." + tableName +" SET " + columnName + " = ?";
	private final Database db;
	
	public ConfiguredDatabaseMigrator(Path path, FileParser<DatabaseSchemaChange> parser, Database db) {
		super(path, parser);
		this.db = db;
	}
	
	void deploy(DatabaseSchemaChange dsb) {
		try {
			db.update(dsb.getDeploySql());
			db.update(updateVersionSql, dsb.getVersion());
		} catch (SQLException e) {
			throw new DatabaseChangeException(e);
		}
	}
	
	void rollback(DatabaseSchemaChange dsb) {
		try {
			db.update(dsb.getRollbackSql());
			db.update(updateVersionSql, dsb.getVersion()-1);
		} catch (SQLException e) {
			throw new DatabaseChangeException(e);
		}
	}
	
	@Override
	public int getCurrentDatabaseVersion() {
		this.setupDatabase();
		try(DatabaseResult dbr = db.query(getVersionSql)){
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
			db.update(setupDbSql);
		} catch (SQLException e) {
			throw new DatabaseChangeException(e);
		}
	}

}
