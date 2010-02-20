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
import static org.deegree.protocol.csw.CSWConstants.CSW_202_PUBLICATION_SCHEMA;
import static org.deegree.protocol.csw.CSWConstants.CSW_PREFIX;
import static org.deegree.protocol.csw.CSWConstants.VERSION_202;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.axiom.om.OMElement;
import org.deegree.commons.configuration.JDBCConnections;
import org.deegree.commons.types.ows.Version;
import org.deegree.commons.xml.stax.XMLStreamWriterWrapper;
import org.deegree.record.persistence.RecordStore;
import org.deegree.record.publication.InsertTransaction;
import org.deegree.record.publication.TransactionOperation;
import org.deegree.services.controller.configuration.DeegreeServicesMetadata;
import org.deegree.services.controller.csw.transaction.Transaction;
import org.deegree.services.controller.utils.HttpResponseWrapper;
import org.deegree.services.csw.CSWService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author: thomas $
 * 
 * @version $Revision: $, $Date: $
 */
public class TransactionHandler {

    private static final Logger LOG = LoggerFactory.getLogger( TransactionHandler.class );

    CSWService service;

    DeegreeServicesMetadata servicesMetadata;

    private static Map<QName, RecordStore> requestedTypeNames;

    Writer stringWriter = new StringWriter();

    public TransactionHandler( CSWService service, DeegreeServicesMetadata servicesMetadata ) {
        this.service = service;
        this.servicesMetadata = servicesMetadata;
    }

    /**
     * 
     * Preprocessing for the export of a {@link Transaction} request
     * 
     * @param trans
     * @param response
     * @throws XMLStreamException
     * @throws IOException
     * @throws SQLException
     */
    public void doTransaction( Transaction trans, HttpResponseWrapper response )
                            throws XMLStreamException, IOException, SQLException {

        LOG.debug( "doTransaction: " + trans );

        Version version = trans.getVersion();

        response.setContentType( trans.getOutputFormat() );

        // to be sure of a valid response
        String schemaLocation = "";
        if ( version.equals( VERSION_202 ) ) {
            schemaLocation = CSW_202_NS + " " + CSW_202_PUBLICATION_SCHEMA;
        }

        XMLStreamWriter xmlWriter = getXMLResponseWriter( response, schemaLocation );
        export( xmlWriter, trans, response, version );
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
    private void export( XMLStreamWriter xmlWriter, Transaction trans, HttpResponseWrapper response, Version version )
                            throws XMLStreamException, SQLException {

        if ( VERSION_202.equals( version ) ) {
            export202( xmlWriter, response, trans );
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
    private void export202( XMLStreamWriter writer, HttpResponseWrapper response, Transaction trans )
                            throws XMLStreamException, SQLException {
        Version version = new Version( 2, 0, 2 );

        JDBCConnections con = servicesMetadata.getJDBCConnections();

        requestedTypeNames = new HashMap<QName, RecordStore>();
        InsertTransaction insert = null;

        int insertCount = 0;
        int updateCount = 0;
        int deleteCount = 0;

        writer.setDefaultNamespace( CSW_202_NS );
        writer.setPrefix( CSW_PREFIX, CSW_202_NS );

        writer.writeStartDocument();
        writer.writeStartElement( CSW_202_NS, "TransactionResponse" );
        writer.writeAttribute( "version", version.toString() );

        writer.writeStartElement( CSW_202_NS, "TransactionSummary" );
        if ( trans.getRequestId() != null ) {
            writer.writeAttribute( "requestId", trans.getRequestId() );
        } else {
        }

        for ( TransactionOperation transact : trans.getOperations() ) {
            switch ( transact.getType() ) {

            case INSERT:

                insert = (InsertTransaction) transact;

                for ( OMElement element : insert.getElement() ) {
                    insertCount++;
                    requestedTypeNames.put(
                                            new QName( element.getNamespace().getNamespaceURI(),
                                                       element.getLocalName(), element.getNamespace().getPrefix() ),
                                            service.getRecordStore( new QName(
                                                                               element.getNamespace().getNamespaceURI(),
                                                                               element.getLocalName(),
                                                                               element.getNamespace().getPrefix() ) ) );

                }

                break;
            case UPDATE:
                updateCount++;

                break;
            case DELETE:
                deleteCount++;

                break;

            }
        }

        writer.writeStartElement( CSW_202_NS, "totalInserted" );
        writer.writeCharacters( Integer.toString( insertCount ) );
        writer.writeEndElement();// totalInserted

        writer.writeStartElement( CSW_202_NS, "totalUpdated" );
        writer.writeCharacters( Integer.toString( updateCount ) );
        writer.writeEndElement();// totalUpdated

        writer.writeStartElement( CSW_202_NS, "totalDeleted" );
        writer.writeCharacters( Integer.toString( deleteCount ) );
        writer.writeEndElement();// totalDeleted

        writer.writeEndElement();// TransactionSummary

        if ( insertCount > 0 ) {
            writer.writeStartElement( CSW_202_NS, "InsertResult" );
            // TODO handle?? where is it?? writer.writeAttribute( "handleRef", trans. );

            for ( RecordStore rec : requestedTypeNames.values() ) {
                rec.transaction( writer, con, insert );

            }

            writer.writeEndElement();// InsertResult
        }

        writer.writeEndElement();// TransactionResponse
        writer.writeEndDocument();

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

        if ( schemaLocation.equals( "" ) ) {
            return writer.getXMLWriter();
        }
        return new XMLStreamWriterWrapper( writer.getXMLWriter(), schemaLocation );
    }
}
