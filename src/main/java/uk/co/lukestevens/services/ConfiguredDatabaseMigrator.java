package uk.co.lukestevens.services;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import uk.co.lukestevens.DatabaseChangeException;
import uk.co.lukestevens.DatabaseSchemaChange;
import uk.co.lukestevens.interfaces.DatabaseMigrator;
import uk.co.lukestevens.interfaces.FileParser;
import uk.co.lukestevens.jdbc.Database;
import uk.co.lukestevens.jdbc.result.DatabaseResult;

public class ConfiguredDatabaseMigrator implements DatabaseMigrator {
	
	private final Path path;
	private final FileParser<DatabaseSchemaChange> parser;
	private final Database db;
	
	private List<DatabaseSchemaChange> changes;
	
	public ConfiguredDatabaseMigrator(Path path, FileParser<DatabaseSchemaChange> parser, Database db) {
		this.path = path;
		this.parser = parser;
		this.db = db;
	}
	
	public void migrate(int version) {
		int currentVersion = this.getCurrentDatabaseVersion();
		if(version == currentVersion) {
			return;
		}
		else if(version > currentVersion) {
			this.deploy(version);
		}
		else {
			this.rollback(version);
		}
	}
	
	public void migrate() {
		this.migrate(this.getLatestChangeVersion());
	}
	
	void deploy(final int version) {
		this.getChanges()
		    .stream()
		    .sorted(SchemeChangeComparators.VERSION_ASC)
		    .filter(dsb -> dsb.getVersion() <= version)
		    .forEachOrdered(this::deploy);
	}
	
	void deploy(DatabaseSchemaChange dsb) {
		try {
			db.update(dsb.getDeploySql());
			db.update("UPDATE version SET version = ?", dsb.getVersion());
		} catch (SQLException e) {
			throw new DatabaseChangeException(e);
		}
	}
	
	void rollback(final int version) {
		this.getChanges()
	    .stream()
	    .sorted(SchemeChangeComparators.VERSION_DESC)
	    .filter(dsb -> dsb.getVersion() > version)
	    .forEachOrdered(this::rollback);
	}
	
	void rollback(DatabaseSchemaChange dsb) {
		try {
			db.update(dsb.getRollbackSql());
			db.update("UPDATE version SET version = ?", dsb.getVersion()-1);
		} catch (SQLException e) {
			throw new DatabaseChangeException(e);
		}
	}
	
	@Override
	public int getCurrentDatabaseVersion() {
		try(DatabaseResult dbr = db.query("SELECT version FROM version;")){
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
	
	int getLatestChangeVersion() {
		return getChanges()
				.stream()
				.sorted(SchemeChangeComparators.VERSION_DESC)
				.map(DatabaseSchemaChange::getVersion)
				.findFirst()
				.orElse(0);
	}
	
	List<DatabaseSchemaChange> getChanges(){
		if(changes == null) {
			changes = new ArrayList<>();
			for(File file : path.toFile().listFiles()) {
				changes.add(parser.parse(file));
			}
		}
		return changes;
	}

}
