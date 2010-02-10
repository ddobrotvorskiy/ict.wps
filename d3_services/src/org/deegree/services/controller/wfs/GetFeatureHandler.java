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

import static org.deegree.commons.xml.CommonNamespaces.GMLNS;
import static org.deegree.commons.xml.CommonNamespaces.XLNNS;
import static org.deegree.protocol.wfs.WFSConstants.VERSION_100;
import static org.deegree.protocol.wfs.WFSConstants.VERSION_110;
import static org.deegree.protocol.wfs.WFSConstants.VERSION_200;
import static org.deegree.protocol.wfs.WFSConstants.WFS_200_NS;
import static org.deegree.protocol.wfs.WFSConstants.WFS_NS;
import static org.deegree.protocol.wfs.getfeature.ResultType.RESULTS;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.deegree.commons.types.ows.Version;
import org.deegree.commons.utils.kvp.InvalidParameterValueException;
import org.deegree.commons.utils.time.DateUtils;
import org.deegree.crs.exceptions.TransformationException;
import org.deegree.crs.exceptions.UnknownCRSException;
import org.deegree.feature.Feature;
import org.deegree.feature.FeatureCollection;
import org.deegree.feature.GenericFeatureCollection;
import org.deegree.feature.persistence.FeatureStore;
import org.deegree.feature.persistence.FeatureStoreException;
import org.deegree.feature.persistence.query.FeatureResultSet;
import org.deegree.feature.persistence.query.Query;
import org.deegree.feature.types.FeatureType;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.geometry.Envelope;
import org.deegree.gml.GMLOutputFactory;
import org.deegree.gml.GMLStreamWriter;
import org.deegree.gml.GMLVersion;
import org.deegree.gml.geometry.GML2GeometryWriter;
import org.deegree.protocol.wfs.getfeature.GetFeature;
import org.deegree.protocol.wfs.getfeature.ResultType;
import org.deegree.services.controller.ows.OWSException;
import org.deegree.services.controller.utils.HttpResponseWrapper;
import org.deegree.services.i18n.Messages;
import org.deegree.services.wfs.WFService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles {@link GetFeature} requests for the {@link WFSController}.
 * 
 * @see WFSController
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: schneider $
 * 
 * @version $Revision: $, $Date: $
 */
class GetFeatureHandler {

    private static final Logger LOG = LoggerFactory.getLogger( GetFeatureHandler.class );

    private final WFSController master;

    private final WFService service;

    /**
     * Creates a new {@link GetFeatureHandler} instance that uses the given service to lookup requested
     * {@link FeatureType}s.
     * 
     * @param master
     *            corresponding WFS controller
     * @param service
     *            WFS instance used to lookup the feature types
     */
    GetFeatureHandler( WFSController master, WFService service ) {
        this.master = master;
        this.service = service;
    }

    /**
     * Performs the given {@link GetFeature} request.
     * 
     * @param request
     *            request to be handled
     * @param response
     *            response that is used to write the result
     * @throws Exception
     */
    void doGetFeature( GetFeature request, HttpResponseWrapper response )
                            throws Exception {

        ResultType type = request.getResultType();
        if ( type == RESULTS || type == null ) {
            doResults( request, response );
        } else {
            doHits( request, response );
        }
    }

    private void doResults( GetFeature request, HttpResponseWrapper response )
                            throws Exception {

        LOG.debug( "Performing GetFeature (results) request." );

        GMLVersion outputFormat = determineOutputFormat( request );
        GetFeatureAnalyzer analyzer = new GetFeatureAnalyzer( request, service, outputFormat );
        String schemaLocation = getSchemaLocation( request.getVersion(), analyzer.getFeatureTypes() );

        int traverseXLinkDepth = 0;
        // TODO
        // int traverseXLinkExpiry = 1;
        String xLinkTemplate = null;

        if ( VERSION_110.equals( request.getVersion() ) || VERSION_200.equals( request.getVersion() ) ) {
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
            xLinkTemplate = master.getObjectXlinkTemplate( request.getVersion() );
        }

        XMLStreamWriter xmlStream = WFSController.getXMLResponseWriter( response, schemaLocation );
        setContentType( outputFormat, response );

        // open "wfs:FeatureCollection" element
        if ( request.getVersion().equals( VERSION_100 ) ) {
            xmlStream.writeStartElement( "wfs", "FeatureCollection", WFS_NS );
        } else if ( request.getVersion().equals( VERSION_110 ) ) {
            xmlStream.writeStartElement( "wfs", "FeatureCollection", WFS_NS );
            xmlStream.writeAttribute( "timeStamp", DateUtils.formatISO8601Date( new Date() ) );
        } else if ( request.getVersion().equals( VERSION_200 ) ) {
            xmlStream.writeStartElement( "wfs", "FeatureCollection", WFS_200_NS );
            xmlStream.writeAttribute( "timeStamp", DateUtils.formatISO8601Date( new Date() ) );
        }

        int maxFeatures = -1;
        if ( request.getMaxFeatures() != null ) {
            maxFeatures = request.getMaxFeatures();
        }
        // TODO make this configurable
        writeFeatureMembersCached( request.getVersion(), xmlStream, analyzer, outputFormat, xLinkTemplate,
                                   traverseXLinkDepth, maxFeatures );
        // writeFeatureMembersStream( xmlStream, analyzer, outputFormat, xLinkTemplate, traverseXLinkDepth );

        // close "wfs:FeatureCollection"
        xmlStream.writeEndElement();
        xmlStream.flush();
    }

