package mobac.program.atlascreators;


/**
 * Creates maps using the AndNav atlas format.
 * 
 * Please note that this atlas format ignores the defined atlas structure. It
 * uses a separate directory for each used map source and inside one directory
 * for each zoom level.
 */
public class AndNav extends OSMTracker {

	public AndNav() {
		super();
		tileFileNamePattern += ".andnav";
	}

}