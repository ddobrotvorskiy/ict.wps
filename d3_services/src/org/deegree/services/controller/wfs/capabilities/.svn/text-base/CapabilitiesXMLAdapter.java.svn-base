//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
 Department of Geography, University of Bonn
 and
 lat/lon GmbH

 This library is free software; you can redistribute it and/or modify it under
 the terms of the GNU Lesser General Public License as published by the Free
 Software Foundation; either version 2.1 of the License, or (at your option)
 any later version.
 This library is distributed in the hope that it will be useful, but WITHOUT
 ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 details.
 You should have received a copy of the GNU Lesser General Public License
 along with this library; if not, write to the Free Software Foundation, Inc.,
 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

 Contact information:

 lat/lon GmbH
 Aennchenstr. 19, 53177 Bonn
 Germany
 http://lat-lon.de/

 Department of Geography, University of Bonn
 Prof. Dr. Klaus Greve
 Postfach 1147, 53001 Bonn
 Germany
 http://www.geographie.uni-bonn.de/deegree/

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package org.deegree.services.controller.wfs.capabilities;

import static org.deegree.protocol.wfs.WFSConstants.VERSION_100;
import static org.deegree.protocol.wfs.WFSConstants.VERSION_110;
import static org.deegree.protocol.wfs.WFSConstants.VERSION_200;
import static org.deegree.protocol.wfs.WFSConstants.WFS_100_CAPABILITIES_SCHEMA_URL;
import static org.deegree.protocol.wfs.WFSConstants.WFS_110_SCHEMA_URL;
import static org.deegree.protocol.wfs.WFSConstants.WFS_200_NS;
import static org.deegree.protocol.wfs.WFSConstants.WFS_200_SCHEMA_URL;
import static org.deegree.protocol.wfs.WFSConstants.WFS_NS;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.deegree.commons.types.ows.Version;
import org.deegree.commons.xml.CommonNamespaces;
import org.deegree.crs.CRS;
import org.deegree.feature.persistence.StoredFeatureTypeMetadata;
import org.deegree.filter.xml.FilterCapabilitiesExporter;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.GeometryTransformer;
import org.deegree.geometry.SimpleGeometryFactory;
import org.deegree.geometry.primitive.Point;
import org.deegree.protocol.wfs.WFSConstants.WFSRequestType;
import org.deegree.services.controller.OGCFrontController;
import org.deegree.services.controller.configuration.DCPType;
import org.deegree.services.controller.configuration.DeegreeServicesMetadata;
import org.deegree.services.controller.configuration.ServiceIdentificationType;
import org.deegree.services.controller.ows.capabilities.OWSCapabilitiesXMLAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generates WFS <code>GetCapabilities</code> response documents.
 * <p>
 * Supported WFS protocol versions:
 * <ul>
 * <li>1.0.0 (in implementation)</li>
 * <li>1.1.0 (in implementation)</li>
 * <li>2.0.0 (in implementation, tentative)</li>
 * </ul>
 * </p>
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 * 
 * @version $Revision:$, $Date:$
 */
public class CapabilitiesXMLAdapter extends OWSCapabilitiesXMLAdapter {

    private static Logger LOG = LoggerFactory.getLogger( CapabilitiesXMLAdapter.class );

    private static final String OGC_NS = "http://www.opengis.net/ogc";

    private static final String OGC_PREFIX = "ogc";

    private static final String GML_NS = "http://www.opengis.net/gml";

    private static final String GML_PREFIX = "gml";

    private static final String WFS_PREFIX = "wfs";

    private static GeometryTransformer transformer;

    static {
        try {
            transformer = new GeometryTransformer( "EPSG:4326" );
        } catch ( Exception e ) {
            LOG.error( "Could not initialize GeometryTransformer." );
        }
    }

