/*******************************************************************************
 * Copyright (c) MOBAC developers
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package mobac.gui.mapview;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryNotificationInfo;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;

import javax.management.Notification;
import javax.management.NotificationBroadcaster;
import javax.management.NotificationListener;


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
