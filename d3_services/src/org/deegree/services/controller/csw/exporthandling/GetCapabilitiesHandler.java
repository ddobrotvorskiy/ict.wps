//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -

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
package org.deegree.services.controller.csw.exporthandling;

import static org.deegree.protocol.csw.CSWConstants.VERSION_202;
import static org.deegree.protocol.csw.CSWConstants.CSW_202_NS;
import static org.deegree.protocol.csw.CSWConstants.CSW_202_DISCOVERY_SCHEMA;
import static org.deegree.protocol.csw.CSWConstants.CSW_PREFIX;

import org.deegree.protocol.csw.CSWConstants.Sections;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.deegree.commons.configuration.KeywordsType;
import org.deegree.commons.configuration.LanguageStringType;
import org.deegree.commons.types.ows.Version;
import org.deegree.commons.xml.CommonNamespaces;
import org.deegree.filter.xml.FilterCapabilitiesExporter;
import org.deegree.services.controller.configuration.DCPType;
import org.deegree.services.controller.configuration.DeegreeServicesMetadata;
import org.deegree.services.controller.configuration.ServiceIdentificationType;
import org.deegree.services.controller.ows.capabilities.OWSCapabilitiesXMLAdapter;
import org.deegree.protocol.csw.CSWConstants.CSWRequestType;

/**
 * Does the exportHandling for the Capabilities
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author: thomas $
 * 
 * @version $Revision: $, $Date: $
 */
public class GetCapabilitiesHandler extends OWSCapabilitiesXMLAdapter {

    private static final String OGC_NS = "http://www.opengis.net/ogc";

    private static final String OGC_PREFIX = "ogc";

    private static LinkedList<String> parameterValues;

    /**
     * Prepocessing for the xml export. Checks which version is requested and delegates it to the right versionexport.
     * In this case, version 2.0.2 of CSW is leaned on the 1.0.0 of the OGC specification.
     * 
     * @param writer
     * @param mainControllerConf
     * @param sections
     * @param identification
     * @param optionalOperations
     * @param version
     * @throws XMLStreamException
     */
    public static void export( XMLStreamWriter writer, DeegreeServicesMetadata mainControllerConf,
                               Set<Sections> sections, ServiceIdentificationType identification,
                               HashMap<String, HashMap> optionalOperations, Version version )
                            throws XMLStreamException {

        if ( VERSION_202.equals( version ) ) {
            export100( writer, sections, identification, mainControllerConf, optionalOperations );
        } else {
            throw new IllegalArgumentException( "Version '" + version + "' is not supported." );
        }
    }

    private static void export100( XMLStreamWriter writer, Set<Sections> sections,
                                   ServiceIdentificationType identification,
                                   DeegreeServicesMetadata mainControllerConf,
                                   HashMap<String, HashMap> optionalOperations )
                            throws XMLStreamException {
        writer.setPrefix( CSW_PREFIX, CSW_202_NS );
        writer.setPrefix( "ows", OWS_NS );
        writer.setPrefix( OGC_PREFIX, OGC_NS );
        writer.setPrefix( "xlink", XLN_NS );

        writer.writeStartDocument();
        writer.writeStartElement( CSW_202_NS, "CSW_Capabilities" );
        writer.writeAttribute( "version", "2.0.2" );
        writer.writeAttribute( "xsi", CommonNamespaces.XSINS, "schemaLocation", CSW_202_NS + " " + CSW_202_DISCOVERY_SCHEMA );

        // ows:ServiceIdentification
        if ( sections.isEmpty() || sections.contains( Sections.ServiceIdentification ) ) {
            exportServiceIdentification( writer, identification );

        }

        // ows:ServiceProvider
        if ( sections.isEmpty() || sections.contains( Sections.ServiceProvider ) ) {
            exportServiceProvider100( writer, mainControllerConf.getServiceProvider() );
        }

        // ows:OperationsMetadata
        if ( sections.isEmpty() || sections.contains( Sections.OperationsMetadata ) ) {

            exportOperationsMetadata( writer, optionalOperations, mainControllerConf.getDCP(), OWS_NS );
        }

        // mandatory
        FilterCapabilitiesExporter.export110( writer );

        writer.writeEndElement();
        writer.writeEndDocument();
    }

