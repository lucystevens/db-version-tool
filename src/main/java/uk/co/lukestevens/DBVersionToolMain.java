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
		
		EncryptionService encryption = new AESEncryptionService(setup.getKey());
		ConfigManager configManager = new ConfigManager(setup.getConfigFile(), encryption);
		Config config = configManager.getAppConfig();

		Database db = new ConfiguredDatabase(config, "migrate");
		FileParser<DatabaseSchemaChange> fileParser = new SchemaChangeParser();
		
		DatabaseMigrator migrator = new ConfiguredDatabaseMigrator(setup.getDirectory().toPath(), fileParser, db);
		
		try {
			if(setup.versionSpecified()) {
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
