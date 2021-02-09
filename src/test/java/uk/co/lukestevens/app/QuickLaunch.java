package uk.co.lukestevens.app;

import java.io.IOException;

import org.apache.commons.cli.ParseException;

import uk.co.lukestevens.DBVersionToolMain;
import uk.co.lukestevens.testing.mocks.EnvironmentVariableMocker;

public class QuickLaunch {

	public static void main(String[] args) throws ParseException, IOException {
		generateVersionFile();
	}
	
	static void generateVersionFile() throws ParseException, IOException {
		DBVersionToolMain.main(new String[] {
				"-d", "C:\\dev\\personal_dev\\db-schemas",
				"--generate-file", "update.sql"
		});
	}
	
	static void migrateLocalDatabase() throws ParseException, IOException {
		EnvironmentVariableMocker.build()
			.with("database.url", "jdbc:postgresql://localhost:5432/dev")
			.with("database.username", "stevensl")
			.with("database.password", "vM4xWLsYGCtBKF7Pqhi2")
			.mock();
	
		// run server
		DBVersionToolMain.main(new String[] {"-d", "C:\\dev\\personal_dev\\db-schemas"});
	}

}
