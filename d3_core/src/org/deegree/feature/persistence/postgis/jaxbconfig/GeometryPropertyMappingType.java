//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vhudson-jaxb-ri-2.1-792 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2009.11.13 at 03:16:35 PM MEZ 
//


package org.deegree.feature.persistence.postgis.jaxbconfig;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for GeometryPropertyMappingType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="GeometryPropertyMappingType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.deegree.org/feature/featuretype}PropertyMappingType">
 *       &lt;choice>
 *         &lt;element ref="{http://www.deegree.org/feature/featuretype}GeometryDBColumn"/>
 *         &lt;element ref="{http://www.deegree.org/feature/featuretype}GeometryPropertyTable"/>
 *       &lt;/choice>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GeometryPropertyMappingType", propOrder = {
    "geometryDBColumn",
    "geometryPropertyTable"
})
public class GeometryPropertyMappingType
    extends PropertyMappingType
{

    @XmlElement(name = "GeometryDBColumn")
    protected GeometryDBColumn geometryDBColumn;
    @XmlElement(name = "GeometryPropertyTable")
    protected GeometryPropertyTable geometryPropertyTable;

    /**
     * Gets the value of the geometryDBColumn property.
     * 
     * @return
     *     possible object is
     *     {@link GeometryDBColumn }
     *     
     */
    public GeometryDBColumn getGeometryDBColumn() {
        return geometryDBColumn;
    }

    /**
     * Sets the value of the geometryDBColumn property.
     * 
     * @param value
     *     allowed object is
     *     {@link GeometryDBColumn }
     *     
     */
    public void setGeometryDBColumn(GeometryDBColumn value) {
        this.geometryDBColumn = value;
    }

    /**
     * Gets the value of the geometryPropertyTable property.
     * 
     * @return
     *     possible object is
     *     {@link GeometryPropertyTable }
     *     
     */
    public GeometryPropertyTable getGeometryPropertyTable() {
        return geometryPropertyTable;
    }

    /**
     * Sets the value of the geometryPropertyTable property.
     * 
     * @param value
     *     allowed object is
     *     {@link GeometryPropertyTable }
     *     
     */
    public void setGeometryPropertyTable(GeometryPropertyTable value) {
        this.geometryPropertyTable = value;
    }

}
