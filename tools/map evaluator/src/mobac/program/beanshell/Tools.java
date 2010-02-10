package mobac.program.beanshell;

import java.security.SecureRandom;

import mobac.mapsources.MapSourceTools;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;

public class Tools {

	public static final SecureRandom RND = new SecureRandom();

	public int getRandomInt(int max) {
		return RND.nextInt(max);
	}

	public byte[] getRandomByteArray(int length) {
		byte[] buf = new byte[length];
		RND.nextBytes(buf);
		return buf;
	}

	public static String encodeBase64(byte[] binaryData) {
		return Base64.encodeBase64String(binaryData);
	}

	public static byte[] decodeBase64(String base64String) {
		return Base64.decodeBase64(base64String);
	}

	public static String encodeQuadTree(int zoom, int tilex, int tiley) {
		return MapSourceTools.encodeQuadTree(zoom, tilex, tiley);
	}

	public static byte[] decodeHex(String hexString) throws DecoderException {
		return Hex.decodeHex(hexString.toCharArray());
	}

	public static String encodeHex(byte[] binaryData) throws DecoderException {
		return Hex.encodeHexString(binaryData);
	}
}
