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

import static org.deegree.protocol.csw.CSWConstants.CSW_202_NS;
import static org.deegree.protocol.csw.CSWConstants.CSW_202_DISCOVERY_SCHEMA;
import static org.deegree.protocol.csw.CSWConstants.CSW_PREFIX;
import static org.deegree.protocol.csw.CSWConstants.VERSION_202;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.deegree.commons.types.ows.Version;
import org.deegree.commons.xml.CommonNamespaces;
import org.deegree.commons.xml.stax.XMLStreamWriterWrapper;
import org.deegree.record.persistence.RecordStore;
import org.deegree.services.controller.csw.CSWController;
import org.deegree.services.controller.csw.describerecord.DescribeRecord;
import org.deegree.services.controller.exception.ControllerException;
import org.deegree.services.controller.ows.OWSException;
import org.deegree.services.controller.utils.HttpResponseWrapper;
import org.deegree.services.csw.CSWService;
import org.deegree.services.i18n.Messages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Defines the export functionality for a {@link DescribeRecord} request
 * 
 * @see CSWController
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author: thomas $
 * 
 * @version $Revision: $, $Date: $
 */
public class DescribeRecordHandler {

    private static final Logger LOG = LoggerFactory.getLogger( DescribeRecordHandler.class );
    
    private static Map<QName, RecordStore> requestedTypeNames;

    CSWService service;

    public DescribeRecordHandler( CSWService service ) {
        this.service = service;

    }

    /**
     * Preprocessing for the export of a {@link DescribeRecord} request
     * 
     * @param descRec
     * @param response
     */
    public void doDescribeRecord( DescribeRecord descRec, HttpResponseWrapper response )
                            throws XMLStreamException, IOException, OWSException {

        Set<RecordStore> rec = determineRequestedRecordStore( descRec );

        if ( rec.size() == 0 ) {
            throw new OWSException( Messages.get( "CSW_NO_RECORDSTORE_DEFINED" ),
                                    ControllerException.NO_APPLICABLE_CODE );
        }

        Version version = descRec.getVersion();
        response.setContentType( descRec.getOutputFormat() );

        XMLStreamWriter xmlWriter = getXMLResponseWriter( response, null );

        export( xmlWriter, rec.iterator().next(), version );
        xmlWriter.flush();

    }

    /**
     * Determines whether there is a typeName delivered in the request. If there is no typeName delivered there should
     * gotten all stored records. Otherwise the requested record should be provided.
     * 
     * @param descRec
     * @return
     */
    private Set<RecordStore> determineRequestedRecordStore( DescribeRecord descRec ) {

        Set<RecordStore> rss = new HashSet<RecordStore>();
        requestedTypeNames = new HashMap<QName, RecordStore>();

        if ( descRec.getTypeNames() == null || descRec.getTypeNames().length == 0 ) {
            LOG.debug( "Describing all served records." );
            rss.addAll( service.getStore() );

        } else {
            for ( QName typeName : descRec.getTypeNames() ) {

                if ( service.getRecordStore( typeName ) != null ) {
                    
                    rss.add( service.getRecordStore( typeName ) );
                    requestedTypeNames.put( typeName, service.getRecordStore( typeName ) );
                
                }

            }

        }

        return rss;

    }

    /**
     * 
     * Exports the correct recognized request and determines to which version export it should delegate the request
     * 
     * @param writer
     * @param rec
     * @throws XMLStreamException
     * @throws FileNotFoundException
     */
    private static void export( XMLStreamWriter writer, RecordStore rec, Version version )
                            throws XMLStreamException, FileNotFoundException {

        if ( VERSION_202.equals( version ) ) {
            export202( writer, rec );
        } else {
            throw new IllegalArgumentException( "Version '" + version + "' is not supported." );
        }

    }

    /**
     * Exporthandling for the version 2.0.2
     * 
     * @param writer
     * @param record
     * @throws XMLStreamException
     * @throws FileNotFoundException
     */
    private static void export202( XMLStreamWriter writer, RecordStore record )
                            throws XMLStreamException, FileNotFoundException {

        writer.setDefaultNamespace( CSW_202_NS );
        writer.setPrefix( CSW_PREFIX, CSW_202_NS );

        writer.writeStartDocument();
        writer.writeStartElement( CSW_202_NS, "DescribeRecordResponse" );
        writer.writeAttribute( "xsi", CommonNamespaces.XSINS, "schemaLocation", CSW_202_NS + " " + CSW_202_DISCOVERY_SCHEMA );

        for ( QName qName : requestedTypeNames.keySet() ) {

            exportSchemaComponent( writer, requestedTypeNames.values(), qName );
        }

        writer.writeEndElement();// DescribeRecordResponse
        writer.writeEndDocument();
    }

    /**
     * SchemaCompontent which encapsulates the requested xml schema
     * 
     * @throws XMLStreamException
     * @throws FileNotFoundException
     * 
     */
    private static void exportSchemaComponent( XMLStreamWriter writer, Collection<RecordStore> record, QName qName )
                            throws XMLStreamException, FileNotFoundException {

        writer.writeStartElement( CSW_202_NS, "SchemaComponent" );
        // TODO change hard-coded...maybe
        // required
        writer.writeAttribute( "schemaLanguage", "XMLSCHEMA" );
        // required
        writer.writeAttribute( "targetNamespace", qName.getNamespaceURI() );
        // optional
        writer.writeAttribute( "parentSchema", "" );

        for ( RecordStore rec : record ) {
            rec.describeRecord( writer, qName );
        }

        writer.writeEndElement();// SchemaComponent

    }

    /**
     * Returns an <code>XMLStreamWriter</code> for writing an XML response document.
     * 
     * @param writer
     *            writer to write the XML to, must not be null
     * @param schemaLocation
     *            allows to specify a value for the 'xsi:schemaLocation' attribute in the root element, must not be null
     * @return
     * @throws XMLStreamException
     * @throws IOException
     */
    static XMLStreamWriter getXMLResponseWriter( HttpResponseWrapper writer, String schemaLocation )
                            throws XMLStreamException, IOException {

        if ( schemaLocation == null ) {
            return writer.getXMLWriter();
        }
        return new XMLStreamWriterWrapper( writer.getXMLWriter(), schemaLocation );
    }

}
