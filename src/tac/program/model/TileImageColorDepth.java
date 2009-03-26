package tac.program.model;

import java.util.NoSuchElementException;

public enum TileImageColorDepth {
	Unchanged("Do not change", -1), EightBit("8 bit (256 colors)", 256), FourBit(
			"4 bit (16 colors)", 26);

	public static TileImageColorDepth getByColorCountDefault(int colorCount) {
		try {
			return getByColorCount(colorCount);
		} catch (NoSuchElementException e) {
			return Unchanged;
		}
	}

	public static TileImageColorDepth getByColorCount(int colorCount) throws NoSuchElementException {
		for (TileImageColorDepth value : TileImageColorDepth.values()) {
			if (value.colorCount == colorCount)
				return value;
		}
		throw new NoSuchElementException("TileImageColorDepth with colorCount=" + colorCount
				+ " is not defined");
	}

	private final String displayName;
	private final int colorCount;

	private TileImageColorDepth(String displayName, int colorCount) {
		this.displayName = displayName;
		this.colorCount = colorCount;
	}

	public int getColorCount() {
		return colorCount;
	}

	public String toString() {
		return displayName;
	}
}
