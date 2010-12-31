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
package mobac.exceptions;

import mobac.program.interfaces.MapInterface;

public class MapCreationException extends Exception {

	private static final long serialVersionUID = 1L;
	private MapInterface map;

	public MapCreationException(String message, MapInterface map, Throwable cause) {
		super(message, cause);
		this.map = map;
	}

	public MapCreationException(String message, MapInterface map) {
		super(message);
		this.map = map;
	}

	public MapCreationException(MapInterface map, Throwable cause) {
		super(cause);
		this.map = map;
	}

	@Override
	public String getMessage() {
		String s = super.getMessage();
		if (map != null) {
			s += "\n" + map.getInfoText();
		}
		return s;

	}

}
