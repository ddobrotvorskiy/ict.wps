//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vhudson-jaxb-ri-2.1-792 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2009.10.06 at 05:25:05 PM MESZ 
//


package org.deegree.services.sos.configuration;

import javax.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.deegree.services.sos.configuration package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {


    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.deegree.services.sos.configuration
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link ServiceConfiguration.Offering }
     * 
     */
    public ServiceConfiguration.Offering createServiceConfigurationOffering() {
        return new ServiceConfiguration.Offering();
    }

    /**
     * Create an instance of {@link ServiceConfiguration.Offering.Procedure.FeatureOfInterest }
     * 
     */
    public ServiceConfiguration.Offering.Procedure.FeatureOfInterest createServiceConfigurationOfferingProcedureFeatureOfInterest() {
        return new ServiceConfiguration.Offering.Procedure.FeatureOfInterest();
    }

    /**
     * Create an instance of {@link Option }
     * 
     */
    public Option createOption() {
        return new Option();
    }

    /**
     * Create an instance of {@link ServiceConfiguration.Offering.Procedure.Sensor }
     * 
     */
    public ServiceConfiguration.Offering.Procedure.Sensor createServiceConfigurationOfferingProcedureSensor() {
        return new ServiceConfiguration.Offering.Procedure.Sensor();
    }

    /**
     * Create an instance of {@link ServiceConfiguration.Offering.Datastore.Connection }
     * 
     */
    public ServiceConfiguration.Offering.Datastore.Connection createServiceConfigurationOfferingDatastoreConnection() {
        return new ServiceConfiguration.Offering.Datastore.Connection();
    }

    /**
     * Create an instance of {@link ServiceConfiguration.Offering.Procedure }
     * 
     */
    public ServiceConfiguration.Offering.Procedure createServiceConfigurationOfferingProcedure() {
        return new ServiceConfiguration.Offering.Procedure();
    }

    /**
     * Create an instance of {@link ServiceConfiguration.Offering.Property }
     * 
     */
    public ServiceConfiguration.Offering.Property createServiceConfigurationOfferingProperty() {
        return new ServiceConfiguration.Offering.Property();
    }

    /**
     * Create an instance of {@link ServiceConfiguration.Offering.Datastore }
     * 
     */
    public ServiceConfiguration.Offering.Datastore createServiceConfigurationOfferingDatastore() {
        return new ServiceConfiguration.Offering.Datastore();
    }

    /**
     * Create an instance of {@link ServiceConfiguration }
     * 
     */
    public ServiceConfiguration createServiceConfiguration() {
        return new ServiceConfiguration();
    }

    /**
     * Create an instance of {@link Column }
     * 
     */
    public Column createColumn() {
        return new Column();
    }

}
