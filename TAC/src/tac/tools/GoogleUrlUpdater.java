package tac.tools;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.tidy.Tidy;

import tac.mapsources.MapSourcesManager;
import tac.program.Logging;
import tac.utilities.Utilities;

public class GoogleUrlUpdater {

	/**
	 * <p>
	 * Recalculates the tile urls for Google Maps, Earth and Terrain and prints
	 * it to std out. Other map sources can not be updated this way.
	 * </p>
	 * 
	 * Requires <a href="http://jtidy.sourceforge.net/">JTidy</a> library.
	 */
	public static void main(String[] args) {
		Logging.disableLogging();
		MapSourcesManager.getAllMapSources(); // just for initializing the
		// MapSourcesManager
		GoogleUrlUpdater g = new GoogleUrlUpdater();
		g.testMapSource("GoogleMaps.url");
		g.testMapSource("GoogleEarth.url");
		g.testMapSource("GoogleTerrain.url");
		g.testGoogleMapMaker();
		System.out.println("Updated map sources: " + g.updatedMapSources);
	}

	private Properties mapSourceUrls = new Properties();

	protected int updatedMapSources = 0;

	public GoogleUrlUpdater() {
		mapSourceUrls.put("GoogleMaps.url", "http://maps.google.com/?ie=UTF8&ll=0,0&spn=0,0&z=2");
		mapSourceUrls.put("GoogleEarth.url",
				"http://maps.google.com/?ie=UTF8&t=k&ll=0,0&spn=0,0&z=2");
		mapSourceUrls.put("GoogleTerrain.url",
				"http://maps.google.com/?ie=UTF8&t=p&ll=0,0&spn=0,0&z=2");
	}

	public void testMapSource(String key) {
		String url = mapSourceUrls.getProperty(key);
		String oldUrlTemplate = System.getProperty(key);
		String newUrlTemplate = getUpdatedUrl(url, true);
		if (!oldUrlTemplate.equals(newUrlTemplate)) {
			System.out.println(key + "=" + newUrlTemplate);
			updatedMapSources++;
		}
	}

	public List<String> extractImgSrcList(String url) throws IOException, XPathExpressionException {
		LinkedList<String> list = new LinkedList<String>();
		URL u = new URL(url);
		// Proxy p = new Proxy(Type.HTTP,new
		// InetSocketAddress("localhost",8888));
		// System.setProperty("java.net.useSystemProxies", "true");
		HttpURLConnection conn = (HttpURLConnection) u.openConnection();

		Tidy tidy = new Tidy();
		tidy.setErrout(new NullPrintWriter());
		Document doc = tidy.parseDOM(conn.getInputStream(), null);

		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();
		XPathExpression expr = xpath.compile("//img[@src]");
		Object result = expr.evaluate(doc, XPathConstants.NODESET);
		NodeList nodes = (NodeList) result;
		for (int i = 0; i < nodes.getLength(); i++) {
			String imgUrl = nodes.item(i).getAttributes().getNamedItem("src").getNodeValue();
			if (imgUrl != null && imgUrl.length() > 0)
				list.add(imgUrl);
		}
		return list;
	}

	public List<String> extractUrlList(String url) throws IOException, XPathExpressionException {
		LinkedList<String> list = new LinkedList<String>();
		HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();

		Tidy tidy = new Tidy();
		tidy.setErrout(new NullPrintWriter());
		Document doc = tidy.parseDOM(conn.getInputStream(), null);

		int len = conn.getContentLength();
		if (len <= 0)
			len = 32000;
		ByteArrayOutputStream bout = new ByteArrayOutputStream(len);

		PrintStream ps = new PrintStream(bout);
		tidy.pprint(doc, ps);
		ps.flush();
		String content = bout.toString();

		Pattern p = Pattern.compile("(http://[\\w\\\\\\./=&?;-]+)");
		Matcher m = p.matcher(content);
		while (m.find()) {
			list.add(m.group());
		}

		return list;
	}

	public String getUpdatedUrl(String serviceUrl, boolean useImgSrcUrlsOnly) {

		try {
			List<String> urls;
			if (useImgSrcUrlsOnly)
				urls = extractImgSrcList(serviceUrl);
			else
				urls = extractUrlList(serviceUrl);

			// System.out.println(urls.size());

			HashSet<String> tileUrlCandidates = new HashSet<String>();

			for (String imgUrl : urls) {
				try {
					// filter out images with relative path
					if (imgUrl.toLowerCase().startsWith("http://")) {
						imgUrl = imgUrl.replaceAll("\\\\x26", "&");
						imgUrl = imgUrl.replaceAll("\\\\x3d", "=");

						// System.out.println(imgUrl);

						URL tileUrl = new URL(imgUrl);

						String host = tileUrl.getHost();
						host = host.replaceFirst("[0-3]", "{\\$servernum}");

						String path = tileUrl.getPath();
						path = path.replaceFirst("x=\\d+", "x={\\$x}");
						path = path.replaceFirst("y=\\d+", "y={\\$y}");
						path = path.replaceFirst("z=\\d+", "z={\\$z}");
						path = path.replaceFirst("hl=[^&]+", "hl={\\$lang}");
						path = path.replaceFirst("&s=\\w*", "");

						if (path.equalsIgnoreCase(tileUrl.getPath()))
							continue; // Nothing was replaced
						// remove Galileo
						String candidate = "http://" + host + path;
						tileUrlCandidates.add(candidate);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
			// System.out.println("Number of possible URLs found: " +
			// tileUrlCandidates.size());
			String c1 = null;
			for (String c : tileUrlCandidates) {
				// System.out.println(c);
				c1 = c;
			}
			return c1;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private void testGoogleMapMaker() {
		try {
			HttpURLConnection c = (HttpURLConnection) new URL("http://www.google.com/mapmaker")
					.openConnection();
			InputStream in = c.getInputStream();
			String html = new String(Utilities.getInputBytes(in));
			in.close();
			Pattern p = Pattern.compile("\\\"gwm.([\\d]+)\\\"");
			Matcher m = p.matcher(html);
			if (!m.find())
				throw new RuntimeException("pattern not found");
			String number = m.group(1);
			String url = "http://gt{$servernum}.google.com/mt/n=404&v=gwm." + number
					+ "&x={$x}&y={$y}&z={$z}";
			if (!url.equals(System.getProperty("GoogleMapMaker.url"))) {
				updatedMapSources++;
				System.out.println("GoogleMapMaker.url=" + url);
			}
			c.disconnect();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static class NullPrintWriter extends PrintWriter {

		public NullPrintWriter() throws FileNotFoundException {
			super(new Writer() {

				@Override
				public void close() throws IOException {
				}

				@Override
				public void flush() throws IOException {
				}

				@Override
				public void write(char[] cbuf, int off, int len) throws IOException {
				}
			});
		}
	}
}
