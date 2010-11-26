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
package mobac.program.interfaces;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Marker interface for atlas creator specific. The implementing class can define on which types of objects in an atlas
 * it can be applied to:
 * <ul>
 * <li>only on maps ({@link MapInterface})</li>
 * <li>only on layers ({@link LayerInterface})</li>
 * <li>only on an whole atlas ({@link AtlasInterface})</li>
 * </ul>
 * <p>
 * Example: <br>
 * <code>class MyParameters implements AtlasCreatorParameters&lt;MapInterface&gt;</code><br>
 * This implementation defines a parameter class that can be applied on maps.
 * </p>
 */
@XmlRootElement
public interface AtlasCreatorParameters<T extends AtlasObject> extends Cloneable {

	/**
	 * 
	 * 
	 *
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target( { ElementType.METHOD })
	public @interface ACPInternalName {

		String name() default "##default";

	}
}
