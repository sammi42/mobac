package mobac.gui.mapview;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryNotificationInfo;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;

import javax.management.Notification;
import javax.management.NotificationBroadcaster;
import javax.management.NotificationListener;

import org.openstreetmap.gui.jmapviewer.MemoryTileCache;

public class PreviewTileCache extends MemoryTileCache implements NotificationListener {

	public PreviewTileCache() {
		super();
		cacheSize = 500;
		MemoryMXBean mbean = ManagementFactory.getMemoryMXBean();
		NotificationBroadcaster emitter = (NotificationBroadcaster) mbean;
		emitter.addNotificationListener(this, null, null);
		// Set-up each memory pool to notify if the free memory falls below 10%
		for (MemoryPoolMXBean memPool : ManagementFactory.getMemoryPoolMXBeans()) {
			if (memPool.isUsageThresholdSupported()) {
				MemoryUsage memUsage = memPool.getUsage();
				memPool.setUsageThreshold((long) (memUsage.getMax() * 0.95));
			}
		}
	}

	/**
	 * In case we are running out of memory we free half of the cached down to a
	 * minimum of 25 cached tiles.
	 */
	public void handleNotification(Notification notification, Object handback) {
		log.trace("Memory notification: " + notification.toString());
		if (!MemoryNotificationInfo.MEMORY_THRESHOLD_EXCEEDED.equals(notification.getType()))
			return;
		synchronized (lruTiles) {
			int count_half = lruTiles.getElementCount() / 2;
			count_half = Math.max(25, count_half);
			if (lruTiles.getElementCount() <= count_half)
				return;
			log.warn("memory low - freeing cached tiles: " + lruTiles.getElementCount() + " -> "
					+ count_half);
			try {
				while (lruTiles.getElementCount() > count_half) {
					removeEntry(lruTiles.getLastElement());
				}
			} catch (Exception e) {
				log.error("", e);
			}
		}
	}
}