    // TODO just dummy values actually...
    private static void exportOperationsMetadata( XMLStreamWriter writer, HashMap<String, HashMap> operations,
                                                  DCPType dcp, String owsNS )
                            throws XMLStreamException {
        writer.writeStartElement( owsNS, "OperationsMetadata" );

        Set<String> operationsKeyName = operations.keySet();

        LinkedList<String> mandatoryOperations = new LinkedList<String>();

        mandatoryOperations.add( CSWRequestType.GetCapabilities.name() );
        mandatoryOperations.add( CSWRequestType.DescribeRecord.name() );
        mandatoryOperations.add( CSWRequestType.GetRecords.name() );

        for ( String name : mandatoryOperations ) {
            writer.writeStartElement( owsNS, "Operation" );
            writer.writeAttribute( "name", name );
            exportDCP( writer, dcp, owsNS );

            if ( name.equals( CSWRequestType.GetCapabilities.name() ) ) {
                writer.writeStartElement( owsNS, "Parameter" );
                writer.writeAttribute( "name", "sections" );

                parameterValues = new LinkedList<String>();

                parameterValues.add( "ServiceIdentification" );
                parameterValues.add( "ServiceProvider" );
                parameterValues.add( "OperationsMetadata" );
                parameterValues.add( "Filter_Capabilities" );

                for ( String value : parameterValues ) {
                    writer.writeStartElement( owsNS, "Value" );
                    writer.writeCharacters( value );
                    writer.writeEndElement();// Value
                }
                writer.writeEndElement();// Parameter

                // Constraints...

                writer.writeEndElement();// Operation
                continue;
            } else if ( name.equals( CSWRequestType.DescribeRecord.name() ) ) {

                writer.writeStartElement( owsNS, "Parameter" );
                writer.writeAttribute( "name", "TypeName" );
                writer.writeStartElement( owsNS, "Value" );
                // TODO kommt aus Backend
                writer.writeCharacters( CSW_PREFIX + ":Record" );
                writer.writeEndElement();// Value
                writer.writeEndElement();// Parameter

                writer.writeStartElement( owsNS, "Parameter" );
                writer.writeAttribute( "name", "outputFormat" );
                writer.writeStartElement( owsNS, "Value" );
                // TODO kommt aus Backend
                writer.writeCharacters( "application/xml" );
                writer.writeEndElement();// Value
                writer.writeEndElement();// Parameter

                writer.writeStartElement( owsNS, "Parameter" );
                writer.writeAttribute( "name", "schemaLocation" );
                writer.writeStartElement( owsNS, "Value" );
                // TODO kommt aus Backend
                writer.writeCharacters( "http://www.w3.org/TR/xmlschema-1/" );
                writer.writeEndElement();// Value
                writer.writeEndElement();// Parameter

                // Constraints...

                writer.writeEndElement();// Operation
                continue;
            } else if ( name.equals( CSWRequestType.GetRecords.name() ) ) {

                // Constraints...

                writer.writeEndElement();// Operation
                continue;
            }
        }

        // for all optional operations...
        if ( !operationsKeyName.isEmpty() || operationsKeyName != null ) {
            // e.g. GetRecordById
            for ( String op1 : operationsKeyName ) {

                writer.writeStartElement( owsNS, "Operation" );
                writer.writeAttribute( "name", op1 );
                exportDCP( writer, dcp, owsNS );

                HashMap<String, HashMap<String, List<String>>> operationsParamConstr = operations.get( op1 );
                Set<String> paramConstr = operationsParamConstr.keySet();
                // e.g. Parameter or Constraint
                for ( String op2 : paramConstr ) {

                    HashMap<String, List<String>> operationsKeyParamConstr = operationsParamConstr.get( op2 );
                    Set<String> keyParamConstr = operationsKeyParamConstr.keySet();

                    // e.g. typename
                    for ( String op3 : keyParamConstr ) {
                        writer.writeStartElement( owsNS, op2 );
                        writer.writeAttribute( "name", op3 );
                        List<String> list = operationsKeyParamConstr.get( op3 );

                        for ( String op4 : list ) {
                            writeElement( writer, owsNS, "Value", op4 );

                        }

                        writer.writeEndElement();// Paramter or Constraint

                    }

                }

                writer.writeEndElement();// Operation

            }
        }

        writer.writeEndElement();// OperationsMetadata

    }

