package tac.data.gpx.gpx10;

import javax.xml.bind.annotation.XmlRegistry;

import tac.data.gpx.common.BoundsType;

/**
 * This object contains factory methods for each Java content interface and Java
 * element interface generated in the tac.data.gpx.gpx10 package.
 * <p>
 * An ObjectFactory allows you to programatically construct new instances of the
 * Java representation for XML content. The Java representation of XML content
 * can consist of schema derived interfaces and classes representing the binding
 * of schema type definitions, element declarations and model groups. Factory
 * methods for each of these are provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

	/**
	 * Create a new ObjectFactory that can be used to create new instances of
	 * schema derived classes for package: tac.data.gpx.gpx10
	 * 
	 */
	public ObjectFactory() {
	}

	/**
	 * Create an instance of {@link Gpx10.Wpt }
	 * 
	 */
	public WptType createGpxWpt() {
		return new WptType();
	}

	/**
	 * Create an instance of {@link TrkType }
	 * 
	 */
	public TrkType createGpxTrk() {
		return new TrkType();
	}

	/**
	 * Create an instance of {@link BoundsType }
	 * 
	 */
	public BoundsType createBoundsType() {
		return new BoundsType();
	}

	/**
	 * Create an instance of {@link TrkType.Trkseg }
	 * 
	 */
	public TrkType.Trkseg createGpxTrkTrkseg() {
		return new TrkType.Trkseg();
	}

	/**
	 * Create an instance of {@link Gpx10.Rte }
	 * 
	 */
	public RteType createGpxRte() {
		return new RteType();
	}

	/**
	 * Create an instance of {@link Gpx10 }
	 * 
	 */
	public Gpx10 createGpx() {
		return new Gpx10();
	}

	/**
	 * Create an instance of {@link RteType.Rtept }
	 * 
	 */
	public RteType.Rtept createGpxRteRtept() {
		return new RteType.Rtept();
	}

	/**
	 * Create an instance of {@link TrkType.Trkseg.Trkpt }
	 * 
	 */
	public TrkType.Trkseg.Trkpt createGpxTrkTrksegTrkpt() {
		return new TrkType.Trkseg.Trkpt();
	}

}
