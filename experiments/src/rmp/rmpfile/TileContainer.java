package rmp.rmpfile;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;

public class TileContainer {
	int number;
	TileContainer previous;
	ArrayList<Tiledata> tiles;
	ArrayList<TileContainer> followUps;

	public TileContainer(int nr) {
		this.number = nr;
		this.previous = null;
		this.tiles = new ArrayList<Tiledata>();
		this.followUps = new ArrayList<TileContainer>();
	}

	public void setPrevious(TileContainer previous) {
		this.previous = previous;
	}

	public void addTile(Tiledata tile, TileContainer next) {
		this.tiles.add(tile);

		if (this.previous != null)
			this.followUps.add(next);
	}

	public int getTileCount() {
		int count = this.tiles.size();

		if (this.previous != null) {
			count += this.previous.getTileCount();
		}
		Iterator<TileContainer> it = this.followUps.iterator();
		while (it.hasNext()) {
			count += it.next().getTileCount();
		}
		return count;
	}

	public int getContainerCount() {
		return (1 + this.followUps.size());
	}

	public void writeTree(OutputStream os) throws IOException {
		if (this.previous != null) {
			this.previous.writeTree(os);

			writeContainer(os);

			for (int i = 0; i < this.followUps.size(); ++i) {
				((TileContainer) this.followUps.get(i)).writeTree(os);
			}
		} else {
			writeContainer(os);
		}
	}

	public void writeContainer(OutputStream os) throws IOException {
		Tools.writeValue(os, getTileCount(), 4);

		Tools.writeValue(os, this.tiles.size(), 2);

		Tools.writeValue(os, (this.previous == null) ? 1 : 0, 2);

		for (int i = 0; i < 99; ++i) {
			int x = 0;
			int y = 0;
			int offset = 0;

			if (i < this.tiles.size()) {
				x = ((Tiledata) this.tiles.get(i)).posx;
				y = ((Tiledata) this.tiles.get(i)).posy;
				offset = ((Tiledata) this.tiles.get(i)).totalOffset;
			}

			Tools.writeValue(os, x, 4);
			Tools.writeValue(os, y, 4);
			Tools.writeValue(os, 0, 4);
			Tools.writeValue(os, offset, 4);
		}

		if (this.previous == null)
			Tools.writeValue(os, 0, 4);
		else {
			Tools.writeValue(os, 3932, 4);
		}

		for (int i = 0; i < 99; ++i) {
			if (i < this.followUps.size())
				Tools.writeValue(os, 3932 + (i + 2) * 1992, 4);
			else
				Tools.writeValue(os, 0, 4);
		}
	}
}

/*
 * Location: E:\TritonMap\tritonrmp.jar Qualified Name:
 * de.picosaan.rmp.TileContainer JD-Core Version: 0.5.2
 */