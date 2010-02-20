//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/base/trunk/resources/eclipse/files_template.xml $
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

package org.deegree.services.controller.wfs;

import static org.deegree.gml.GMLVersion.GML_31;
import static org.deegree.services.controller.exception.ControllerException.NO_APPLICABLE_CODE;

import java.io.IOException;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.deegree.commons.utils.kvp.InvalidParameterValueException;
import org.deegree.commons.xml.CommonNamespaces;
import org.deegree.crs.exceptions.TransformationException;
import org.deegree.crs.exceptions.UnknownCRSException;
import org.deegree.feature.Feature;
import org.deegree.feature.persistence.FeatureStore;
import org.deegree.feature.persistence.FeatureStoreException;
import org.deegree.feature.types.FeatureType;
import org.deegree.geometry.Geometry;
import org.deegree.gml.GMLOutputFactory;
import org.deegree.gml.GMLStreamWriter;
import org.deegree.gml.GMLVersion;
import org.deegree.gml.feature.GMLFeatureWriter;
import org.deegree.protocol.wfs.getgmlobject.GetGmlObject;
import org.deegree.services.controller.ows.OWSException;
import org.deegree.services.controller.utils.HttpResponseWrapper;
import org.deegree.services.i18n.Messages;
import org.deegree.services.wfs.WFService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles {@link GetGmlObject} requests for the {@link WFSController}.
 * 
 * @see WFSController
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: schneider $
 * 
 * @version $Revision: $, $Date: $
 */
public class GetGmlObjectHandler {

    private static final Logger LOG = LoggerFactory.getLogger( GetGmlObjectHandler.class );

    private WFSController master;

    private WFService service;

    /**
     * Creates a new {@link GetGmlObjectHandler} instance that uses the given service to lookup requested
     * {@link FeatureType}s.
     * 
     * @param master
     * 
     * @param service
     *            WFS instance used to lookup the feature types
     */
    GetGmlObjectHandler( WFSController master, WFService service ) {
        this.master = master;
        this.service = service;
    }

    /**
     * Performs the given {@link GetGmlObject} request.
     * 
     * @param request
     *            request to be handled
     * @param response
     *            response that is used to write the result
     * @throws OWSException
     *             if a WFS specific exception occurs, e.g. the requested object is not known
     * @throws IOException
     * @throws XMLStreamException
     */
    void doGetGmlObject( GetGmlObject request, HttpResponseWrapper response )
                            throws OWSException, XMLStreamException, IOException {

        LOG.debug( "doGetGmlObject: " + request );
        Object o = null;
        for ( FeatureStore fs : service.getStores() ) {
            try {
                o = fs.getObjectById( request.getRequestedId() );
            } catch ( FeatureStoreException e ) {
                throw new OWSException( e.getMessage(), NO_APPLICABLE_CODE );
            }
            if ( o != null ) {
                break;
            }
        }

        if ( o == null ) {
            throw new OWSException(
                                    new InvalidParameterValueException( Messages.getMessage( "WFS_NO_SUCH_OBJECT",
                                                                                             request.getRequestedId() ) ) );
        }

        if ( o instanceof Feature ) {
            sendFeature( request, (Feature) o, response );
        } else if ( o instanceof Geometry ) {
            sendGeometry( (Geometry) o, response );
        } else {
            throw new OWSException(
                                    new InvalidParameterValueException(
                                                                        Messages.getMessage(
                                                                                             "WFS_UNSUPPORTED_GML_OBJECT",
                                                                                             request.getRequestedId() ) ) );
        }
    }

    private void sendFeature( GetGmlObject request, Feature feature, HttpResponseWrapper response )
                            throws XMLStreamException, IOException, OWSException {
        response.setContentType( "text/xml; charset=UTF-8" );
        String schemaLocation = WFSController.getSchemaLocation( request.getVersion(), feature.getName() );
        XMLStreamWriter writer = WFSController.getXMLResponseWriter( response, schemaLocation );

        writer.setPrefix( "ogc", "http://www.opengis.net/ogc" );
        writer.setPrefix( "wfs", "http://www.opengis.net/wfs" );
        writer.setPrefix( "xlink", "http://www.w3.org/1999/xlink" );

        int traverseXLinkDepth = 0;
        if ( request.getTraverseXlinkDepth() != null ) {
            if ( "*".equals( request.getTraverseXlinkDepth() ) ) {
                traverseXLinkDepth = -1;
            } else {
                try {
                    traverseXLinkDepth = Integer.parseInt( request.getTraverseXlinkDepth() );
                } catch ( NumberFormatException e ) {
                    String msg = Messages.get( "WFS_TRAVERSEXLINKDEPTH_INVALID", request.getTraverseXlinkDepth() );
                    throw new OWSException( new InvalidParameterValueException( msg ) );
                }

            }
        }
        int traverseXLinkExpiry = 1;

        GMLFeatureWriter encoder = new GMLFeatureWriter( GML_31, writer, null, null,
                                                             master.getObjectXlinkTemplate( request.getVersion() ),
                                                             null, traverseXLinkDepth, traverseXLinkExpiry, false );
        try {
            encoder.export( feature );
        } catch ( UnknownCRSException e ) {
            // cannot happen (we're requesting the native CRS)
        } catch ( TransformationException e ) {
            // cannot happen (we're requesting the native CRS)
        }
        writer.flush();
    }

    private void sendGeometry( Geometry geometry, HttpResponseWrapper response )
                            throws IOException, XMLStreamException {

        response.setContentType( "text/xml; charset=UTF-8" );
        String schemaLocation = CommonNamespaces.GMLNS + " http://schemas.opengis.net/gml/3.1.1/base/gml.xsd";
        XMLStreamWriter writer = WFSController.getXMLResponseWriter( response, schemaLocation );

        GMLStreamWriter gmlStream = GMLOutputFactory.createGMLStreamWriter( GMLVersion.GML_31, writer );
        try {
            gmlStream.write( geometry );
        } catch ( UnknownCRSException e ) {
            // cannot happen (we're requesting the native CRS)
        } catch ( TransformationException e ) {
            // cannot happen (we're requesting the native CRS)
        }
        writer.flush();
    }
}
