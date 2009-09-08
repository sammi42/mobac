package tac.data.gpx.interfaces;

/**
 * Getter and setter methods of an GPX Waypoint element that are equal in GPX1.0
 * and GPX1.1
 */
public interface Wpt extends GpxPoint {

	/**
	 * @return Way point name
	 */
	public String getName();

	/**
	 * Sets the way point name
	 * 
	 * @param name
	 */
	public void setName(String name);

	/**
	 * @return Way point comment
	 */
	public String getCmt();

	/**
	 * Sets the way point comment
	 * 
	 * @param cmt
	 */
	public void setCmt(String cmt);

	/**
	 * @return Way point description
	 */
	public String getDesc();

	/**
	 * Sets the way point description
	 * 
	 * @param desc
	 */
	public void setDesc(String desc);
}
