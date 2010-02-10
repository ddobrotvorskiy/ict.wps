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
package org.deegree.services.controller.csw.transaction;

import static org.deegree.commons.xml.CommonNamespaces.OGCNS;
import static org.deegree.protocol.csw.CSWConstants.VERSION_202;

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.axiom.om.OMElement;
import org.deegree.commons.types.ows.Version;
import org.deegree.commons.utils.kvp.InvalidParameterValueException;
import org.deegree.commons.xml.XMLParsingException;
import org.deegree.commons.xml.XPath;
import org.deegree.commons.xml.stax.XMLStreamReaderWrapper;
import org.deegree.filter.Filter;
import org.deegree.filter.xml.Filter110XMLDecoder;
import org.deegree.protocol.csw.CSWConstants.TransactionType;
import org.deegree.protocol.i18n.Messages;
import org.deegree.record.publication.InsertTransaction;
import org.deegree.record.publication.TransactionOperation;
import org.deegree.services.controller.csw.AbstractCSWRequestXMLAdapter;

/**
 * XML-Requesthandling for the {@link Transaction} operation.
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author: thomas $
 * 
 * @version $Revision: $, $Date: $
 */
public class TransactionXMLAdapter extends AbstractCSWRequestXMLAdapter {

    public Transaction parse( Version version ) {

        if ( version == null ) {
            version = Version.parseVersion( getRequiredNodeAsString( rootElement, new XPath( "@version", nsContext ) ) );
        }

        Transaction result = null;

        if ( VERSION_202.equals( version ) ) {
            result = parse202();
        } else {
            String msg = Messages.get( "UNSUPPORTED_VERSION", version, Version.getVersionsString( VERSION_202 ) );
            throw new InvalidParameterValueException( msg );
        }

        return result;
    }

