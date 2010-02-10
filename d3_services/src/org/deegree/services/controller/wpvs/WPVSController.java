//$HeadURL: http://wald.intevation.org/svn/deegree/deegree3/services/trunk/src/org/deegree/services/controller/wpvs/WPVSController.java $
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

package org.deegree.services.controller.wpvs;

import static javax.xml.stream.XMLOutputFactory.IS_REPAIRING_NAMESPACES;
import static org.deegree.protocol.wpvs.WPVSConstants.VERSION_040;
import static org.deegree.protocol.wpvs.WPVSConstants.WPVS_NS;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.servlet.ServletException;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.axiom.om.OMElement;
import org.apache.commons.fileupload.FileItem;
import org.deegree.commons.types.ows.Version;
import org.deegree.commons.utils.kvp.KVPUtils;
import org.deegree.commons.utils.kvp.MissingParameterException;
import org.deegree.commons.xml.NamespaceContext;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.XPath;
import org.deegree.commons.xml.stax.FormattingXMLStreamWriter;
import org.deegree.protocol.ows.capabilities.GetCapabilities;
import org.deegree.protocol.ows.capabilities.GetCapabilitiesKVPParser;
import org.deegree.protocol.wpvs.WPVSConstants.WPVSRequestType;
import org.deegree.rendering.r3d.opengl.JOGLChecker;
import org.deegree.services.controller.ImplementationMetadata;
import org.deegree.services.controller.AbstractOGCServiceController;
import org.deegree.services.controller.configuration.DCPType;
import org.deegree.services.controller.configuration.DeegreeServicesMetadata;
import org.deegree.services.controller.configuration.ServiceIdentificationType;
import org.deegree.services.controller.configuration.ServiceProviderType;
import org.deegree.services.controller.exception.ControllerException;
import org.deegree.services.controller.exception.ControllerInitException;
import org.deegree.services.controller.ows.OWSException;
import org.deegree.services.controller.ows.OWSException110XMLAdapter;
import org.deegree.services.controller.utils.HttpRequestWrapper;
import org.deegree.services.controller.utils.HttpResponseWrapper;
import org.deegree.services.controller.wpvs.capabilities.CapabilitiesXMLAdapter;
import org.deegree.services.controller.wpvs.configuration.PublishedInformation;
import org.deegree.services.controller.wpvs.configuration.PublishedInformation.AllowedOperations;
import org.deegree.services.controller.wpvs.getview.GetView;
import org.deegree.services.controller.wpvs.getview.GetViewKVPAdapter;
import org.deegree.services.controller.wpvs.getview.GetViewResponseParameters;
import org.deegree.services.exception.ServiceInitException;
import org.deegree.services.wpvs.PerspectiveViewService;
import org.deegree.services.wpvs.configuration.ServiceConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The <code>WPVSController</code> class currently implements the 1.0.0 draft of the WPVS.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author: mschneider $
 * 
 * @version $Revision: 20008 $, $Date: 2009-10-06 22:37:55 +0700 (Втр, 06 Окт 2009) $
 */
public class WPVSController extends AbstractOGCServiceController {

    private final static Logger LOG = LoggerFactory.getLogger( WPVSController.class );

    private PerspectiveViewService service;

    private ServiceIdentificationType identification;

    private ServiceProviderType provider;

    private PublishedInformation publishedInformation;

    private final static String CONFIG_SCHEMA_FILE = "/META-INF/schemas/wpvs/0.3.0/wpvs_service_configuration.xsd";

    private final static String PUBLISHED_SCHEMA_FILE = "/META-INF/schemas/wpvs/0.3.0/wpvs_published_information.xsd";

    private List<String> allowedOperations = new LinkedList<String>();

    private static final ImplementationMetadata<WPVSRequestType> IMPLEMENTATION_METADATA = new ImplementationMetadata<WPVSRequestType>() {
        {
            supportedVersions = new Version[] { VERSION_040 };
            handledNamespaces = new String[] { WPVS_NS };
            handledRequests = WPVSRequestType.class;
            supportedConfigVersions = new Version[] { Version.parseVersion( "0.3.0" ) };
        }
    };

    @Override
    public void init( XMLAdapter controllerConf, DeegreeServicesMetadata serviceMetadata )
                            throws ControllerInitException {

        init( serviceMetadata, IMPLEMENTATION_METADATA, controllerConf );

        LOG.info( "Checking for JOGL." );
        JOGLChecker.check();
        LOG.info( "JOGL status check successful." );

        NamespaceContext nsContext = new NamespaceContext();
        nsContext.addNamespace( "wpvs", "http://www.deegree.org/services/wpvs" );
        try {
            publishedInformation = parsePublishedInformation( nsContext, controllerConf );
            if ( publishedInformation != null ) {
                syncServiceIdentification();
                syncServiceProvider();
            }
            ServiceConfiguration sc = parseServerConfiguration( nsContext, controllerConf );
            service = new PerspectiveViewService( controllerConf, sc );
        } catch ( JAXBException e ) {
            throw new ControllerInitException( e.getMessage(), e );
        } catch ( ServiceInitException e ) {
            throw new ControllerInitException( e.getMessage(), e );
        }

    }

