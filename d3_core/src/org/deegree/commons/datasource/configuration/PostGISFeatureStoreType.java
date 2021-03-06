//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vhudson-jaxb-ri-2.1-792 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2009.12.23 at 09:59:14 AM CET 
//


package org.deegree.commons.datasource.configuration;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;
import org.deegree.commons.configuration.GMLVersionType;


/**
 * <p>Java class for PostGISFeatureStoreType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="PostGISFeatureStoreType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.deegree.org/datasource}FeatureStoreType">
 *       &lt;sequence>
 *         &lt;element name="StorageSRS" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="JDBCConnId" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="DBSchemaQualifier" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="GMLSchemaFileURL" maxOccurs="unbounded">
 *           &lt;complexType>
 *             &lt;simpleContent>
 *               &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema>string">
 *                 &lt;attribute name="gmlVersion" use="required" type="{http://www.deegree.org/commons}GMLVersionType" />
 *               &lt;/extension>
 *             &lt;/simpleContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PostGISFeatureStoreType", propOrder = {
    "storageSRS",
    "jdbcConnId",
    "dbSchemaQualifier",
    "gmlSchemaFileURL"
})
public class PostGISFeatureStoreType
    extends FeatureStoreType
{

    @XmlElement(name = "StorageSRS", required = true)
    protected String storageSRS;
    @XmlElement(name = "JDBCConnId", required = true)
    protected String jdbcConnId;
    @XmlElement(name = "DBSchemaQualifier")
    protected String dbSchemaQualifier;
    @XmlElement(name = "GMLSchemaFileURL", required = true)
    protected List<PostGISFeatureStoreType.GMLSchemaFileURL> gmlSchemaFileURL;

    /**
     * Gets the value of the storageSRS property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getStorageSRS() {
        return storageSRS;
    }

    /**
     * Sets the value of the storageSRS property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStorageSRS(String value) {
        this.storageSRS = value;
    }

    /**
     * Gets the value of the jdbcConnId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getJDBCConnId() {
        return jdbcConnId;
    }

    /**
     * Sets the value of the jdbcConnId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setJDBCConnId(String value) {
        this.jdbcConnId = value;
    }

    /**
     * Gets the value of the dbSchemaQualifier property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDBSchemaQualifier() {
        return dbSchemaQualifier;
    }

    /**
     * Sets the value of the dbSchemaQualifier property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDBSchemaQualifier(String value) {
        this.dbSchemaQualifier = value;
    }

    /**
     * Gets the value of the gmlSchemaFileURL property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the gmlSchemaFileURL property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getGMLSchemaFileURL().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link PostGISFeatureStoreType.GMLSchemaFileURL }
     * 
     * 
     */
    public List<PostGISFeatureStoreType.GMLSchemaFileURL> getGMLSchemaFileURL() {
        if (gmlSchemaFileURL == null) {
            gmlSchemaFileURL = new ArrayList<PostGISFeatureStoreType.GMLSchemaFileURL>();
        }
        return this.gmlSchemaFileURL;
    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;simpleContent>
     *     &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema>string">
     *       &lt;attribute name="gmlVersion" use="required" type="{http://www.deegree.org/commons}GMLVersionType" />
     *     &lt;/extension>
     *   &lt;/simpleContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "value"
    })
    public static class GMLSchemaFileURL {

        @XmlValue
        protected String value;
        @XmlAttribute(required = true)
        protected GMLVersionType gmlVersion;

        /**
         * Gets the value of the value property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getValue() {
            return value;
        }

        /**
         * Sets the value of the value property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setValue(String value) {
            this.value = value;
        }

        /**
         * Gets the value of the gmlVersion property.
         * 
         * @return
         *     possible object is
         *     {@link GMLVersionType }
         *     
         */
        public GMLVersionType getGmlVersion() {
            return gmlVersion;
        }

        /**
         * Sets the value of the gmlVersion property.
         * 
         * @param value
         *     allowed object is
         *     {@link GMLVersionType }
         *     
         */
        public void setGmlVersion(GMLVersionType value) {
            this.gmlVersion = value;
        }

    }

}
