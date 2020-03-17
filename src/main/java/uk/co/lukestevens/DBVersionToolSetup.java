package uk.co.lukestevens;

import java.io.File;

import uk.co.lukestevens.cli.CommandLineOption;
import uk.co.lukestevens.cli.CommandLineUsage;
import uk.co.lukestevens.cli.setup.KeyBasedSetup;

@CommandLineUsage("java -jar DBVersionTool.jar")
public class DBVersionToolSetup extends KeyBasedSetup {
	
	@CommandLineOption(opt = "v", longOpt = "version", description = "The version to migrate this database to. Defaults to the latest.", optional = true)
	private Integer version;
	
	@CommandLineOption(opt = "d", longOpt = "dir", description = "The directory containing the sql scripts to execute.", optional = false)
	private File directory;
	
	public boolean versionSpecified() {
		return version != null;
	}

	public Integer getVersion() {
		return version;
	}

	public File getDirectory() {
		return directory;
	}

}
