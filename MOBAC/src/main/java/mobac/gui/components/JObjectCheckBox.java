package mobac.gui.components;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JCheckBox;

/**
 * An extended {@link JCheckBox} implementation that allows to link one user
 * object to the checkbox.
 * 
 * @param <E>
 *            type of the user object linked/stored with the checkbox
 */
public class JObjectCheckBox<E> extends JCheckBox {

	private static final long serialVersionUID = 1L;
	
	private E object;

	public JObjectCheckBox(Icon icon) {
		super(icon);
	}

	public JObjectCheckBox(String text) {
		super(text);
	}

	public JObjectCheckBox(Action a) {
		super(a);
	}

	public JObjectCheckBox(Icon icon, boolean selected) {
		super(icon, selected);
	}

	public JObjectCheckBox(String text, boolean selected) {
		super(text, selected);
	}

	public JObjectCheckBox(String text, Icon icon) {
		super(text, icon);
	}

	public JObjectCheckBox(String text, Icon icon, boolean selected) {
		super(text, icon, selected);
	}

	public E getObject() {
		return object;
	}

	public void setObject(E object) {
		this.object = object;
	}

}
