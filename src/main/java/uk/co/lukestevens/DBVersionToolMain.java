package uk.co.lukestevens;

import java.io.IOException;
import org.apache.commons.cli.ParseException;

import uk.co.lukestevens.cli.CLIParser;
import uk.co.lukestevens.config.Config;
import uk.co.lukestevens.config.ConfigManager;
import uk.co.lukestevens.encryption.AESEncryptionService;
import uk.co.lukestevens.encryption.EncryptionService;
import uk.co.lukestevens.interfaces.DatabaseMigrator;
import uk.co.lukestevens.interfaces.FileParser;
import uk.co.lukestevens.jdbc.ConfiguredDatabase;
import uk.co.lukestevens.jdbc.Database;
import uk.co.lukestevens.services.ConfiguredDatabaseMigrator;
import uk.co.lukestevens.services.SchemaChangeParser;

public class DBVersionToolMain {
	
	public static void main(String[] args) throws ParseException, IOException {
		CLIParser parser = new CLIParser();
		DBVersionToolSetup setup = parser.parseCommandLine(args, DBVersionToolSetup.class);
		if(setup == null) {
			System.exit(0);
		}
		
		EncryptionService encryption = new AESEncryptionService(setup.getKey());
		ConfigManager configManager = new ConfigManager(setup.getConfigFile(), encryption);
		Config config = configManager.getAppConfig();
		
		System.out.println("Running " + config.getApplicationName() + ". Version: " + config.getApplicationVersion());

		String alias = config.getAsStringOrDefault("migration.db.alias", "migration");
		Database db = new ConfiguredDatabase(config, alias);
		FileParser<DatabaseSchemaChange> fileParser = new SchemaChangeParser();
		
		DatabaseMigrator migrator = new ConfiguredDatabaseMigrator(setup.getDirectory().toPath(), fileParser, db);
		
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
