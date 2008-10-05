package tac.gui;

import java.awt.Window;
import java.awt.Component;
import java.awt.Container;
import java.awt.Frame;

import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JToolBar;

public class UpdateGUI{

	/**
		 * Method to attempt a dynamic update for any GUI accessible by this JVM. It will
		 * filter through all frames and sub-components of the frames.
		 */
		public static void updateAllUIs() {
			Frame frames[];
			frames = Frame.getFrames();
			 
			for (int i = 0; i < frames.length; i++)	{
				updateWindowUI(frames[i]);
			}
		}
	 
		/**
		 * Method to attempt a dynamic update for all components of the given <code>Window</code>.
		 * @param window The <code>Window</code> for which the look and feel update has to be performed against.
		 */
		public static void updateWindowUI(Window window) {
			try	{
				updateComponentTreeUI(window);
			} catch(Exception exception) { }
	 
			Window windows[] = window.getOwnedWindows();
	 
			for (int i = 0; i < windows.length; i++)
				updateWindowUI(windows[i]);
		}
	 
		/**
		 * A simple minded look and feel change: ask each node in the tree
		 * to <code>updateUI()</code> -- that is, to initialize its UI property
		 * with the current look and feel.
		 *
		 * Based on the Sun SwingUtilities.updateComponentTreeUI, but ensures that
		 * the update happens on the components of a JToolbar before the JToolbar
		 * itself.
		 */
		public static void updateComponentTreeUI(Component c) {
			updateComponentTreeUI0(c);
			c.invalidate();
			c.validate();
			c.repaint();
		}
	 
		private static void updateComponentTreeUI0(Component c) {
	 
			Component[] children = null;
	 
			if (c instanceof JToolBar) {
				children = ((JToolBar)c).getComponents();
	 
				if (children != null) {
					for(int i = 0; i < children.length; i++) {
						updateComponentTreeUI0(children[i]);
					}
				}
				((JComponent) c).updateUI();
			} else {
				if (c instanceof JComponent) {
					((JComponent) c).updateUI();
				}
	 			if (c instanceof JMenu) {
					children = ((JMenu)c).getMenuComponents();
				}
				else if (c instanceof Container) {
					children = ((Container)c).getComponents();
				}
	 			if (children != null) {
					for(int i = 0; i < children.length; i++) {
						updateComponentTreeUI0(children[i]);
					}
				}
			}
		}
	}