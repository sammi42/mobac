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
package mobac.gui.panels;

import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.TreeSet;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import mobac.gui.components.JCollapsiblePanel;
import mobac.gui.components.JTileSizeCombo;
import mobac.program.annotations.SupportedParameters;
import mobac.program.atlascreators.AtlasCreator;
import mobac.program.model.AtlasOutputFormat;
import mobac.program.model.Settings;
import mobac.program.model.TileImageFormat;
import mobac.program.model.TileImageParameters;
import mobac.program.model.TileImageParameters.Name;
import mobac.utilities.GBC;
import mobac.utilities.Utilities;

public class JTileImageParametersPanel extends JCollapsiblePanel {

	private static final long serialVersionUID = 1L;

	private JCheckBox enableCustomTileProcessingCheckButton;
	private JLabel tileSizeWidthLabel;
	private JLabel tileSizeHeightLabel;
	private JLabel tileImageFormatLabel;
	private JTileSizeCombo tileSizeWidth;
	private JTileSizeCombo tileSizeHeight;
	private JComboBox tileImageFormat;
	
	private boolean widthEnabled = true; 
	private boolean heightEnabled = true; 
	private boolean formatEnabled = true; 

	public JTileImageParametersPanel() {
		super("Layer settings: custom tile processing", new GridBagLayout());
		setName("TileImageParameters");

		enableCustomTileProcessingCheckButton = new JCheckBox("Recreate/adjust map tiles (CPU intensive)");
		enableCustomTileProcessingCheckButton.addActionListener(new EnableCustomTileSizeCheckButtonListener());
		enableCustomTileProcessingCheckButton.setToolTipText("<html>If this option is disabled each "
				+ "map tile (size: 256x256) is used axactly as downloaded " + "from the server (faster).<br>"
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

		JPanel tileSizePanel = new JPanel(new GridBagLayout());
		tileSizePanel.add(tileSizeWidthLabel, gbc_std);
		tileSizePanel.add(tileSizeWidth, gbc_std);
		tileSizePanel.add(tileSizeHeightLabel, gbc_std);
		tileSizePanel.add(tileSizeHeight, gbc_eol);
		JPanel tileColorDepthPanel = new JPanel();
		tileColorDepthPanel.add(tileImageFormatLabel);
		tileColorDepthPanel.add(tileImageFormat);
		contentContainer.add(enableCustomTileProcessingCheckButton, gbc_eol);
		contentContainer.add(tileSizePanel, GBC.eol());
		contentContainer.add(tileColorDepthPanel, GBC.eol());
	}

	public void loadSettings() {
		Settings settings = Settings.getInstance();
		tileImageFormat.setSelectedItem(settings.getTileImageFormat());
		enableCustomTileProcessingCheckButton.setSelected(settings.isCustomTileSize());
		tileSizeHeight.setValue(settings.getTileSize().height);
		tileSizeWidth.setValue(settings.getTileSize().width);
	}

	public void saveSettings() {
		Settings settings = Settings.getInstance();
		settings.setCustomTileSize(enableCustomTileProcessingCheckButton.isSelected());
		Dimension tileSize = new Dimension(tileSizeWidth.getValue(), tileSizeHeight.getValue());
		settings.setTileSize(tileSize);
		settings.setTileImageFormat((TileImageFormat) tileImageFormat.getSelectedItem());
	}

	public TileImageParameters getSelectedTileImageParameters() {
		TileImageParameters customTileParameters = null;
		boolean customTileSize = enableCustomTileProcessingCheckButton.isSelected();
		if (customTileSize) {
			int width = tileSizeWidth.getValue();
			int height = tileSizeHeight.getValue();
			TileImageFormat format = (mobac.program.model.TileImageFormat) tileImageFormat.getSelectedItem();
			customTileParameters = new TileImageParameters(width, height, format);
		}
		return customTileParameters;
	}

	public void atlasFormatChanged(AtlasOutputFormat newAtlasOutputFormat) {
		Class<? extends AtlasCreator> atlasCreatorClass = newAtlasOutputFormat.getMapCreatorClass();
		SupportedParameters params = atlasCreatorClass.getAnnotation(SupportedParameters.class);
		if (params != null) {
			TreeSet<TileImageParameters.Name> paramNames = new TreeSet<TileImageParameters.Name>(Arrays.asList(params.names()));
			formatEnabled = paramNames.contains(Name.format);
			widthEnabled = paramNames.contains(Name.width);
			heightEnabled = paramNames.contains(Name.height);
			enableCustomTileProcessingCheckButton.setEnabled(true);
		} else {
			formatEnabled = false;
			widthEnabled = false;
			heightEnabled = false;
			enableCustomTileProcessingCheckButton.setEnabled(false);
		}
		updateControlsState();
	}

	public void updateControlsState() {
		boolean b = false;
		if (enableCustomTileProcessingCheckButton.isEnabled())
			b = enableCustomTileProcessingCheckButton.isSelected();
		tileSizeWidth.setEnabled(b && widthEnabled);
		tileSizeWidthLabel.setEnabled(b && widthEnabled);
		tileSizeHeightLabel.setEnabled(b && heightEnabled);
		tileSizeHeight.setEnabled(b && heightEnabled);
		tileImageFormatLabel.setEnabled(b && formatEnabled);
		tileImageFormat.setEnabled(b && formatEnabled);
	}

	public String getValidationErrorMessages() {
		String errorText = "";
		if (!enableCustomTileProcessingCheckButton.isSelected())
			return errorText;
		if (!tileSizeHeight.isValueValid())
			errorText += "Value of \"Tile Size Height\" must be between " + JTileSizeCombo.MIN + " and "
					+ JTileSizeCombo.MAX + ". \n";

		if (!tileSizeWidth.isValueValid())
			errorText += "Value of \"Tile Size Width\" must be between " + JTileSizeCombo.MIN + " and "
					+ JTileSizeCombo.MAX + ". \n";
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
				JOptionPane.showMessageDialog(null,
						"<html>This image format is requires additional libraries to be installed:<br>"
								+ "<b>Java Advanced Image library</b>" + "(jai_core.jar & jai_codec.jar)<br>"
								+ "For more details please see the file <b>README.HTM</b> "
								+ "in section <b>Requirements</b>.</html>",
						"Image format not available - libraries missing", JOptionPane.ERROR_MESSAGE);
				tileImageFormat.setSelectedIndex(0);
			}
		}
	}

}
