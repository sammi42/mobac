package tac.program.model;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Vector;

import javax.swing.JOptionPane;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessOrder;
import javax.xml.bind.annotation.XmlAccessorOrder;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.log4j.Logger;
import org.openstreetmap.gui.jmapviewer.OsmTileLoader;

import tac.gui.actions.GpxLoad;
import tac.gui.mapview.ScaleBar;
import tac.gui.panels.JCoordinatesPanel;
import tac.mapsources.CustomMapSource;
import tac.mapsources.MapSourcesManager;
import tac.mapsources.impl.Google;
import tac.program.UserAgent;
import tac.utilities.Utilities;

@XmlRootElement
@XmlAccessorOrder(XmlAccessOrder.ALPHABETICAL)
public class Settings {

	private static Logger log = Logger.getLogger(Settings.class);

	public static final File FILE = new File(getUserDir(), "settings.xml");

	private static Settings instance;

	private static final String SYSTEM_PROXY_HOST = System.getProperty("http.proxyHost");
	private static final String SYSTEM_PROXY_PORT = System.getProperty("http.proxyPort");

	public int maxMapSize = 32767;

	private boolean tileStoreEnabled = true;

	/**
	 * Mapview related settings
	 */
	public int mapviewZoom = 3;
	public int mapviewGridZoom = -1;
	public EastNorthCoordinate mapviewCenterCoordinate = new EastNorthCoordinate(50, 9);

	public Point mapviewSelectionMax = null;
	public Point mapviewSelectionMin = null;

	@XmlElement(nillable = false)
	public String mapviewMapSource = MapSourcesManager.DEFAULT.getName();

	private String elementName = "Layer name";

	private String userAgent = UserAgent.FF3_XP;

	private int downloadThreadCount = 4;

	private boolean customTileProcessing = false;
	private Dimension tileSize = new Dimension(256, 256);
	private TileImageFormat tileImageFormat = TileImageFormat.PNG;
	private AtlasOutputFormat atlasOutputFormat = AtlasOutputFormat.TaredAtlas;

	public String atlasOutputDirectory;
	public String tileStoreDirectory;

	/**
	 * Timeout in seconds (default 10 seconds)
	 */
	private int connectionTimeout = 10;

	private String googleLanguage = "en";

	/**
	 * Development mode enabled/disabled
	 * <p>
	 * In development mode one additional map source is available for using TAC
	 * Debug TileServer
	 * </p>
	 */
	@XmlElement
	private boolean devMode = false;

	/**
	 * Saves the last used directory of the GPX file chooser dialog. Used in
	 * {@link GpxLoad}.
	 */
	public String gpxFileChooserDir = "";

	public final MainWindowSettings mainWindow = new MainWindowSettings();

	public static class MainWindowSettings {
		public Dimension size = new Dimension();
		public Point position = new Point(-1, -1);
		public Boolean maximized = true;

		@XmlElementWrapper(name = "collapsedPanels")
		@XmlElement(name = "collapsedPanel")
		public Vector<String> collapsedPanels = new Vector<String>();
	}

	/**
	 * Network settings
	 */
	private ProxyType proxyType = ProxyType.SYSTEM;
	private String customProxyHost = "";
	private String customProxyPort = "";

	private Vector<String> disabledMapSources = new Vector<String>();

	@XmlElement(name = "customMapSource")
	public Vector<CustomMapSource> customMapSources = new Vector<CustomMapSource>();

	@XmlElement(name = "MapSourcesUpdate")
	public final MapSourcesUpdate mapSourcesUpdate = new MapSourcesUpdate();

	public static class MapSourcesUpdate {
		/**
		 * Last ETag value retrieved while online map source update.
		 * 
		 * @see MapSourcesManager#mapsourcesOnlineUpdate()
		 * @see http://en.wikipedia.org/wiki/HTTP_ETag
		 */
		public String etag;

		public Date lastUpdate;
	}

	private Settings() {
		Dimension dScreen = Toolkit.getDefaultToolkit().getScreenSize();
		mainWindow.size.width = (int) (0.9f * dScreen.width);
		mainWindow.size.height = (int) (0.9f * dScreen.height);
		mainWindow.collapsedPanels.add(JCoordinatesPanel.NAME);
		mainWindow.collapsedPanels.add("Gpx");
	}

	public static Settings getInstance() {
		if (instance != null)
			return instance;
		synchronized (Settings.class) {
			if (instance == null) {
				instance = new Settings();
			}
			return instance;
		}
	}

	public static void load() throws JAXBException {
		try {
			JAXBContext context = JAXBContext.newInstance(Settings.class);
			Unmarshaller um = context.createUnmarshaller();
			instance = (Settings) um.unmarshal(FILE);
		} finally {
			Settings s = getInstance();
			s.applyProxySettings();
		}
	}

