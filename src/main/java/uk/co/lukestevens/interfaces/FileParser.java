package uk.co.lukestevens.interfaces;

import java.io.File;

public interface FileParser<T> {
	
	T parse(File file);

}