    /*
     * private static void writeVersionAndUpdateSequence( XMLStreamWriter writer, int updateSequence ) throws
     * XMLStreamException { writer.writeAttribute( "version", VERSION_202.toString() ); writer.writeAttribute(
     * "updateSequence", Integer.toString( updateSequence ) ); }
     */

    private static void exportServiceIdentification( XMLStreamWriter writer, ServiceIdentificationType identification )
                            throws XMLStreamException {
        writer.writeStartElement( CSW_202_NS, Sections.ServiceIdentification.toString() );

        for ( String oneTitle : identification.getTitle() ) {
            writeElement( writer, CSW_202_NS, "Title", oneTitle );
        }

        for ( String oneAbstract : identification.getAbstract() ) {
            writeElement( writer, CSW_202_NS, "Abstract", oneAbstract );
        }
        String fees = "NONE";
        if ( identification != null ) {
            // keywords [0,n]
            exportKeywords( writer, identification.getKeywords() );

            // fees [1]
            fees = identification.getFees();
            if ( isEmpty( fees ) ) {
                identification.setFees( "NONE" );
            }
            fees = identification.getFees();

            // accessConstraints [0,n]
            exportAccessConstraints( writer, identification );

        }
        // fees = fees.replaceAll( "\\W", " " );
        writeElement( writer, CSW_202_NS, "fees", fees );

        writer.writeEndElement();
    }

    private static void exportOperation( XMLStreamWriter writer, String operation, DCPType dcp )
                            throws XMLStreamException {

        writer.writeStartElement( CSW_202_NS, "Operation" );
        writer.writeAttribute( CSW_202_NS, "name", operation );

        writer.writeStartElement( CSW_202_NS, "DCP" );
        writer.writeStartElement( CSW_202_NS, "HTTP" );

        if ( !isEmpty( dcp.getHTTPGet() ) ) {
            writer.writeStartElement( CSW_202_NS, "Get" );
            /*
             * referenced by Content of the OWS-Spec; should be the CI_OnlineResource of ISO 19115
             * https://www.ngdc.noaa.gov/wiki/index.php?title=CI_OnlineResource
             * 
             * or see: owsOperationsMetadata.xsd, ows19115subset.xsd, owsCommons.xsd
             */
            writer.writeStartElement( CSW_202_NS, "URL" );
            // mandatory
            writeElement( writer, CSW_202_NS, "Linkage", XLN_NS, "href", dcp.getHTTPGet() );

            // TODO
            // protocol optional e.g. http, ftp
            // applicationProfile optional
            // name optional
            // description optional
            // function optional

            // writeElement( writer, CSW_202_NS, "OnlineResource", XLN_NS, "href", dcp.getHTTPGet() );
            writer.writeEndElement(); // URL

            // TODO nach ows-Spec owsOperationsMetadata.xsd
            writer.writeStartElement( CSW_202_NS, "Constraint" );
            // [1] mandatory
            writer.writeAttribute( CSW_202_NS, "name", "" );
            // [1,*] mandatory according to Spec, [0,*] according to schema...so this is used
            writer.writeStartElement( CSW_202_NS, "Value" );
            // [0,*] optional
            writer.writeStartElement( CSW_202_NS, "Metadata" );

            writer.writeEndElement(); // Metadata
            writer.writeEndElement(); // Value
            writer.writeEndElement(); // Constraint
            writer.writeEndElement(); // Get
        }
        if ( !isEmpty( dcp.getHTTPPost() ) ) {
            writer.writeStartElement( CSW_202_NS, "Post" );
            /*
             * referenced by Content of the OWS-Spec; should be the CI_OnlineResource of ISO 19115
             * https://www.ngdc.noaa.gov/wiki/index.php?title=CI_OnlineResource
             */
            writer.writeStartElement( CSW_202_NS, "URL" );
            // mandatory
            writeElement( writer, CSW_202_NS, "Linkage", XLN_NS, "href", dcp.getHTTPPost() );

            // TODO
            // protocol optional e.g. http, ftp
            // applicationProfile optional
            // name optional
            // description optional
            // function optional

            // writeElement( writer, CSW_202_NS, "OnlineResource", XLN_NS, "href", dcp.getHTTPGet() );
            writer.writeEndElement(); // URL
            writer.writeEndElement(); // Post
        }
        writer.writeEndElement(); // HTTP
        writer.writeEndElement(); // DCP

        // TODO
        // writer.writeStartElement( Parameter );
        // writer.writeStartElement( Constraints );
        // writer.writeStartElement( Metadata );

        // writer.writeEndElement(); // Parameter
        // writer.writeEndElement(); // Constraints
        // writer.writeEndElement(); // Metadata

        writer.writeEndElement(); // Operation

    }