    /**
     * Produces a <code>WFS_Capabilities</code> document compliant to the specified WFS version.
     * 
     * @param version
     *            requested version (must be 1.0.0, 1.1.0 or 2.0.0)
     * @param xmlWriter
     * @param mainControllerConf
     * @param servedFts
     *            served feature types (in document order)
     * @param sections
     *            uppercased names of the sections to be exported or null (export all sections)
     * @throws XMLStreamException
     * @throws IllegalArgumentException
     *             if the specified version is not 1.0.0, 1.1.0 or 2.0.0
     */
    public static void export( Version version, XMLStreamWriter xmlWriter, DeegreeServicesMetadata mainControllerConf,
                               StoredFeatureTypeMetadata[] servedFts, Set<String> sections )
                            throws XMLStreamException {
        if ( VERSION_100.equals( version ) ) {
            export100( xmlWriter, mainControllerConf, servedFts );
        } else if ( VERSION_110.equals( version ) ) {
            export110( xmlWriter, mainControllerConf, servedFts, sections );
        } else if ( VERSION_200.equals( version ) ) {
            export200( xmlWriter, mainControllerConf, servedFts, sections );
        } else {
            throw new IllegalArgumentException( "Version '" + version + "' is not supported." );
        }
    }

    /**
     * Produces a <code>WFS_Capabilities</code> document that complies to the WFS 1.0.0 specification.
     * 
     * @param writer
     * @param serviceMetadata
     * @param servedFts
     *            served feature types (in document order)
     * @throws XMLStreamException
     */
    public static void export100( XMLStreamWriter writer, DeegreeServicesMetadata serviceMetadata,
                                  StoredFeatureTypeMetadata[] servedFts )
                            throws XMLStreamException {
        writer.setPrefix( WFS_PREFIX, WFS_NS );
        writer.setPrefix( "ows", OWS_NS );
        writer.setPrefix( OGC_PREFIX, OGC_NS );
        writer.setPrefix( GML_PREFIX, GML_NS );
        writer.setPrefix( "xlink", XLN_NS );

        writer.writeStartDocument();
        writer.writeStartElement( WFS_NS, "WFS_Capabilities" );
        writer.writeAttribute( "version", "1.0.0" );
        writer.writeAttribute( "xsi", CommonNamespaces.XSINS, "schemaLocation", WFS_NS + " "
                                                                                + WFS_100_CAPABILITIES_SCHEMA_URL );

        // wfs:Service (type="wfs:ServiceType")
        exportService( writer, serviceMetadata );

        // wfs:Capability (type="wfs:CapabilityType")
        exportCapability( writer, serviceMetadata );

        // wfs:FeatureTypeList (type="wfs:FeatureTypeListType")
        writer.writeStartElement( WFS_NS, "FeatureTypeList" );

        exportOperations( writer );

        for ( StoredFeatureTypeMetadata ft : servedFts ) {

            // wfs:FeatureType
            writer.writeStartElement( WFS_NS, "FeatureType" );

            // wfs:Name
            writer.writeStartElement( WFS_NS, "Name" );
            QName ftName = ft.getType().getName();

            if ( ftName.getNamespaceURI() != XMLConstants.NULL_NS_URI ) {
                String prefix = ftName.getPrefix();
                if ( ftName.getPrefix() == null || ftName.getPrefix().equals( "" ) ) {
                    LOG.warn( "Feature type '" + ftName + "' has no prefix!? This should not happen." );
                    prefix = "app";
                }
                writer.setPrefix( prefix, ftName.getNamespaceURI() );
                writer.writeCharacters( prefix + ":" + ftName.getLocalPart() );
            } else {
                writer.writeCharacters( ftName.getLocalPart() );
            }
            writer.writeEndElement();

            // wfs:Title (minOccurs=0, maxOccurs=1)
            writer.writeStartElement( WFS_NS, "Title" );
            writer.writeCharacters( ft.getTitle() );
            writer.writeEndElement();

            // wfs:Abstract (minOccurs=0, maxOccurs=1)
            if ( ft.getAbstract() != null ) {
                writer.writeStartElement( WFS_NS, "Abstract" );
                writer.writeCharacters( ft.getAbstract() );
                writer.writeEndElement();
            }

            // wfs:Keywords (minOccurs=0, maxOccurs=1)
            if ( ft.getKeywords() != null ) {
                writer.writeStartElement( WFS_NS, "Keywords" );
                writer.writeCharacters( ft.getKeywords() );
                writer.writeEndElement();
            }

            // wfs:SRS (minOccurs=1, maxOccurs=1)
            writer.writeStartElement( WFS_NS, "SRS" );
            if ( ft.getDefaultCRS() == null ) {
                LOG.warn( "No default CRS for feature type '" + ftName + "' defined, using 'EPSG:4326'." );
                writer.writeCharacters( "EPSG:4326" );
            } else {
                writer.writeCharacters( ft.getDefaultCRS().getName() );
            }
            writer.writeEndElement();

            // wfs:LatLongBoundingBox (minOccurs=0, maxOccurs=unbounded)
            Envelope env = ft.getStore().getEnvelope( ftName );
            if ( env != null ) {
                try {
                    env = (Envelope) transformer.transform( env );
                    writer.writeStartElement( WFS_NS, "LatLongBoundingBox" );
                    Point min = env.getMin();
                    Point max = env.getMax();
                    writer.writeAttribute( "minx", "" + min.get1() );
                    writer.writeAttribute( "miny", "" + min.get0() );
                    writer.writeAttribute( "maxx", "" + max.get1() );
                    writer.writeAttribute( "maxy", "" + max.get0() );
                    writer.writeEndElement();
                } catch ( Exception e ) {
                    LOG.error( "Cannot transform feature type envelope to WGS84." );
                }
            }
            writer.writeEndElement();
        }
        writer.writeEndElement();

        // ogc:Filter_Capabilities
        FilterCapabilitiesExporter.export100( writer );

        writer.writeEndElement();
        writer.writeEndDocument();
    }

