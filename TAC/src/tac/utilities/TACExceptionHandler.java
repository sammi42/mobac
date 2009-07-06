package tac.utilities;

import java.awt.AWTEvent;
import java.awt.EventQueue;
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

import org.apache.log4j.Logger;

import tac.mapsources.MapSourcesManager;
import tac.program.TACInfo;

public class TACExceptionHandler implements Thread.UncaughtExceptionHandler {

	private static final TACExceptionHandler instance = new TACExceptionHandler();

	private static final Logger log = Logger.getLogger(TACExceptionHandler.class);

	static {
		Thread.setDefaultUncaughtExceptionHandler(instance);
	}

	public static void registerForCurrentThread() {
		Thread t = Thread.currentThread();
		log.trace("Registering TAC exception handler for thread \"" + t.getName() + "\" ["
				+ t.getId() + "]");
		t.setUncaughtExceptionHandler(instance);
	}

	public static TACExceptionHandler getInstance() {
		return instance;
	}

	private TACExceptionHandler() {
		super();
	}

	public void uncaughtException(Thread t, Throwable e) {
		processException(t, e);
	}

	public static void processException(Throwable e) {
		processException(Thread.currentThread(), e);
	}

	public static void processException(Thread t, Throwable e) {
		log.error("Uncaught exception: ", e);
		showExceptionDialog(e);
	}

	private static String prop(String key) {
		String s = System.getProperty(key);
		if (s != null)
			return s;
		else
			return "";
	}

	public static void showExceptionDialog(Throwable e) {
		String exceptionName = e.getClass().getSimpleName();
		try {
			StringBuilder sb = new StringBuilder();
			sb.append("\nVersion: " + TACInfo.getCompleteTitle());
			sb.append("\nPlatform: " + prop("os.name") + " (" + prop("os.version") + ")");
			String windowManager = System.getProperty("sun.desktop");
			if (windowManager != null)
				sb.append(" (" + windowManager + ")");

			String dist = OSUtilities.getLinuxDistributionName();
			if (dist != null)
				sb.append("\nDistribution name: " + dist);

			sb.append("\nJava VM: " + prop("java.vm.name") + " (" + prop("java.version") + ")");
			sb.append("\nMapsources rev: "
					+ MapSourcesManager.getMapSourcesRev(System.getProperties()));

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

	public static void installToolkitEventQueueProxy() {
		EventQueue queue = Toolkit.getDefaultToolkit().getSystemEventQueue();
		queue.push(new EventQueueProxy());
	}

	/**
	 * Catching all Runtime Exceptions in Swing
	 * 
	 * http://ruben42.wordpress.com/2009/03/30/catching-all-runtime-exceptions-
	 * in-swing/
	 */
	protected static class EventQueueProxy extends EventQueue {

		protected void dispatchEvent(AWTEvent newEvent) {
			try {
				super.dispatchEvent(newEvent);
			} catch (Throwable e) {
				TACExceptionHandler.processException(Thread.currentThread(), e);
			}
		}

	}

	public static void main(String[] args) {
		try {
			throw new RuntimeException("Test");
		} catch (Exception e) {
			showExceptionDialog(e);
		}
	}
}
