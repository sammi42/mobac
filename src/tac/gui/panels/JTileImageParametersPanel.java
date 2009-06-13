package tac.gui.panels;

import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import tac.gui.components.JTileSizeCombo;
import tac.program.Settings;
import tac.program.model.TileImageFormat;
import tac.program.model.TileImageParameters;
import tac.utilities.GBC;
import tac.utilities.Utilities;

public class JTileImageParametersPanel extends JPanel {

	private JCheckBox enableCustomTileProcessingCheckButton;
	private JLabel tileSizeWidthLabel;
	private JLabel tileSizeHeightLabel;
	private JLabel tileImageFormatLabel;
	private JTileSizeCombo tileSizeWidth;
	private JTileSizeCombo tileSizeHeight;
	private JComboBox tileImageFormat;

	public JTileImageParametersPanel() {
		super(new GridBagLayout());
		setBorder(BorderFactory.createTitledBorder("Layer settings: custom tile processing"));

		enableCustomTileProcessingCheckButton = new JCheckBox(
				"Recreate/adjust map tiles (CPU intensive)");
		enableCustomTileProcessingCheckButton
				.addActionListener(new EnableCustomTileSizeCheckButtonListener());
		enableCustomTileProcessingCheckButton
				.setToolTipText("<html>If this option is disabled each "
						+ "map tile (size: 256x256) is used axactly as downloaded "
						+ "from the server (faster).<br>"
						+ "Otherwise each tile is newly created which allows to "
						+ "use custom tile size (slower / CPU intensive).</html>");

		tileSizeWidthLabel = new JLabel("Width:");
		tileSizeWidth = new JTileSizeCombo();
		tileSizeWidth.setToolTipText("Tile width");

		tileSizeHeightLabel = new JLabel("Height:");
		tileSizeHeight = new JTileSizeCombo();
		tileSizeHeight.setToolTipText("Tile height");

		tileImageFormatLabel = new JLabel("Tile format:");
		tileImageFormat = new JComboBox(TileImageFormat.values());
		tileImageFormat.setMaximumRowCount(tileImageFormat.getItemCount());
		tileImageFormat.addActionListener(new TileImageFormatListener());

		GBC gbc_std = GBC.std().insets(5, 2, 5, 3);
		GBC gbc_eol = GBC.eol().insets(5, 2, 5, 3);

		add(enableCustomTileProcessingCheckButton, gbc_eol);
		JPanel tileSizePanel = new JPanel(new GridBagLayout());
		tileSizePanel.add(tileSizeWidthLabel, gbc_std);
		tileSizePanel.add(tileSizeWidth, gbc_std);
		tileSizePanel.add(tileSizeHeightLabel, gbc_std);
		tileSizePanel.add(tileSizeHeight, gbc_eol);
		add(tileSizePanel, GBC.eol());
		JPanel tileColorDepthPanel = new JPanel();
		tileColorDepthPanel.add(tileImageFormatLabel);
		tileColorDepthPanel.add(tileImageFormat);
		add(tileColorDepthPanel, GBC.eol());
	}

	public void loadSettings() {
		Settings settings = Settings.getInstance();
		tileImageFormat.setSelectedItem(settings.getTileImageFormat());
		enableCustomTileProcessingCheckButton.setSelected(settings.isCustomTileSize());
		tileSizeHeight.setValue(settings.getTileHeight());
		tileSizeWidth.setValue(settings.getTileWidth());
	}

	public void saveSettings() {
		Settings settings = Settings.getInstance();
		settings.setCustomTileSize(enableCustomTileProcessingCheckButton.isSelected());
		settings.setTileWidth(tileSizeWidth.getValue());
		settings.setTileHeight(tileSizeHeight.getValue());
		settings.setTileImageFormat((TileImageFormat) tileImageFormat.getSelectedItem());
	}

	public TileImageParameters getSelectedTileImageParameters() {
		TileImageParameters customTileParameters = null;
		boolean customTileSize = enableCustomTileProcessingCheckButton.isSelected();
		if (customTileSize) {
			customTileParameters = new TileImageParameters();
			customTileParameters.width = tileSizeWidth.getValue();
			customTileParameters.height = tileSizeHeight.getValue();
			customTileParameters.format = (tac.program.model.TileImageFormat) tileImageFormat
					.getSelectedItem();
		}
		return customTileParameters;
	}

	public void updateControlsState() {
		boolean b = enableCustomTileProcessingCheckButton.isSelected();
		tileSizeWidthLabel.setEnabled(b);
		tileSizeHeightLabel.setEnabled(b);
		tileImageFormatLabel.setEnabled(b);
		tileSizeHeight.setEnabled(b);
		tileSizeWidth.setEnabled(b);
		tileImageFormat.setEnabled(b);
	}

	public String getValidationErrorMessages() {
		String errorText = new String();
		if (!enableCustomTileProcessingCheckButton.isSelected())
			return errorText;
		if (!tileSizeHeight.isValueValid())
			errorText += "Value of \"Tile Size Height\" must be between " + JTileSizeCombo.MIN
					+ " and " + JTileSizeCombo.MAX + ". \n";

		if (!tileSizeWidth.isValueValid())
			errorText += "Value of \"Tile Size Width\" must be between " + JTileSizeCombo.MIN
					+ " and " + JTileSizeCombo.MAX + ". \n";
		return errorText;
	}

	private class EnableCustomTileSizeCheckButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			updateControlsState();
		}
	}

	private class TileImageFormatListener implements ActionListener {

		public void actionPerformed(ActionEvent event) {
			TileImageFormat tif = (TileImageFormat) tileImageFormat.getSelectedItem();
			if (tif == TileImageFormat.PNG4Bit || tif == TileImageFormat.PNG8Bit) {
				if (Utilities.testJaiColorQuantizerAvailable())
					return;
				JOptionPane
						.showMessageDialog(null,
								"<html>This image format is requires additional libraries to be installed:<br>"
										+ "<b>Java Advanced Image library</b>"
										+ "(jai_core.jar & jai_codec.jar)<br>"
										+ "For more details please see the file <b>README.HTM</b> "
										+ "in section <b>Requirements</b>.</html>",
								"Image format not available - libraries missing",
								JOptionPane.ERROR_MESSAGE);
				tileImageFormat.setSelectedIndex(0);
			}
		}
	}

}