    /**
     * @return
     */
    private Transaction parse202() {

        String requestId = getNodeAsString( rootElement, new XPath( "@requestId", nsContext ), null );

        boolean verboseRequest = getNodeAsBoolean( rootElement, new XPath( "@verboseRequest", nsContext ), false );

        List<OMElement> transChildElements = getRequiredElements( rootElement, new XPath( "*", nsContext ) );

        List<TransactionOperation> operations = new ArrayList<TransactionOperation>();

        for ( OMElement transChildElement : transChildElements ) {
            TransactionType type = null;
            QName typeName = null;
            String handle = null;
            String transType = transChildElement.getLocalName();

            if ( transType.equalsIgnoreCase( TransactionType.INSERT.name() ) ) {
                type = TransactionType.INSERT;
            } else if ( transType.equalsIgnoreCase( TransactionType.DELETE.name() ) ) {
                type = TransactionType.DELETE;
            } else if ( transType.equalsIgnoreCase( TransactionType.UPDATE.name() ) ) {
                type = TransactionType.UPDATE;
            }

            switch ( type ) {

            case INSERT:

                List<OMElement> transChildElementInsert = getRequiredElements( transChildElement, new XPath( "*", nsContext ) );
                typeName = getNodeAsQName( transChildElement, new XPath( "@typeName", nsContext ), null );
                handle = getNodeAsString( transChildElement, new XPath( "@handle", nsContext ), null );
                
                operations.add( new InsertTransaction( transChildElementInsert, typeName, handle ) );
                break;

            case DELETE:
                typeName = getNodeAsQName( transChildElement, new XPath( "@typeName", nsContext ), null );
                handle = getNodeAsString( transChildElement, new XPath( "@handle", nsContext ), null );

                Filter constraintDelete = null;

                OMElement transChildElementDelete = getRequiredElement( transChildElement, new XPath( "*", nsContext ) );

                Version versionConstraint = getRequiredNodeAsVersion( transChildElementDelete, new XPath( "@version",
                                                                                                          nsContext ) );

                OMElement filterEl = transChildElementDelete.getFirstChildWithName( new QName( OGCNS, "Filter" ) );
                OMElement cqlTextEl = transChildElementDelete.getFirstChildWithName( new QName( "", "CQLTEXT" ) );

                try {
                    // TODO remove usage of wrapper (necessary at the moment to work around problems
                    // with AXIOM's

                    XMLStreamReader xmlStream = new XMLStreamReaderWrapper(
                                                                            filterEl.getXMLStreamReaderWithoutCaching(),
                                                                            null );
                    // skip START_DOCUMENT
                    xmlStream.nextTag();

                    if ( versionConstraint.equals( new Version( 1, 1, 0 ) ) ) {

                        constraintDelete = Filter110XMLDecoder.parse( xmlStream );
                        System.out.println( constraintDelete );

                    } else {
                        String msg = Messages.get( "FILTER_VERSION NOT SPECIFIED", versionConstraint,
                                                   Version.getVersionsString( new Version( 1, 1, 0 ) ) );
                        throw new InvalidParameterValueException( msg );
                    }
                } catch ( XMLStreamException e ) {
                    e.printStackTrace();
                    throw new XMLParsingException( this, filterEl, e.getMessage() );
                }

                operations.add( new DeleteTransaction( handle, typeName, constraintDelete ) );
                break;

            case UPDATE:
                handle = getNodeAsString( transChildElement, new XPath( "@handle", nsContext ), null );

                OMElement transChildElementUpdate = getRequiredElement( transChildElement, new XPath( "*", nsContext ) );

                Filter constraintUpdate = null;

                RecordProperty recordProperty = null;
                List<RecordProperty> recordProperties = new ArrayList<RecordProperty>();

                List<OMElement> recordPropertyElements = getRequiredElements( transChildElementUpdate,
                                                                              new XPath( "RecordProperty", nsContext ) );

                for ( OMElement recordPropertyElement : recordPropertyElements ) {
                    QName name = getRequiredNodeAsQName( recordPropertyElement, new XPath( "Name", nsContext ) );
                    OMElement value = getElement( recordPropertyElement, new XPath( "Value", nsContext ) );

                    recordProperty = new RecordProperty( name, value );
                    recordProperties.add( recordProperty );
                }

                Version versionConstraintUpdate = getRequiredNodeAsVersion( transChildElementUpdate,
                                                                            new XPath( "@version", nsContext ) );

                OMElement filterElUpdate = transChildElementUpdate.getFirstChildWithName( new QName( OGCNS, "Filter" ) );
                OMElement cqlTextElUpdate = transChildElementUpdate.getFirstChildWithName( new QName( "", "CQLTEXT" ) );

                try {
                    // TODO remove usage of wrapper (necessary at the moment to work around problems
                    // with AXIOM's

                    XMLStreamReader xmlStream = new XMLStreamReaderWrapper(
                                                                            filterElUpdate.getXMLStreamReaderWithoutCaching(),
                                                                            null );
                    // skip START_DOCUMENT
                    xmlStream.nextTag();

                    if ( versionConstraintUpdate.equals( new Version( 1, 1, 0 ) ) ) {

                        constraintUpdate = Filter110XMLDecoder.parse( xmlStream );
                        System.out.println( constraintUpdate );

                    } else {
                        String msg = Messages.get( "FILTER_VERSION NOT SPECIFIED", versionConstraintUpdate,
                                                   Version.getVersionsString( new Version( 1, 1, 0 ) ) );
                        throw new InvalidParameterValueException( msg );
                    }
                } catch ( XMLStreamException e ) {
                    e.printStackTrace();
                    throw new XMLParsingException( this, filterElUpdate, e.getMessage() );
                }

                operations.add( new UpdateTransaction( handle, typeName, constraintUpdate, recordProperties ) );
                break;

            }
        }

        return new Transaction( new Version( 2, 0, 2 ), operations, requestId, verboseRequest );
    }
}