	public static void save() throws JAXBException {
		JAXBContext context = JAXBContext.newInstance(Settings.class);
		Marshaller m = context.createMarshaller();
		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		ByteArrayOutputStream bo = null;
		FileOutputStream fo = null;
		try {
			// First we write to a buffer and if that works be write the buffer
			// to disk. Direct writing to file may result in an defect xml file
			// in case of an error
			bo = new ByteArrayOutputStream();
			m.marshal(getInstance(), bo);
			fo = new FileOutputStream(FILE);
			fo.write(bo.toByteArray());
		} catch (IOException e) {
			throw new JAXBException(e);
		} finally {
			Utilities.closeStream(fo);
		}
	}

	public static String getUserDir() {
		return System.getProperty("user.dir");
	}

	public static void loadOrQuit() {
		try {
			load();
		} catch (JAXBException e) {
			log.error(e);
			JOptionPane.showMessageDialog(null,
					"Could not read file settings.xml program will exit.", "Error",
					JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}
	}

	public boolean isTileStoreEnabled() {
		return tileStoreEnabled;
	}

	public void setTileStoreEnabled(boolean tileStoreEnabled) {
		this.tileStoreEnabled = tileStoreEnabled;
	}

	public String getElemntName() {
		return elementName;
	}

	public void setElementName(String name) {
		this.elementName = name;
	}

	public int getDownloadThreadCount() {
		return downloadThreadCount;
	}

	public void setDownloadThreadCount(int threadCount) {
		this.downloadThreadCount = threadCount;
	}

	public String getUserAgent() {
		return userAgent;
	}

	public void setUserAgent(String userAgent) {
		OsmTileLoader.USER_AGENT = userAgent;
		this.userAgent = userAgent;
	}

	public String getGoogleLanguage() {
		return googleLanguage;
	}

	public void setGoogleLanguage(String googleLanguage) {
		this.googleLanguage = googleLanguage;
		Google.LANG = googleLanguage;
	}

	public int getConnectionTimeout() {
		return connectionTimeout;
	}

	public void setConnectionTimeout(int connectionTimeout) {
		this.connectionTimeout = connectionTimeout;
	}

	public boolean isCustomTileSize() {
		return customTileProcessing;
	}

	public void setCustomTileSize(boolean customTileSize) {
		this.customTileProcessing = customTileSize;
	}

	public Dimension getTileSize() {
		return tileSize;
	}

	public void setTileSize(Dimension tileSize) {
		this.tileSize = tileSize;
	}

	public boolean isDevModeEnabled() {
		return devMode;
	}

	public TileImageFormat getTileImageFormat() {
		return tileImageFormat;
	}

	public void setTileImageFormat(TileImageFormat tileImageFormat) {
		this.tileImageFormat = tileImageFormat;
	}

	public AtlasOutputFormat getAtlasOutputFormat() {
		return atlasOutputFormat;
	}

	public void setAtlasOutputFormat(AtlasOutputFormat atlasOutputFormat) {
		this.atlasOutputFormat = atlasOutputFormat;
	}

	public ProxyType getProxyType() {
		return proxyType;
	}

	public void setProxyType(ProxyType proxyType) {
		this.proxyType = proxyType;
	}

	public String getCustomProxyHost() {
		return customProxyHost;
	}

	public String getCustomProxyPort() {
		return customProxyPort;
	}

	public void setCustomProxyHost(String proxyHost) {
		this.customProxyHost = proxyHost;
	}

	public void setCustomProxyPort(String proxyPort) {
		this.customProxyPort = proxyPort;
	}

	public Vector<String> getDisabledMapSources() {
		return disabledMapSources;
	}

	@XmlElementWrapper(name = "disabledMapSources")
	@XmlElement(name = "mapSource")
	public void setDisabledMapSources(Vector<String> disabledMapSources) {
		this.disabledMapSources = disabledMapSources;
	}

	public void applyProxySettings() {
		String newProxyHost = null;
		String newProxyPort = null;
		switch (proxyType) {
		case SYSTEM:
			System.setProperty("java.net.useSystemProxies", "true");
			log.info("Proxy configuration applied: system settings");
			return;
		case APP_SETTINGS:
			newProxyHost = SYSTEM_PROXY_HOST;
			newProxyPort = SYSTEM_PROXY_PORT;
			break;
		case CUSTOM:
			newProxyHost = customProxyHost;
			newProxyPort = customProxyPort;
		}
		Utilities.setHttpProxyHost(newProxyHost);
		Utilities.setHttpProxyPort(newProxyPort);
		System.setProperty("java.net.useSystemProxies", "false");
		log.info("Proxy configuration applied: host=" + newProxyHost + " port=" + newProxyPort);
	}

	@XmlElement
	public void setUnitSystem(UnitSystem unitSystem) {
		if (unitSystem == null)
			unitSystem = UnitSystem.Metric;
		ScaleBar.unitSystem = unitSystem;
	}

	public UnitSystem getUnitSystem() {
		return ScaleBar.unitSystem;
	}
}