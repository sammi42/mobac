package featuretests;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import mobac.mapsources.impl.Google;
import mobac.program.Logging;
import mobac.program.tilestore.TileStore;
import mobac.program.tilestore.berkeleydb.BerkeleyDbTileStore;
import mobac.program.tilestore.berkeleydb.DelayedInterruptThread;

public class TileCoverageTest {

	public static class MyThread extends DelayedInterruptThread {

		public MyThread() {
			super("Test");
		}

		@Override
		public void run() {
			BerkeleyDbTileStore tileStore = (BerkeleyDbTileStore) TileStore.getInstance();
			try {
				int zoom = 8;
				BufferedImage image = tileStore.getCacheCoverage(new Google.GoogleMaps(), zoom,
						new Point(80, 85), new Point(1 << zoom, 1 << zoom));
				ImageIO.write(image, "png", new File("test.png"));
				JFrame f = new JFrame("Example");
				f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				f.getContentPane().add(
						new JLabel(new ImageIcon(image.getScaledInstance(image.getWidth() * 4,
								image.getHeight() * 4, 0))));
				f.pack();
				f.setLocationRelativeTo(null);
				f.setVisible(true);
			} catch (Exception e) {
				e.printStackTrace();
			}
			tileStore.closeAll(true);
		}

	}

	public static void main(String[] args) throws InterruptedException {
		Logging.configureConsoleLogging();

		TileStore.initialize();
		Thread t = new MyThread();
		t.start();
		t.join();
	}

}
