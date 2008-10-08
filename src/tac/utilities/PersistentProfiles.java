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

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import tac.program.Profile;

public class PersistentProfiles {

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

					NodeList nameElmntLst = fstElmnt.getElementsByTagName("name");
					Element nameElmnt = (Element) nameElmntLst.item(0);
					NodeList name = nameElmnt.getChildNodes();
					String profileName = ((Node) name.item(0)).getNodeValue();

					theProfile.setProfileName(profileName);

					NodeList latMaxElmntLst = fstElmnt.getElementsByTagName("latMax");
					Element latMaxElmnt = (Element) latMaxElmntLst.item(0);
					NodeList latMax = latMaxElmnt.getChildNodes();

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

					NodeList latMinElmntLst = fstElmnt.getElementsByTagName("latMin");
					Element latMinElmnt = (Element) latMinElmntLst.item(0);
					NodeList latMin = latMinElmnt.getChildNodes();

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

					NodeList longMaxElmntLst = fstElmnt.getElementsByTagName("longMax");
					Element longMaxElmnt = (Element) longMaxElmntLst.item(0);
					NodeList longMax = longMaxElmnt.getChildNodes();

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

					NodeList longMinElmntLst = fstElmnt.getElementsByTagName("longMin");
					Element longMinElmnt = (Element) longMinElmntLst.item(0);
					NodeList longMin = longMinElmnt.getChildNodes();

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

					boolean[] zoomLevels = new boolean[10];

					NodeList zoomLevelOneElmntLst = fstElmnt.getElementsByTagName("zoomLevelOne");
					Element zoomLevelOneElmnt = (Element) zoomLevelOneElmntLst.item(0);
					NodeList zoomLevelOne = zoomLevelOneElmnt.getChildNodes();

					zoomLevels[0] = Boolean.parseBoolean(((Node) zoomLevelOne.item(0))
							.getNodeValue());

					NodeList zoomLevelTwoElmntLst = fstElmnt.getElementsByTagName("zoomLevelTwo");
					Element zoomLevelTwoElmnt = (Element) zoomLevelTwoElmntLst.item(0);
					NodeList zoomLevelTwo = zoomLevelTwoElmnt.getChildNodes();

					zoomLevels[1] = Boolean.parseBoolean(((Node) zoomLevelTwo.item(0))
							.getNodeValue());

					NodeList zoomLevelThreeElmntLst = fstElmnt
							.getElementsByTagName("zoomLevelThree");
					Element zoomLevelThreeElmnt = (Element) zoomLevelThreeElmntLst.item(0);
					NodeList zoomLevelThree = zoomLevelThreeElmnt.getChildNodes();

					zoomLevels[2] = Boolean.parseBoolean(((Node) zoomLevelThree.item(0))
							.getNodeValue());

					NodeList zoomLevelFourElmntLst = fstElmnt.getElementsByTagName("zoomLevelFour");
					Element zoomLevelFourElmnt = (Element) zoomLevelFourElmntLst.item(0);
					NodeList zoomLevelFour = zoomLevelFourElmnt.getChildNodes();

					zoomLevels[3] = Boolean.parseBoolean(((Node) zoomLevelFour.item(0))
							.getNodeValue());

					NodeList zoomLevelFiveElmntLst = fstElmnt.getElementsByTagName("zoomLevelFive");
					Element zoomLevelFiveElmnt = (Element) zoomLevelFiveElmntLst.item(0);
					NodeList zoomLevelFive = zoomLevelFiveElmnt.getChildNodes();

					zoomLevels[4] = Boolean.parseBoolean(((Node) zoomLevelFive.item(0))
							.getNodeValue());

					NodeList zoomLevelSixElmntLst = fstElmnt.getElementsByTagName("zoomLevelSix");
					Element zoomLevelSixElmnt = (Element) zoomLevelSixElmntLst.item(0);
					NodeList zoomLevelSix = zoomLevelSixElmnt.getChildNodes();

					zoomLevels[5] = Boolean.parseBoolean(((Node) zoomLevelSix.item(0))
							.getNodeValue());

					NodeList zoomLevelSevenElmntLst = fstElmnt
							.getElementsByTagName("zoomLevelSeven");
					Element zoomLevelSevenElmnt = (Element) zoomLevelSevenElmntLst.item(0);
					NodeList zoomLevelSeven = zoomLevelSevenElmnt.getChildNodes();

					zoomLevels[6] = Boolean.parseBoolean(((Node) zoomLevelSeven.item(0))
							.getNodeValue());

					NodeList zoomLevelEightElmntLst = fstElmnt
							.getElementsByTagName("zoomLevelEight");
					Element zoomLevelEightElmnt = (Element) zoomLevelEightElmntLst.item(0);
					NodeList zoomLevelEight = zoomLevelEightElmnt.getChildNodes();

					zoomLevels[7] = Boolean.parseBoolean(((Node) zoomLevelEight.item(0))
							.getNodeValue());

