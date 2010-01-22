package mobac.gui.components;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

public class JDirectoryChooser extends JFileChooser {

	private static final long serialVersionUID = -1954689476383812988L;

	public JDirectoryChooser() {
		super();
		setDialogType(CUSTOM_DIALOG);
		setDialogTitle("Select Directory");
		//setApproveButtonText("Select Directory");
		setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		setAcceptAllFileFilterUsed(false);
		addChoosableFileFilter(new FileFilter() {

			@Override
			public boolean accept(File f) {
				return f.isDirectory();
			}

			@Override
			public String getDescription() {
				return "Directories";
			}
		});
	}

	@Override
	public void approveSelection() {
		if (!this.getFileFilter().accept(this.getSelectedFile()))
			return;
		super.approveSelection();
	}

}
