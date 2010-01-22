package mobac.utilities.imageio;

/**
 * Common constants used in the PNG file format.
 */
public class PngConstants {

	/**
	 * PNG magic file bytes
	 */
	public static final byte[] SIGNATURE = { (byte) 137, 80, 78, 71, 13, 10, 26, 10 };
	
	/**
	 * Chunk header identifiers
	 */
	public static final int IHDR = (int) 0x49484452; // png header
	public static final int TEXT = (int) 0x74455874; // textual information
	public static final int PLTE = (int) 0x504C5445; // color palette
	public static final int IDAT = (int) 0x49444154; // image data
	public static final int IEND = (int) 0x49454E44; // end of file

	public static final byte COLOR_PALETTE = 3;
	public static final byte COLOR_TRUECOLOR = 2;

	public static final byte COMPRESSION_DEFLATE = 0;
	
	public static final byte FILTER_SET_1 = 0;
	
	public static final byte INTERLACE_NONE = 0;

	public static final byte FILTER_TYPE_NONE = 0;
	public static final byte FILTER_TYPE_SUB = 1;
	public static final byte FILTER_TYPE_UP = 2;
	public static final byte FILTER_TYPE_AVERAGE = 0;
	public static final byte FILTER_TYPE_PAETH = 4;

}
