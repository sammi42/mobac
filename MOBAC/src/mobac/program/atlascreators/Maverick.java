package mobac.program.atlascreators;

/**
 * Creates maps using the <a
 * href="http://www.codesector.com/maverick.php">Maverick</a> atlas format
 * (Android application).
 */
public class Maverick extends OSMTracker {

	public Maverick() {
		super();
		tileFileNamePattern += ".tile";
	}

}
