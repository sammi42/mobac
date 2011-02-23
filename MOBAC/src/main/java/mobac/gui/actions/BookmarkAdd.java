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
package mobac.gui.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import mobac.gui.components.JBookmarkMenuItem;
import mobac.gui.mapview.PreviewMap;
import mobac.program.model.Bookmark;

public class BookmarkAdd implements ActionListener {

	private final PreviewMap previewMap;
	private final JMenu bookmarks;

	public BookmarkAdd(PreviewMap previewMap, JMenu bookmarks) {
		this.previewMap = previewMap;
		this.bookmarks = bookmarks;
	}

	public void actionPerformed(ActionEvent arg0) {
		Bookmark bm = previewMap.getPositionBookmark();
		JMenuItem newItem = new JBookmarkMenuItem(bm);
		bookmarks.add(newItem);
	}

}