					NodeList zoomLevelNineElmntLst = fstElmnt.getElementsByTagName("zoomLevelNine");
					Element zoomLevelNineElmnt = (Element) zoomLevelNineElmntLst.item(0);
					NodeList zoomLevelNine = zoomLevelNineElmnt.getChildNodes();

					zoomLevels[8] = Boolean.parseBoolean(((Node) zoomLevelNine.item(0))
							.getNodeValue());

					NodeList zoomLevelTenElmntLst = fstElmnt.getElementsByTagName("zoomLevelTen");
					Element zoomLevelTenElmnt = (Element) zoomLevelTenElmntLst.item(0);
					NodeList zoomLevelTen = zoomLevelTenElmnt.getChildNodes();

					zoomLevels[9] = Boolean.parseBoolean(((Node) zoomLevelTen.item(0))
							.getNodeValue());

					theProfile.setZoomLevels(zoomLevels);

					NodeList tileSizeWidthElmntLst = fstElmnt.getElementsByTagName("tileSizeWidth");
					Element tileSizeWidthElmnt = (Element) tileSizeWidthElmntLst.item(0);
					NodeList tileSizeWidth = tileSizeWidthElmnt.getChildNodes();

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

					NodeList tileSizeHeightElmntLst = fstElmnt
							.getElementsByTagName("tileSizeHeight");
					Element tileSizeHeightElmnt = (Element) tileSizeHeightElmntLst.item(0);
					NodeList tileSizeHeight = tileSizeHeightElmnt.getChildNodes();

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

					NodeList customTileSizeWidthElmntLst = fstElmnt
							.getElementsByTagName("customTileSizeWidth");
					Element customTileSizeWidthElmnt = (Element) customTileSizeWidthElmntLst
							.item(0);
					NodeList customTileSizeWidth = customTileSizeWidthElmnt.getChildNodes();

					try {
						theProfile.setCustomTileSizeWidth(validateCustomTileSize(Integer
								.parseInt(((Node) customTileSizeWidth.item(0)).getNodeValue())));
					} catch (NumberFormatException nfe) {
						JOptionPane.showMessageDialog(null, "Profile: " + profileName
								+ " has illegal value in <customTileSizeWidth> tag\nValue: "
								+ ((Node) customTileSizeWidth.item(0)).getNodeValue()
								+ "\n\nPlease correct and restart application",
								"Error in profiles.xml file", JOptionPane.ERROR_MESSAGE);
						System.exit(0);
					}

					NodeList customTileSizeHeightElmntLst = fstElmnt
							.getElementsByTagName("customTileSizeHeight");
					Element customTileSizeHeightElmnt = (Element) customTileSizeHeightElmntLst
							.item(0);
					NodeList customTileSizeHeight = customTileSizeHeightElmnt.getChildNodes();

					try {
						theProfile.setCustomTileSizeHeight(validateCustomTileSize(Integer
								.parseInt(((Node) customTileSizeHeight.item(0)).getNodeValue())));
					} catch (NumberFormatException nfe) {
						JOptionPane.showMessageDialog(null, "Profile: " + profileName
								+ " has illegal value in <customTileSizeHeight> tag\nValue: "
								+ ((Node) customTileSizeHeight.item(0)).getNodeValue()
								+ "\n\nPlease correct and restart application",
								"Error in profiles.xml file", JOptionPane.ERROR_MESSAGE);
						System.exit(0);
					}

					NodeList atlasNameElmntLst = fstElmnt.getElementsByTagName("atlasName");
					Element atlasNameElmnt = (Element) atlasNameElmntLst.item(0);
					NodeList atlasName = atlasNameElmnt.getChildNodes();

					theProfile.setAtlasName(((Node) atlasName.item(0)).getNodeValue());

