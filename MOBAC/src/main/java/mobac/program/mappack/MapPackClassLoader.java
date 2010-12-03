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
package mobac.program.mappack;

import java.net.URL;
import java.net.URLClassLoader;

/**
 * Loads all classes below the package <code>mobac.mapsources.mappacks</code> from the map source packages and
 * everything else from the <code>fallback</code> {@link ClassLoader}. Therefore in difference to the standard parent
 * {@link ClassLoader} concept this implementation first tries to load the and then asks the fallback whereas usually it
 * is the opposite (first try to load via parent and only if that fails try to do it self).
 */
public class MapPackClassLoader extends URLClassLoader {

	private final String packageName;

	private final ClassLoader fallback;

	public MapPackClassLoader(String packageName, URL[] urls, ClassLoader fallback) {
		super(urls, null);
		this.packageName = packageName;
		this.fallback = fallback;
	}

	@Override
	public Class<?> loadClass(String name) throws ClassNotFoundException {
		if (name.startsWith(packageName)) {
			// System.out.println("Loading from map pack: " + name);
			return super.loadClass(name);
		} else {
			// System.out.println("Loading from fallback: " + name);
			return fallback.loadClass(name);
		}
	}

}
