package tac.data.gpx.interfaces;

/**
 * Getter and setter methods of an GPX Waypoint element that are same in GPX1.0
 * and GPX1.1
 */
public interface Wpt extends GpxPoint {

	public String getName();

	public void setName(String name);

	public String getCmt();

	public void setCmt(String cmt);

	public String getDesc();

	public void setDesc(String desc);
}
