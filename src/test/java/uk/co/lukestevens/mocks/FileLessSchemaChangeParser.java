package uk.co.lukestevens.mocks;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import uk.co.lukestevens.services.SchemaChangeParser;

public class FileLessSchemaChangeParser extends SchemaChangeParser {

	List<String> lines = new ArrayList<>();

	public void setLines(List<String> lines) {
		this.lines = lines;
	}
	
	public void setLines(String...lines) {
		this.lines = new ArrayList<>();
		for(String line : lines) {
			this.lines.add(line);
		}
	}
	
	@Override
	protected List<String> getLines(File file){
		return lines;
	}

}
