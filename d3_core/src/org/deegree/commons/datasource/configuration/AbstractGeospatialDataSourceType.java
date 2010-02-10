//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vhudson-jaxb-ri-2.1-792 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2009.12.23 at 09:59:14 AM CET 
//


package org.deegree.commons.datasource.configuration;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * Base type of all data sources.
 * 
 * <p>Java class for AbstractGeospatialDataSourceType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="AbstractGeospatialDataSourceType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.deegree.org/datasource}AbstractDataSourceType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.deegree.org/datasource}BBoxConstraint" minOccurs="0"/>
 *         &lt;element ref="{http://www.deegree.org/datasource}ScaleConstraint" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AbstractGeospatialDataSourceType", propOrder = {
    "bBoxConstraint",
    "scaleConstraint"
})
@XmlSeeAlso({
    AbstractWebBasedDataSourceType.class,
    GeospatialFileSystemDataSourceType.class,
    ConstrainedDatabaseDataSourceType.class,
    MultiResolutionDataSource.class,
    RasterFileDataSourceType.class
})
public abstract class AbstractGeospatialDataSourceType
    extends AbstractDataSourceType
{

    @XmlElement(name = "BBoxConstraint")
    protected BBoxConstraint bBoxConstraint;
    @XmlElement(name = "ScaleConstraint")
    protected ScaleConstraint scaleConstraint;

    /**
     * Gets the value of the bBoxConstraint property.
     * 
     * @return
     *     possible object is
     *     {@link BBoxConstraint }
     *     
     */
    public BBoxConstraint getBBoxConstraint() {
        return bBoxConstraint;
    }

    /**
     * Sets the value of the bBoxConstraint property.
     * 
     * @param value
     *     allowed object is
     *     {@link BBoxConstraint }
     *     
     */
    public void setBBoxConstraint(BBoxConstraint value) {
        this.bBoxConstraint = value;
    }

    /**
     * Gets the value of the scaleConstraint property.
     * 
     * @return
     *     possible object is
     *     {@link ScaleConstraint }
     *     
     */
    public ScaleConstraint getScaleConstraint() {
        return scaleConstraint;
    }

    /**
     * Sets the value of the scaleConstraint property.
     * 
     * @param value
     *     allowed object is
     *     {@link ScaleConstraint }
     *     
     */
    public void setScaleConstraint(ScaleConstraint value) {
        this.scaleConstraint = value;
    }

}
