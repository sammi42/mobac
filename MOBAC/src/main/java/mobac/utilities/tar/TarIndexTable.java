package mobac.utilities.tar;

import java.util.Hashtable;

public class TarIndexTable {

	/**
	 * Maps tile name to TAR block index (each block has 512 bytes).
	 */
	private Hashtable<String, Integer> hashTable;

	public TarIndexTable(int initialCapacity) {
		hashTable = new Hashtable<String, Integer>(initialCapacity);
	}

	public void addTarEntry(String filename, long streamPos) {
		assert ((streamPos & 0x1F) == 0);
		int tarBlockIndex = (int) (streamPos >> 9);
		hashTable.put(filename, new Integer(tarBlockIndex));
	}

	public long getEntryOffset(String filename) {
		Integer tarBlockIndex = hashTable.get(filename);
		if (tarBlockIndex == null)
			return -1;
		long offset = ((long) (tarBlockIndex)) << 9;
		return offset;
	}

	public int size() {
		return hashTable.size();
	}
}