					profilesVector.addElement(theProfile);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return profilesVector;
	}

	public static void store(Vector<Profile> theProfileVector) {
		PrintWriter out = null;
		try {
			out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(System
					.getProperty("user.dir")
					+ System.getProperty("file.separator") + "profiles.xml"), "UTF-16"));

			out.println("<?xml version=\"1.0\" encoding=\"UTF-16\"?>");

			out.println("<!DOCTYPE profiles [");
			out.println("	<!ELEMENT profiles (profile)>");
			out
					.println("	<!ELEMENT profile (name, latMax, latMin, longMax, longMin, zoomLevelOne, zoomLevelTwo, zoomLevelThree, zoomLevelFour, zoomLevelFive, zoomLevelSix, zoomLevelSeven, zoomLevelEight, zoomLevelNine, zoomLevelTen, tileSizeWidth, tileSizeHeight, customTileSizeWidth, customTileSizeWidth, atlasName)>");
			out.println("	<!ELEMENT name (CDATA)>");
			out.println("	<!ELEMENT latMax (CDATA)>");
			out.println("	<!ELEMENT latMin (CDATA)>");
			out.println("	<!ELEMENT longMax (CDATA)>");
			out.println("	<!ELEMENT longMin (CDATA)>");
			out.println("	<!ELEMENT zoomLevelOne (CDATA)>");
			out.println("	<!ELEMENT zoomLevelTwo (CDATA)>");
			out.println("	<!ELEMENT zoomLevelThree (CDATA)>");
			out.println("	<!ELEMENT zoomLevelFour (CDATA)>");
			out.println("	<!ELEMENT zoomLevelFive (CDATA)>");
			out.println("	<!ELEMENT zoomLevelSix (CDATA)>");
			out.println("	<!ELEMENT zoomLevelSeven (CDATA)>");
			out.println("	<!ELEMENT zoomLevelEight (CDATA)>");
			out.println("	<!ELEMENT zoomLevelNine (CDATA)>");
			out.println("	<!ELEMENT zoomLevelTen (CDATA)>");
			out.println("	<!ELEMENT tileSizeWidth (CDATA)>");
			out.println("	<!ELEMENT tileSizeHeight (CDATA)>");
			out.println("	<!ELEMENT customTileSizeWidth (CDATA)>");
			out.println("	<!ELEMENT customTileSizeHeight (CDATA)>");
			out.println("	<!ELEMENT atlasName (CDATA)>");
			out.println("]>");

			out.println("<profiles>");

			for (int i = 0; i < theProfileVector.size(); i++) {
				out.println("    <profile>");
				out.println("        <name>"
						+ ((Profile) (theProfileVector.elementAt(i))).getProfileName() + "</name>");
				out.println("        <latMax>"
						+ ((Profile) (theProfileVector.elementAt(i))).getLatitudeMax()
						+ "</latMax>");
				out.println("        <latMin>"
						+ ((Profile) (theProfileVector.elementAt(i))).getLatitudeMin()
						+ "</latMin>");
				out.println("        <longMax>"
						+ ((Profile) (theProfileVector.elementAt(i))).getLongitudeMax()
						+ "</longMax>");
				out.println("        <longMin>"
						+ ((Profile) (theProfileVector.elementAt(i))).getLongitudeMin()
						+ "</longMin>");
				out.println("        <zoomLevelOne>"
						+ (((Profile) (theProfileVector.elementAt(i))).getZoomLevels())[0]
						+ "</zoomLevelOne>");
				out.println("        <zoomLevelTwo>"
						+ (((Profile) (theProfileVector.elementAt(i))).getZoomLevels())[1]
						+ "</zoomLevelTwo>");
				out.println("        <zoomLevelThree>"
						+ (((Profile) (theProfileVector.elementAt(i))).getZoomLevels())[2]
						+ "</zoomLevelThree>");
				out.println("        <zoomLevelFour>"
						+ (((Profile) (theProfileVector.elementAt(i))).getZoomLevels())[3]
						+ "</zoomLevelFour>");
				out.println("        <zoomLevelFive>"
						+ (((Profile) (theProfileVector.elementAt(i))).getZoomLevels())[4]
						+ "</zoomLevelFive>");
				out.println("        <zoomLevelSix>"
						+ (((Profile) (theProfileVector.elementAt(i))).getZoomLevels())[5]
						+ "</zoomLevelSix>");
				out.println("        <zoomLevelSeven>"
						+ (((Profile) (theProfileVector.elementAt(i))).getZoomLevels())[6]
						+ "</zoomLevelSeven>");
				out.println("        <zoomLevelEight>"
						+ (((Profile) (theProfileVector.elementAt(i))).getZoomLevels())[7]
						+ "</zoomLevelEight>");
				out.println("        <zoomLevelNine>"
						+ (((Profile) (theProfileVector.elementAt(i))).getZoomLevels())[8]
						+ "</zoomLevelNine>");
				out.println("        <zoomLevelTen>"
						+ (((Profile) (theProfileVector.elementAt(i))).getZoomLevels())[9]
						+ "</zoomLevelTen>");
				out.println("        <tileSizeWidth>"
						+ ((Profile) (theProfileVector.elementAt(i))).getTileSizeWidth()
						+ "</tileSizeWidth>");
				out.println("        <tileSizeHeight>"
						+ ((Profile) (theProfileVector.elementAt(i))).getTileSizeHeight()
						+ "</tileSizeHeight>");
				out.println("        <customTileSizeWidth>"
						+ ((Profile) (theProfileVector.elementAt(i))).getCustomTileSizeWidth()
						+ "</customTileSizeWidth>");
				out.println("        <customTileSizeHeight>"
						+ ((Profile) (theProfileVector.elementAt(i))).getCustomTileSizeHeight()
						+ "</customTileSizeHeight>");
				out.println("        <atlasName>"
						+ ((Profile) (theProfileVector.elementAt(i))).getAtlasName()
						+ "</atlasName>");
				out.println("    </profile>");
			}
			out.print("</profiles>");
		} catch (IOException e) {
		}
		out.close();
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
		if (tileSize < 256)
			return 256;
		if (tileSize % 256 != 0)
			return 256;

		return tileSize;
	}

	public static int validateCustomTileSize(int tileSize) {

		if (tileSize > 1792)
			return 1792;
		if (tileSize < 1)
			return 256;

		return tileSize;
	}
}