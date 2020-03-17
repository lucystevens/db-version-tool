package uk.co.lukestevens;

public class DatabaseSchemaChange {
	
	private final int version;
	private final String deploySql;
	private final String rollbackSql;
	
	public DatabaseSchemaChange(int version, String deploySql, String rollbackSql) {
		this.version = version;
		this.deploySql = deploySql;
		this.rollbackSql = rollbackSql;
	}

	public int getVersion() {
		return version;
	}

	public String getDeploySql() {
		return deploySql;
	}

	public String getRollbackSql() {
		return rollbackSql;
	}
}
