package unittests;

import java.io.File;

import mobac.mapsources.DefaultMapSourcesManager;
import mobac.program.atlascreators.NokiaSportsTracker;
import mobac.program.interfaces.AtlasInterface;

public class AtlasDownloadTestCase extends AbstractAtlasCreatorTestCase {

	public AtlasDownloadTestCase() {
		super();
	}

	public void testGoogleEarchOverlay() throws Exception {
		AtlasInterface atlas;
		//atlas = loadAtlas("Germany10-12");
		//atlas = loadAtlas("HamburgPark");
		DefaultMapSourcesManager.initialize();
		atlas = loadAtlas("Munich6-16");
		File dir = createAtlas(atlas, new NokiaSportsTracker());
		assertNotNull(dir);
	}

//	public void testGarminCustom() throws Exception {
//		AtlasInterface atlas = loadAtlas("HamburgPark");
//		File dir = createAtlas(atlas, new GarminCustom());
//		assertNotNull(dir);
//	}

}
