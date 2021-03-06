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
 * A raster file datasource can only use raster files (georeferenced, typed rasterfiles) from the file system.
 * 
 * <p>Java class for RasterFileDataSourceType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="RasterFileDataSourceType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.deegree.org/datasource}AbstractGeospatialDataSourceType">
 *       &lt;choice>
 *         &lt;element ref="{http://www.deegree.org/datasource}RasterFile"/>
 *         &lt;element ref="{http://www.deegree.org/datasource}RasterDirectory"/>
 *       &lt;/choice>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RasterFileDataSourceType", propOrder = {
    "rasterFile",
    "rasterDirectory"
})
@XmlSeeAlso({
    RasterDataSource.class
})
public abstract class RasterFileDataSourceType
    extends AbstractGeospatialDataSourceType
{

    @XmlElement(name = "RasterFile")
    protected RasterFileType rasterFile;
    @XmlElement(name = "RasterDirectory")
    protected RasterFileSetType rasterDirectory;

    /**
     * Gets the value of the rasterFile property.
     * 
     * @return
     *     possible object is
     *     {@link RasterFileType }
     *     
     */
    public RasterFileType getRasterFile() {
        return rasterFile;
    }

    /**
     * Sets the value of the rasterFile property.
     * 
     * @param value
     *     allowed object is
     *     {@link RasterFileType }
     *     
     */
    public void setRasterFile(RasterFileType value) {
        this.rasterFile = value;
    }

    /**
     * Gets the value of the rasterDirectory property.
     * 
     * @return
     *     possible object is
     *     {@link RasterFileSetType }
     *     
     */
    public RasterFileSetType getRasterDirectory() {
        return rasterDirectory;
    }

    /**
     * Sets the value of the rasterDirectory property.
     * 
     * @param value
     *     allowed object is
     *     {@link RasterFileSetType }
     *     
     */
    public void setRasterDirectory(RasterFileSetType value) {
        this.rasterDirectory = value;
    }

}
