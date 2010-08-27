package mobac.program.atlascreators;

/**
 * Marker-Interface which has to implemented by all atlas creator classes which require the SQLite libraries.
 */
public interface RequiresSQLite {

	/**
	 * Accumulate tiles in batch process until 20MB of heap are remaining
	 */
	public static final long HEAP_MIN = 20 * 1024 * 1024;
}
