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

import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.deegree.commons.configuration.JDBCConnections;
import org.deegree.commons.types.ows.Version;
import org.deegree.commons.utils.time.DateUtils;
import org.deegree.commons.xml.stax.XMLStreamWriterWrapper;
import org.deegree.record.persistence.GenericDatabaseDS;
import org.deegree.record.persistence.RecordStore;
import org.deegree.record.persistence.sqltransform.postgres.TransformatorPostGres;
import org.deegree.services.controller.configuration.DeegreeServicesMetadata;
import org.deegree.services.controller.csw.CSWController;
import org.deegree.services.controller.csw.getrecords.GetRecords;
import org.deegree.services.controller.utils.HttpResponseWrapper;
import org.deegree.services.csw.CSWService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Defines the export functionality for a {@link GetRecords} request
 * 
 * @see CSWController
 * 
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author: thomas $
 * 
 * @version $Revision: $, $Date: $
 */
public class GetRecordsHandler {

    private static final Logger LOG = LoggerFactory.getLogger( GetRecordsHandler.class );

    private static Map<QName, RecordStore> requestedTypeNames;

    CSWService service;

    DeegreeServicesMetadata servicesMetadata;

    public GetRecordsHandler( CSWService service, DeegreeServicesMetadata servicesMetadata ) {
        this.service = service;
        this.servicesMetadata = servicesMetadata;
    }

    /**
     * Preprocessing for the export of a {@link GetRecords} request
     * 
     * @param getRec
     * @param response
     * @throws XMLStreamException
     * @throws IOException
     * @throws SQLException
     */
    public void doGetRecords( GetRecords getRec, HttpResponseWrapper response )
                            throws XMLStreamException, IOException, SQLException {

        LOG.debug( "doGetRecords: " + getRec );

        Version version = getRec.getVersion();

        response.setContentType( getRec.getOutputFormat() );

        // to be sure of a valid response
        String schemaLocation = "";
        if ( getRec.getVersion() == VERSION_202 ) {
            schemaLocation = CSW_202_NS + " " + CSW_202_DISCOVERY_SCHEMA;
        }

        XMLStreamWriter xmlWriter = getXMLResponseWriter( response, schemaLocation );
        export( xmlWriter, getRec, response, version );
        xmlWriter.flush();
    }

    /**
     * Exports the correct recognized request and determines to which version export it should delegate the request
     * 
     * @param xmlWriter
     * @param getRec
     * @param response
     * @param version
     * @throws XMLStreamException
     * @throws SQLException
     */
    private void export( XMLStreamWriter xmlWriter, GetRecords getRec, HttpResponseWrapper response, Version version )
                            throws XMLStreamException, SQLException {

        if ( VERSION_202.equals( version ) ) {
            export202( xmlWriter, response, getRec );
        } else {
            throw new IllegalArgumentException( "Version '" + version + "' is not supported." );
        }

    }

    /**
     * 
     * Exporthandling for the version 2.0.2
     * 
     * @param xmlWriter
     * @param getRec
     * @throws XMLStreamException
     * @throws SQLException
     */
    private void export202( XMLStreamWriter writer, HttpResponseWrapper response, GetRecords getRec )
                            throws XMLStreamException, SQLException {
        Version version = new Version( 2, 0, 2 );

        writer.setDefaultNamespace( CSW_202_NS );
        writer.setPrefix( CSW_PREFIX, CSW_202_NS );

        writer.writeStartDocument();
        writer.writeStartElement( CSW_202_NS, "GetRecordsResponse" );

        // ---------
        JDBCConnections con = servicesMetadata.getJDBCConnections();

        searchStatus( writer, version );
        searchResult( writer, getRec, con, version );
        // ---------

        writer.writeEndDocument();

    }

    /**
     * Exports the timestamp of the request
     * 
     * @param writer
     * @param version
     * @throws XMLStreamException
     */
    private void searchStatus( XMLStreamWriter writer, Version version )
                            throws XMLStreamException {

        if ( VERSION_202.equals( version ) ) {
            writer.writeStartElement( CSW_202_NS, "SearchStatus" );
        } else {
            throw new IllegalArgumentException( "Version '" + version + "' is not supported." );
        }

        writer.writeAttribute( "timestamp", DateUtils.formatISO8601Date( new Date() ) );

        writer.writeEndElement();// SearchStatus

    }

    /**
     * 
     * Exports the result container that contains the requested elements
     * 
     * @param writer
     * @param typeNames
     * @param elementSet
     * @param con
     * @param resultType
     * @param version
     * @throws XMLStreamException
     * @throws SQLException
     */
    private void searchResult( XMLStreamWriter writer, GetRecords getRec, JDBCConnections con, Version version )
                            throws XMLStreamException, SQLException {

        Set<RecordStore> rss = new HashSet<RecordStore>();
        requestedTypeNames = new HashMap<QName, RecordStore>();

        if ( VERSION_202.equals( version ) ) {

            writer.writeStartElement( CSW_202_NS, "SearchResults" );

            // Question if there is the specified record available
            for ( QName typeName : getRec.getTypeNames() ) {
                if ( service.getRecordStore( typeName ) != null ) {
                    rss.add( service.getRecordStore( typeName ) );
                    requestedTypeNames.put( typeName, service.getRecordStore( typeName ) );
                }
            }
            // TODO sortBy handling

            

            TransformatorPostGres filterExpression = new TransformatorPostGres( getRec.getConstraint() );
            GenericDatabaseDS gdds = new GenericDatabaseDS( filterExpression.getStringWriter(), getRec.getResultType(),
                                                            getRec.getElementSetName(), getRec.getMaxRecords(), getRec.getStartPosition(),
                                                            filterExpression.getTable(), filterExpression.getColumn() );

            System.out.println( filterExpression.getStringWriter().toString() );
            

            // commits the record to the getRecords operation
            for ( QName qName : requestedTypeNames.keySet() ) {
                for ( RecordStore rec : requestedTypeNames.values() ) {
                    rec.getRecords( writer, qName, con, gdds );
                }
            }

        } else {
            throw new IllegalArgumentException( "Version '" + version + "' is not supported." );
        }

        writer.writeEndElement();// SearchResult

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
