package unittests;

import java.io.InputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import junit.framework.TestCase;
import junit.framework.TestResult;
import mobac.exceptions.AtlasTestException;
import mobac.program.AtlasThread;
import mobac.program.Logging;
import mobac.program.atlascreators.AtlasCreator;
import mobac.program.interfaces.AtlasInterface;
import mobac.program.model.Atlas;
import mobac.program.model.Profile;
import mobac.program.tilestore.TileStore;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * Base {@link TestCase} used for testing a specific {@link AtlasCreator}
 * implementation and/or downloading of a Atlas.
 */
public abstract class AbstractAtlasCreatorTestCase extends TestCase {

	protected Logger log;

	public AbstractAtlasCreatorTestCase() {
		super();
		Logging.configureConsoleLogging(Level.INFO);
		log = Logger.getLogger(this.getClass());
		TileStore.initialize();
	}

	protected void createAtlas(String profileName, Class<? extends AtlasCreator> atlasCreatorClass)
			throws InstantiationException, IllegalAccessException, JAXBException,
			AtlasTestException, InterruptedException {
		AtlasCreator atlasCreator = atlasCreatorClass.newInstance();
		createAtlas(profileName, atlasCreator);
	}

	protected void createAtlas(String profileName, AtlasCreator atlasCreator) throws JAXBException,
			AtlasTestException, InterruptedException {
		String profileFile = "profiles/" + Profile.getProfileFileName(profileName);
		InputStream in = ClassLoader.getSystemResourceAsStream(profileFile);
		assertNotNull(in);
		AtlasInterface atlas = loadAtlas(in);
		createAtlas(atlas, atlasCreator);
	}

	protected AtlasInterface loadAtlas(InputStream in) throws JAXBException {
		JAXBContext context = JAXBContext.newInstance(Atlas.class);
		Unmarshaller um = context.createUnmarshaller();
		return (AtlasInterface) um.unmarshal(in);
	}

	protected void createAtlas(AtlasInterface atlas, AtlasCreator atlasCreator)
			throws AtlasTestException, InterruptedException {
		AtlasThread atlasThread = new AtlasThread(atlas);
		// Settings.getInstance().setAtlasOutputDirectory("atlases/")
		atlasThread.start();
		atlasThread.join();
	}

	@Override
	public TestResult run() {
		TestResult result = super.run();
		TileStore.getInstance().closeAll(true);
		return result;
	}

}
