package uk.co.lukestevens.services;

import java.util.Comparator;

import uk.co.lukestevens.DatabaseSchemaChange;

public class SchemeChangeComparators {

	public static final Comparator<DatabaseSchemaChange> VERSION_ASC = (dsb1, dsb2) ->  dsb1.getVersion() - dsb2.getVersion();
	public static final Comparator<DatabaseSchemaChange> VERSION_DESC = (dsb1, dsb2) ->  dsb2.getVersion() - dsb1.getVersion();

}
