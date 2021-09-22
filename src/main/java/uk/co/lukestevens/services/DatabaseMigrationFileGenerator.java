package uk.co.lukestevens.services;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import uk.co.lukestevens.DatabaseChangeException;
import uk.co.lukestevens.DatabaseSchemaChange;
import uk.co.lukestevens.interfaces.FileParser;

public class DatabaseMigrationFileGenerator extends AbstractDatabaseMigrator {

	int currentVersion = 0;
	final Path file;
	StringBuilder content;
	
	public DatabaseMigrationFileGenerator(Path path, FileParser<DatabaseSchemaChange> parser, Path file) {
		super(path, parser);
		this.file = file;
	}
	
	@Override
	public void migrate(int version) {
		content = new StringBuilder();
		super.migrate(version);
		addSql(setupDbSql);
		addSql("UPDATE " + schemaName + "." + tableName +" SET " + columnName + "=" + version + ";");
		try {
			writeToFile();
		} catch (IOException e) {
			throw new DatabaseChangeException(e);
		}
		currentVersion = version;
	}
	
	void writeToFile() throws IOException {
		Files.write(file, content.toString().getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
	}
	
	void deploy(DatabaseSchemaChange dsb) {
		addSql(dsb.getDeploySql());
	}

	// As this doesn't use a real database, and assumes the current version
	// is 0. This should never be called
	void rollback(DatabaseSchemaChange dsb) {
		throw new IllegalStateException("Cannot rollback DatabaseMigrationFileGenerator from version 0");
	}
	
	void addSql(String sql) {
		content.append(sql.trim());
	}
	
	@Override
	public int getCurrentDatabaseVersion() {
		return currentVersion;
	}

}
