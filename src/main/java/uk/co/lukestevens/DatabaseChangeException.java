package uk.co.lukestevens;

public class DatabaseChangeException extends RuntimeException {

	private static final long serialVersionUID = 5238104280329258323L;

	public DatabaseChangeException(String arg0) {
		super(arg0);
	}

	public DatabaseChangeException(Throwable arg0) {
		super(arg0);
	}


}
