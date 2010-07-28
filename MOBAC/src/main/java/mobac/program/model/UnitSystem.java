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
package mobac.program.model;

public enum UnitSystem {

	Metric(6367.5, 1000, "km", "m"), Imperial(3963.192, 5280, "mi", "ft");

	public final double earthRadius;
	public final String unitLarge;
	public final String unitSmall;
	public final int unitFactor;
	public final double maxAngularDistSmall;

	private UnitSystem(double earthRadius, int unitFactor, String unitLarge, String unitSmall) {
		this.earthRadius = earthRadius;
		this.unitFactor = unitFactor;
		this.unitLarge = unitLarge;
		this.unitSmall = unitSmall;
		this.maxAngularDistSmall = 1 / (earthRadius * unitFactor);
	}

}
