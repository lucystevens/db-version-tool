package uk.co.lukestevens;

import java.io.IOException;
import org.apache.commons.cli.ParseException;

import uk.co.lukestevens.cli.CLIParser;
import uk.co.lukestevens.config.Config;
import uk.co.lukestevens.config.models.EnvironmentConfig;
import uk.co.lukestevens.db.Database;
import uk.co.lukestevens.interfaces.DatabaseMigrator;
import uk.co.lukestevens.interfaces.FileParser;
import uk.co.lukestevens.jdbc.ConfiguredDatabase;
import uk.co.lukestevens.services.ConfiguredDatabaseMigrator;
import uk.co.lukestevens.services.DatabaseMigrationFileGenerator;
import uk.co.lukestevens.services.SchemaChangeParser;

public class DBVersionToolMain {
	
	public static void main(String[] args) throws ParseException, IOException {
		CLIParser parser = new CLIParser();
		DBVersionToolSetup setup = parser.parseCommandLine(args, DBVersionToolSetup.class);
		if(setup == null) {
			System.exit(0);
		}
		
		Config config = new EnvironmentConfig();
		config.load();
		
		System.out.println("Running db-version-tool. Version: 2.0.0");

		FileParser<DatabaseSchemaChange> fileParser = new SchemaChangeParser();
		DatabaseMigrator migrator;
		if(setup.generateFile()) {
			migrator = new DatabaseMigrationFileGenerator(setup.getDirectory().toPath(), fileParser, setup.getGeneratedFile().toPath());
		}
		else {
			Database db = new ConfiguredDatabase(config);
			migrator = new ConfiguredDatabaseMigrator(setup.getDirectory().toPath(), fileParser, db);
		}
		
		try {
			if(!setup.versionSpecified()) {
				migrator.migrate();
			}
			else {
				migrator.migrate(setup.getVersion());
			}
		}
		finally {
			int newVersion = migrator.getCurrentDatabaseVersion();
			System.out.println("Migrated to database version " + newVersion);
		}
	}

}
