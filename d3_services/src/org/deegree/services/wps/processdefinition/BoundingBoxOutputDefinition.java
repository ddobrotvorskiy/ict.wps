//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vhudson-jaxb-ri-2.1-792 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2009.10.06 at 05:26:16 PM MESZ 
//


package org.deegree.services.wps.processdefinition;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * Description of a bounding box output parameter of the process.
 * 
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.deegree.org/services/wps}ProcessOutputType">
 *       &lt;sequence>
 *         &lt;element name="DefaultCRS" type="{http://www.w3.org/2001/XMLSchema}anyURI"/>
 *         &lt;element name="OtherCRS" type="{http://www.w3.org/2001/XMLSchema}anyURI" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "defaultCRS",
    "otherCRS"
})
public class BoundingBoxOutputDefinition
    extends ProcessletOutputDefinition
{

    @XmlElement(name = "DefaultCRS", required = true)
    @XmlSchemaType(name = "anyURI")
    protected String defaultCRS;
    @XmlElement(name = "OtherCRS")
    @XmlSchemaType(name = "anyURI")
    protected List<String> otherCRS;

    /**
     * Gets the value of the defaultCRS property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDefaultCRS() {
        return defaultCRS;
    }

    /**
     * Sets the value of the defaultCRS property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDefaultCRS(String value) {
        this.defaultCRS = value;
    }

    /**
     * Gets the value of the otherCRS property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the otherCRS property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getOtherCRS().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getOtherCRS() {
        if (otherCRS == null) {
            otherCRS = new ArrayList<String>();
        }
        return this.otherCRS;
    }

}