    private void writeFeatureMembersStream( XMLStreamWriter xmlStream, GetFeatureAnalyzer analyzer,
                                            GMLVersion outputFormat, String xLinkTemplate, int traverseXLinkDepth,
                                            int maxFeatures )
                            throws XMLStreamException, UnknownCRSException, TransformationException,
                            FeatureStoreException, FilterEvaluationException {

        // "gml:boundedBy" is necessary for GML 2
        if ( outputFormat.equals( GMLVersion.GML_2 ) ) {
            xmlStream.writeStartElement( "gml", "boundedBy", GMLNS );
            xmlStream.writeStartElement( "gml", "null", GMLNS );
            xmlStream.writeCharacters( "unknown" );
            xmlStream.writeEndElement();
            xmlStream.writeEndElement();
        }

        GMLStreamWriter gmlStream = GMLOutputFactory.createGMLStreamWriter( outputFormat, xmlStream );
        gmlStream.setLocalXLinkTemplate( xLinkTemplate );
        gmlStream.setXLinkExpansion( traverseXLinkDepth );
        gmlStream.setFeatureProperties( analyzer.getRequestedProps() );
        gmlStream.setOutputCRS( analyzer.getRequestedCRS() );

        bindFeatureTypePrefixes( xmlStream, analyzer.getFeatureTypes() );

        // retrieve and write result features
        int featuresAdded = 0; // limit the number of features written to maxfeatures
        for ( Map.Entry<FeatureStore, List<Query>> fsToQueries : analyzer.getQueries().entrySet() ) {
            FeatureStore fs = fsToQueries.getKey();
            Query[] queries = fsToQueries.getValue().toArray( new Query[fsToQueries.getValue().size()] );
            FeatureResultSet rs = fs.query( queries );
            try {
                for ( Feature feature : rs ) {
                    if ( gmlStream.isObjectExported( feature.getId() ) ) {
                        xmlStream.writeEmptyElement( "gml", "featureMember", GMLNS );
                        xmlStream.writeAttribute( "xlink", XLNNS, "href", "#" + feature.getId() );
                    } else {
                        xmlStream.writeStartElement( "gml", "featureMember", GMLNS );
                        gmlStream.write( feature );
                        xmlStream.writeEndElement();
                    }

                    featuresAdded++;
                    if ( featuresAdded == maxFeatures ) {
                        break;
                    }
                }
            } finally {
                rs.close();
            }
        }
    }

