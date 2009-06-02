/**
 * Package level definition of adapters for JAXB 
 */
@XmlJavaTypeAdapters( {
		@XmlJavaTypeAdapter(value = PointAdapter.class, type = java.awt.Point.class),
		@XmlJavaTypeAdapter(value = DimensionAdapter.class, type = java.awt.Dimension.class) })
package tac.program.model;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapters;

import tac.program.jaxb.DimensionAdapter;
import tac.program.jaxb.PointAdapter;

