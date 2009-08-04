package tac.data.gpx.interfaces;

import java.util.List;


/**
 * Getter and setter methods of an GPX file that are same in GPX1.0 and GPX1.1
 */
public interface Gpx {

	public String getVersion();

	public String getCreator();
	
	public List<? extends Wpt> getWpt();
}
