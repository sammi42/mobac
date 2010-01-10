package tac.gui.components;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.net.URL;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.TimerTask;

import org.openstreetmap.gui.jmapviewer.MapGridLayer;

import tac.gui.mapview.PreviewMap;
import tac.mapsources.BeanShellMapSource;

public class LogPreviewMap extends PreviewMap {

	private final LinkedList<LogEntry> logLines = new LinkedList<LogEntry>();
	private Font logFont = new Font("Sans Serif", Font.BOLD, 14);
	private Font tileInfoFont = new Font("Sans Serif", Font.BOLD, 12);

	// private Timer timer = new Timer();

	public LogPreviewMap() {
		super();
		setZoomContolsVisible(true);
		setTileGridVisible(true);
		setTileGridVisible(true);
		// timer.schedule(new LogRemoverTimerTask(), 0, 500);
	}

	@Override
	public void setTileGridVisible(boolean tileGridVisible) {
		if (isTileGridVisible() == tileGridVisible)
			return;
		if (tileGridVisible) {
			mapGridLayer = new MapGridInfoLayer();
			addMapTileLayers(mapGridLayer);
		} else {
			removeMapTileLayers(mapGridLayer);
			mapGridLayer = null;
		}
		repaint();
	}

	@Override
	protected void paintComponent(Graphics graphics) {
		super.paintComponent(graphics);
		int y = 18;
		graphics.setFont(logFont);
		graphics.setColor(Color.RED);
		synchronized (logLines) {
			for (LogEntry entry : logLines) {
				graphics.drawString(entry.msg, 50, y);
				y += 20;
			}
		}
	}

	public void addLog(String msg) {
		LogEntry entry = new LogEntry();
		entry.msg = msg;
		synchronized (logLines) {
			logLines.addFirst(entry);
			if (logLines.size() > 10)
				logLines.removeLast();
		}
	}

	public static class LogEntry {
		long time = System.currentTimeMillis();
		String msg;
	}

	public class LogRemoverTimerTask extends TimerTask {

		@Override
		public void run() {
			long minTime = System.currentTimeMillis() - 4000;
			boolean dirty = false;
			try {
				synchronized (logLines) {
					while (true) {
						LogEntry e = logLines.getLast();
						if (e.time < minTime) {
							logLines.removeLast();
							dirty = true;
						} else
							break;
					}
				}
			} catch (NoSuchElementException e) {
			}
			if (dirty)
				repaint();
		}
	}

	public class MapGridInfoLayer extends MapGridLayer {

		public void paintTile(Graphics g, int gx, int gy, int tilex, int tiley, int zoom) {
			g.setColor(Color.BLACK);
			g.drawRect(gx, gy, tileSize, tileSize);
			g.setFont(tileInfoFont);
			g.setColor(Color.BLUE);
			g.drawString("zoom=" + zoom, gx + 4, gy += 14);
			g.drawString("x=" + tilex, gx + 4, gy += 16);
			g.drawString("y=" + tiley, gx + 4, gy += 16);
			if (mapSource instanceof BeanShellMapSource) {
				try {
					String tileUrl = ((BeanShellMapSource) mapSource)
							.getTileUrl(zoom, tilex, tiley);
					URL url = new URL(tileUrl);
					String strUrl = url.getHost() + url.getPath();
					if (url.getQuery() != null && url.getQuery().length() > 0)
						strUrl += "?" + url.getQuery();
					drawUrl(g, strUrl, gx + 4, gy += 16, tileSize);
				} catch (Exception e) {
				}
			}
		}
	}

	private void drawUrl(Graphics g, String s, int x, int y, int width) {
		FontMetrics fm = g.getFontMetrics();
		int lineHeight = fm.getHeight();

		int curX = x;
		int curY = y;
		width -= 15;
		
		int beginIndex = 0;
		for (int i = 0; i < s.length(); i++) {
			String sub = s.substring(beginIndex, i);
			int wordWidth = fm.stringWidth(sub);
			if (wordWidth >= width) {
				g.drawString(sub, curX, curY);
				curY += lineHeight;
				beginIndex = i;
				sub = null;
			}
		}
		if (beginIndex != s.length()) {
			String sub = s.substring(beginIndex, s.length());
			g.drawString(sub, curX, curY);
		}
	}
}
