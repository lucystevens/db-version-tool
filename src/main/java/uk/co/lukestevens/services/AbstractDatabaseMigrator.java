package uk.co.lukestevens.services;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import uk.co.lukestevens.DatabaseSchemaChange;
import uk.co.lukestevens.interfaces.DatabaseMigrator;
import uk.co.lukestevens.interfaces.FileParser;

public abstract class AbstractDatabaseMigrator implements DatabaseMigrator {
	
	final Path path;
	final FileParser<DatabaseSchemaChange> parser;
	
	List<DatabaseSchemaChange> changes;
	
	public AbstractDatabaseMigrator(Path path, FileParser<DatabaseSchemaChange> parser) {
		this.path = path;
		this.parser = parser;
	}
	
	public void migrate(int version) {
		int currentVersion = this.getCurrentDatabaseVersion();
		if(version == currentVersion) {
			return;
		}
		else if(version > currentVersion) {
			this.deploy(version, currentVersion);
		}
		else {
			this.rollback(version, currentVersion);
		}
	}
	
	public void migrate() {
		this.migrate(this.getLatestChangeVersion());
	}
	
	void deploy(final int version, final int currentVersion) {
		this.getChanges()
		    .stream()
		    .sorted(SchemeChangeComparators.VERSION_ASC)
		    .filter(dsb -> dsb.getVersion() <= version && dsb.getVersion() > currentVersion)
		    .forEachOrdered(this::deploy);
	}
	
	abstract void deploy(DatabaseSchemaChange dsb);
	
	void rollback(final int version, final int currentVersion) {
		this.getChanges()
	    .stream()
	    .sorted(SchemeChangeComparators.VERSION_DESC)
	    .filter(dsb -> dsb.getVersion() > version && dsb.getVersion() <= currentVersion)
	    .forEachOrdered(this::rollback);
	}
	
	abstract void rollback(DatabaseSchemaChange dsb);
	
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
			File filepath = path.toFile();
			for(File file : filepath.listFiles()) {
				if(file.getName().endsWith(".sql")) {
					changes.add(parser.parse(file));
				}
			}
		}
		return changes;
	}

}
