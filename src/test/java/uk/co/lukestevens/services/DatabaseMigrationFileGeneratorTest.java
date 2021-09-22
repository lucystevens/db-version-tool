package uk.co.lukestevens.services;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import uk.co.lukestevens.DatabaseSchemaChange;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DatabaseMigrationFileGeneratorTest {

    Path outputFile = Paths.get("generatedTestOutput.sql");

    @AfterEach
    public void setup() throws IOException {
        Files.deleteIfExists(outputFile);
    }
    
    @Test
    public void testGetCurrentDatabaseVersion() {
        DatabaseMigrationFileGenerator migrator = new DatabaseMigrationFileGenerator(null, null, outputFile);
        int version = migrator.getCurrentDatabaseVersion();
        assertEquals(0, version);
    }

    @Test
    public void testMigrate_withSingleSqlChange() throws SQLException, IOException {
        DatabaseMigrationFileGenerator migrator = new DatabaseMigrationFileGenerator(null, null, outputFile);
        this.setChanges(migrator,
                new DatabaseSchemaChange(53, "CREATE TABLE test(col varchar);", "DROP TABLE test")
        );

        migrator.migrate(53);

        int version = migrator.getCurrentDatabaseVersion();
        assertEquals(53, version);

        assertOutputEquals(Paths.get("src/test/resources/expected-sql/testMigrate_withSingleSqlChange.sql"));
    }

    @Test
    public void testMigrate_withMultipleSqlChanges() throws IOException, SQLException {
        DatabaseMigrationFileGenerator migrator = new DatabaseMigrationFileGenerator(null, null, outputFile);
        this.setChanges(migrator,
                new DatabaseSchemaChange(53, "CREATE TABLE test(col varchar);", "DROP TABLE test"),
                new DatabaseSchemaChange(60, "INSERT INTO test VALUES('value1');", "DELETE FROM test WHERE COL='value1';"),
                new DatabaseSchemaChange(71, "ALTER TABLE test ADD intcol int DEFAULT 13;", "ALTER TABLE test DROP COLUMN intcol;"),
                new DatabaseSchemaChange(77, "INSERT INTO test VALUES('value2', DEFAULT);", "DELETE FROM test WHERE COL='value2';"),
                new DatabaseSchemaChange(80, "UPDATE test SET intcol = 18 WHERE col = 'value1';", "UPDATE test SET intcol = 13 WHERE col = 'value1';")
        );

        migrator.migrate(77);
        int version = migrator.getCurrentDatabaseVersion();
        assertEquals(77, version);

        assertOutputEquals(Paths.get("src/test/resources/expected-sql/testMigrate_withMultipleSqlChanges.sql"));
    }

    /*
     * Helper method to set changes on the migrator and avoid using the filesystem
     */
    void setChanges(DatabaseMigrationFileGenerator migrator, DatabaseSchemaChange...changesArr) {
        migrator.changes = new ArrayList<>();
        Collections.addAll(migrator.changes, changesArr);
    }

    void assertOutputEquals(Path expectedFile) throws IOException {
        String actual = Files.readString(outputFile);
        String expected = Files.readString(expectedFile);
        assertEquals(expected, actual);
    }

}
