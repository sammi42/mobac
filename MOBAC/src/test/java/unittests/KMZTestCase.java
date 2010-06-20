package unittests;

import java.io.File;

import mobac.program.atlascreators.GarminCustom;
import mobac.program.atlascreators.GoogleEarthOverlay;
import mobac.program.interfaces.AtlasInterface;

public class KMZTestCase extends AbstractAtlasCreatorTestCase {

	public KMZTestCase() {
		super();
	}

	public void testGoogleEarchOverlay() throws Exception {
		AtlasInterface atlas = loadAtlas("HamburgPark");
		File dir = createAtlas(atlas, new GoogleEarthOverlay());
		assertNotNull(dir);
	}

//	public void testGarminCustom() throws Exception {
//		AtlasInterface atlas = loadAtlas("HamburgPark");
//		File dir = createAtlas(atlas, new GarminCustom());
//		assertNotNull(dir);
//	}

}
