//$HeadURL: http://wald.intevation.org/svn/deegree/deegree3/services/trunk/src-api/org/deegree/services/controller/OGCFrontController.java $
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
package org.deegree.services.controller;

import java.beans.Introspector;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PushbackReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.imageio.spi.IIORegistry;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPHeader;
import org.apache.axiom.soap.impl.builder.StAXSOAPModelBuilder;
import org.apache.axiom.soap.impl.llom.soap11.SOAP11Factory;
import org.apache.axiom.soap.impl.llom.soap12.SOAP12Factory;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.LogManager;
import org.deegree.commons.configuration.JDBCConnections;
import org.deegree.commons.configuration.ProxyConfiguration;
import org.deegree.commons.jdbc.ConnectionManager;
import org.deegree.commons.types.ows.Version;
import org.deegree.commons.utils.DeegreeAALogoUtils;
import org.deegree.commons.utils.ProxyUtils;
import org.deegree.commons.utils.TempFileManager;
import org.deegree.commons.version.DeegreeModuleInfo;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.XMLProcessingException;
import org.deegree.crs.configuration.CRSConfiguration;
import org.deegree.services.controller.configuration.AllowedServices;
import org.deegree.services.controller.configuration.ConfiguredServicesType;
import org.deegree.services.controller.configuration.DeegreeServicesMetadata;
import org.deegree.services.controller.configuration.ServiceType;
import org.deegree.services.controller.ows.OWSException;
import org.deegree.services.controller.utils.HttpRequestWrapper;
import org.deegree.services.controller.utils.HttpResponseWrapper;
import org.deegree.services.controller.wcs.ServiceException120XMLAdapter;
import org.deegree.services.i18n.Messages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Acts as the single communication entry point and dispatcher to all deegree OGC web services (WMS, WFS, WCS, CSW, WPS,
 * SOS...).
 * <p>
 * Calls to {@link #doGet(HttpServletRequest, HttpServletResponse)} and
 * {@link #doPost(HttpServletRequest, HttpServletResponse)} are processed as follows:
 * <nl>
 * <li>The DCP-type of the incoming request is determined. This must be one of the following:
 * <ul>
 * <li>KVP</li>
 * <li>XML</li>
 * <li>SOAP (OGC style, the XML request is the child element of the SOAP body)</li>
 * </ul>
 * </li>
 * <li>The responsible {@link AbstractOGCServiceController} instance is determined and one of the following methods is
 * called:
 * <ul>
 * <li>{@link AbstractOGCServiceController#doKVP(Map, HttpRequestWrapper, HttpResponseWrapper, List)}</li>
 * <li>{@link AbstractOGCServiceController#doXML(XMLAdapter, HttpRequestWrapper, HttpResponseWrapper, List)}</li>
 * <li>
 * {@link AbstractOGCServiceController#doSOAP(SOAPEnvelope, HttpRequestWrapper, HttpResponseWrapper, List, SOAPFactory)}
 * </li>
 * </ul>
 * </li>
 * </nl>
 * </p>
 * 
 * @see AbstractOGCServiceController
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema </a>
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author: aschmitz $
 * 
 * @version $Revision: 21754 $, $Date: 2010-01-05 21:46:15 +0600 (Втр, 05 Янв 2010) $
 */
public class OGCFrontController extends HttpServlet {

    private static Logger LOG = LoggerFactory.getLogger( OGCFrontController.class );

    private static final long serialVersionUID = -1379869403008798932L;

    private static final Version SUPPORTED_CONFIG_VERSION = Version.parseVersion( "0.3.0" );

    /** used to decode (already URL-decoded) query strings */
    private static final String DEFAULT_ENCODING = "UTF-8";

    private static DeegreeServicesMetadata serviceConfig;

    private static final String DEFAULT_CONFIG_PATH = "WEB-INF/conf";

    private static final String DEFAULT_CONFIG_URL = DEFAULT_CONFIG_PATH + "/services_metadata.xml";

    // maps service names (e.g. 'WMS', 'WFS', ...) to responsible subcontrollers
    private static final Map<AllowedServices, AbstractOGCServiceController> serviceNameToController = new HashMap<AllowedServices, AbstractOGCServiceController>();

    // maps service namespaces (e.g. 'http://www.opengis.net/wms', 'http://www.opengis.net/wfs', ...) to the
    // responsible subcontrollers
    private static final Map<String, AbstractOGCServiceController> serviceNSToController = new HashMap<String, AbstractOGCServiceController>();

    // maps request names (e.g. 'GetMap', 'DescribeFeatureType') to the responsible subcontrollers
    private static final Map<String, AbstractOGCServiceController> requestNameToController = new HashMap<String, AbstractOGCServiceController>();

    private static final String defaultTMPDir = System.getProperty( "java.io.tmpdir" );

    private static final InheritableThreadLocal<RequestContext> CONTEXT = new InheritableThreadLocal<RequestContext>();

    /**
     * Returns the {@link RequestContext} associated with the calling thread.
     * <p>
     * NOTE: This method will only return a correct result if the calling thread originated in the
     * {@link #doGet(HttpServletRequest, HttpServletResponse)} or
     * {@link #doPost(HttpServletRequest, HttpServletResponse)} of this class (or has been spawned as a child thread by
     * such a thread).
     * </p>
     * 
     * @return the {@link RequestContext} associated with the calling thread
     */
    public static RequestContext getContext() {
        RequestContext context = CONTEXT.get();
        LOG.debug( "Retrieving RequestContext for current thread " + Thread.currentThread() + ": " + context );
        return context;
    }

    /**
     * Returns the HTTP URL for communicating with the OGCFrontController over the web (for POST requests).
     * <p>
     * NOTE: This method will only return a correct result if the calling thread originated in the
     * {@link #doGet(HttpServletRequest, HttpServletResponse)} or
     * {@link #doPost(HttpServletRequest, HttpServletResponse)} of this class (or has been spawned as a child thread by
     * such a thread).
     * </p>
     * 
     * @return the HTTP URL (for POST requests)
     */
    public static String getHttpPostURL() {
        String url = null;
        if ( serviceConfig.getDCP().getHTTPPost() != null ) {
            url = serviceConfig.getDCP().getHTTPPost();
        } else {
            url = getContext().getRequestedBaseURL();
        }
        return url;
    }

    /**
     * Returns the HTTP URL for communicating with the OGCFrontController over the web (for GET requests).
     * <p>
     * NOTE: This method will only return a correct result if the calling thread originated in the
     * {@link #doGet(HttpServletRequest, HttpServletResponse)} or
     * {@link #doPost(HttpServletRequest, HttpServletResponse)} of this class (or has been spawned as a child thread by
     * such a thread).
     * </p>
     * 
     * @return the HTTP URL (for GET requests)
     */
    public static String getHttpGetURL() {
        String url = null;
        if ( serviceConfig.getDCP().getHTTPGet() != null ) {
            url = serviceConfig.getDCP().getHTTPGet();
        } else {
            url = getContext().getRequestedBaseURL() + "?";
        }
        return url;
    }

    /**
     * Handles HTTP GET requests.
     * <p>
     * An HTTP GET request implies that input parameters are specified as key-value pairs. However, at least one OGC
     * service specification allows the sending of XML requests via GET (see WCS 1.0.0 specification, section 6.3.3). In
     * this case, the query string contains no <code>key=value</code> pairs, but the (URL encoded) xml. The encoding
     * ensures that no <code>=</code> char (parameter/value delimiters) occur in the string.
     */
    @SuppressWarnings("unchecked")
    @Override
    protected void doGet( HttpServletRequest request, HttpServletResponse response )
                            throws ServletException, IOException {
        long entryTime = System.currentTimeMillis();
        HttpRequestWrapper requestWrapper = new HttpRequestWrapper( request );

        if ( LOG.isDebugEnabled() ) {
            LOG.debug( "HTTP headers:" );
            Enumeration<String> headerEnum = request.getHeaderNames();
            while ( headerEnum.hasMoreElements() ) {
                String headerName = headerEnum.nextElement();
                LOG.debug( "- " + headerName + "='" + request.getHeader( headerName ) + "'" );
            }
        }

        String queryString = request.getQueryString();
        try {
            LOG.debug( "doGet(), query string: '" + queryString + "'" );

            if ( queryString == null ) {
                OWSException ex = new OWSException( "The request did not contain any parameters.",
                                                    "MissingParameterValue" );
                sendException( ex, response );
                return;
            }

            // handle as XML, if no "=" is found in the query string
            boolean isXML = !queryString.contains( "=" );
            List<FileItem> multiParts = checkAndRetrieveMultiparts( requestWrapper );
            if ( isXML ) {
                XMLAdapter requestDoc = null;
                String dummySystemId = "HTTP Get request from " + request.getRemoteAddr() + ":"
                                       + request.getRemotePort();
                if ( multiParts != null && multiParts.size() > 0 ) {
                    requestDoc = new XMLAdapter();
                    requestDoc.load( multiParts.get( 0 ).getInputStream() );
                    requestDoc.setSystemId( dummySystemId );
                } else {
                    // decode query string
                    String decodedString = URLDecoder.decode( queryString, DEFAULT_ENCODING );
                    StringReader reader = new StringReader( decodedString );
                    requestDoc = new XMLAdapter( reader, dummySystemId );
                }
                if ( isSOAPRequest( requestDoc ) ) {
                    dispatchSOAPRequest( requestDoc, requestWrapper, response, multiParts );
                } else {
                    dispatchXMLRequest( requestDoc, requestWrapper, response, multiParts );
                }
            } else {
                // for GET requests, there is no standard way for defining the used encoding
                Map<String, String> normalizedKVPParams = getNormalizedKVPMap( request.getQueryString(),
                                                                               DEFAULT_ENCODING );
                LOG.debug( "parameter map: " + normalizedKVPParams );
                dispatchKVPRequest( normalizedKVPParams, requestWrapper, response, multiParts );
            }
        } catch ( XMLProcessingException e ) {
            // the message might be more meaningful
            OWSException ex = new OWSException( "The request did not contain KVP parameters and no parseable XML.",
                                                "MissingParameterValue", "request" );
            sendException( ex, response );
            return;
        } catch ( Throwable e ) {
            LOG.debug( "Handling HTTP-GET request took: " + ( System.currentTimeMillis() - entryTime )
                       + " ms before sending exception." );
            LOG.debug( e.getMessage(), e );
            OWSException ex = new OWSException( e.getLocalizedMessage(), e, "InvalidRequest" );
            sendException( ex, response );
            return;
        }
        LOG.debug( "Handling HTTP-GET request with status 'success' took: " + ( System.currentTimeMillis() - entryTime )
                   + " ms." );
    }

    private Map<String, String> getNormalizedKVPMap( String queryString, String encoding )
                            throws UnsupportedEncodingException {

        Map<String, List<String>> keyToValueList = new HashMap<String, List<String>>();

        for ( String pair : queryString.split( "&" ) ) {
            // ignore empty key-values (prevents NPEs later)
            if ( pair.length() == 0 || !pair.contains( "=" ) ) {
                continue;
            }
            // NOTE: there may be more than one '=' character in pair, so the first one is taken as delimiter
            String[] parts = pair.split( "=", 2 );
            String key = parts[0];
            String value = null;
            if ( parts.length == 2 ) {
                value = parts[1];
            } else {
                if ( parts[0].endsWith( "=" ) ) {
                    value = "";
                }
            }
            List<String> values = keyToValueList.get( key );
            if ( values == null ) {
                values = new ArrayList<String>();
            }
            values.add( value );
            keyToValueList.put( key, values );
        }

        Map<String, String[]> keyToValueArray = new HashMap<String, String[]>();
        for ( String key : keyToValueList.keySet() ) {
            List<String> valueList = keyToValueList.get( key );
            String[] valueArray = new String[valueList.size()];
            valueList.toArray( valueArray );
            keyToValueArray.put( key, valueArray );
        }

        Map<String, String> kvpParamsUC = new HashMap<String, String>();
        for ( String key : keyToValueArray.keySet() ) {
            String[] values = keyToValueArray.get( key );
            if ( values != null && values.length > 0 ) {
                String decodedValue = URLDecoder.decode( values[0], encoding );
                kvpParamsUC.put( key.toUpperCase(), decodedValue );
            }
        }
        return kvpParamsUC;
    }

    /**
     * Handles HTTP POST requests.
     * <p>
     * An HTTP POST request specifies parameters in the request body. OGC service specifications use three different
     * ways to encode the parameters:
     * <ul>
     * <li><b>KVP</b>: Parameters are given as <code>key=value</code> pairs which are separated using the &amp;
     * character. This is equivalent to standard HTTP GET requests, except that the parameters are not encoded in the
     * query string, but in the POST body. In this case, the <code>content-type</code> field in the header must be
     * <code>application/x-www-form-urlencoded</code>.</li>
     * <li><b>XML</b>: The POST body contains an XML document. In this case, the <code>content-type</code> field in the
     * header has to be <code>text/xml</code>, but the implemenation does not rely on this in order to be more tolerant
     * to clients.</li>
     * <li><b>SOAP</b>: TODO</li>
     * <li><b>Multipart</b>: TODO</li>
     * </ul>
     */
    @SuppressWarnings("unchecked")
    @Override
    protected void doPost( HttpServletRequest request, HttpServletResponse response )
                            throws ServletException, IOException {

        if ( LOG.isDebugEnabled() ) {
            LOG.debug( "HTTP headers:" );
            Enumeration<String> headerEnum = request.getHeaderNames();
            while ( headerEnum.hasMoreElements() ) {
                String headerName = headerEnum.nextElement();
                LOG.debug( "- " + headerName + "='" + request.getHeader( headerName ) + "'" );
            }
        }

        LOG.debug( "doPost(), contentType: '" + request.getContentType() + "'" );
        long entryTime = System.currentTimeMillis();
        try {
            HttpRequestWrapper requestWrapper = new HttpRequestWrapper( request );
            // check if content-type implies that it's a KVP request
            String contentType = request.getContentType();
            boolean isKVP = false;
            if ( contentType != null ) {
                isKVP = request.getContentType().startsWith( "application/x-www-form-urlencoded" );
            }
            List<FileItem> multiParts = checkAndRetrieveMultiparts( requestWrapper );
            if ( isKVP ) {
                String queryString = readPostBodyAsString( requestWrapper );
                LOG.debug( "Treating POST input stream as KVP parameters. Raw input: '" + queryString + "'." );
                Map<String, String> normalizedKVPParams = null;
                String encoding = request.getCharacterEncoding();
                if ( encoding == null ) {
                    LOG.debug( "Request has no further encoding information. Defaulting to '" + DEFAULT_ENCODING + "'." );
                    normalizedKVPParams = getNormalizedKVPMap( queryString, DEFAULT_ENCODING );
                } else {
                    LOG.debug( "Client encoding information :" + encoding );
                    normalizedKVPParams = getNormalizedKVPMap( queryString, encoding );
                }
                dispatchKVPRequest( normalizedKVPParams, requestWrapper, response, multiParts );
            } else {
                // if( hanlde multiparts, get first mulitpart body(?).
                // body->requestDoc

                InputStream requestInputStream = null;
                if ( multiParts != null && multiParts.size() > 0 ) {
                    for ( int i = 0; i < multiParts.size() && requestInputStream == null; ++i ) {
                        FileItem item = multiParts.get( i );
                        if ( item != null ) {
                            LOG.debug( "Using multipart item: " + i + " with contenttype: " + item.getContentType()
                                       + " as the request." );
                            requestInputStream = item.getInputStream();
                        }
                    }
                } else {
                    requestInputStream = requestWrapper.getInputStream();
                }
                if ( requestInputStream == null ) {
                    String msg = "Could not create a valid inputstream from request "
                                 + ( ( multiParts != null && multiParts.size() > 0 ) ? "without" : "with" )
                                 + " multiparts.";
                    LOG.error( msg );
                    throw new IOException( msg );
                }

                PushbackReader pbr = new PushbackReader( new InputStreamReader( requestInputStream ), 1024 );
                int c = pbr.read();
                if ( c != 65279 && c != 65534 ) {
                    // no BOM (byte order mark)! push char back into reader
                    pbr.unread( c );
                } else {
                    throw new IOException( "Invalid Byte order mark." );
                }
                String dummySystemId = "HTTP Post request from " + request.getRemoteAddr() + ":"
                                       + request.getRemotePort();
                XMLAdapter requestDoc = new XMLAdapter( pbr, dummySystemId );
                if ( isSOAPRequest( requestDoc ) ) {
                    dispatchSOAPRequest( requestDoc, requestWrapper, response, multiParts );
                } else {
                    dispatchXMLRequest( requestDoc, requestWrapper, response, multiParts );
                }
            }
        } catch ( Throwable e ) {
            LOG.debug( "Handling HTTP-POST request took: " + ( System.currentTimeMillis() - entryTime )
                       + " ms before sending exception." );
            LOG.debug( e.getMessage(), e );
            OWSException ex = new OWSException( e.getLocalizedMessage(), "InvalidRequest" );
            sendException( ex, response );
        }
        LOG.debug( "Handling HTTP-POST request with status 'success' took: "
                   + ( System.currentTimeMillis() - entryTime ) + " ms." );
    }

    private String readPostBodyAsString( HttpServletRequest request )
                            throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        BufferedInputStream bis = new BufferedInputStream( request.getInputStream() );
        byte[] readBuffer = new byte[255];
        int numBytes = -1;
        while ( ( numBytes = bis.read( readBuffer ) ) != -1 ) {
            bos.write( readBuffer, 0, numBytes );
        }
        return bos.toString().trim();
    }

    /**
     * Checks if the given request is a multi part request. If so it will return the multiparts as a list of
     * {@link FileItem} else <code>null</code> will be returned.
     * 
     * @param request
     *            to check
     * @return a list of multiparts or <code>null</code> if it was not a multipart request.
     * @throws FileUploadException
     *             if there are problems reading/parsing the request or storing files.
     */
    @SuppressWarnings("unchecked")
    private List<FileItem> checkAndRetrieveMultiparts( HttpRequestWrapper request )
                            throws FileUploadException {
        List<FileItem> result = null;
        if ( ServletFileUpload.isMultipartContent( request ) ) {
            // Create a factory for disk-based file items
            FileItemFactory factory = new DiskFileItemFactory();
            LOG.debug( "The incoming request is a multipart request." );
            // Create a new file upload handler
            ServletFileUpload upload = new ServletFileUpload( factory );

            // Parse the request
            result = upload.parseRequest( request );
            LOG.debug( "The multipart request contains: " + result.size() + " items." );
            if ( LOG.isDebugEnabled() ) {
                for ( FileItem item : result ) {
                    LOG.debug( item.toString() );
                }
            }
        }
        return result;
    }

    /**
     * Dispatches a KVP request to the responsible {@link AbstractOGCServiceController}. Both GET and POST are handled
     * by this method.
     * <p>
     * The responsible {@link AbstractOGCServiceController} is identified according to this strategy:
     * <nl>
     * <li>If a <code>SERVICE</code> attribute is present, it is used to determine the controller.</li>
     * <li>If no <code>SERVICE</code> attribute is present, the value of the <code>REQUEST</code> attribute is taken to
     * determine the controller.</li>
     * </nl>
     * </p>
     * 
     * @param normalizedKVPParams
     * @param requestWrapper
     * @param response
     * @param multiParts
     * @throws ServletException
     * @throws IOException
     */
    private void dispatchKVPRequest( Map<String, String> normalizedKVPParams, HttpRequestWrapper requestWrapper,
                                     HttpServletResponse response, List<FileItem> multiParts )
                            throws ServletException, IOException {

        // extract (deegree specific) security information and bind to current thread
        String user = normalizedKVPParams.get( "USER" );
        String password = normalizedKVPParams.get( "PASSWORD" );
        String tokenId = normalizedKVPParams.get( "SESSIONID" );
        bindContextToThread( requestWrapper, user, password, tokenId );

        AbstractOGCServiceController subController = null;
        // first try service parameter, SERVICE-parameter is mandatory for each service and request (except WMS 1.0.0)
        String service = normalizedKVPParams.get( "SERVICE" );
        String request = normalizedKVPParams.get( "REQUEST" );
        if ( service != null ) {
            try {
                subController = determineResponsibleControllerByServiceName( service );
            } catch ( IllegalArgumentException e ) {
                // I know that the SOS tests test for the appropriate service exception here, so sending a OWS commons
                // 1.1 one should be fine
                OWSException ex = new OWSException( Messages.get( "CONTROLLER_INVALID_SERVICE", service ),
                                                    "InvalidParameterValue", "service" );
                sendException( ex, response );
                return;
            }
        } else {
            // dispatch according to REQUEST-parameter
            if ( request != null ) {
                subController = determineResponsibleControllerByRequestName( request );
            }
        }
        if ( subController != null ) {
            LOG.debug( "Dispatching request to subcontroller class: " + subController.getClass().getName() );
            HttpResponseWrapper responseWrapper = new HttpResponseWrapper( response );
            long dispatchTime = FrontControllerStats.requestDispatched();
            try {
                subController.doKVP( normalizedKVPParams, requestWrapper, responseWrapper, multiParts );
            } finally {
                FrontControllerStats.requestFinished( dispatchTime );
            }
            if ( LOG.isDebugEnabled() ) {
                validateResponse( responseWrapper );
            }
            responseWrapper.flushBuffer();
        } else {
            String msg = null;
            if ( service == null && request == null ) {
                msg = "Neither 'SERVICE' nor 'REQUEST' parameter is present. Cannot determine responsible subcontroller.";
            } else {
                msg = "Unable to determine the subcontroller for request type '" + request + "' and service type '"
                      + service + "'.";
            }
            OWSException ex = new OWSException( msg, "MissingParameterValue", "service" );
            sendException( ex, response );
        }
    }

    private void validateResponse( HttpResponseWrapper responseWrapper ) {
        responseWrapper.validate();
    }

    /**
     * Dispatches an XML request to the responsible {@link AbstractOGCServiceController}. Both GET and POST are handled
     * by this method.
     * <p>
     * The responsible {@link AbstractOGCServiceController} is identified by the namespace of the root element.
     * 
     * @param requestDoc
     * @param requestWrapper
     * @param response
     * @param multiParts
     * @throws ServletException
     * @throws IOException
     */
    private void dispatchXMLRequest( XMLAdapter requestDoc, HttpRequestWrapper requestWrapper,
                                     HttpServletResponse response, List<FileItem> multiParts )
                            throws ServletException, IOException {

        OMElement rootElement = requestDoc.getRootElement();

        // TODO this produces problems if the XML is not well-formed
        LOG.debug( "The request was\n{}", rootElement );

        // extract (deegree specific) security information and bind to current thread
        String user = rootElement.getAttributeValue( new QName( "user" ) );
        String password = rootElement.getAttributeValue( new QName( "password" ) );
        String tokenId = rootElement.getAttributeValue( new QName( "sessionId" ) );
        bindContextToThread( requestWrapper, user, password, tokenId );

        String ns = rootElement.getNamespace().getNamespaceURI();
        AbstractOGCServiceController subcontroller = determineResponsibleControllerByNS( ns );
        if ( subcontroller != null ) {
            LOG.debug( "Dispatching request to subcontroller class: " + subcontroller.getClass().getName() );
            HttpResponseWrapper responseWrapper = new HttpResponseWrapper( response );
            long dispatchTime = FrontControllerStats.requestDispatched();
            try {
                subcontroller.doXML( requestDoc, requestWrapper, responseWrapper, multiParts );
            } finally {
                FrontControllerStats.requestFinished( dispatchTime );
            }
            if ( LOG.isDebugEnabled() ) {
                validateResponse( responseWrapper );
            }
            responseWrapper.flushBuffer();
        } else {
            String msg = "No subcontroller for request namespace '" + ns + "' available.";
            throw new ServletException( msg );
        }

    }

    /**
     * Dispatches a SOAP request to the responsible {@link AbstractOGCServiceController}. Both GET and POST are handled
     * by this method.
     * <p>
     * The responsible {@link AbstractOGCServiceController} is identified by the namespace of the first child of the
     * SOAP body element.
     * 
     * @param requestDoc
     * @param requestWrapper
     * @param response
     * @param multiParts
     * @throws ServletException
     * @throws IOException
     */
    private void dispatchSOAPRequest( XMLAdapter requestDoc, HttpRequestWrapper requestWrapper,
                                      HttpServletResponse response, List<FileItem> multiParts )
                            throws ServletException, IOException {

        LOG.debug( "Handling soap request." );
        OMElement root = requestDoc.getRootElement();
        SOAPFactory factory = null;
        String ns = root.getNamespace().getNamespaceURI();
        if ( "http://schemas.xmlsoap.org/soap/envelope/".equals( ns ) ) {
            factory = new SOAP11Factory();
        } else {
            factory = new SOAP12Factory();
        }

        StAXSOAPModelBuilder soap = new StAXSOAPModelBuilder( root.getXMLStreamReaderWithoutCaching(), factory,
                                                              factory.getSoapVersionURI() );

        SOAPEnvelope envelope = soap.getSOAPEnvelope();

        // extract (deegree specific) security information and bind to current thread
        String user = null;
        String password = null;
        String tokenId = null;
        SOAPHeader header = envelope.getHeader();
        if ( header != null ) {
            OMElement userElement = header.getFirstChildWithName( new QName( "http://www.deegree.org/security", "user" ) );
            if ( userElement != null ) {
                user = userElement.getText();
            }
            OMElement passwordElement = header.getFirstChildWithName( new QName( "http://www.deegree.org/security",
                                                                                 "password" ) );
            if ( passwordElement != null ) {
                password = passwordElement.getText();
            }
            OMElement sessionIdElement = header.getFirstChildWithName( new QName( "http://www.deegree.org/security",
                                                                                  "sessionId" ) );
            if ( sessionIdElement != null ) {
                tokenId = sessionIdElement.getText();
            }
        }
        bindContextToThread( requestWrapper, user, password, tokenId );

        AbstractOGCServiceController subcontroller = determineResponsibleControllerByNS( envelope.getSOAPBodyFirstElementNS().getNamespaceURI() );
        if ( subcontroller != null ) {
            LOG.debug( "Dispatching request to subcontroller class: " + subcontroller.getClass().getName() );
            HttpResponseWrapper responseWrapper = new HttpResponseWrapper( response );
            long dispatchTime = FrontControllerStats.requestDispatched();
            try {
                subcontroller.doSOAP( envelope, requestWrapper, responseWrapper, multiParts, factory );
            } finally {
                FrontControllerStats.requestFinished( dispatchTime );
            }
            if ( LOG.isDebugEnabled() ) {
                validateResponse( responseWrapper );
            }
            responseWrapper.flushBuffer();
        } else {
            String msg = "No subcontroller for request namespace '"
                         + envelope.getSOAPBodyFirstElementNS().getNamespaceURI() + "' available.";
            throw new ServletException( msg );
        }
    }

    private boolean isSOAPRequest( XMLAdapter requestDoc )
                            throws ServletException {
        if ( requestDoc.getRootElement() == null ) {
            String msg = "Request document does not contain a root element.";
            throw new ServletException( msg );
        }
        String ns = requestDoc.getRootElement().getNamespace().getNamespaceURI();
        String localName = requestDoc.getRootElement().getLocalName();
        return ( "http://schemas.xmlsoap.org/soap/envelope/".equals( ns ) || "http://www.w3.org/2003/05/soap-envelope".equals( ns ) )
               && "Envelope".equals( localName );
    }

    /**
     * Determines the {@link AbstractOGCServiceController} that is responsible for handling requests to a certain
     * service type, e.g. WMS, WFS.
     * 
     * @param serviceType
     *            service type code, e.g. "WMS" or "WFS"
     * @return responsible <code>SecuredSubController</code> or null, if no responsible controller was found
     */
    private AbstractOGCServiceController determineResponsibleControllerByServiceName( String serviceType ) {
        AllowedServices service = AllowedServices.fromValue( serviceType );
        if ( service == null ) {
            return null;
        }
        return serviceNameToController.get( service );
    }

    /**
     * Determines the {@link AbstractOGCServiceController} that is responsible for handling requests with a certain
     * name, e.g. GetMap, GetFeature.
     * 
     * @param requestName
     *            request name, e.g. "GetMap" or "GetFeature"
     * @return responsible <code>SecuredSubController</code> or null, if no responsible controller was found
     */
    private AbstractOGCServiceController determineResponsibleControllerByRequestName( String requestName ) {
        return requestNameToController.get( requestName );
    }

    /**
     * Determines the {@link AbstractOGCServiceController} that is responsible for handling requests to a certain
     * service type, e.g. WMS, WFS.
     * 
     * @param ns
     *            service type code, e.g. "WMS" or "WFS"
     * @return responsible <code>SecuredSubController</code> or null, if no responsible controller was found
     */
    private AbstractOGCServiceController determineResponsibleControllerByNS( String ns ) {
        return serviceNSToController.get( ns );
    }

    /**
     * Return all active service controllers.
     * 
     * @return the instance of the requested service used by OGCFrontController, or null if the service is not
     *         registered.
     */
    public static Map<String, AbstractOGCServiceController> getServiceControllers() {
        Map<String, AbstractOGCServiceController> nameToController = new HashMap<String, AbstractOGCServiceController>();
        for ( AllowedServices serviceName : serviceNameToController.keySet() ) {
            nameToController.put( serviceName.value(), serviceNameToController.get( serviceName ) );
        }
        return nameToController;
    }

    /**
     * Returns the service controller instance based on the class of the service controller.
     * 
     * @param c
     *            class of the requested service controller, e.g. <code>WPSController.getClass()</code>
     * @return the instance of the requested service used by OGCFrontController, or null if no such service controller
     *         is active
     */
    public static AbstractOGCServiceController getServiceController( Class<? extends AbstractOGCServiceController> c ) {
        AbstractOGCServiceController result = null;
        for ( AbstractOGCServiceController it : serviceNSToController.values() ) {
            if ( c == it.getClass() ) {
                result = it;
                break;
            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void init( ServletConfig config )
                            throws ServletException {
        try {
            super.init( config );
            String configURL = getInitParameter( "ServicesConfiguration" );
            if ( configURL == null ) {
                configURL = DEFAULT_CONFIG_URL;
            }

            LOG.info( "--------------------------------------------------------------------------------" );
            DeegreeAALogoUtils.logInfo( LOG );
            LOG.info( "--------------------------------------------------------------------------------" );

            URL resolvedConfigURL = null;
            try {
                resolvedConfigURL = resolveFileLocation( configURL, config.getServletContext() );
            } catch ( MalformedURLException e ) {
                throw new ServletException( "Resolving of parameter 'ServicesConfiguration' failed: "
                                            + e.getLocalizedMessage(), e );
            }
            if ( resolvedConfigURL == null ) {
                throw new ServletException( "Resolving service configuration url failed!" );
            }

            unmarshallConfiguration( resolvedConfigURL );

            Version configVersion = Version.parseVersion( serviceConfig.getConfigVersion() );
            if ( !configVersion.equals( SUPPORTED_CONFIG_VERSION ) ) {
                String msg = "The main service configuration file '" + resolvedConfigURL
                             + " uses configuration format version " + serviceConfig.getConfigVersion()
                             + ", but this deegree version only supports version " + SUPPORTED_CONFIG_VERSION
                             + ". Information on resolving this issue can be found at "
                             + "'https://wiki.deegree.org/deegreeWiki/deegree3/ConfigurationVersions'. ";
                LOG.debug( "********************************************************************************" );
                LOG.error( msg );
                LOG.debug( "********************************************************************************" );
                throw new ServletException( msg );
            }

            LOG.info( "deegree modules" );
            LOG.info( "--------------------------------------------------------------------------------" );
            LOG.info( "" );
            for ( DeegreeModuleInfo moduleInfo : DeegreeModuleInfo.getRegisteredModules() ) {
                LOG.info( " - " + moduleInfo.toString() );
            }
            LOG.info( "" );
            LOG.info( "--------------------------------------------------------------------------------" );
            LOG.info( "System info" );
            LOG.info( "--------------------------------------------------------------------------------" );
            LOG.info( "" );
            // LOG.info( "- context : " + getServletContext().getServletContextName() );
            // LOG.info( "- real path : " + getServletContext().getRealPath( "/" ) );
            LOG.info( "- java version      : " + System.getProperty( "java.version" ) + " ("
                      + System.getProperty( "java.vendor" ) + ")" );
            LOG.info( "- operating system  : " + System.getProperty( "os.name" ) + " ("
                      + System.getProperty( "os.version" ) + ", " + System.getProperty( "os.arch" ) + ")" );
            LOG.info( "- default encoding  : " + DEFAULT_ENCODING );
            LOG.info( "- temp directory    : " + defaultTMPDir );
            LOG.info( "" );

            LOG.info( "--------------------------------------------------------------------------------" );
            LOG.info( "Proxy configuration." );
            LOG.info( "--------------------------------------------------------------------------------" );
            ProxyConfiguration proxyConfig = serviceConfig.getProxyConfiguration();
            try {
                if ( proxyConfig != null ) {
                    ProxyUtils.setupProxyParameters( proxyConfig );
                }
                ProxyUtils.logProxyConfiguration( LOG );
            } catch ( Exception e ) {
                e.printStackTrace();
            }
            LOG.info( "" );
            LOG.info( "--------------------------------------------------------------------------------" );
            LOG.info( "Setting up temporary file storage." );
            LOG.info( "--------------------------------------------------------------------------------" );
            TempFileManager.init( config.getServletContext().getContextPath() );
            LOG.info( "" );

            LOG.info( "--------------------------------------------------------------------------------" );
            LOG.info( "Setting up JDBC connection pools." );
            LOG.info( "--------------------------------------------------------------------------------" );
            JDBCConnections jdbcConfig = serviceConfig.getJDBCConnections();
            if ( jdbcConfig != null ) {
                try {
                    ConnectionManager.addConnections( jdbcConfig );
                } catch ( Exception e ) {
                    String msg = "Setting up JDBC connection pools failed: " + e.getLocalizedMessage();
                    LOG.error( msg, e );
                    throw new ServletException( msg, e );
                }
            } else {
                LOG.info( "No JDBC connections defined." );
            }
            LOG.info( "" );

            LOG.info( "--------------------------------------------------------------------------------" );
            LOG.info( "Starting webservices." );
            LOG.info( "--------------------------------------------------------------------------------" );
            LOG.info( "" );

            ConfiguredServicesType servicesConfigured = serviceConfig.getConfiguredServices();
            List<ServiceType> services = null;
            if ( servicesConfigured != null ) {
                services = servicesConfigured.getService();
                if ( services != null && services.size() > 0 ) {
                    LOG.info( "The file: " + resolvedConfigURL );
                    LOG.info( "Provided following services:" );
                    for ( ServiceType s : services ) {
                        URL configLocation = null;
                        try {
                            configLocation = new URL( resolvedConfigURL, s.getConfigurationLocation() );
                        } catch ( MalformedURLException e ) {
                            LOG.error( e.getMessage(), e );
                            return;
                        }
                        s.setConfigurationLocation( configLocation.toExternalForm() );

                        LOG.info( " - " + s.getServiceName() );
                    }
                    LOG.info( "ATTENTION - Skipping the loading of all services in conf/ which are not listed above." );
                }
            }
            if ( services == null || services.size() == 0 ) {
                LOG.info( "No service elements were supplied in the file: '" + resolvedConfigURL
                          + "' -- trying to use the default loading mechanism." );
                try {
                    services = loadServicesFromDefaultLocation();
                } catch ( MalformedURLException e ) {
                    throw new ServletException( "Error loading service configurations: " + e.getMessage() );
                }
            }
            if ( services.size() == 0 ) {
                throw new ServletException(
                                            "No deegree web services could be loaded (manually or automatically) please take a look at your configuration file: "
                                                                    + resolvedConfigURL
                                                                    + " and or your WEB-INF/conf directory." );
            }

            for ( ServiceType configuredService : services ) {
                AbstractOGCServiceController serviceController = instantiateServiceController( configuredService );
                if ( serviceController != null ) {
                    registerSubController( configuredService, serviceController );
                }
            }
            LOG.info( "" );
            LOG.info( "--------------------------------------------------------------------------------" );
            LOG.info( "Webservices started." );
            LOG.info( "--------------------------------------------------------------------------------" );
        } catch ( NoClassDefFoundError e ) {
            LOG.error( "Initialization failed!" );
            LOG.error( "You probably forgot to add a required .jar to the WEB-INF/lib directory." );
            LOG.error( "The resource that could not be found was '{}'.", e.getMessage() );
            LOG.debug( "Stack trace:", e );
        } catch ( Exception e ) {
            LOG.error( "Initialization failed!" );
            LOG.error( "An unexpected error was caught:", e );
        }
    }

    @Override
    public void destroy() {
        super.destroy();
        LOG.info( "--------------------------------------------------------------------------------" );
        LOG.info( "Shutting down deegree in context '" + getServletContext().getServletContextName() + "'..." );
        for ( AllowedServices serviceName : serviceNameToController.keySet() ) {
            AbstractOGCServiceController subcontroller = serviceNameToController.get( serviceName );
            LOG.info( "Shutting down '" + serviceName + "'." );
            try {
                subcontroller.destroy();
            } catch ( Exception e ) {
                String msg = "Error destroying subcontroller '" + subcontroller.getClass().getName() + "': "
                             + e.getMessage();
                LOG.error( msg, e );
            }
        }
        LOG.info( "deegree OGC webservices shut down." );
        ConnectionManager.destroy();
        LOG.info( "--------------------------------------------------------------------------------" );
        plugClassLoaderLeaks();
    }

    private void registerSubController( ServiceType configuredService, AbstractOGCServiceController serviceController ) {

        // associate service name (abbreviation) with controller instance
        LOG.debug( "Service name '" + configuredService.getServiceName() + "' -> '"
                   + serviceController.getClass().getSimpleName() + "'" );
        serviceNameToController.put( configuredService.getServiceName(), serviceController );

        // associate request types with controller instance
        for ( String request : serviceController.getHandledRequests() ) {
            // skip GetCapabilities requests
            if ( !( "GetCapabilities".equals( request ) ) ) {
                LOG.debug( "Request type '" + request + "' -> '" + serviceController.getClass().getSimpleName() + "'" );
                requestNameToController.put( request, serviceController );
            }
        }

        // associate namespaces with controller instance
        for ( String ns : serviceController.getHandledNamespaces() ) {
            LOG.debug( "Namespace '" + ns + "' -> '" + serviceController.getClass().getSimpleName() + "'" );
            serviceNSToController.put( ns, serviceController );
        }
    }

    /**
     * Apply workarounds for classloader leaks, see <a
     * href="https://wiki.deegree.org/deegreeWiki/ClassLoaderLeaks">ClassLoaderLeaks in deegree wiki</a>.
     */
    private void plugClassLoaderLeaks() {

        // deregister all JDBC drivers loaded by webapp classloader
        Enumeration<Driver> e = DriverManager.getDrivers();
        while ( e.hasMoreElements() ) {
            Driver driver = e.nextElement();
            try {
                if ( driver.getClass().getClassLoader() == getClass().getClassLoader() )
                    DriverManager.deregisterDriver( driver );
            } catch ( SQLException e1 ) {
                LOG.error( "Cannot unload driver: " + driver );
            }
        }

        LogFactory.releaseAll();
        LogManager.shutdown();

        // SLF4JLogFactory.releaseAll(); // should be the same as the LogFactory.releaseAll call
        // IIORegistry.getDefaultInstance().deregisterAll(); // breaks ImageIO
        Iterator<Class<?>> i = IIORegistry.getDefaultInstance().getCategories();
        while ( i.hasNext() ) {
            Class<?> c = i.next();
            Iterator<?> k = IIORegistry.getDefaultInstance().getServiceProviders( c, false );
            while ( k.hasNext() ) {
                Object o = k.next();
                if ( o.getClass().getClassLoader() == getClass().getClassLoader() ) {
                    IIORegistry.getDefaultInstance().deregisterServiceProvider( o );
                    LOG.debug( "Deregistering " + o );
                }
            }
        }

        Introspector.flushCaches();

        // just clear the configurations for now, it does not hurt
        CRSConfiguration.DEFINED_CONFIGURATIONS.clear();
    }

    /**
     * Creates an instance of a sub controller which is valid for the given configured Service, by applying following
     * conventions:
     * <ul>
     * <li>The sub controller must extend {@link AbstractOGCServiceController}</li>
     * <li>The package of the controller is the package of this class.[SERVICE_ABBREV_lower_case]</li>
     * <li>The name of the controller must be [SERVICE_NAME_ABBREV]Controller</li>
     * <li>The controller must have a constructor with a String parameter</li>
     * </ul>
     * If all above conditions are met, the instantiated controller will be returned else <code>null</code>
     * 
     * @param configuredService
     * @return the instantiated secured sub controller or <code>null</code> if an error occurred.
     */
    @SuppressWarnings("unchecked")
    private AbstractOGCServiceController instantiateServiceController( ServiceType configuredService ) {
        AbstractOGCServiceController subController = null;
        if ( configuredService == null ) {
            return subController;
        }

        final String serviceName = configuredService.getServiceName().name();
        final String packageName = OGCFrontController.class.getPackage().getName();

        // something like org.deegree.services.controller.wfs.WFSController
        String controller = packageName + "." + serviceName.toLowerCase() + "." + serviceName + "Controller";

        LOG.info( "" );
        LOG.info( "--------------------------------------------------------------------------------" );
        LOG.info( "Starting " + serviceName + "." );
        LOG.info( "--------------------------------------------------------------------------------" );
        LOG.info( "Configuration file: '" + configuredService.getConfigurationLocation() + "'" );
        if ( configuredService.getControllerClass() != null ) {
            LOG.info( "Using custom controller class '{}'.", configuredService.getControllerClass() );
            controller = configuredService.getControllerClass();
        }
        try {
            long time = System.currentTimeMillis();
            Class<AbstractOGCServiceController> subControllerClass = (Class<AbstractOGCServiceController>) Class.forName(
                                                                                                                          controller,
                                                                                                                          false,
                                                                                                                          OGCFrontController.class.getClassLoader() );
            subController = subControllerClass.newInstance();
            XMLAdapter controllerConf = new XMLAdapter( new URL( configuredService.getConfigurationLocation() ) );
            subController.init( controllerConf, serviceConfig );
            LOG.info( "" );
            // round to exactly two decimals, I think their should be a java method for this though
            double startupTime = Math.round( ( ( System.currentTimeMillis() - time ) * 0.1 ) ) * 0.01;
            LOG.info( serviceName + " startup successful (took: " + startupTime + " seconds)" );
        } catch ( Exception e ) {
            LOG.error( "Initializing " + serviceName + " failed: " + e.getMessage(), e );
            LOG.info( "" );
            LOG.info( serviceName + " startup failed." );
            subController = null;
        }
        return subController;
    }

    /**
     * Iterates over all directories in the conf/ directory and returns the service/configuration mappings as a list.
     * This default service loading mechanism implies the following directory structure:
     * <ul>
     * <li>conf/</li>
     * <li>conf/[SERVICE_NAME]/ (upper-case abbreviation of a deegree web service, please take a look at
     * {@link AllowedServices})</li>
     * <li>conf/[SERVICE_NAME]/[SERVICE_NAME]_configuration.xml</li>
     * </ul>
     * If all conditions are met the service type is added to resulting list. If none of the underlying directories meet
     * above criteria, an empty list will be returned.
     * 
     * @return the list of services found in the conf directory. Or an empty list if the above conditions are not met
     *         for any directory in the conf directory.
     * @throws MalformedURLException
     */
    private List<ServiceType> loadServicesFromDefaultLocation()
                            throws MalformedURLException {
        File configurationDir = null;
        try {
            configurationDir = new File( resolveFileLocation( DEFAULT_CONFIG_PATH, getServletContext() ).toURI() );
        } catch ( MalformedURLException e ) {
            LOG.error( e.getMessage(), e );
        } catch ( URISyntaxException e ) {
            LOG.error( e.getMessage(), e );
        }
        List<ServiceType> loadedServices = new ArrayList<ServiceType>();
        if ( configurationDir == null || !configurationDir.isDirectory() ) {
            LOG.error( "Could not read from the default configuration directory (" + DEFAULT_CONFIG_PATH
                       + ") because it is not a directory." );
            return loadedServices;

        }
        LOG.info( "Using default directory: " + configurationDir.getAbsolutePath()
                  + " to scan for webservice configurations." );
        File[] files = configurationDir.listFiles();
        if ( files == null || files.length == 0 ) {
            LOG.error( "No files found in default configuration directory, hence no services to load." );
            return loadedServices;
        }
        for ( File f : files ) {
            if ( f.isDirectory() ) {
                String tmp = f.getName();
                if ( tmp != null && !"".equals( tmp.trim() ) ) {
                    tmp = tmp.trim().toUpperCase();
                    AllowedServices as;
                    try {
                        as = AllowedServices.fromValue( tmp );
                    } catch ( IllegalArgumentException ex ) {
                        LOG.warn( "The directory '"
                                  + tmp
                                  + "' found in the configuration directory is not a valid deegree webservice, so skipping it." );
                        continue;
                    }
                    LOG.debug( "Trying to create a frontcontroller for service: " + tmp
                               + " found in the configuration directory." );
                    final String allowedName = tmp + "_configuration.xml";
                    File[] configFiles = f.listFiles( new FilenameFilter() {
                        public boolean accept( File dir, String name ) {
                            return allowedName.equalsIgnoreCase( name );
                        }
                    } );
                    if ( configFiles == null || configFiles.length == 0 ) {
                        LOG.warn( "The directory: " + f.getAbsolutePath() + ", does not contain a file : " + tmp
                                  + "_configuration.xml, hence no service will be loaded from this location." );
                    } else {
                        ServiceType configuredService = new ServiceType();
                        configuredService.setConfigurationLocation( configFiles[0].toURI().toURL().toString() );
                        configuredService.setServiceName( as );
                        loadedServices.add( configuredService );
                    }
                }
            }

        }

        return loadedServices;
    }

    /**
     * Unmarshalls the configuration file with a little help from jaxb.
     * 
     * @param resolvedConfigURL
     *            pointing to the configuration file.
     * @throws ServletException
     */
    private synchronized void unmarshallConfiguration( URL resolvedConfigURL )
                            throws ServletException {
        if ( serviceConfig == null ) {
            try {
                String contextName = OGCFrontController.class.getPackage().getName() + ".configuration";
                JAXBContext jc = JAXBContext.newInstance( contextName );
                Unmarshaller unmarshaller = jc.createUnmarshaller();
                serviceConfig = (DeegreeServicesMetadata) unmarshaller.unmarshal( resolvedConfigURL );
            } catch ( JAXBException e ) {
                String msg = "Could not unmarshall frontcontroller configuration: " + e.getMessage();
                LOG.error( msg, e );
                throw new ServletException( msg, e );
            }
        }
    }

    /**
     * 'Heuristical' method to retrieve the {@link URL} for a file referenced from an init-param of a webapp config file
     * which may be:
     * <ul>
     * <li>a (absolute) <code>URL</code></li>
     * <li>a file location</li>
     * <li>a (relative) URL which in turn is resolved using <code>ServletContext.getRealPath</code></li>
     * </ul>
     * 
     * @param location
     * @param context
     * @return the full (and whitespace-escaped) URL
     * @throws MalformedURLException
     */
    private URL resolveFileLocation( String location, ServletContext context )
                            throws MalformedURLException {
        URL serviceConfigurationURL = null;

        LOG.debug( "Resolving configuration file location: '" + location + "'..." );
        try {
            // construction of URI performs whitespace escaping
            serviceConfigurationURL = new URI( location ).toURL();
        } catch ( Exception e ) {
            LOG.debug( "No valid (absolute) URL. Trying context.getRealPath() now." );
            String realPath = context.getRealPath( location );
            if ( realPath == null ) {
                LOG.debug( "No 'real path' available. Trying to parse as a file location now." );
                serviceConfigurationURL = new File( location ).toURI().toURL();
            } else {
                try {
                    // realPath may either be a URL or a File
                    serviceConfigurationURL = new URI( realPath ).toURL();
                } catch ( Exception e2 ) {
                    LOG.debug( "'Real path' cannot be parsed as URL. Trying to parse as a file location now." );
                    // construction of URI performs whitespace escaping
                    serviceConfigurationURL = new File( realPath ).toURI().toURL();
                    LOG.debug( "serviceConfigurationURL: " + serviceConfigurationURL );
                }
            }
        }
        return serviceConfigurationURL;
    }

    private void bindContextToThread( HttpServletRequest request, String username, String password, String tokenId ) {
        RequestContext context = new RequestContext( request, username, password, tokenId );
        CONTEXT.set( context );
        LOG.debug( "Initialized RequestContext for Thread " + Thread.currentThread() + "=" + context );
    }

    /**
     * Sends an exception report to the client.
     * <p>
     * NOTE: Usually, exception reports are generated by the specific service controller. This method is only used when
     * the request is so broken that it cannot be dispatched.
     * </p>
     * 
     * @param e
     *            exception to be serialized
     * @param res
     *            response object
     * @throws ServletException
     */
    private void sendException( OWSException e, HttpServletResponse res )
                            throws ServletException {
        Collection<AbstractOGCServiceController> values = serviceNameToController.values();
        if ( values.size() > 0 ) {
            AbstractOGCServiceController first = values.iterator().next();
            AbstractOGCServiceController.sendException( "application/vnd.ogc.se_xml", "UTF-8", null, 200,
                                                        first.getExceptionSerializer(), e, res );
        } else {
            AbstractOGCServiceController.sendException( "application/vnd.ogc.se_xml", "UTF-8", null, 200,
                                                        new ServiceException120XMLAdapter(), e, res );
        }
    }
}
