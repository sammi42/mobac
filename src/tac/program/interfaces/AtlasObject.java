package tac.program.interfaces;

/**
 * Marker interface that indicates that the implementing class/instance is an
 * atlas or is part of an atlas (layer or map)
 */
public interface AtlasObject {

	/**
	 * Called after loading the complete atlas from a profile.
	 * 
	 * @return any problems found? <code>true</code>=yes
	 */
	public boolean checkData();
}
