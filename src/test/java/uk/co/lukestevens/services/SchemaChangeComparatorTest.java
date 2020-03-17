package uk.co.lukestevens.services;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import uk.co.lukestevens.DatabaseSchemaChange;
import uk.co.lukestevens.services.SchemeChangeComparators;

public class SchemaChangeComparatorTest {

	@Test
	public void testSortDatabaseSchemaChangesAscending() {
		List<DatabaseSchemaChange> changes = new ArrayList<>();
		changes.add(new DatabaseSchemaChange(1, null, null));
		changes.add(new DatabaseSchemaChange(4, null, null));
		changes.add(new DatabaseSchemaChange(6, null, null));
		changes.add(new DatabaseSchemaChange(3, null, null));

		
		changes.sort(SchemeChangeComparators.VERSION_ASC);
		
		assertEquals(1, changes.get(0).getVersion());
		assertEquals(3, changes.get(1).getVersion());
		assertEquals(4, changes.get(2).getVersion());
		assertEquals(6, changes.get(3).getVersion());
	}
	
	@Test
	public void testSortDatabaseSchemaChangesDescending() {
		List<DatabaseSchemaChange> changes = new ArrayList<>();
		changes.add(new DatabaseSchemaChange(1, null, null));
		changes.add(new DatabaseSchemaChange(4, null, null));
		changes.add(new DatabaseSchemaChange(6, null, null));
		changes.add(new DatabaseSchemaChange(3, null, null));

		
		changes.sort(SchemeChangeComparators.VERSION_DESC);
		
		assertEquals(6, changes.get(0).getVersion());
		assertEquals(4, changes.get(1).getVersion());
		assertEquals(3, changes.get(2).getVersion());
		assertEquals(1, changes.get(3).getVersion());
	}

}
