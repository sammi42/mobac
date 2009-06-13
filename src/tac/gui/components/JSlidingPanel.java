package tac.gui.components;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import tac.utilities.GBC;
import tac.utilities.Utilities;

/**
 * Test class implementing a nice sliding panel (incomplete)
 */
public class JSlidingPanel extends JPanel implements MouseListener {

	private static final long serialVersionUID = 1L;

	private JLabel showHideArrow;
	private JLabel titleLabel;
	private JPanel contentPanel;
	private JPanel emptyPanel;
	private static ImageIcon arrowUp = new ImageIcon();
	private static ImageIcon arrowDown = new ImageIcon();

	static {
		try {
			arrowUp = Utilities.loadResourceImageIcon("arrow_up.png");
			arrowDown = Utilities.loadResourceImageIcon("arrow_down.png");
		} catch (Exception e) {
		}
	}

	public JSlidingPanel(String title) {
		this(title, true);
	}

	public JSlidingPanel(String title, boolean defaultVisible) {
		super(new GridBagLayout());
		showHideArrow = new JLabel(arrowUp);
		showHideArrow.setMinimumSize(new Dimension(40, 40));
		showHideArrow.setPreferredSize(showHideArrow.getPreferredSize());
		titleLabel = new JLabel(title);
		titleLabel.addMouseListener(this);
		showHideArrow.addMouseListener(this);
		contentPanel = new JPanel();
		contentPanel.setBorder(BorderFactory.createTitledBorder("Selection coordinates (min/max)"));
		//contentPanel.setBorder(BorderFactory.createEtchedBorder());
		emptyPanel = new JPanel();
		emptyPanel.setBorder(contentPanel.getBorder());
		setContentPanelVisible(defaultVisible);
		super.add(showHideArrow, GBC.std().insets(5, 2, 5, 2));
		super.add(titleLabel, GBC.eol().fill(GBC.HORIZONTAL));
		super.add(contentPanel, GBC.std().gridwidth(2).fill());
		super.add(emptyPanel, GBC.std().gridwidth(2).fill());
	}

	public JPanel getContentPanel() {
		return contentPanel;
	}

	public void setContentPanelVisible(boolean visible) {
		contentPanel.setVisible(visible);
		emptyPanel.setVisible(!visible);
		if (contentPanel.isVisible()) {
			showHideArrow.setIcon(arrowUp);
		} else {
			showHideArrow.setIcon(arrowDown);
		}
	}

	public String getTitle() {
		return titleLabel.getText();
	}

	public void setTitle(String newTitle) {
		titleLabel.setText(newTitle);
	}

	@Override
	public Component add(Component comp, int index) {
		return contentPanel.add(comp, index);
	}

	@Override
	public void add(Component comp, Object constraints, int index) {
		contentPanel.add(comp, constraints, index);
	}

	@Override
	public void add(Component comp, Object constraints) {
		contentPanel.add(comp, constraints);
	}

	@Override
	public Component add(Component comp) {
		return contentPanel.add(comp);
	}

	@Override
	public Component add(String name, Component comp) {
		return contentPanel.add(name, comp);
	}

	public void mouseClicked(MouseEvent e) {
		if (e.getButton() != MouseEvent.BUTTON1)
			return;
		setContentPanelVisible(!contentPanel.isVisible());
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	public void mousePressed(MouseEvent e) {
	}

	public void mouseReleased(MouseEvent e) {
	}

}
