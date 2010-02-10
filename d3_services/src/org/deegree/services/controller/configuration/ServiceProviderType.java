//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vhudson-jaxb-ri-2.1-792 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2010.01.07 at 04:43:39 PM CET 
//


package org.deegree.services.controller.configuration;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ServiceProviderType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ServiceProviderType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="ProviderName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="ProviderSite" type="{http://www.w3.org/2001/XMLSchema}anyURI"/>
 *         &lt;element name="ServiceContact" type="{http://www.deegree.org/webservices}ServiceContactType"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ServiceProviderType", propOrder = {
    "providerName",
    "providerSite",
    "serviceContact"
})
public class ServiceProviderType {

    @XmlElement(name = "ProviderName", required = true)
    protected String providerName;
    @XmlElement(name = "ProviderSite", required = true)
    @XmlSchemaType(name = "anyURI")
    protected String providerSite;
    @XmlElement(name = "ServiceContact", required = true)
    protected ServiceContactType serviceContact;

    /**
     * Gets the value of the providerName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getProviderName() {
        return providerName;
    }

    /**
     * Sets the value of the providerName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setProviderName(String value) {
        this.providerName = value;
    }

    /**
     * Gets the value of the providerSite property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getProviderSite() {
        return providerSite;
    }

    /**
     * Sets the value of the providerSite property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setProviderSite(String value) {
        this.providerSite = value;
    }

    /**
     * Gets the value of the serviceContact property.
     * 
     * @return
     *     possible object is
     *     {@link ServiceContactType }
     *     
     */
    public ServiceContactType getServiceContact() {
        return serviceContact;
    }

    /**
     * Sets the value of the serviceContact property.
     * 
     * @param value
     *     allowed object is
     *     {@link ServiceContactType }
     *     
     */
    public void setServiceContact(ServiceContactType value) {
        this.serviceContact = value;
    }

}