    /*
     * private static void exportCapability( XMLStreamWriter writer, DeegreeServicesMetadata serviceMetadata,
     * List<String> allowedOperations, int updateSequence, boolean isSection ) throws XMLStreamException {
     * 
     * writer.writeStartElement( CSW_202_NS, "Capability" ); if ( isSection ) { // @version // @updateSequence
     * writeVersionAndUpdateSequence( writer, updateSequence ); }
     * 
     * // exportOperationsMetadata( writer, allowedOperations, serviceMetadata.getDCP() );
     * 
     * writer.writeStartElement( CSW_202_NS, "Exception" ); writeElement( writer, CSW_202_NS, "Format",
     * "application/vnd.ogc.se_xml" );
     * 
     * // VendorSpecificCapabilities [0,1] // any
     * 
     * writer.writeEndElement(); // Exception writer.writeEndElement(); // Capability }
     */

    /**
     * @param writer
     * @param type
     * @param link
     * @throws XMLStreamException
     */

    /*
     * public static void exportMetadataLink( XMLStreamWriter writer, String type, String link ) throws
     * XMLStreamException { if ( link != null && !"".equals( link ) ) {
     * 
     * writer.writeStartElement( CSW_202_NS, "metadataLink" ); writer.writeAttribute( CommonNamespaces.XLNNS, "href",
     * link ); String t = type; if ( !( "FGDC".equals( type ) || "TC211".equals( type ) ) ) { t = "other"; }
     * writer.writeAttribute( "metadataType", t );
     * 
     * writer.writeEndElement();// WCS_100_NS, "metadataLink" ); } }
     */

    private final static boolean isEmpty( String value ) {
        return value == null || "".equals( value );
    }

    /**
     * write a list of keywords in csw 2.0.2 style.
     * 
     * @param writer
     * @param keywords
     * @throws XMLStreamException
     */
    public static void exportKeywords( XMLStreamWriter writer, List<KeywordsType> keywords )
                            throws XMLStreamException {
        if ( !keywords.isEmpty() ) {
            for ( KeywordsType kwt : keywords ) {
                if ( kwt != null ) {
                    writer.writeStartElement( CSW_202_NS, "keywords" );
                    List<LanguageStringType> keyword = kwt.getKeyword();
                    for ( LanguageStringType lst : keyword ) {
                        exportKeyword( writer, lst );
                        // -> keyword [1, n]
                    }
                    // -> type [0,1]
                    // exportCodeType( writer, kwt.getType() );
                    writer.writeEndElement();// WCS_100_NS, "keywords" );
                }
            }

        }

    }

    /**
     * @param writer
     * @param lst
     * @throws XMLStreamException
     */
    public static void exportKeyword( XMLStreamWriter writer, LanguageStringType lst )
                            throws XMLStreamException {
        if ( lst != null ) {
            writeElement( writer, CSW_202_NS, "keyword", lst.getValue() );
        }
    }

    private static void exportAccessConstraints( XMLStreamWriter writer, ServiceIdentificationType identification )
                            throws XMLStreamException {
        List<String> accessConstraints = identification.getAccessConstraints();

        if ( accessConstraints.isEmpty() ) {
            accessConstraints.add( "NONE" );

        } else {
            for ( String ac : accessConstraints ) {
                if ( !ac.isEmpty() ) {
                    writeElement( writer, CSW_202_NS, "accessConstraints", ac );
                }
            }
        }

    }

}
