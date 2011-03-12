package mobac.gui.mapview;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.net.URL;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.TimerTask;

import mobac.gui.mapview.layer.MapGridLayer;
import mobac.mapsources.AbstractHttpMapSource;

public class LogPreviewMap extends PreviewMap {

	private final LinkedList<LogEntry> logLines = new LinkedList<LogEntry>();
	private Font logFont = new Font("Sans Serif", Font.BOLD, 14);
	private Font tileInfoFont = new Font("Sans Serif", Font.BOLD, 12);

	// private Timer timer = new Timer();

	public LogPreviewMap() {
		super();
		usePlaceHolderTiles = false;
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
			if (tilex < 0 || tiley < 0)
				return;
			int max = mapSource.getMapSpace().getMaxPixels(zoom);
			int tileSize = mapSource.getMapSpace().getTileSize();
			if (tilex * tileSize >= max || tiley * tileSize >= max)
				return;
			g.setColor(Color.BLACK);
			g.drawRect(gx, gy, tileSize, tileSize);
			g.setFont(tileInfoFont);
			g.setColor(Color.BLUE);
			drawStringBG(g, "zoom=" + zoom, gx + 4, gy += 14);
			drawStringBG(g, "x=" + tilex, gx + 4, gy += 16);
			drawStringBG(g, "y=" + tiley, gx + 4, gy += 16);
			String tileUrl = null;
			try {
				if (mapSource instanceof AbstractHttpMapSource)
					tileUrl = ((AbstractHttpMapSource) mapSource).getTileUrl(zoom, tilex, tiley);
				if (tileUrl != null) {
					URL url = new URL(tileUrl);
					drawUrl(g, "host=" + url.getHost(), gx + 4, gy += 16, tileSize);
					drawUrl(g, url.getPath(), gx + 4, gy += 16, tileSize);

					String strQuery = url.getQuery();
					if (strQuery != null && strQuery.length() > 0) {
						drawUrl(g, "?" + strQuery, gx + 4, gy += 16, tileSize);
					}
				}
			} catch (Exception e) {
			}
		}
	}

	private void drawUrl(Graphics g, String s, int x, int y, int width) {
		FontMetrics fm = g.getFontMetrics();
		int lineHeight = fm.getHeight();
		int ascent = fm.getAscent();

		int curX = x;
		int curY = y;
		width -= 15;

		int beginIndex = 0;
		for (int i = 0; i < s.length(); i++) {
			String sub = s.substring(beginIndex, i);
			int textWidth = fm.stringWidth(sub);
			if (textWidth >= width) {
				g.setColor(Color.WHITE);
				g.fillRect(curX - 2, curY - ascent, textWidth + 2, lineHeight);
				g.setColor(Color.BLUE);
				g.drawString(sub, curX, curY);
				curY += lineHeight;
				beginIndex = i;
				sub = null;
			}
		}
		if (beginIndex != s.length()) {
			String sub = s.substring(beginIndex, s.length());
			g.setColor(Color.WHITE);
			int textWidth = fm.stringWidth(sub);
			g.fillRect(curX - 2, curY - ascent, textWidth + 2, lineHeight);
			g.setColor(Color.BLUE);
			g.drawString(sub, curX, curY);
		}
	}

	protected void drawStringBG(Graphics g, String s, int curX, int curY) {
		FontMetrics fm = g.getFontMetrics();
		int lineHeight = fm.getHeight();
		int ascent = fm.getAscent();
		g.setColor(Color.WHITE);
		int textWidth = fm.stringWidth(s);
		g.fillRect(curX - 2, curY - ascent, textWidth + 2, lineHeight);
		g.setColor(Color.BLUE);
		g.drawString(s, curX, curY);
	}
}