    private PublishedInformation parsePublishedInformation( NamespaceContext nsContext, XMLAdapter controllerConf )
                            throws JAXBException {
        Unmarshaller u = getUnmarshaller( "org.deegree.services.controller.wpvs.configuration", PUBLISHED_SCHEMA_FILE );

        XPath xp = new XPath( "wpvs:PublishedInformation", nsContext );
        OMElement elem = controllerConf.getElement( controllerConf.getRootElement(), xp );
        PublishedInformation result = null;
        if ( elem != null ) {
            result = (PublishedInformation) u.unmarshal( elem.getXMLStreamReaderWithoutCaching() );
            if ( result != null ) {
                // mandatory
                allowedOperations.add( WPVSRequestType.GetCapabilities.name() );
                allowedOperations.add( WPVSRequestType.GetView.name() );
                AllowedOperations configuredOperations = result.getAllowedOperations();
                if ( configuredOperations != null ) {
                    if ( configuredOperations.getGetDescription() != null ) {
                        LOG.warn( "The GetDescription operation was configured, this operation is currently not supported by the WPVS." );
                        allowedOperations.add( WPVSRequestType.GetDescription.name() );
                    }
                    if ( configuredOperations.getGetLegendGraphic() != null ) {
                        LOG.warn( "The GetLegendGraphic operation was configured, this operation is currently not supported by the WPVS." );
                        allowedOperations.add( WPVSRequestType.GetLegendGraphic.name() );
                    }
                }
            }
        }
        return result;
    }

    /**
     * @param nsContext
     * @param controllerConf
     * @throws JAXBException
     */
    private ServiceConfiguration parseServerConfiguration( NamespaceContext nsContext, XMLAdapter controllerConf )
                            throws JAXBException {

        Unmarshaller u = getUnmarshaller( "org.deegree.services.wpvs.configuration", CONFIG_SCHEMA_FILE );

        XPath xp = new XPath( "wpvs:ServiceConfiguration", nsContext );
        OMElement elem = controllerConf.getRequiredElement( controllerConf.getRootElement(), xp );

        return (ServiceConfiguration) u.unmarshal( elem.getXMLStreamReaderWithoutCaching() );

    }

    @Override
    public void destroy() {
        // nottin yet
    }

    @Override
    protected void doKVP( Map<String, String> normalizedKVPParams, HttpRequestWrapper request,
                          HttpResponseWrapper response, List<FileItem> multiParts )
                            throws ServletException, IOException {
        WPVSRequestType mappedRequest = null;
        String requestName = null;
        try {
            requestName = KVPUtils.getRequired( normalizedKVPParams, "REQUEST" );
        } catch ( MissingParameterException e ) {
            sendServiceException( new OWSException( e.getMessage(), OWSException.MISSING_PARAMETER_VALUE ), response );
            return;
        }
        mappedRequest = IMPLEMENTATION_METADATA.getRequestTypeByName( requestName );

        if ( mappedRequest == null ) {
            sendServiceException( new OWSException( "Unknown request: " + requestName + " is not known to the WPVS.",
                                                    OWSException.OPERATION_NOT_SUPPORTED ), response );
            return;
        }
        try {
            LOG.debug( "Incoming request was mapped as a: " + mappedRequest );
            switch ( mappedRequest ) {
            case GetCapabilities:
                sendCapabilities( normalizedKVPParams, request, response );
                break;
            case GetView:
                sendGetViewResponse( normalizedKVPParams, request, response );
                break;
            default:
                sendServiceException( new OWSException( mappedRequest + " is not implemented yet.",
                                                        OWSException.OPERATION_NOT_SUPPORTED ), response );
            }
        } catch ( Throwable t ) {
            sendServiceException( new OWSException( "An exception occurred while processing your request: "
                                                    + t.getMessage(), ControllerException.NO_APPLICABLE_CODE ),
                                  response );
        }

    }

    /**
     * @param normalizedKVPParams
     * @param request
     * @param response
     * @throws ServletException
     */
    private void sendGetViewResponse( Map<String, String> normalizedKVPParams, HttpRequestWrapper request,
                                      HttpResponseWrapper response )
                            throws ServletException {
        try {
            String encoding = ( request.getCharacterEncoding() == null ) ? "UTF-8" : request.getCharacterEncoding();
            GetView gvReq = GetViewKVPAdapter.create( normalizedKVPParams, encoding, service.getTranslationVector(),
                                                      service.getNearClippingPlane(), service.getFarClippingPlane() );

            // first see if the requested image typ is supported
            GetViewResponseParameters responseParameters = gvReq.getResponseParameters();
            String format = responseParameters.getFormat();
            testResultMimeType( format );

            // render the image
            BufferedImage gvResponseImage = service.getPerspectiveViewImage( gvReq );
            String ioFormat = mimeToFormat( format );
            LOG.debug( "Requested format: " + format + " was mapped to response ioformat: " + ioFormat );
            if ( gvResponseImage != null ) {
                try {
                    ImageIO.write( gvResponseImage, ioFormat, response.getOutputStream() );
                } catch ( IOException e ) {
                    throw new OWSException( "An error occurred while writing the result image to the stream because: "
                                            + e.getLocalizedMessage(), ControllerException.NO_APPLICABLE_CODE );
                }
                response.setContentLength( response.getBufferSize() );
                response.setContentType( format );

            }
        } catch ( OWSException e ) {
            sendServiceException( e, response );
        }

    }

