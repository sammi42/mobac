package tac.data.gpx.interfaces;

import java.math.BigDecimal;

import javax.xml.datatype.XMLGregorianCalendar;

/**
 * Common getter and setter methods of a GPX point / coordinate. It has the
 * following members:
 * <ul>
 * <li><b>lat</b>itude</li>
 * <li><b>lon</b>gitude</li>
 * <li><b>ele</b>vation</li>
 * <li><b>time</b></li>
 * </ul>
 * 
 */
public interface GpxPoint {
	public BigDecimal getLon();

	public void setLon(BigDecimal lon);

	public BigDecimal getLat();

	public void setLat(BigDecimal lat);

	public BigDecimal getEle();

	public void setEle(BigDecimal ele);

	public XMLGregorianCalendar getTime();

	public void setTime(XMLGregorianCalendar time);

}
