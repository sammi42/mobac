package SQLite;

/**
 * Class for SQLite related exceptions.
 */

public class SQLiteException extends java.lang.Exception {

	/**
	 * Construct a new SQLite exception.
	 * 
	 * @param string
	 *            error message
	 */
	public SQLiteException(String string) {
		super(string);
	}

	public SQLiteException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public SQLiteException(Throwable arg0) {
		super(arg0);
	}
	
	
}
