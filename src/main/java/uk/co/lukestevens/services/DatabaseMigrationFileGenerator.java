package uk.co.lukestevens.services;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import uk.co.lukestevens.DatabaseChangeException;
import uk.co.lukestevens.DatabaseSchemaChange;
import uk.co.lukestevens.interfaces.FileParser;

public class DatabaseMigrationFileGenerator extends AbstractDatabaseMigrator {
	
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
		try {
			writeToFile();
		} catch (IOException e) {
			throw new DatabaseChangeException(e);
		}
	}
	
	void writeToFile() throws IOException {
		Files.write(file, content.toString().getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
	}
	
	void deploy(DatabaseSchemaChange dsb) {
		addSql(dsb.getDeploySql());
	}
	
	void rollback(DatabaseSchemaChange dsb) {
		addSql(dsb.getRollbackSql());
	}
	
	void addSql(String sql) {
		content.append(sql.trim());
	}
	
	@Override
	public int getCurrentDatabaseVersion() {
		return 0;
	}

}
