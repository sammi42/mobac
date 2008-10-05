package moller.preview;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.openstreetmap.gui.jmapviewer.OsmFileCacheTileLoader;

public class MapPreview extends JPanel implements ChangeListener {

	private static final long serialVersionUID = 1L;

	PreviewMap map;

	JSlider gridZoomSlider;
	JLabel gridZoomLabel;

	public MapPreview() {
		super();
		setLayout(new BorderLayout());
		map = new PreviewMap();
		OsmFileCacheTileLoader tileLoader = new OsmFileCacheTileLoader(map);
		tileLoader.setTileCacheDir("tiles");
		map.setTileLoader(tileLoader);
		// if (gui.getMapPos() != null) {
		// map.setDisplayPositionByLatLon(gui.getMapPos().x, gui.getMapPos().y,
		// gui.getMapZoom());
		// }
		new PreviewMapController(map);
		JPanel topPanel = new JPanel();
		JPanel buttonPanel = new JPanel();
		JButton ok = new JButton("Ok");
		ok.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				takeOverValues();
			}
		});
		JButton cancel = new JButton("Cancel");
		cancel.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}
		});
		gridZoomSlider = new JSlider(1, 17);
		gridZoomSlider.addChangeListener(this);
		// try {
		// gridZoomSlider.setValue(gui.getZValue());
		// } catch (Exception e) {
		// }
		map.setGridZoom(gridZoomSlider.getValue());
		gridZoomLabel = new JLabel();
		stateChanged(null);
		buttonPanel.add(ok);
		buttonPanel.add(cancel);
		topPanel.add(gridZoomSlider);
		topPanel.add(gridZoomLabel);
		add(map, BorderLayout.CENTER);
		add(topPanel, BorderLayout.NORTH);
		add(buttonPanel, BorderLayout.SOUTH);
	}

	protected void takeOverValues() {
		/*int zoomDiff1 = PreviewMap.MAX_ZOOM - gridZoomSlider.getValue();
		if (map.iSelectionRectStart == null || map.iSelectionRectEnd == null)
			return;
		int x_min = (map.iSelectionRectStart.x >> zoomDiff1);
		int y_min = (map.iSelectionRectStart.y >> zoomDiff1);
		int x_max = (map.iSelectionRectEnd.x >> zoomDiff1);
		int y_max = (map.iSelectionRectEnd.y >> zoomDiff1);
		x_min = x_min >> 8 << 8;
		y_min = y_min >> 8 << 8;
		x_max = (x_max + 255) >> 8 << 8;
		y_max = (y_max + 255) >> 8 << 8;
		// gui.setValues(gridZoomSlider.getValue(), x_min, y_min, x_max, y_max);
		setVisible(false);*/
	}

	public void showDialog() {
		setVisible(true);
	}

	public void stateChanged(ChangeEvent e) {
		gridZoomLabel.setText("Zoom: " + gridZoomSlider.getValue());
		map.setGridZoom(gridZoomSlider.getValue());
		map.repaint();
	}

	public Point2D.Double getPosition() {
		return map.getPosition();
	}

}
