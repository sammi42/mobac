package unittests;

import java.io.File;

import mobac.program.atlascreators.AndNav;
import mobac.program.interfaces.AtlasInterface;

public class AtlasDownloadTestCase extends AbstractAtlasCreatorTestCase {

	public AtlasDownloadTestCase() {
		super();
	}

	public void testGoogleEarchOverlay() throws Exception {
		AtlasInterface atlas = loadAtlas("Germany10-12");
		File dir = createAtlas(atlas, new AndNav());
		assertNotNull(dir);
	}

//	public void testGarminCustom() throws Exception {
//		AtlasInterface atlas = loadAtlas("HamburgPark");
//		File dir = createAtlas(atlas, new GarminCustom());
//		assertNotNull(dir);
//	}

}