    private static void exportOperations( XMLStreamWriter writer )
                            throws XMLStreamException {
        writer.writeStartElement( WFS_NS, "Operations" );
        writer.writeEmptyElement( WFS_NS, "Insert" );
        writer.writeEmptyElement( WFS_NS, "Update" );
        writer.writeEmptyElement( WFS_NS, "Delete" );
        writer.writeEmptyElement( WFS_NS, "Query" );
        writer.writeEmptyElement( WFS_NS, "Lock" );
        writer.writeEndElement();
    }

    /**
     * Produces a <code>WFS_Capabilities</code> document that complies to the WFS 1.1.0 specification.
     * 
     * @param writer
     * @param serviceMetadata
     * @param servedFts
     *            served feature types (in document order)
     * @param sections
     *            uppercased names of the sections to be exported or null (export all sections)
     * @throws XMLStreamException
     */
    public static void export110( XMLStreamWriter writer, DeegreeServicesMetadata serviceMetadata,
                                  StoredFeatureTypeMetadata[] servedFts, Set<String> sections )
                            throws XMLStreamException {

        writer.setPrefix( WFS_PREFIX, WFS_NS );
        writer.setPrefix( OWS_PREFIX, OWS_NS );
        writer.setPrefix( OGC_PREFIX, OGC_NS );
        writer.setPrefix( GML_PREFIX, GML_NS );
        writer.setPrefix( "xlink", XLN_NS );

        writer.writeStartDocument();
        writer.writeStartElement( WFS_NS, "WFS_Capabilities" );
        writer.writeAttribute( "version", "1.1.0" );
        writer.writeAttribute( "xsi", CommonNamespaces.XSINS, "schemaLocation", WFS_NS + " " + WFS_110_SCHEMA_URL );

        // ows:ServiceIdentification
        if ( sections == null || sections.contains( "SERVICEIDENTIFICATION" ) ) {
            List<Version> serviceVersions = new ArrayList<Version>();
            serviceVersions.add( Version.parseVersion( "1.0.0" ) );
            exportServiceIdentification100( writer, serviceMetadata.getServiceIdentification(), "WFS", serviceVersions );
        }

        // ows:ServiceProvider
        if ( sections == null || sections.contains( "SERVICEPROVIDER" ) ) {
            exportServiceProvider100( writer, serviceMetadata.getServiceProvider() );
        }

        // ows:OperationsMetadata
        if ( sections == null || sections.contains( "OPERATIONSMETADATA" ) ) {
            List<String> operations = new LinkedList<String>();
            operations.add( WFSRequestType.DescribeFeatureType.name() );
            operations.add( WFSRequestType.GetCapabilities.name() );
            operations.add( WFSRequestType.GetFeature.name() );
            operations.add( WFSRequestType.GetFeatureWithLock.name() );
            operations.add( WFSRequestType.GetGmlObject.name() );
            operations.add( WFSRequestType.LockFeature.name() );
            operations.add( WFSRequestType.Transaction.name() );
            // TODO
            DCPType dcp = new DCPType();
            dcp.setHTTPGet( OGCFrontController.getHttpGetURL() );
            dcp.setHTTPPost( OGCFrontController.getHttpPostURL() );
            exportOperationsMetadata100( writer, operations, dcp );
        }

        // wfs:FeatureTypeList
        if ( sections == null || sections.contains( "FEATURETYPELIST" ) ) {
            writer.writeStartElement( WFS_NS, "FeatureTypeList" );
            for ( StoredFeatureTypeMetadata ft : servedFts ) {
                QName ftName = ft.getType().getName();
                writer.writeStartElement( WFS_NS, "FeatureType" );
                // wfs:Name
                writer.writeStartElement( WFS_NS, "Name" );
                String prefix = ftName.getPrefix();
                if ( prefix == null || prefix.equals( "" ) ) {
                    LOG.warn( "Feature type '" + ftName + "' has no prefix!? This should not happen." );
                    prefix = "app";
                }
                if ( ftName.getNamespaceURI() != XMLConstants.NULL_NS_URI ) {
                    // TODO what about the namespace prefix?
                    writer.setPrefix( prefix, ftName.getNamespaceURI() );
                    writer.writeCharacters( prefix + ":" + ftName.getLocalPart() );
                } else {
                    writer.writeCharacters( ftName.getLocalPart() );
                }
                writer.writeEndElement();

                // wfs:Title
                writer.writeStartElement( WFS_NS, "Title" );
                writer.writeCharacters( ft.getTitle() );
                writer.writeEndElement();

                // wfs:Abstract (minOccurs=0, maxOccurs=1)
                writer.writeStartElement( WFS_NS, "Abstract" );
                writer.writeCharacters( ft.getAbstract() );
                writer.writeEndElement();

                // ows:Keywords (minOccurs=0, maxOccurs=unbounded)
                // writer.writeStartElement( OWS_NS, "Keywords" );
                // writer.writeCharacters( "keywords" );
                // writer.writeEndElement();

                // wfs:DefaultSRS / wfs:NoSRS
                writeElement( writer, WFS_NS, "DefaultSRS", getCRSNameAsURI( ft.getDefaultCRS() ) );

                // ows:WGS84BoundingBox (minOccurs=0, maxOccurs=unbounded)
                Envelope env = ft.getStore().getEnvelope( ftName );
                if ( env != null ) {
                    try {
                        env = (Envelope) transformer.transform( env );
                    } catch ( Exception e ) {
                        LOG.error( "Cannot transform feature type envelope to WGS84." );
                    }
                } else {
                    env = new SimpleGeometryFactory().createEnvelope( -180, -90, 180, 90, new CRS( "EPSG:4326" ) );
                }

                writer.writeStartElement( OWS_NS, "WGS84BoundingBox" );
                Point min = env.getMin();
                Point max = env.getMax();
                writer.writeStartElement( OWS_NS, "LowerCorner" );
                writer.writeCharacters( min.get1() + " " + min.get0() );
                writer.writeEndElement();
                writer.writeStartElement( OWS_NS, "UpperCorner" );
                writer.writeCharacters( max.get1() + " " + max.get0() );
                writer.writeEndElement();
                writer.writeEndElement();

                // TODO Operations, OutputFormats, ...

                writer.writeEndElement();
            }
            writer.writeEndElement();
        }

        // wfs:ServesGMLObjectTypeList
        if ( sections == null || sections.contains( "SERVESGMLOBJECTTYPELIST" ) ) {
            // TODO
        }

        // wfs:SupportsGMLObjectTypeList
        if ( sections == null || sections.contains( "SUPPORTSGMLOBJECTTYPELIST" ) ) {
            // TODO
        }

        // 'ogc:Filter_Capabilities' (mandatory)
        FilterCapabilitiesExporter.export110( writer );

        writer.writeEndElement();
        writer.writeEndDocument();
    }

