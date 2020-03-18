package uk.co.lukestevens.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import uk.co.lukestevens.DatabaseSchemaChange;
import uk.co.lukestevens.config.Config;
import uk.co.lukestevens.config.ConfigManager;
import uk.co.lukestevens.encryption.EncryptionService;
import uk.co.lukestevens.encryption.IgnoredEncryptionService;
import uk.co.lukestevens.jdbc.result.DatabaseResult;
import uk.co.lukestevens.services.ConfiguredDatabaseMigrator;
import uk.co.lukestevens.test.db.TestDatabase;

public class ConfiguredDatabaseMigratorTest {
	
	static Config config;
	TestDatabase db;
	
	@BeforeAll
	public static void loadConfig() throws IOException {
		EncryptionService encryption = new IgnoredEncryptionService();
		File configFile = new File("src/test/resources/conf/test.conf");
		ConfigManager configManager = new ConfigManager(configFile, encryption);
		config = configManager.getAppConfig();
	}
	
	@BeforeEach
	public void setup() throws SQLException, IOException { 
		db = new TestDatabase();
		db.executeFile("setup");
	}
	
	@Test
	public void testGetLatestChangeVersion() {
		ConfiguredDatabaseMigrator migrator = new ConfiguredDatabaseMigrator(null, null, db);
		this.setChanges(migrator, 
			new DatabaseSchemaChange(1, null, null),
			new DatabaseSchemaChange(4, null, null),
			new DatabaseSchemaChange(6, null, null),
			new DatabaseSchemaChange(3, null, null)
		);
		
		int version = migrator.getLatestChangeVersion();
		assertEquals(6, version);
	}
	
	@Test
	public void testGetLatestChangeVersionNoChanges() {
		ConfiguredDatabaseMigrator migrator = new ConfiguredDatabaseMigrator(null, null, db);
		this.setChanges(migrator);
		
		int version = migrator.getLatestChangeVersion();
		assertEquals(0, version);
	}
	
	@Test
	public void testGetCurrentDatabaseVersionDefault() {
		ConfiguredDatabaseMigrator migrator = new ConfiguredDatabaseMigrator(null, null, db);
		int version = migrator.getCurrentDatabaseVersion();
		assertEquals(0, version);
	}
	
	@Test
	public void testGetCurrentDatabaseVersionChanged() throws SQLException {
		db.update("UPDATE core.version SET version = ?", 73);
		
		ConfiguredDatabaseMigrator migrator = new ConfiguredDatabaseMigrator(null, null, db);
		int version = migrator.getCurrentDatabaseVersion();
		assertEquals(73, version);
	}
	
	@Test
	public void testApplySqlChange() throws SQLException {
		ConfiguredDatabaseMigrator migrator = new ConfiguredDatabaseMigrator(null, null, db);
		DatabaseSchemaChange dsb = new DatabaseSchemaChange(53, "CREATE TABLE test(col varchar);", null);
		migrator.deploy(dsb);
		
		int version = migrator.getCurrentDatabaseVersion();
		assertEquals(53, version);
		
		// Will throw exception if table doesn't exist
		db.query("SELECT * FROM test;");
	}
	
	@Test
	public void testDeploySqlChanges() throws IOException, SQLException {
		ConfiguredDatabaseMigrator migrator = new ConfiguredDatabaseMigrator(null, null, db);
		this.setChanges(migrator, 
			new DatabaseSchemaChange(53, "CREATE TABLE test(col varchar);", "DROP TABLE test"),
			new DatabaseSchemaChange(60, "INSERT INTO test VALUES('value1');", "DELETE FROM test WHERE COL='value1';"),
			new DatabaseSchemaChange(71, "ALTER TABLE test ADD intcol int DEFAULT 13;", "ALTER TABLE test DROP COLUMN intcol;"),
			new DatabaseSchemaChange(77, "INSERT INTO test VALUES('value2', DEFAULT);", "DELETE FROM test WHERE COL='value2';"),
			new DatabaseSchemaChange(80, "UPDATE test SET intcol = 18 WHERE col = 'value1';", "UPDATE test SET intcol = 13 WHERE col = 'value1';")
		);
		
		migrator.deploy(77, 0);
		int version = migrator.getCurrentDatabaseVersion();
		assertEquals(77, version);
		
		try(DatabaseResult dbr = db.query("SELECT * FROM test;")){
			ResultSet rs = dbr.getResultSet();
			rs.next();
			assertEquals("value1", rs.getString("col"));
			assertEquals(13, rs.getInt("intcol"));
			rs.next();
			assertEquals("value2", rs.getString("col"));
			assertEquals(13, rs.getInt("intcol"));
		}
	}
	
