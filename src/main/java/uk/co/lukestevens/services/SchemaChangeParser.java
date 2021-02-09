package uk.co.lukestevens.services;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import uk.co.lukestevens.DatabaseChangeException;
import uk.co.lukestevens.DatabaseSchemaChange;
import uk.co.lukestevens.interfaces.FileParser;

public class SchemaChangeParser implements FileParser<DatabaseSchemaChange> {

	@Override
	public DatabaseSchemaChange parse(File file){
		int version = this.getVersion(file.getName());
		
		List<String> lines = this.getLines(file);
		String deploySql = null, rollbackSql = null;
		StringBuilder buffer = new StringBuilder();
		
		for(String line : lines) {
			if(line.matches("\\/\\/ *-* *ROLLBACK *-* *")) {
				deploySql = buffer.toString();
				buffer = new StringBuilder();
			}
			else if(!line.startsWith("//")){
				buffer.append(line.trim());
			}
		}
		rollbackSql = buffer.toString();
		
		if(deploySql == null) {
			throw new DatabaseChangeException("Rollback script missing for version change " + version);
		}
		else if (deploySql.trim().isEmpty()) {
			throw new DatabaseChangeException("Deploy script missing for version change " + version);
		}
		
		return new DatabaseSchemaChange(version, deploySql, rollbackSql);
	}
	
	protected List<String> getLines(File file){
		try {
			return Files.readAllLines(file.toPath());
		} catch (IOException e) {
			throw new DatabaseChangeException(e);
		}
	}
	
	int getVersion(String filename){
		String[] nameparts = filename.split("_");
		if(nameparts.length == 0) {
			throw new DatabaseChangeException(filename + " is not a valid schema version name");
		}
		
		if(!nameparts[0].matches("\\d+")) {
			throw new DatabaseChangeException(filename + " is not a valid schema version name. It must start with the version number.");
		}
		
		return Integer.parseInt(nameparts[0]);
	}
	
	

}
