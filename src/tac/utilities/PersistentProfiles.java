package tac.utilities;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Vector;

import javax.swing.JOptionPane;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import tac.program.model.Profile;

public class PersistentProfiles {

	private static Logger log = Logger.getLogger(PersistentProfiles.class);

	public static Vector<Profile> load(File profiles) {
		Vector<Profile> profilesVector = new Vector<Profile>();

		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(profiles);
			doc.getDocumentElement().normalize();

			NodeList nodeLst = doc.getElementsByTagName("profile");

			for (int s = 0; s < nodeLst.getLength(); s++) {

				Profile theProfile = new Profile();

				Node fstNode = nodeLst.item(s);

				if (fstNode.getNodeType() == Node.ELEMENT_NODE) {

					Element fstElmnt = (Element) fstNode;

					NodeList name = getNamedNodeChildren(fstElmnt,"name");
					String profileName = ((Node) name.item(0)).getNodeValue();

					theProfile.setProfileName(profileName);

					NodeList latMax = getNamedNodeChildren(fstElmnt,"latMax");
					try {
						theProfile.setLatitudeMax(validateCoordinate("lat", Double
								.parseDouble(((Node) latMax.item(0)).getNodeValue())));
					} catch (NumberFormatException nfe) {
						JOptionPane.showMessageDialog(null, "Profile: " + profileName
								+ " has illegal value in <latMax> tag\nValue: "
								+ ((Node) latMax.item(0)).getNodeValue()
								+ "\n\nPlease correct and restart application",
								"Error in profiles.xml file", JOptionPane.ERROR_MESSAGE);
						System.exit(0);
					}

					NodeList latMin = getNamedNodeChildren(fstElmnt,"latMin");
					try {
						theProfile.setLatitudeMin(validateCoordinate("lat", Double
								.parseDouble(((Node) latMin.item(0)).getNodeValue())));
					} catch (NumberFormatException nfe) {
						JOptionPane.showMessageDialog(null, "Profile: " + profileName
								+ " has illegal value in <latMin> tag\nValue: "
								+ ((Node) latMin.item(0)).getNodeValue()
								+ "\n\nPlease correct and restart application",
								"Error in profiles.xml file", JOptionPane.ERROR_MESSAGE);
						System.exit(0);
					}

					NodeList longMax = getNamedNodeChildren(fstElmnt,"longMax");
					try {
						theProfile.setLongitudeMax(validateCoordinate("long", Double
								.parseDouble(((Node) longMax.item(0)).getNodeValue())));
					} catch (NumberFormatException nfe) {
						JOptionPane.showMessageDialog(null, "Profile: " + profileName
								+ " has illegal value in <longMax> tag\nValue: "
								+ ((Node) longMax.item(0)).getNodeValue()
								+ "\n\nPlease correct and restart application",
								"Error in profiles.xml file", JOptionPane.ERROR_MESSAGE);
						System.exit(0);
					}

					NodeList longMin = getNamedNodeChildren(fstElmnt,"longMin");

					try {
						theProfile.setLongitudeMin(validateCoordinate("long", Double
								.parseDouble(((Node) longMin.item(0)).getNodeValue())));
					} catch (NumberFormatException nfe) {
						JOptionPane.showMessageDialog(null, "Profile: " + profileName
								+ " has illegal value in <longMin> tag\nValue: "
								+ ((Node) longMin.item(0)).getNodeValue()
								+ "\n\nPlease correct and restart application",
								"Error in profiles.xml file", JOptionPane.ERROR_MESSAGE);
						System.exit(0);
					}

					NodeList mapSource = getNamedNodeChildren(fstElmnt, "mapSource");
					theProfile.setMapSource(mapSource.item(0).getNodeValue());

					NodeList zoomLevelNodes = getNamedNodeChildren(fstElmnt, "zoomLevels");
					int[] zoomLevelsInt = new int[zoomLevelNodes.getLength()];

					int zoomLevelCount = 0;
					int zoomLevelMax = 0;
					for (int i = 0; i < zoomLevelNodes.getLength(); i++) {
						try {
							Element e = (Element) zoomLevelNodes.item(i);
							String nodeName = e.getNodeName();
							if (nodeName.startsWith("z")) {
								nodeName = nodeName.substring(1);
								int z = Integer.parseInt(nodeName);
								zoomLevelMax = Math.max(z, zoomLevelMax);
								zoomLevelsInt[zoomLevelCount++] = z;
							}
						} catch (Exception e) {
						}
					}

					boolean[] zoomLevels = new boolean[zoomLevelMax + 1];
					for (int i = 0; i < zoomLevels.length; i++)
						zoomLevels[i] = false;

					for (int i = 0; i < zoomLevelCount; i++) {
						try {
							zoomLevels[zoomLevelsInt[i]] = true;
						} catch (Exception e) {
						}
					}
					theProfile.setZoomLevels(zoomLevels);

					NodeList tileSizeWidth = getNamedNodeChildren(fstElmnt,"tileSizeWidth");
					try {
						theProfile.setTileSizeWidth(validateTileSize(Integer
								.parseInt(((Node) tileSizeWidth.item(0)).getNodeValue())));
					} catch (NumberFormatException nfe) {
						JOptionPane.showMessageDialog(null, "Profile: " + profileName
								+ " has illegal value in <tileSizeWidth> tag\nValue: "
								+ ((Node) tileSizeWidth.item(0)).getNodeValue()
								+ "\n\nPlease correct and restart application",
								"Error in profiles.xml file", JOptionPane.ERROR_MESSAGE);
						System.exit(0);
					}

					NodeList tileSizeHeight = getNamedNodeChildren(fstElmnt,"tileSizeHeight");
					try {
						theProfile.setTileSizeHeight(validateTileSize(Integer
								.parseInt(((Node) tileSizeHeight.item(0)).getNodeValue())));
					} catch (NumberFormatException nfe) {
						JOptionPane.showMessageDialog(null, "Profile: " + profileName
								+ " has illegal value in <tileSizeHeight> tag\nValue: "
								+ ((Node) tileSizeHeight.item(0)).getNodeValue()
								+ "\n\nPlease correct and restart application",
								"Error in profiles.xml file", JOptionPane.ERROR_MESSAGE);
						System.exit(0);
					}

					NodeList atlasName = getNamedNodeChildren(fstElmnt,"atlasName");
					theProfile.setAtlasName(((Node) atlasName.item(0)).getNodeValue());

					profilesVector.addElement(theProfile);
				}
			}
		} catch (Exception e) {
			log.error("Error while loading profile", e);
		}
		return profilesVector;
	}

	protected static NodeList getNamedNodeChildren(Element parent, String tagName)
			throws ProfileException {
		NodeList ElmntLst = parent.getElementsByTagName(tagName);
		if (ElmntLst.getLength() > 1)
			throw new ProfileException("More than one \"" + tagName + "\" entry!");
		if (ElmntLst.getLength() == 0)
			throw new ProfileException("No \"" + tagName + "\" entry found!");
		Element Elmnt = (Element) ElmntLst.item(0);
		return Elmnt.getChildNodes();
	}

	public static void store(Vector<Profile> theProfileVector) {
		PrintWriter out = null;
		try {
			out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(System
					.getProperty("user.dir")
					+ System.getProperty("file.separator") + "profiles.xml"), "UTF-16"));

			out.println("<?xml version=\"1.0\" encoding=\"UTF-16\"?>");

			// out.println("<!DOCTYPE profiles [");
			// out.println("	<!ELEMENT profiles (profile)>");
			// out
			// .println("	<!ELEMENT profile (name, latMax, latMin, longMax, "
			// +
			// "longMin, zoomLevelOne, zoomLevelTwo, zoomLevelThree,
			// zoomLevelFour,
			// zoomLevelFive, zoomLevelSix, zoomLevelSeven, zoomLevelEight,
			// zoomLevelNine, zoomLevelTen, tileSizeWidth, tileSizeHeight,
			// customTileSizeWidth, customTileSizeWidth, atlasName)>"
			// );
			// out.println("	<!ELEMENT name (CDATA)>");
			// out.println("	<!ELEMENT latMax (CDATA)>");
			// out.println("	<!ELEMENT latMin (CDATA)>");
			// out.println("	<!ELEMENT longMax (CDATA)>");
			// out.println("	<!ELEMENT longMin (CDATA)>");
			// out.println("	<!ELEMENT zoomLevelOne (CDATA)>");
			// out.println("	<!ELEMENT zoomLevelTwo (CDATA)>");
			// out.println("	<!ELEMENT zoomLevelThree (CDATA)>");
			// out.println("	<!ELEMENT zoomLevelFour (CDATA)>");
			// out.println("	<!ELEMENT zoomLevelFive (CDATA)>");
			// out.println("	<!ELEMENT zoomLevelSix (CDATA)>");
			// out.println("	<!ELEMENT zoomLevelSeven (CDATA)>");
			// out.println("	<!ELEMENT zoomLevelEight (CDATA)>");
			// out.println("	<!ELEMENT zoomLevelNine (CDATA)>");
			// out.println("	<!ELEMENT zoomLevelTen (CDATA)>");
			// out.println("	<!ELEMENT tileSizeWidth (CDATA)>");
			// out.println("	<!ELEMENT tileSizeHeight (CDATA)>");
			// out.println("	<!ELEMENT customTileSizeWidth (CDATA)>");
			// out.println("	<!ELEMENT customTileSizeHeight (CDATA)>");
			// out.println("	<!ELEMENT atlasName (CDATA)>");
			// out.println("]>");

			out.println("<profiles>");

			for (Profile profile : theProfileVector) {
				out.println("    <profile>");
				out.println("        <name>" + profile.getProfileName() + "</name>");
				out.println("        <atlasName>" + profile.getAtlasName() + "</atlasName>");
				out.println("        <mapSource>" + profile.getMapSource() + "</mapSource>");
				out.println("        <latMax>" + profile.getLatitudeMax() + "</latMax>");
				out.println("        <latMin>" + profile.getLatitudeMin() + "</latMin>");
				out.println("        <longMax>" + profile.getLongitudeMax() + "</longMax>");
				out.println("        <longMin>" + profile.getLongitudeMin() + "</longMin>");
				out.println("        <zoomLevels>");
				boolean[] zoomLevels = profile.getZoomLevels();
				for (int i = 0; i < zoomLevels.length; i++) {
					if (zoomLevels[i])
						out.println("            <z" + i + " />");
				}
				out.println("        </zoomLevels>");
				out.println("        <tileSizeWidth>" + profile.getTileSizeWidth()
						+ "</tileSizeWidth>");
				out.println("        <tileSizeHeight>" + profile.getTileSizeHeight()
						+ "</tileSizeHeight>");
				out.println("    </profile>");
			}
			out.print("</profiles>");
		} catch (IOException e) {
			log.error("Error while saving profile", e);
		} finally {
			Utilities.closeWriter(out);
		}
	}

	public static Double validateCoordinate(String direction, Double directionValue) {

		if (direction.equals("lat")) {
			if (directionValue > 85.0)
				return 85.0;
			if (directionValue < -85.0)
				return -85.0;
		} else {
			if (directionValue > 179.0)
				return 179.0;
			if (directionValue < -179.0)
				return -179.0;
		}
		return directionValue;
	}

	public static int validateTileSize(int tileSize) {
		if (tileSize > 1792)
			return 1792;
		if (tileSize < 1)
			return 256;
		return tileSize;
	}

	public static class ProfileException extends Exception {

		private static final long serialVersionUID = 1L;

		public ProfileException(String message) {
			super(message);
		}

	}
}