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
package mobac.ts_util;

public class TileStoreUtil {

	public static void main(String[] args) {
		testForMobacJar();
		Main.main(args);
	}

	private static void testForMobacJar() {
		try {
			Class.forName("mobac.StartMOBAC");
			return;
		} catch (ClassNotFoundException e) {
			System.out.println("Unable to find \"Mobile_Atlas_Creator.jar\".\n"
					+ "Please make sure that \"ts-util.jar\" is located in the same directory.");
			System.exit(1);
		}
	}
}
