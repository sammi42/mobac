package mobac.program.interfaces;

import mobac.exceptions.InvalidNameException;

/**
 * Marker interface that indicates that the implementing class/instance is an
 * atlas or is part of an atlas (layer or map)
 */
public interface AtlasObject {

	public String getName();

	public void setName(String newName) throws InvalidNameException;

	/**
	 * Called after loading the complete atlas from a profile.
	 * 
	 * @return any problems found? <code>true</code>=yes
	 */
	public boolean checkData();
}