    // TODO this should become an integral part of the CRS API
    private static String getCRSNameAsURI( CRS crs ) {
        String name = crs.getName();
        name = "urn:x-ogc:def:crs:" + name;
        return name;
    }

    /**
     * Produces a <code>WFS_Capabilities</code> document that complies to the WFS 2.0.0 specification (tentative).
     * 
     * @param writer
     * @param serviceMetadata
     * @param servedFts
     *            served feature types (in document order)
     * @param sections
     *            uppercased names of the sections to be exported or null (export all sections)
     * @throws XMLStreamException
     */
    public static void export200( XMLStreamWriter writer, DeegreeServicesMetadata serviceMetadata,
                                  StoredFeatureTypeMetadata[] servedFts, Set<String> sections )
                            throws XMLStreamException {

        writer.setPrefix( WFS_PREFIX, WFS_200_NS );
        writer.setPrefix( "ows", OWS_NS );
        writer.setPrefix( OGC_PREFIX, OGC_NS );
        writer.setPrefix( GML_PREFIX, GML_NS );
        writer.setPrefix( "xlink", XLN_NS );

        writer.writeStartDocument();
        writer.writeStartElement( WFS_200_NS, "WFS_Capabilities" );
        writer.writeAttribute( "version", "2.0.0" );
        writer.writeAttribute( "xsi", CommonNamespaces.XSINS, "schemaLocation", WFS_200_NS + " " + WFS_200_SCHEMA_URL );

        // ows:ServiceIdentification
        if ( sections.size() == 0 || sections.contains( "ServiceIdentification" ) ) {
            // exportServiceIdentification( writer );
        }

        // ows:ServiceProvider
        if ( sections.size() == 0 || sections.contains( "ServiceProvider" ) ) {
            exportServiceProvider100( writer, serviceMetadata.getServiceProvider() );
        }

        // ows:OperationsMetadata
        if ( sections.size() == 0 || sections.contains( "OperationsMetadata" ) ) {
            List<String> operations = new LinkedList<String>();
            operations.add( WFSRequestType.DescribeFeatureType.name() );
            operations.add( WFSRequestType.GetCapabilities.name() );
            operations.add( WFSRequestType.GetFeature.name() );
            operations.add( WFSRequestType.GetFeatureWithLock.name() );
            operations.add( WFSRequestType.GetGmlObject.name() );
            operations.add( WFSRequestType.LockFeature.name() );
            operations.add( WFSRequestType.Transaction.name() );
            exportOperationsMetadata100( writer, operations, serviceMetadata.getDCP() );
        }

        // wfs:FeatureTypeList
        if ( sections.size() == 0 || sections.contains( "FeatureTypeList" ) ) {
            // for ( QName ftName : servedFts ) {
            // writer.writeStartElement( WFS_200_NS, "FeatureType" );
            // // wfs:Name
            // writer.writeStartElement( WFS_200_NS, "Name" );
            // if ( ftName.getNamespaceURI() != XMLConstants.NULL_NS_URI ) {
            // // TODO evaluate namespace prefix generation strategies
            // writer.writeAttribute( "xmlns:app", ftName.getNamespaceURI() );
            // writer.writeCharacters( "app:" + ftName.getLocalPart() );
            // } else {
            // writer.writeCharacters( ftName.getLocalPart() );
            // }
            // writer.writeEndElement();
            //
            // // wfs:Title
            // writer.writeStartElement( WFS_200_NS, "Title" );
            // writer.writeCharacters( "title" );
            // writer.writeEndElement();
            //
            // // wfs:Abstract (minOccurs=0, maxOccurs=1)
            // writer.writeStartElement( WFS_200_NS, "Abstract" );
            // writer.writeCharacters( "abstract" );
            // writer.writeEndElement();
            //
            // // wfs:Keywords (minOccurs=0, maxOccurs=1)
            // writer.writeStartElement( WFS_200_NS, "Keywords" );
            // writer.writeCharacters( "keywords" );
            // writer.writeEndElement();
            //
            // // TODO Operations, OutputFormats, ...
            //
            // writer.writeEndElement();
            // }
        }

        // wfs:ServesGMLObjectTypeList
        if ( sections.size() == 0 || sections.contains( "ServesGMLObjectTypeList" ) ) {
            // TODO
        }

        // wfs:SupportsGMLObjectTypeList
        if ( sections.size() == 0 || sections.contains( "SupportsGMLObjectTypeList" ) ) {
            // TODO
        }

        // ogc:Filter_Capabilities
        if ( sections.size() == 0 || sections.contains( "Filter_Capabilities" ) ) {
            // TODO
        }

        writer.writeEndElement();
        writer.writeEndDocument();
    }

