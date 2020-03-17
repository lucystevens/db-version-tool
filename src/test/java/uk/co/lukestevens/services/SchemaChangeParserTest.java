package uk.co.lukestevens.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;

import org.junit.jupiter.api.Test;

import uk.co.lukestevens.DatabaseChangeException;
import uk.co.lukestevens.DatabaseSchemaChange;
import uk.co.lukestevens.mocks.FileLessSchemaChangeParser;
import uk.co.lukestevens.services.SchemaChangeParser;

public class SchemaChangeParserTest {

	public SchemaChangeParserTest() {
		// TODO Auto-generated constructor stub
	}

	@Test
	public void testGetVersionValid() {
		SchemaChangeParser parser = new SchemaChangeParser();
		int version = parser.getVersion("001_INSERT_SOME_COLUMN.sql");
		assertEquals(1, version);
		
		int version2 = parser.getVersion("22_ALTER_SOME_TABLE.sql");
		assertEquals(22, version2);
	}
	
	@Test
	public void testGetVersionNoUnderscore() {
		SchemaChangeParser parser = new SchemaChangeParser();
		assertThrows(
			DatabaseChangeException.class,
			() -> parser.getVersion("001INSERTSOMECOLUMN.sql"),
			"001INSERTSOMECOLUMN.sql is not a valid schema version name"
		);
	}
	
	@Test
	public void testGetVersionNoNumber() {
		SchemaChangeParser parser = new SchemaChangeParser();
		assertThrows(
			DatabaseChangeException.class,
			() -> parser.getVersion("INSERT_SOME_COLUMN.sql"),
			"INSERT_SOME_COLUMN.sql is not a valid schema version name. It must start with the version number."
		);
	}
	
	@Test
	public void testParseValidFile() {
		FileLessSchemaChangeParser parser = new FileLessSchemaChangeParser();
		parser.setLines(
			"// This creates a sample table",
			"CREATE table (",
			"id INTEGER PRIMARY KEY,",
			"value VARCHAR",
			");",
			"",
			"// ---- ROLLBACK ----- ",
			"DROP TABLE IF EXISTS table;"
		);
		
		DatabaseSchemaChange dsb = parser.parse(new File("001_CREATE_TABLE.sql"));
		assertEquals(1, dsb.getVersion());
		assertEquals("CREATE table ( id INTEGER PRIMARY KEY, value VARCHAR );  ", dsb.getDeploySql());
		assertEquals("DROP TABLE IF EXISTS table; ", dsb.getRollbackSql());
	}
	
	@Test
	public void testParseFileWithoutDeploy() {
		FileLessSchemaChangeParser parser = new FileLessSchemaChangeParser();
		parser.setLines(
			"// ---- ROLLBACK ----- ",
			"DROP TABLE IF EXISTS table;"
		);
		
		assertThrows(
			DatabaseChangeException.class,
			() -> parser.parse(new File("002_CREATE_TABLE.sql")),
			"Deploy script missing for version change 2"
		);
	}
	
	@Test
	public void testParseFileWithoutRollback() {
		FileLessSchemaChangeParser parser = new FileLessSchemaChangeParser();
		parser.setLines(
			"// This creates a sample table",
			"CREATE table (",
			"id INTEGER PRIMARY KEY,",
			"value VARCHAR",
			");",
			""
		);
		
		assertThrows(
			DatabaseChangeException.class,
			() -> parser.parse(new File("003_CREATE_TABLE.sql")),
			"Rollback script missing for version change 3"
		);
	}
	
	@Test
	public void testParseEmptyFile() {
		FileLessSchemaChangeParser parser = new FileLessSchemaChangeParser();

		
		assertThrows(
			DatabaseChangeException.class,
			() -> parser.parse(new File("004_CREATE_TABLE.sql")),
			"Rollback script missing for version change 4"
		);
	}
}
