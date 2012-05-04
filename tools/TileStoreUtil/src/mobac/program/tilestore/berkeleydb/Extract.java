package mobac.program.tilestore.berkeleydb;

import java.io.File;
import java.io.FileOutputStream;

import mobac.mapsources.MapSourceTools;
import mobac.program.model.TileImageType;
import mobac.program.tilestore.TileStore;
import mobac.program.tilestore.berkeleydb.BerkeleyDbTileStore.TileDatabase;
import mobac.ts_util.Main;
import mobac.utilities.Utilities;

import com.sleepycat.persist.EntityCursor;

public class Extract {

	public static void extract(File sourceDir, File destDir) {
		BerkeleyDbTileStore tileStore = (BerkeleyDbTileStore) TileStore.getInstance();
		TileDatabase dbSource = null;
		try {
			dbSource = tileStore.new TileDatabase("Source", sourceDir);
			Main.log.info("Source tile store entry count: " + dbSource.entryCount());
			long count = 0;
			EntityCursor<TileDbEntry> cursor = dbSource.getTileIndex().entities();
			try {
				TileDbEntry entry = cursor.next();
				while (entry != null) {
					Main.log.trace("Extracting " + entry.shortInfo());
					String pattern = "{$z}/{$x}/{$y}.{$ext}";
					String fileName = MapSourceTools.formatMapUrl(pattern, entry.getZoom(), entry.getX(), entry.getY());
					byte[] data = entry.getData();
					TileImageType type = Utilities.getImageType(data);
					fileName = fileName.replace("{$ext}", type.getFileExt());

					File f = new File(destDir, fileName);
					Utilities.mkDirs(f.getParentFile());
					FileOutputStream fout = new FileOutputStream(f);
					fout.write(data);
					fout.flush();
					fout.close();
					count++;
					entry = cursor.next();
				}
			} finally {
				cursor.close();
			}
			Main.log.info("Number of extracted tiles: " + count);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			dbSource.close(false);
		}
	}

}