    private static void exportService( XMLStreamWriter writer, DeegreeServicesMetadata serviceMetadata )
                            throws XMLStreamException {

        ServiceIdentificationType serviceId = serviceMetadata.getServiceIdentification();

        writer.writeStartElement( WFS_NS, "Service" );

        // wfs:Name (type="string")
        writeElement( writer, WFS_NS, "Name", "d3_wfs" );

        // wfs:Title (type="string)
        writeElement( writer, WFS_NS, "Title", "deegree 3 WFS 1.0.0" );

        // wfs:Abstract
        writeElement( writer, WFS_NS, "Abstract", "abstract" );

        // wfs:Keywords

        // wfs:OnlineResource (type=???)
        writeElement( writer, WFS_NS, "OnlineResource",
                      serviceMetadata.getServiceProvider().getServiceContact().getOnlineResource() );

        // wfs:Fees
        if ( serviceId != null && serviceId.getFees() != null ) {
            writeElement( writer, WFS_NS, "Fees", serviceId.getFees() );
        }

        // wfs:AccessConstraints

        writer.writeEndElement();
    }

    private static void exportCapability( XMLStreamWriter writer, DeegreeServicesMetadata serviceMetadata )
                            throws XMLStreamException {

        writer.writeStartElement( WFS_NS, "Capability" );
        writer.writeStartElement( WFS_NS, "Request" );

        String getURL = OGCFrontController.getHttpGetURL();
        String postURL = OGCFrontController.getHttpPostURL();

        // wfs:GetCapabilities
        writer.writeStartElement( WFS_NS, WFSRequestType.GetCapabilities.name() );
        exportGetDCPType( writer, getURL );
        exportPostDCPType( writer, postURL );
        writer.writeEndElement();

        // wfs:DescribeFeatureType
        writer.writeStartElement( WFS_NS, WFSRequestType.DescribeFeatureType.name() );
        writer.writeStartElement( WFS_NS, "SchemaDescriptionLanguage" );
        writer.writeStartElement( WFS_NS, "XMLSCHEMA" );
        writer.writeEndElement();
        writer.writeEndElement();
        exportGetDCPType( writer, getURL );
        exportPostDCPType( writer, postURL );
        writer.writeEndElement();

        // wfs:Transaction
        writer.writeStartElement( WFS_NS, WFSRequestType.Transaction.name() );
        exportGetDCPType( writer, getURL );
        exportPostDCPType( writer, postURL );
        writer.writeEndElement();

        // wfs:GetFeature
        writer.writeStartElement( WFS_NS, WFSRequestType.GetFeature.name() );
        writer.writeStartElement( WFS_NS, "ResultFormat" );
        writer.writeStartElement( WFS_NS, "GML2" );
        writer.writeEndElement();
        writer.writeEndElement();
        exportGetDCPType( writer, getURL );
        exportPostDCPType( writer, postURL );
        writer.writeEndElement();

        // wfs:GetFeatureWithLock
        writer.writeStartElement( WFS_NS, WFSRequestType.GetFeatureWithLock.name() );
        writer.writeStartElement( WFS_NS, "ResultFormat" );
        writer.writeStartElement( WFS_NS, "GML2" );
        writer.writeEndElement();
        writer.writeEndElement();
        exportGetDCPType( writer, getURL );
        exportPostDCPType( writer, postURL );
        writer.writeEndElement();

        // wfs:LockFeature
        writer.writeStartElement( WFS_NS, WFSRequestType.LockFeature.name() );
        exportGetDCPType( writer, getURL );
        exportPostDCPType( writer, postURL );
        writer.writeEndElement();

        writer.writeEndElement();
        writer.writeEndElement();
    }

    private static void exportGetDCPType( XMLStreamWriter writer, String getURL )
                            throws XMLStreamException {

        if ( getURL != null ) {
            writer.writeStartElement( WFS_NS, "DCPType" );
            writer.writeStartElement( WFS_NS, "HTTP" );

            // ows:Get (type="ows:GetType")
            writer.writeStartElement( WFS_NS, "Get" );
            writer.writeAttribute( "onlineResource", getURL );
            writer.writeEndElement();

            writer.writeEndElement();
            writer.writeEndElement();
        }
    }

    private static void exportPostDCPType( XMLStreamWriter writer, String postURL )
                            throws XMLStreamException {
        if ( postURL != null ) {
            writer.writeStartElement( WFS_NS, "DCPType" );
            writer.writeStartElement( WFS_NS, "HTTP" );

            // ows:Post (type="ows:PostType")
            writer.writeStartElement( WFS_NS, "Post" );
            writer.writeAttribute( "onlineResource", postURL );
            writer.writeEndElement();

            writer.writeEndElement();
            writer.writeEndElement();
        }
    }
}
