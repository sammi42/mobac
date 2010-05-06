package unittests;

import mobac.program.atlascreators.BigPlanetSql;
import mobac.program.atlascreators.SQLitexGPS;

public class SQLiteTestCase extends AbstractAtlasCreatorTestCase {

	public SQLiteTestCase() {
		super();
	}

	public void testXGPS() throws Exception {
		log.info("Starting test testXGPS");
		createAtlas("HamburgPark", SQLitexGPS.class);
	}

	public void testRMaps() throws Exception {
		log.info("Starting test testRMaps");
		createAtlas("HamburgPark", BigPlanetSql.class);
	}

	
}
