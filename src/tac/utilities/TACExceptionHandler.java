package tac.utilities;

import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.HyperlinkEvent.EventType;

import tac.program.TACInfo;

public class TACExceptionHandler implements Thread.UncaughtExceptionHandler {

	public void uncaughtException(Thread t, Throwable e) {
		e.printStackTrace();
		showExceptionDialog(t, e);
	}

	private static String prop(String key) {
		String s = System.getProperty(key);
		if (s != null)
			return s;
		else
			return "";
	}

	public static void showExceptionDialog(Thread t, Throwable e) {
		String exceptionName = e.getClass().getSimpleName();
		try {
			StringBuilder sb = new StringBuilder();
			sb.append("\nVersion: " + TACInfo.getCompleteTitle());
			sb.append("\nPlatform: " + prop("os.name") + " (" + prop("os.version") + ")");
			sb.append("\nJava VM: " + prop("java.vm.name") + " (" + prop("java.version") + ")");

			StringWriter stack = new StringWriter();
			e.printStackTrace(new PrintWriter(stack));
			sb.append("\n\n" + stack.getBuffer().toString());

			JPanel p = new JPanel(new GridBagLayout());
			String url = "https://sourceforge.net/tracker/?group_id=238075&atid=1105494";
			String guiText = "" + "An unexpected exception occurred (" + exceptionName + ")<br>"
					+ "<p>Please report a ticket in the bug tracker " + "on <a href=\"" + url
					+ "\">SourceForge.net</a><br>"
					+ "Include your steps to get to the error (as detailed as possible)!</p>"
					+ "Be sure to include the following information:";
			JEditorPane text = new JEditorPane("text/html", "");
			text.setOpaque(true);
			text.setBackground(UIManager.getColor("JFrame.background"));
			text.setEditable(false);
			text.addHyperlinkListener(new HyperlinkListener() {

				public void hyperlinkUpdate(HyperlinkEvent e) {
					if (e.getEventType() != EventType.ACTIVATED)
						return;
					try {
						BrowserLauncher.openURL(e.getURL().toString());
					} catch (IOException e1) {
					}
				}
			});
			p.add(text, GBC.eol());
			try {
				Toolkit.getDefaultToolkit().getSystemClipboard().setContents(
						new StringSelection(sb.toString()), new ClipboardOwner() {
							public void lostOwnership(Clipboard clipboard, Transferable contents) {
							}
						});
				guiText += "<p>(The text has already been copied to your clipboard.)</p>";
			} catch (RuntimeException x) {
			}
			text.setText("<html>" + guiText + "</html>");

			JTextArea info = new JTextArea(sb.toString(), 20, 60);
			info.setCaretPosition(0);
			info.setEditable(false);
			p.add(new JScrollPane(info), GBC.eop());

			JOptionPane.showMessageDialog(null, p, "Unexpected Exception: " + exceptionName,
					JOptionPane.ERROR_MESSAGE);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	public static void main(String[] args) {
		try {
			throw new RuntimeException("Test");
		} catch (Exception e) {
			showExceptionDialog(Thread.currentThread(), e);
		}
	}
}
