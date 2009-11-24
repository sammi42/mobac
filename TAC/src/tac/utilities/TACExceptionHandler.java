package tac.utilities;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;

import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import org.apache.log4j.Logger;

import tac.mapsources.MapSourcesManager;
import tac.program.Logging;
import tac.program.TACInfo;

import com.sleepycat.je.ExceptionEvent;
import com.sleepycat.je.ExceptionListener;

public class TACExceptionHandler implements Thread.UncaughtExceptionHandler, ExceptionListener {

	private static final TACExceptionHandler instance = new TACExceptionHandler();

	private static final Logger log = Logger.getLogger(TACExceptionHandler.class);

	private static final double MB_DIV = 1024d * 1024d;

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

	public void exceptionThrown(ExceptionEvent paramExceptionEvent) {
		Exception e = paramExceptionEvent.getException();
		log.error("Exception in tile store: " + paramExceptionEvent.toString(), e);
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
			StringBuilder sb = new StringBuilder(1024);
			sb.append("Version: " + TACInfo.getCompleteTitle());
			sb.append("\nPlatform: " + prop("os.name") + " (" + prop("os.version") + ")");
			String windowManager = System.getProperty("sun.desktop");
			if (windowManager != null)
				sb.append(" (" + windowManager + ")");

			String dist = OSUtilities.getLinuxDistributionName();
			if (dist != null)
				sb.append("\nDistribution name: " + dist);

			sb.append("\nJava VM: " + prop("java.vm.name") + " (" + prop("java.runtime.version")
					+ ")");
			if (e.getClass().equals(java.lang.OutOfMemoryError.class)) {
				Runtime r = Runtime.getRuntime();
				sb.append(String.format("\nMax heap size: %3.2f MiB", r.maxMemory() / MB_DIV));
			}
			sb.append("\nMapsources rev: "
					+ MapSourcesManager.getMapSourcesRev(System.getProperties()));

			sb.append("\n\nError hierarchy:");
			Throwable tmp = e;
			while (tmp != null) {
				sb.append("\n  " + tmp.getClass().getSimpleName() + ": " + tmp.getMessage());
				tmp = tmp.getCause();
			}

			StringWriter stack = new StringWriter();
			e.printStackTrace(new PrintWriter(stack));
			sb.append("\n\n#############################################################\n\n");
			sb.append(stack.getBuffer().toString());
			sb.append("\n#############################################################");

			JPanel panel = new JPanel(new BorderLayout());
			String url = "http://sourceforge.net/tracker/?group_id=238075&atid=1105494";
			String guiText = "" + "An unexpected exception occurred (" + exceptionName + ")<br>"
					+ "<p>Please report a ticket in the bug tracker " + "on <a href=\"" + url
					+ "\">SourceForge.net</a><br>"
					+ "<b>Please include a detailed description of your performed actions <br>"
					+ "before the error occurred.</b></p>"
					+ "Be sure to include the following information:";
			JEditorPane text = new JEditorPane("text/html", "");
			text.setOpaque(true);
			text.setBackground(UIManager.getColor("JFrame.background"));
			text.setEditable(false);
			text.addHyperlinkListener(new HyperlinkListener() {

				public void hyperlinkUpdate(HyperlinkEvent e) {
					if (e.getEventType() != HyperlinkEvent.EventType.ACTIVATED)
						return;
					try {
						Desktop.getDesktop().browse(e.getURL().toURI());
					} catch (Exception e1) {
						log.error("", e1);
					}
				}
			});
			panel.add(text, BorderLayout.NORTH);
			try {
				StringSelection contents = new StringSelection(sb.toString());
				ClipboardOwner owner = new ClipboardOwner() {
					public void lostOwnership(Clipboard clipboard, Transferable contents) {
					}
				};
				Toolkit.getDefaultToolkit().getSystemClipboard().setContents(contents, owner);
				guiText += "<p>(The following text has already been copied to your clipboard.)</p>";
			} catch (RuntimeException x) {
				log.error("", x);
			}
			text.setText("<html>" + guiText + "</html>");

			JTextArea info = new JTextArea(sb.toString(), 20, 60);
			info.setCaretPosition(0);
			info.setEditable(false);
			info.setMinimumSize(new Dimension(200, 150));
			panel.add(new JScrollPane(info), BorderLayout.CENTER);
			panel.setMinimumSize(new Dimension(700, 300));
			panel.validate();
			JOptionPane.showMessageDialog(null, panel, "Unexpected Exception: " + exceptionName,
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
		for (;;) {
			ArrayList<byte[]> list = new ArrayList<byte[]>();
			try {
				Logging.configureConsoleLogging();
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
				for (int i = 0; i < 10000; i++) {
					list.add(new byte[300000]);
				}
				throw new RuntimeException("Test", new Exception("Inner"));
			} catch (Exception e) {
				showExceptionDialog(e);
			} catch (Error e) {
				showExceptionDialog(e);
			}
			break;
		}
	}
}
