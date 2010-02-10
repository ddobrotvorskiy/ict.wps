//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vhudson-jaxb-ri-2.1-792 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2009.10.06 at 05:25:23 PM MESZ 
//


package org.deegree.services.wcs.configuration;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * The value(s) of a given axis, either the nullvalue
 * 
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice>
 *         &lt;element name="SingleValue" type="{http://www.deegree.org/services/wcs}TypedType" maxOccurs="unbounded"/>
 *         &lt;element name="Interval" type="{http://www.deegree.org/services/wcs}IntervalType" maxOccurs="unbounded"/>
 *       &lt;/choice>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "singleValue",
    "interval"
})
@XmlRootElement(name = "AxisValue")
public class AxisValue {

    @XmlElement(name = "SingleValue")
    protected List<TypedType> singleValue;
    @XmlElement(name = "Interval")
    protected List<IntervalType> interval;

    /**
     * Gets the value of the singleValue property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the singleValue property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSingleValue().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TypedType }
     * 
     * 
     */
    public List<TypedType> getSingleValue() {
        if (singleValue == null) {
            singleValue = new ArrayList<TypedType>();
        }
        return this.singleValue;
    }

    /**
     * Gets the value of the interval property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the interval property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getInterval().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link IntervalType }
     * 
     * 
     */
    public List<IntervalType> getInterval() {
        if (interval == null) {
            interval = new ArrayList<IntervalType>();
        }
        return this.interval;
    }

}