	@Test
	public void testRollbackSqlChanges() throws IOException, SQLException {
		db.update(
			"CREATE TABLE test(col varchar);" +
			"INSERT INTO test VALUES('value1');" +
			"ALTER TABLE test ADD intcol int DEFAULT 13;" + 
			"INSERT INTO test VALUES('value2', DEFAULT);" +
			"UPDATE test SET intcol = 18 WHERE col = 'value';"
		);
		db.update("UPDATE core.version SET version = ?", 80);
		
		ConfiguredDatabaseMigrator migrator = new ConfiguredDatabaseMigrator(null, null, db);
		this.setChanges(migrator, 
			new DatabaseSchemaChange(53, "CREATE TABLE test(col varchar);", "DROP TABLE test"),
			new DatabaseSchemaChange(60, "INSERT INTO test VALUES('value1');", "DELETE FROM test WHERE COL='value1';"),
			new DatabaseSchemaChange(71, "ALTER TABLE test ADD intcol int DEFAULT 13;", "ALTER TABLE test DROP COLUMN intcol;"),
			new DatabaseSchemaChange(77, "INSERT INTO test VALUES('value2', DEFAULT);", "DELETE FROM test WHERE COL='value2';"),
			new DatabaseSchemaChange(80, "UPDATE test SET intcol = 18 WHERE col = 'value1';", "UPDATE test SET intcol = 13 WHERE col = 'value1';")
		);
		
		migrator.rollback(60, 80);
		int version = migrator.getCurrentDatabaseVersion();
		assertEquals(70, version);
		
		try(DatabaseResult dbr = db.query("SELECT * FROM test;")){
			ResultSet rs = dbr.getResultSet();
			rs.next();
			assertEquals("value1", rs.getString("col"));
			assertThrows(SQLException.class, () -> rs.getInt("intcol"));
			assertFalse(rs.next());
		}
	}
	
	@Test
	public void testMigrate() throws IOException, SQLException {
		ConfiguredDatabaseMigrator migrator = new ConfiguredDatabaseMigrator(null, null, db);
		
		this.setChanges(migrator, 
			new DatabaseSchemaChange(53, "CREATE TABLE test(col varchar);", "DROP TABLE test"),
			new DatabaseSchemaChange(60, "INSERT INTO test VALUES('value1');", "DELETE FROM test WHERE COL='value1';"),
			new DatabaseSchemaChange(71, "ALTER TABLE test ADD intcol int DEFAULT 13;", "ALTER TABLE test DROP COLUMN intcol;"),
			new DatabaseSchemaChange(77, "INSERT INTO test VALUES('value2', DEFAULT);", "DELETE FROM test WHERE COL='value2';"),
			new DatabaseSchemaChange(80, "UPDATE test SET intcol = 18 WHERE col = 'value1';", "UPDATE test SET intcol = 13 WHERE col = 'value1';")
		);
		
		migrator.migrate();
		int version = migrator.getCurrentDatabaseVersion();
		assertEquals(80, version);
		
		try(DatabaseResult dbr = db.query("SELECT * FROM test;")){
			ResultSet rs = dbr.getResultSet();
			rs.next();
			assertEquals("value1", rs.getString("col"));
			assertEquals(18, rs.getInt("intcol"));
			rs.next();
			assertEquals("value2", rs.getString("col"));
			assertEquals(13, rs.getInt("intcol"));
		}
		
		migrator.migrate(65);
		int version2 = migrator.getCurrentDatabaseVersion();
		assertEquals(70, version2);
		
		try(DatabaseResult dbr = db.query("SELECT * FROM test;")){
			ResultSet rs = dbr.getResultSet();
			rs.next();
			assertEquals("value1", rs.getString("col"));
			assertThrows(SQLException.class, () -> rs.getInt("intcol"));
			assertFalse(rs.next());
		}
	}

	/*
	 * Helper method to set changes on the migrator and avoid using the filesystem
	 */
	void setChanges(ConfiguredDatabaseMigrator migrator, DatabaseSchemaChange...changesArr) {
		List<DatabaseSchemaChange> changes = new ArrayList<>();
		for(DatabaseSchemaChange change : changesArr) {
			changes.add(change);
		}
		
		try {
			Field f = ConfiguredDatabaseMigrator.class.getDeclaredField("changes");
			f.setAccessible(true);
			f.set(migrator, changes);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	

}