    private void writeFeatureMembersCached( Version wfsVersion, XMLStreamWriter xmlStream, GetFeatureAnalyzer analyzer,
                                            GMLVersion outputFormat, String xLinkTemplate, int traverseXLinkDepth,
                                            int maxFeatures )
                            throws XMLStreamException, UnknownCRSException, TransformationException,
                            FeatureStoreException, FilterEvaluationException {

        FeatureCollection allFeatures = new GenericFeatureCollection();
        Set<String> fids = new HashSet<String>();

        // retrieve maxfeatures features
        int featuresAdded = 0;
        for ( Map.Entry<FeatureStore, List<Query>> fsToQueries : analyzer.getQueries().entrySet() ) {
            FeatureStore fs = fsToQueries.getKey();
            Query[] queries = fsToQueries.getValue().toArray( new Query[fsToQueries.getValue().size()] );
            FeatureResultSet rs = fs.query( queries );
            try {
                for ( Feature feature : rs ) {
                    if ( !fids.contains( feature.getId() ) ) {
                        allFeatures.add( feature );
                        fids.add( feature.getId() );
                        featuresAdded++;
                        if ( featuresAdded == maxFeatures ) {
                            break;
                        }
                    }
                }
            } finally {
                rs.close();
            }
        }

        if ( !wfsVersion.equals( VERSION_100 ) ) {
            xmlStream.writeAttribute( "numberOfFeatures", "" + allFeatures.size() );
        }

        Envelope fcEnvelope = allFeatures.getEnvelope();
        if ( outputFormat.equals( GMLVersion.GML_2 ) ) {
            xmlStream.writeStartElement( "gml", "boundedBy", GMLNS );
            if ( fcEnvelope == null ) {
                xmlStream.writeStartElement( "gml", "null", GMLNS );
                xmlStream.writeCharacters( "inapplicable" );
                xmlStream.writeEndElement();
            } else {
                new GML2GeometryWriter( xmlStream ).export( fcEnvelope );
            }
            xmlStream.writeEndElement();
        }
        // TODO GML 3 boundedBy

        GMLStreamWriter gmlStream = GMLOutputFactory.createGMLStreamWriter( outputFormat, xmlStream );
        gmlStream.setLocalXLinkTemplate( xLinkTemplate );
        gmlStream.setXLinkExpansion( traverseXLinkDepth );
        gmlStream.setFeatureProperties( analyzer.getRequestedProps() );
        gmlStream.setOutputCRS( analyzer.getRequestedCRS() );

        bindFeatureTypePrefixes( xmlStream, analyzer.getFeatureTypes() );

        // retrieve and write result features
        for ( Feature member : allFeatures ) {
            if ( gmlStream.isObjectExported( member.getId() ) ) {
                xmlStream.writeEmptyElement( "gml", "featureMember", GMLNS );
                xmlStream.writeAttribute( "xlink", XLNNS, "href", "#" + member.getId() );
            } else {
                xmlStream.writeStartElement( "gml", "featureMember", GMLNS );
                gmlStream.write( member );
                xmlStream.writeEndElement();
            }
        }
    }

    private void bindFeatureTypePrefixes( XMLStreamWriter xmlStream, Collection<FeatureType> fts )
                            throws XMLStreamException {

        if ( fts == null ) {
            fts = service.getFeatureTypes();
        }

        Map<String, String> nsToPrefix = new HashMap<String, String>();
        for ( FeatureType ft : fts ) {
            QName ftName = ft.getName();
            if ( ftName.getPrefix() != null ) {
                nsToPrefix.put( ftName.getNamespaceURI(), ftName.getPrefix() );
            }
        }

        for ( Map.Entry<String, String> nsBinding : nsToPrefix.entrySet() ) {
            xmlStream.setPrefix( nsBinding.getValue(), nsBinding.getKey() );
        }
    }

    private void doHits( GetFeature request, HttpResponseWrapper response )
                            throws OWSException, XMLStreamException, IOException, FeatureStoreException,
                            FilterEvaluationException {

        LOG.debug( "Performing GetFeature (hits) request." );

        GMLVersion outputFormat = determineOutputFormat( request );
        GetFeatureAnalyzer analyzer = new GetFeatureAnalyzer( request, service, outputFormat );
        String schemaLocation = getSchemaLocation( request.getVersion(), analyzer.getFeatureTypes() );

        XMLStreamWriter xmlStream = WFSController.getXMLResponseWriter( response, schemaLocation );

        setContentType( outputFormat, response );

        // open "wfs:FeatureCollection" element
        if ( request.getVersion().equals( VERSION_100 ) ) {
            xmlStream.writeStartElement( "wfs", "FeatureCollection", WFS_NS );
        } else if ( request.getVersion().equals( VERSION_110 ) ) {
            xmlStream.writeStartElement( "wfs", "FeatureCollection", WFS_NS );
            xmlStream.writeAttribute( "timeStamp", DateUtils.formatISO8601Date( new Date() ) );
        } else if ( request.getVersion().equals( VERSION_200 ) ) {
            xmlStream.writeStartElement( "wfs", "FeatureCollection", WFS_200_NS );
            xmlStream.writeAttribute( "timeStamp", DateUtils.formatISO8601Date( new Date() ) );
        }

        int numHits = 0;

        for ( Map.Entry<FeatureStore, List<Query>> fsToQueries : analyzer.getQueries().entrySet() ) {
            FeatureStore fs = fsToQueries.getKey();
            Query[] queries = fsToQueries.getValue().toArray( new Query[fsToQueries.getValue().size()] );
            // TODO what about features that occur multiple times as result of different queries?
            numHits += fs.queryHits( queries );
        }

        xmlStream.writeAttribute( "numberOfFeatures", "" + numHits );

        // "gml:boundedBy" is necessary for GML 2
        // TODO strategies for including the correct value
        if ( outputFormat.equals( GMLVersion.GML_2 ) ) {
            xmlStream.writeStartElement( "gml", GMLNS, "boundedBy" );
            xmlStream.writeStartElement( GMLNS, "null" );
            xmlStream.writeCharacters( "not available (WFS streaming mode)" );
            xmlStream.writeEndElement();
            xmlStream.writeEndElement();
        }

        // close "wfs:FeatureCollection"
        xmlStream.writeEndElement();
        xmlStream.flush();

    }

