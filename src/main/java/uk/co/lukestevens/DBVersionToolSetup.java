package uk.co.lukestevens;

import java.io.File;

import uk.co.lukestevens.cli.CommandLineOption;
import uk.co.lukestevens.cli.CommandLineUsage;

@CommandLineUsage("java -jar DBVersionTool.jar")
public class DBVersionToolSetup {
	
	@CommandLineOption(opt = "v", longOpt = "version", description = "The version to migrate this database to. Defaults to the latest.", optional = true)
	private Integer version;
	
	@CommandLineOption(opt = "d", longOpt = "dir", description = "The directory containing the sql scripts to execute.", optional = false)
	private File directory;
	
	@CommandLineOption(opt = "f", longOpt = "generate-file", description = "Filepath to create a file at, instead of directly updating the database", optional = true)
	private File generatedFile;
	
	public boolean versionSpecified() {
		return version != null;
	}

	public Integer getVersion() {
		return version;
	}

	public File getDirectory() {
		return directory;
	}

	public boolean generateFile() {
		return generatedFile != null;
	}
	
	public File getGeneratedFile() {
		return generatedFile;
	}
	
}
