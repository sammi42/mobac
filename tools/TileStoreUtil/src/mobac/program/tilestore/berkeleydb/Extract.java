package mobac.program.tilestore.berkeleydb;

import java.io.File;
import java.io.FileOutputStream;

import mobac.mapsources.MapSourceTools;
import mobac.program.model.TileImageType;
import mobac.program.tilestore.TileStore;
import mobac.program.tilestore.berkeleydb.BerkeleyDbTileStore.TileDatabase;
import mobac.utilities.Utilities;

import com.sleepycat.persist.EntityCursor;

public class Extract {

	public static void extract(File sourceDir, File destDir) {
		BerkeleyDbTileStore tileStore = (BerkeleyDbTileStore) TileStore.getInstance();
		TileDatabase dbSource = null;
		try {
			dbSource = tileStore.new TileDatabase("Source", sourceDir);
			TileStoreUtil.log.info("Source tile store entry count: " + dbSource.entryCount());
			EntityCursor<TileDbEntry> cursor = dbSource.getTileIndex().entities();
			try {
				TileDbEntry entry = cursor.next();
				while (entry != null) {
					TileStoreUtil.log.trace("Extracting " + entry.shortInfo());
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

					entry = cursor.next();
				}
			} finally {
				cursor.close();
			}
			TileStoreUtil.log.info("Destination tile store entry count: " + dbSource.entryCount() + " (after merging)");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			dbSource.close(false);
		}
	}

}
