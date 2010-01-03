package tac.gui.components;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.TimerTask;

import org.openstreetmap.gui.jmapviewer.MapGridLayer;

import tac.gui.mapview.PreviewMap;

public class LogPreviewMap extends PreviewMap {

	private final LinkedList<LogEntry> logLines = new LinkedList<LogEntry>();
	private Font logFont = new Font("Sans Serif", Font.BOLD, 14);
	private Font tileInfoFont = new Font("Sans Serif", Font.BOLD, 12);

	// private Timer timer = new Timer();

	public LogPreviewMap() {
		super();
		setZoomContolsVisible(true);
		setTileGridVisible(true);
		mapGridLayer = new MapGridInfoLayer();
		// timer.schedule(new LogRemoverTimerTask(), 0, 500);
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
		}
	}
}