    /**
     * @param format
     * @return
     */
    private String mimeToFormat( String format ) {
        String[] split = format.split( "/" );
        String result = format;
        if ( split.length > 1 ) {
            result = split[split.length - 1];
            split = result.split( ";" );
            if ( split.length >= 1 ) {
                result = split[0];
            }
        }
        return result;
    }

    /**
     * Retrieve the imagewriter for the requested format.
     * 
     * @param format
     *            mimetype to be supported
     * @throws OWSException
     *             if no writer was found for the given format.
     */
    private void testResultMimeType( String format )
                            throws OWSException {
        Iterator<ImageWriter> imageWritersByMIMEType = ImageIO.getImageWritersByMIMEType( format );
        ImageWriter writer = null;
        if ( imageWritersByMIMEType != null ) {
            while ( imageWritersByMIMEType.hasNext() && writer == null ) {
                ImageWriter iw = imageWritersByMIMEType.next();
                if ( iw != null ) {
                    writer = iw;
                }
            }
        }
        if ( writer == null ) {
            throw new OWSException( "No imagewriter for given image format: " + format,
                                    OWSException.OPERATION_NOT_SUPPORTED );
        }
    }

    private void sendCapabilities( Map<String, String> map, HttpRequestWrapper request, HttpResponseWrapper response )
                            throws IOException {
        GetCapabilities req = GetCapabilitiesKVPParser.parse( map );

        DCPType wpvsDCP = new DCPType();
        DCPType dcps = mainControllerConf.getDCP();
        String getUrl = request.getRequestURL().toString();
        if ( dcps != null && dcps.getHTTPGet() != null && !"".equals( dcps.getHTTPGet().trim() ) ) {
            getUrl = dcps.getHTTPGet();
        }
        if ( !getUrl.endsWith( "?" ) ) {
            getUrl += "?";
        }
        wpvsDCP.setHTTPGet( getUrl );

        /*
         * post is currently not supported
         */
        // String postUrl = request.getRequestURL().toString();
        // if ( dcps != null && dcps.getHTTPPost() != null && !"".equals( dcps.getHTTPPost().trim() ) ) {
        // postUrl = dcps.getHTTPPost();
        // }
        // wpvsDCP.setHTTPPost( postUrl );
        XMLOutputFactory factory = XMLOutputFactory.newInstance();
        factory.setProperty( IS_REPAIRING_NAMESPACES, true );
        try {
            XMLStreamWriter xsw = factory.createXMLStreamWriter( response.getOutputStream(), "UTF-8" );
            FormattingXMLStreamWriter xmlWriter = new FormattingXMLStreamWriter( xsw );
            xmlWriter.writeStartDocument();
            new CapabilitiesXMLAdapter().export040( xmlWriter, req, identification, provider, allowedOperations,
                                                    wpvsDCP, service.getServiceConfiguration() );
            xmlWriter.writeEndDocument();
        } catch ( XMLStreamException e ) {
            throw new IOException( e );
        }
    }

    /**
     * @param e
     * @param response
     */
    private void sendServiceException( OWSException e, HttpResponseWrapper response )
                            throws ServletException {
        LOG.error( "Unable to forfil request, sending exception.", e );
        sendException( "application/vnd.ogc.se_xml", "UTF-8", null, 200, new OWSException110XMLAdapter(), e, response );

    }

    @Override
    protected void doXML( XMLAdapter requestDoc, HttpRequestWrapper request, HttpResponseWrapper response,
                          List<FileItem> multiParts )
                            throws ServletException, IOException {

        sendServiceException( new OWSException( "Currently only Http Get requests with key value pairs are supported.",
                                                OWSException.OPERATION_NOT_SUPPORTED ), response );
    }

    /**
     * sets the identification to the main controller or it will be synchronized with the maincontroller.
     */
    private void syncServiceIdentification() {
        if ( identification == null ) {
            if ( publishedInformation == null || publishedInformation.getServiceIdentification() == null ) {
                LOG.info( "Using global service identification because no WPVS specific service identification was defined." );
                identification = mainControllerConf.getServiceIdentification();
            } else {
                identification = synchronizeServiceIdentificationWithMainController( publishedInformation.getServiceIdentification() );
            }
        }
    }

    /**
     * sets the provider to the provider of the configured main controller or it will be synchronized with it's values.
     */
    private void syncServiceProvider() {
        if ( provider == null ) {
            if ( publishedInformation == null || publishedInformation.getServiceProvider() == null ) {
                LOG.info( "Using gloval serviceProvider because no WPVS specific service provider was defined." );
                provider = mainControllerConf.getServiceProvider();
            } else {
                provider = synchronizeServiceProviderWithMainControllerConf( publishedInformation.getServiceProvider() );
            }
        }
    }
}