    /**
     * Returns the value for the <code>xsi:schemaLocation</code> attribute in the response document.
     * 
     * @param requestVersion
     *            requested WFS version, must not be <code>null</code>
     * @param requestedFts
     *            requested feature types, can be <code>null</code> (any feature type may occur in the output)
     * @return value for the <code>xsi:schemaLocation</code> attribute, never <code>null</code>
     */
    private String getSchemaLocation( Version requestVersion, Collection<FeatureType> requestedFts ) {

        String schemaLocation = null;
        if ( VERSION_100.equals( requestVersion ) ) {
            schemaLocation = WFS_NS + " http://schemas.opengis.net/wfs/1.0.0/WFS-basic.xsd";
        } else if ( VERSION_110.equals( requestVersion ) ) {
            schemaLocation = WFS_NS + " http://schemas.opengis.net/wfs/1.1.0/wfs.xsd";
        } else if ( VERSION_200.equals( requestVersion ) ) {
            schemaLocation = WFS_200_NS + " http://schemas.opengis.net/wfs/2.0.0/wfs.xsd";
        } else {
            throw new RuntimeException( "Internal error: Unhandled WFS version: " + requestVersion );
        }

        if ( requestedFts == null ) {
            requestedFts = service.getFeatureTypes();
        }

        QName[] requestedFtNames = new QName[requestedFts.size()];
        int i = 0;
        for ( FeatureType requestedFt : requestedFts ) {
            requestedFtNames[i++] = requestedFt.getName();
        }

        return schemaLocation + " " + WFSController.getSchemaLocation( requestVersion, requestedFtNames );
    }

    /**
     * Sets the content type header for the HTTP response.
     * 
     * TODO integrate handling for custom formats
     * 
     * @param outputFormat
     *            output format to be used, must not be <code>null</code>
     * @param response
     *            http response, must not be <code>null</code>
     */
    private void setContentType( GMLVersion outputFormat, HttpServletResponse response ) {

        switch ( outputFormat ) {
        case GML_2:
            response.setContentType( "text/xml; subtype=gml/2.1.2" );
            break;
        case GML_30:
            response.setContentType( "text/xml; subtype=gml/3.0.1" );
            break;
        case GML_31:
            response.setContentType( "text/xml; subtype=gml/3.1.1" );
            break;
        case GML_32:
            response.setContentType( "text/xml; subtype=gml/3.2.1" );
            break;
        }
    }

    /**
     * Determines the requested (GML) output format.
     * 
     * TODO integrate handling for custom formats
     * 
     * @param request
     *            request to be analyzed, must not be <code>null</code>
     * @return version to use for the written GML, never <code>null</code>
     * @throws OWSException
     *             if the requested format is not supported
     */
    private GMLVersion determineOutputFormat( GetFeature request )
                            throws OWSException {

        GMLVersion gmlVersion = null;

        Version requestVersion = request.getVersion();
        String outputFormat = request.getOutputFormat();
        if ( outputFormat == null ) {
            // default values for the different WFS version
            if ( request.getVersion() == VERSION_100 ) {
                gmlVersion = GMLVersion.GML_2;
            } else if ( request.getVersion() == VERSION_110 ) {
                gmlVersion = GMLVersion.GML_31;
            } else if ( request.getVersion() == VERSION_200 ) {
                gmlVersion = GMLVersion.GML_32;
            } else {
                throw new RuntimeException( "Internal error: Unhandled WFS version: " + requestVersion );
            }
        } else {
            if ( "text/xml; subtype=gml/2.1.2".equals( outputFormat ) || "GML2".equals( outputFormat ) ) {
                gmlVersion = GMLVersion.GML_2;
            } else if ( "text/xml; subtype=gml/3.1.1".equals( outputFormat ) ) {
                gmlVersion = GMLVersion.GML_31;
            } else if ( "text/xml; subtype=gml/3.2.1".equals( outputFormat ) ) {
                gmlVersion = GMLVersion.GML_32;
            } else {
                String msg = "Unsupported output format '" + outputFormat + "'";
                throw new OWSException( msg, OWSException.INVALID_PARAMETER_VALUE, "outputFormat" );
            }
        }
        return gmlVersion;
    }
}
