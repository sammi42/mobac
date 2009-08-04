package tac.data.gpx.gpx10;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;

import tac.data.gpx.interfaces.GpxPoint;

/**
 * <p>
 * Java class for anonymous complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base=&quot;{http://www.w3.org/2001/XMLSchema}anyType&quot;&gt;
 *       &lt;sequence&gt;
 *         &lt;element name=&quot;name&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}string&quot; minOccurs=&quot;0&quot;/&gt;
 *         &lt;element name=&quot;cmt&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}string&quot; minOccurs=&quot;0&quot;/&gt;
 *         &lt;element name=&quot;desc&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}string&quot; minOccurs=&quot;0&quot;/&gt;
 *         &lt;element name=&quot;src&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}string&quot; minOccurs=&quot;0&quot;/&gt;
 *         &lt;element name=&quot;url&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}anyURI&quot; minOccurs=&quot;0&quot;/&gt;
 *         &lt;element name=&quot;urlname&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}string&quot; minOccurs=&quot;0&quot;/&gt;
 *         &lt;element name=&quot;number&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}nonNegativeInteger&quot; minOccurs=&quot;0&quot;/&gt;
 *         &lt;any/&gt;
 *         &lt;element name=&quot;trkseg&quot; maxOccurs=&quot;unbounded&quot; minOccurs=&quot;0&quot;&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base=&quot;{http://www.w3.org/2001/XMLSchema}anyType&quot;&gt;
 *                 &lt;sequence&gt;
 *                   &lt;element name=&quot;trkpt&quot; maxOccurs=&quot;unbounded&quot; minOccurs=&quot;0&quot;&gt;
 *                     &lt;complexType&gt;
 *                       &lt;complexContent&gt;
 *                         &lt;restriction base=&quot;{http://www.w3.org/2001/XMLSchema}anyType&quot;&gt;
 *                           &lt;sequence&gt;
 *                             &lt;element name=&quot;ele&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}decimal&quot; minOccurs=&quot;0&quot;/&gt;
 *                             &lt;element name=&quot;time&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}dateTime&quot; minOccurs=&quot;0&quot;/&gt;
 *                             &lt;element name=&quot;course&quot; type=&quot;{http://www.topografix.com/GPX/1/0}degreesType&quot; minOccurs=&quot;0&quot;/&gt;
 *                             &lt;element name=&quot;speed&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}decimal&quot; minOccurs=&quot;0&quot;/&gt;
 *                             &lt;element name=&quot;magvar&quot; type=&quot;{http://www.topografix.com/GPX/1/0}degreesType&quot; minOccurs=&quot;0&quot;/&gt;
 *                             &lt;element name=&quot;geoidheight&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}decimal&quot; minOccurs=&quot;0&quot;/&gt;
 *                             &lt;element name=&quot;name&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}string&quot; minOccurs=&quot;0&quot;/&gt;
 *                             &lt;element name=&quot;cmt&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}string&quot; minOccurs=&quot;0&quot;/&gt;
 *                             &lt;element name=&quot;desc&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}string&quot; minOccurs=&quot;0&quot;/&gt;
 *                             &lt;element name=&quot;src&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}string&quot; minOccurs=&quot;0&quot;/&gt;
 *                             &lt;element name=&quot;url&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}anyURI&quot; minOccurs=&quot;0&quot;/&gt;
 *                             &lt;element name=&quot;urlname&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}string&quot; minOccurs=&quot;0&quot;/&gt;
 *                             &lt;element name=&quot;sym&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}string&quot; minOccurs=&quot;0&quot;/&gt;
 *                             &lt;element name=&quot;type&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}string&quot; minOccurs=&quot;0&quot;/&gt;
 *                             &lt;element name=&quot;fix&quot; type=&quot;{http://www.topografix.com/GPX/1/0}fixType&quot; minOccurs=&quot;0&quot;/&gt;
 *                             &lt;element name=&quot;sat&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}nonNegativeInteger&quot; minOccurs=&quot;0&quot;/&gt;
 *                             &lt;element name=&quot;hdop&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}decimal&quot; minOccurs=&quot;0&quot;/&gt;
 *                             &lt;element name=&quot;vdop&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}decimal&quot; minOccurs=&quot;0&quot;/&gt;
 *                             &lt;element name=&quot;pdop&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}decimal&quot; minOccurs=&quot;0&quot;/&gt;
 *                             &lt;element name=&quot;ageofdgpsdata&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}decimal&quot; minOccurs=&quot;0&quot;/&gt;
 *                             &lt;element name=&quot;dgpsid&quot; type=&quot;{http://www.topografix.com/GPX/1/0}dgpsStationType&quot; minOccurs=&quot;0&quot;/&gt;
 *                             &lt;any/&gt;
 *                           &lt;/sequence&gt;
 *                           &lt;attribute name=&quot;lat&quot; use=&quot;required&quot; type=&quot;{http://www.topografix.com/GPX/1/0}latitudeType&quot; /&gt;
 *                           &lt;attribute name=&quot;lon&quot; use=&quot;required&quot; type=&quot;{http://www.topografix.com/GPX/1/0}longitudeType&quot; /&gt;
 *                         &lt;/restriction&gt;
 *                       &lt;/complexContent&gt;
 *                     &lt;/complexType&gt;
 *                   &lt;/element&gt;
 *                 &lt;/sequence&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "name", "cmt", "desc", "src", "url", "urlname", "number", "any",
		"trkseg" })
public class TrkType {

	protected String name;
	protected String cmt;
	protected String desc;
	protected String src;
	@XmlSchemaType(name = "anyURI")
	protected String url;
	protected String urlname;
	@XmlSchemaType(name = "nonNegativeInteger")
	protected BigInteger number;
	@XmlAnyElement(lax = true)
	protected List<Object> any;
	protected List<TrkType.Trkseg> trkseg;

	/**
	 * Gets the value of the name property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the value of the name property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setName(String value) {
		this.name = value;
	}

	/**
	 * Gets the value of the cmt property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getCmt() {
		return cmt;
	}

	/**
	 * Sets the value of the cmt property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setCmt(String value) {
		this.cmt = value;
	}

	/**
	 * Gets the value of the desc property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getDesc() {
		return desc;
	}

	/**
	 * Sets the value of the desc property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setDesc(String value) {
		this.desc = value;
	}

	/**
	 * Gets the value of the src property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getSrc() {
		return src;
	}

	/**
	 * Sets the value of the src property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setSrc(String value) {
		this.src = value;
	}

	/**
	 * Gets the value of the url property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * Sets the value of the url property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setUrl(String value) {
		this.url = value;
	}

	/**
	 * Gets the value of the urlname property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getUrlname() {
		return urlname;
	}

	/**
	 * Sets the value of the urlname property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setUrlname(String value) {
		this.urlname = value;
	}

	/**
	 * Gets the value of the number property.
	 * 
	 * @return possible object is {@link BigInteger }
	 * 
	 */
	public BigInteger getNumber() {
		return number;
	}

	/**
	 * Sets the value of the number property.
	 * 
	 * @param value
	 *            allowed object is {@link BigInteger }
	 * 
	 */
	public void setNumber(BigInteger value) {
		this.number = value;
	}

	/**
	 * Gets the value of the any property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the any property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getAny().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list {@link Object }
	 * 
	 * 
	 */
	public List<Object> getAny() {
		if (any == null) {
			any = new ArrayList<Object>();
		}
		return this.any;
	}

	/**
	 * Gets the value of the trkseg property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the trkseg property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getTrkseg().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list
	 * {@link TrkType.Trkseg }
	 * 
	 * 
	 */
	public List<TrkType.Trkseg> getTrkseg() {
		if (trkseg == null) {
			trkseg = new ArrayList<TrkType.Trkseg>();
		}
		return this.trkseg;
	}

	/**
	 * <p>
	 * Java class for anonymous complex type.
	 * 
	 * <p>
	 * The following schema fragment specifies the expected content contained
	 * within this class.
	 * 
	 * <pre>
	 * &lt;complexType&gt;
	 *   &lt;complexContent&gt;
	 *     &lt;restriction base=&quot;{http://www.w3.org/2001/XMLSchema}anyType&quot;&gt;
	 *       &lt;sequence&gt;
	 *         &lt;element name=&quot;trkpt&quot; maxOccurs=&quot;unbounded&quot; minOccurs=&quot;0&quot;&gt;
	 *           &lt;complexType&gt;
	 *             &lt;complexContent&gt;
	 *               &lt;restriction base=&quot;{http://www.w3.org/2001/XMLSchema}anyType&quot;&gt;
	 *                 &lt;sequence&gt;
	 *                   &lt;element name=&quot;ele&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}decimal&quot; minOccurs=&quot;0&quot;/&gt;
	 *                   &lt;element name=&quot;time&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}dateTime&quot; minOccurs=&quot;0&quot;/&gt;
	 *                   &lt;element name=&quot;course&quot; type=&quot;{http://www.topografix.com/GPX/1/0}degreesType&quot; minOccurs=&quot;0&quot;/&gt;
	 *                   &lt;element name=&quot;speed&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}decimal&quot; minOccurs=&quot;0&quot;/&gt;
	 *                   &lt;element name=&quot;magvar&quot; type=&quot;{http://www.topografix.com/GPX/1/0}degreesType&quot; minOccurs=&quot;0&quot;/&gt;
	 *                   &lt;element name=&quot;geoidheight&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}decimal&quot; minOccurs=&quot;0&quot;/&gt;
	 *                   &lt;element name=&quot;name&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}string&quot; minOccurs=&quot;0&quot;/&gt;
	 *                   &lt;element name=&quot;cmt&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}string&quot; minOccurs=&quot;0&quot;/&gt;
	 *                   &lt;element name=&quot;desc&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}string&quot; minOccurs=&quot;0&quot;/&gt;
	 *                   &lt;element name=&quot;src&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}string&quot; minOccurs=&quot;0&quot;/&gt;
	 *                   &lt;element name=&quot;url&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}anyURI&quot; minOccurs=&quot;0&quot;/&gt;
	 *                   &lt;element name=&quot;urlname&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}string&quot; minOccurs=&quot;0&quot;/&gt;
	 *                   &lt;element name=&quot;sym&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}string&quot; minOccurs=&quot;0&quot;/&gt;
	 *                   &lt;element name=&quot;type&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}string&quot; minOccurs=&quot;0&quot;/&gt;
	 *                   &lt;element name=&quot;fix&quot; type=&quot;{http://www.topografix.com/GPX/1/0}fixType&quot; minOccurs=&quot;0&quot;/&gt;
	 *                   &lt;element name=&quot;sat&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}nonNegativeInteger&quot; minOccurs=&quot;0&quot;/&gt;
	 *                   &lt;element name=&quot;hdop&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}decimal&quot; minOccurs=&quot;0&quot;/&gt;
	 *                   &lt;element name=&quot;vdop&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}decimal&quot; minOccurs=&quot;0&quot;/&gt;
	 *                   &lt;element name=&quot;pdop&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}decimal&quot; minOccurs=&quot;0&quot;/&gt;
	 *                   &lt;element name=&quot;ageofdgpsdata&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}decimal&quot; minOccurs=&quot;0&quot;/&gt;
	 *                   &lt;element name=&quot;dgpsid&quot; type=&quot;{http://www.topografix.com/GPX/1/0}dgpsStationType&quot; minOccurs=&quot;0&quot;/&gt;
	 *                   &lt;any/&gt;
	 *                 &lt;/sequence&gt;
	 *                 &lt;attribute name=&quot;lat&quot; use=&quot;required&quot; type=&quot;{http://www.topografix.com/GPX/1/0}latitudeType&quot; /&gt;
	 *                 &lt;attribute name=&quot;lon&quot; use=&quot;required&quot; type=&quot;{http://www.topografix.com/GPX/1/0}longitudeType&quot; /&gt;
	 *               &lt;/restriction&gt;
	 *             &lt;/complexContent&gt;
	 *           &lt;/complexType&gt;
	 *         &lt;/element&gt;
	 *       &lt;/sequence&gt;
	 *     &lt;/restriction&gt;
	 *   &lt;/complexContent&gt;
	 * &lt;/complexType&gt;
	 * </pre>
	 * 
	 * 
	 */
	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlType(name = "", propOrder = { "trkpt" })
	public static class Trkseg {

		protected List<TrkType.Trkseg.Trkpt> trkpt;

		/**
		 * Gets the value of the trkpt property.
		 * 
		 * <p>
		 * This accessor method returns a reference to the live list, not a
		 * snapshot. Therefore any modification you make to the returned list
		 * will be present inside the JAXB object. This is why there is not a
		 * <CODE>set</CODE> method for the trkpt property.
		 * 
		 * <p>
		 * For example, to add a new item, do as follows:
		 * 
		 * <pre>
		 * getTrkpt().add(newItem);
		 * </pre>
		 * 
		 * 
		 * <p>
		 * Objects of the following type(s) are allowed in the list
		 * {@link TrkType.Trkseg.Trkpt }
		 * 
		 * 
		 */
		public List<TrkType.Trkseg.Trkpt> getTrkpt() {
			if (trkpt == null) {
				trkpt = new ArrayList<TrkType.Trkseg.Trkpt>();
			}
			return this.trkpt;
		}

		/**
		 * <p>
		 * Java class for anonymous complex type.
		 * 
		 * <p>
		 * The following schema fragment specifies the expected content
		 * contained within this class.
		 * 
		 * <pre>
		 * &lt;complexType&gt;
		 *   &lt;complexContent&gt;
		 *     &lt;restriction base=&quot;{http://www.w3.org/2001/XMLSchema}anyType&quot;&gt;
		 *       &lt;sequence&gt;
		 *         &lt;element name=&quot;ele&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}decimal&quot; minOccurs=&quot;0&quot;/&gt;
		 *         &lt;element name=&quot;time&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}dateTime&quot; minOccurs=&quot;0&quot;/&gt;
		 *         &lt;element name=&quot;course&quot; type=&quot;{http://www.topografix.com/GPX/1/0}degreesType&quot; minOccurs=&quot;0&quot;/&gt;
		 *         &lt;element name=&quot;speed&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}decimal&quot; minOccurs=&quot;0&quot;/&gt;
		 *         &lt;element name=&quot;magvar&quot; type=&quot;{http://www.topografix.com/GPX/1/0}degreesType&quot; minOccurs=&quot;0&quot;/&gt;
		 *         &lt;element name=&quot;geoidheight&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}decimal&quot; minOccurs=&quot;0&quot;/&gt;
		 *         &lt;element name=&quot;name&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}string&quot; minOccurs=&quot;0&quot;/&gt;
		 *         &lt;element name=&quot;cmt&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}string&quot; minOccurs=&quot;0&quot;/&gt;
		 *         &lt;element name=&quot;desc&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}string&quot; minOccurs=&quot;0&quot;/&gt;
		 *         &lt;element name=&quot;src&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}string&quot; minOccurs=&quot;0&quot;/&gt;
		 *         &lt;element name=&quot;url&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}anyURI&quot; minOccurs=&quot;0&quot;/&gt;
		 *         &lt;element name=&quot;urlname&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}string&quot; minOccurs=&quot;0&quot;/&gt;
		 *         &lt;element name=&quot;sym&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}string&quot; minOccurs=&quot;0&quot;/&gt;
		 *         &lt;element name=&quot;type&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}string&quot; minOccurs=&quot;0&quot;/&gt;
		 *         &lt;element name=&quot;fix&quot; type=&quot;{http://www.topografix.com/GPX/1/0}fixType&quot; minOccurs=&quot;0&quot;/&gt;
		 *         &lt;element name=&quot;sat&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}nonNegativeInteger&quot; minOccurs=&quot;0&quot;/&gt;
		 *         &lt;element name=&quot;hdop&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}decimal&quot; minOccurs=&quot;0&quot;/&gt;
		 *         &lt;element name=&quot;vdop&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}decimal&quot; minOccurs=&quot;0&quot;/&gt;
		 *         &lt;element name=&quot;pdop&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}decimal&quot; minOccurs=&quot;0&quot;/&gt;
		 *         &lt;element name=&quot;ageofdgpsdata&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}decimal&quot; minOccurs=&quot;0&quot;/&gt;
		 *         &lt;element name=&quot;dgpsid&quot; type=&quot;{http://www.topografix.com/GPX/1/0}dgpsStationType&quot; minOccurs=&quot;0&quot;/&gt;
		 *         &lt;any/&gt;
		 *       &lt;/sequence&gt;
		 *       &lt;attribute name=&quot;lat&quot; use=&quot;required&quot; type=&quot;{http://www.topografix.com/GPX/1/0}latitudeType&quot; /&gt;
		 *       &lt;attribute name=&quot;lon&quot; use=&quot;required&quot; type=&quot;{http://www.topografix.com/GPX/1/0}longitudeType&quot; /&gt;
		 *     &lt;/restriction&gt;
		 *   &lt;/complexContent&gt;
		 * &lt;/complexType&gt;
		 * </pre>
		 * 
		 * 
		 */
		@XmlAccessorType(XmlAccessType.FIELD)
		@XmlType(name = "", propOrder = { "ele", "time", "course", "speed", "magvar",
				"geoidheight", "name", "cmt", "desc", "src", "url", "urlname", "sym", "type",
				"fix", "sat", "hdop", "vdop", "pdop", "ageofdgpsdata", "dgpsid", "any" })
		public static class Trkpt implements GpxPoint {

			protected BigDecimal ele;
			protected XMLGregorianCalendar time;
			protected BigDecimal course;
			protected BigDecimal speed;
			protected BigDecimal magvar;
			protected BigDecimal geoidheight;
			protected String name;
			protected String cmt;
			protected String desc;
			protected String src;
			@XmlSchemaType(name = "anyURI")
			protected String url;
			protected String urlname;
			protected String sym;
			protected String type;
			protected String fix;
			@XmlSchemaType(name = "nonNegativeInteger")
			protected BigInteger sat;
			protected BigDecimal hdop;
			protected BigDecimal vdop;
			protected BigDecimal pdop;
			protected BigDecimal ageofdgpsdata;
			protected Integer dgpsid;
			@XmlAnyElement(lax = true)
			protected List<Object> any;
			@XmlAttribute(required = true)
			protected BigDecimal lat;
			@XmlAttribute(required = true)
			protected BigDecimal lon;

			/**
			 * Gets the value of the ele property.
			 * 
			 * @return possible object is {@link BigDecimal }
			 * 
			 */
			public BigDecimal getEle() {
				return ele;
			}

			/**
			 * Sets the value of the ele property.
			 * 
			 * @param value
			 *            allowed object is {@link BigDecimal }
			 * 
			 */
			public void setEle(BigDecimal value) {
				this.ele = value;
			}

			/**
			 * Gets the value of the time property.
			 * 
			 * @return possible object is {@link XMLGregorianCalendar }
			 * 
			 */
			public XMLGregorianCalendar getTime() {
				return time;
			}

			/**
			 * Sets the value of the time property.
			 * 
			 * @param value
			 *            allowed object is {@link XMLGregorianCalendar }
			 * 
			 */
			public void setTime(XMLGregorianCalendar value) {
				this.time = value;
			}

			/**
			 * Gets the value of the course property.
			 * 
			 * @return possible object is {@link BigDecimal }
			 * 
			 */
			public BigDecimal getCourse() {
				return course;
			}

			/**
			 * Sets the value of the course property.
			 * 
			 * @param value
			 *            allowed object is {@link BigDecimal }
			 * 
			 */
			public void setCourse(BigDecimal value) {
				this.course = value;
			}

			/**
			 * Gets the value of the speed property.
			 * 
			 * @return possible object is {@link BigDecimal }
			 * 
			 */
			public BigDecimal getSpeed() {
				return speed;
			}

			/**
			 * Sets the value of the speed property.
			 * 
			 * @param value
			 *            allowed object is {@link BigDecimal }
			 * 
			 */
			public void setSpeed(BigDecimal value) {
				this.speed = value;
			}

			/**
			 * Gets the value of the magvar property.
			 * 
			 * @return possible object is {@link BigDecimal }
			 * 
			 */
			public BigDecimal getMagvar() {
				return magvar;
			}

			/**
			 * Sets the value of the magvar property.
			 * 
			 * @param value
			 *            allowed object is {@link BigDecimal }
			 * 
			 */
			public void setMagvar(BigDecimal value) {
				this.magvar = value;
			}

			/**
			 * Gets the value of the geoidheight property.
			 * 
			 * @return possible object is {@link BigDecimal }
			 * 
			 */
			public BigDecimal getGeoidheight() {
				return geoidheight;
			}

			/**
			 * Sets the value of the geoidheight property.
			 * 
			 * @param value
			 *            allowed object is {@link BigDecimal }
			 * 
			 */
			public void setGeoidheight(BigDecimal value) {
				this.geoidheight = value;
			}

			/**
			 * Gets the value of the name property.
			 * 
			 * @return possible object is {@link String }
			 * 
			 */
			public String getName() {
				return name;
			}

			/**
			 * Sets the value of the name property.
			 * 
			 * @param value
			 *            allowed object is {@link String }
			 * 
			 */
			public void setName(String value) {
				this.name = value;
			}

			/**
			 * Gets the value of the cmt property.
			 * 
			 * @return possible object is {@link String }
			 * 
			 */
			public String getCmt() {
				return cmt;
			}

			/**
			 * Sets the value of the cmt property.
			 * 
			 * @param value
			 *            allowed object is {@link String }
			 * 
			 */
			public void setCmt(String value) {
				this.cmt = value;
			}

			/**
			 * Gets the value of the desc property.
			 * 
			 * @return possible object is {@link String }
			 * 
			 */
			public String getDesc() {
				return desc;
			}

			/**
			 * Sets the value of the desc property.
			 * 
			 * @param value
			 *            allowed object is {@link String }
			 * 
			 */
			public void setDesc(String value) {
				this.desc = value;
			}

			/**
			 * Gets the value of the src property.
			 * 
			 * @return possible object is {@link String }
			 * 
			 */
			public String getSrc() {
				return src;
			}

			/**
			 * Sets the value of the src property.
			 * 
			 * @param value
			 *            allowed object is {@link String }
			 * 
			 */
			public void setSrc(String value) {
				this.src = value;
			}

			/**
			 * Gets the value of the url property.
			 * 
			 * @return possible object is {@link String }
			 * 
			 */
			public String getUrl() {
				return url;
			}

			/**
			 * Sets the value of the url property.
			 * 
			 * @param value
			 *            allowed object is {@link String }
			 * 
			 */
			public void setUrl(String value) {
				this.url = value;
			}

			/**
			 * Gets the value of the urlname property.
			 * 
			 * @return possible object is {@link String }
			 * 
			 */
			public String getUrlname() {
				return urlname;
			}

			/**
			 * Sets the value of the urlname property.
			 * 
			 * @param value
			 *            allowed object is {@link String }
			 * 
			 */
			public void setUrlname(String value) {
				this.urlname = value;
			}

			/**
			 * Gets the value of the sym property.
			 * 
			 * @return possible object is {@link String }
			 * 
			 */
			public String getSym() {
				return sym;
			}

			/**
			 * Sets the value of the sym property.
			 * 
			 * @param value
			 *            allowed object is {@link String }
			 * 
			 */
			public void setSym(String value) {
				this.sym = value;
			}

			/**
			 * Gets the value of the type property.
			 * 
			 * @return possible object is {@link String }
			 * 
			 */
			public String getType() {
				return type;
			}

			/**
			 * Sets the value of the type property.
			 * 
			 * @param value
			 *            allowed object is {@link String }
			 * 
			 */
			public void setType(String value) {
				this.type = value;
			}

			/**
			 * Gets the value of the fix property.
			 * 
			 * @return possible object is {@link String }
			 * 
			 */
			public String getFix() {
				return fix;
			}

			/**
			 * Sets the value of the fix property.
			 * 
			 * @param value
			 *            allowed object is {@link String }
			 * 
			 */
			public void setFix(String value) {
				this.fix = value;
			}

			/**
			 * Gets the value of the sat property.
			 * 
			 * @return possible object is {@link BigInteger }
			 * 
			 */
			public BigInteger getSat() {
				return sat;
			}

			/**
			 * Sets the value of the sat property.
			 * 
			 * @param value
			 *            allowed object is {@link BigInteger }
			 * 
			 */
			public void setSat(BigInteger value) {
				this.sat = value;
			}

			/**
			 * Gets the value of the hdop property.
			 * 
			 * @return possible object is {@link BigDecimal }
			 * 
			 */
			public BigDecimal getHdop() {
				return hdop;
			}

			/**
			 * Sets the value of the hdop property.
			 * 
			 * @param value
			 *            allowed object is {@link BigDecimal }
			 * 
			 */
			public void setHdop(BigDecimal value) {
				this.hdop = value;
			}

			/**
			 * Gets the value of the vdop property.
			 * 
			 * @return possible object is {@link BigDecimal }
			 * 
			 */
			public BigDecimal getVdop() {
				return vdop;
			}

			/**
			 * Sets the value of the vdop property.
			 * 
			 * @param value
			 *            allowed object is {@link BigDecimal }
			 * 
			 */
			public void setVdop(BigDecimal value) {
				this.vdop = value;
			}

			/**
			 * Gets the value of the pdop property.
			 * 
			 * @return possible object is {@link BigDecimal }
			 * 
			 */
			public BigDecimal getPdop() {
				return pdop;
			}

			/**
			 * Sets the value of the pdop property.
			 * 
			 * @param value
			 *            allowed object is {@link BigDecimal }
			 * 
			 */
			public void setPdop(BigDecimal value) {
				this.pdop = value;
			}

			/**
			 * Gets the value of the ageofdgpsdata property.
			 * 
			 * @return possible object is {@link BigDecimal }
			 * 
			 */
			public BigDecimal getAgeofdgpsdata() {
				return ageofdgpsdata;
			}

			/**
			 * Sets the value of the ageofdgpsdata property.
			 * 
			 * @param value
			 *            allowed object is {@link BigDecimal }
			 * 
			 */
			public void setAgeofdgpsdata(BigDecimal value) {
				this.ageofdgpsdata = value;
			}

			/**
			 * Gets the value of the dgpsid property.
			 * 
			 * @return possible object is {@link Integer }
			 * 
			 */
			public Integer getDgpsid() {
				return dgpsid;
			}

			/**
			 * Sets the value of the dgpsid property.
			 * 
			 * @param value
			 *            allowed object is {@link Integer }
			 * 
			 */
			public void setDgpsid(Integer value) {
				this.dgpsid = value;
			}

			/**
			 * Gets the value of the any property.
			 * 
			 * <p>
			 * This accessor method returns a reference to the live list, not a
			 * snapshot. Therefore any modification you make to the returned
			 * list will be present inside the JAXB object. This is why there is
			 * not a <CODE>set</CODE> method for the any property.
			 * 
			 * <p>
			 * For example, to add a new item, do as follows:
			 * 
			 * <pre>
			 * getAny().add(newItem);
			 * </pre>
			 * 
			 * 
			 * <p>
			 * Objects of the following type(s) are allowed in the list
			 * {@link Object }
			 * 
			 * 
			 */
			public List<Object> getAny() {
				if (any == null) {
					any = new ArrayList<Object>();
				}
				return this.any;
			}

			/**
			 * Gets the value of the lat property.
			 * 
			 * @return possible object is {@link BigDecimal }
			 * 
			 */
			public BigDecimal getLat() {
				return lat;
			}

			/**
			 * Sets the value of the lat property.
			 * 
			 * @param value
			 *            allowed object is {@link BigDecimal }
			 * 
			 */
			public void setLat(BigDecimal value) {
				this.lat = value;
			}

			/**
			 * Gets the value of the lon property.
			 * 
			 * @return possible object is {@link BigDecimal }
			 * 
			 */
			public BigDecimal getLon() {
				return lon;
			}

			/**
			 * Sets the value of the lon property.
			 * 
			 * @param value
			 *            allowed object is {@link BigDecimal }
			 * 
			 */
			public void setLon(BigDecimal value) {
				this.lon = value;
			}

		}

	}

}
