package tac.gui;

import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JTextArea;

import tac.mapsources.BeanShellMapSource;

public class CookieManager extends JFrame {

	JTextArea cookies;

	public CookieManager() {
		super("Cookie Manager");
		cookies = new JTextArea(8, 200);
		setMinimumSize(new Dimension(300,300));
		add(cookies);
	}

	public void updateCookies(BeanShellMapSource ms) {
		String t = cookies.getText();
		String[] lines = t.split("\\n");
		ms.getCookies().clear();
		for (String line : lines)
			ms.getCookies().add(line);
	}
}
