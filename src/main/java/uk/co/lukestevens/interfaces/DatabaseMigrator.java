package uk.co.lukestevens.interfaces;

public interface DatabaseMigrator {
	
	public void migrate(int version);
	
	public void migrate();
	
	public int getCurrentDatabaseVersion();

}
