//$HeadURL$ svn+ssh://sthomas@svn.wald.intevation.org/deegree/deegree3/services/trunk/src/org/deegree/services/controller/csw/CSWController.java $
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
package org.deegree.services.controller.csw;

import static org.deegree.protocol.csw.CSWConstants.CSW_202_NS;
import static org.deegree.protocol.csw.CSWConstants.VERSION_202;

import java.beans.Statement;
import java.io.IOException;
import java.io.Writer;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.axiom.om.OMElement;
import org.apache.commons.fileupload.FileItem;
import org.deegree.commons.configuration.JDBCConnections;
import org.deegree.commons.configuration.PooledConnection;
import org.deegree.commons.jdbc.ConnectionManager;
import org.deegree.commons.types.ows.Version;
import org.deegree.commons.utils.kvp.InvalidParameterValueException;
import org.deegree.commons.utils.kvp.KVPUtils;
import org.deegree.commons.utils.kvp.MissingParameterException;
import org.deegree.commons.xml.NamespaceContext;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.XMLParsingException;
import org.deegree.commons.xml.XPath;
import org.deegree.commons.xml.stax.FormattingXMLStreamWriter;
import org.deegree.commons.xml.stax.XMLStreamWriterWrapper;
import org.deegree.protocol.csw.CSWConstants;
import org.deegree.protocol.csw.CSWConstants.CSWRequestType;
import org.deegree.protocol.csw.CSWConstants.Sections;
import org.deegree.protocol.ows.capabilities.GetCapabilities;
import org.deegree.record.persistence.RecordStoreException;
import org.deegree.services.controller.AbstractOGCServiceController;
import org.deegree.services.controller.ImplementationMetadata;
import org.deegree.services.controller.configuration.DeegreeServicesMetadata;
import org.deegree.services.controller.configuration.ServiceIdentificationType;
import org.deegree.services.controller.configuration.ServiceProviderType;
import org.deegree.services.controller.csw.exporthandling.GetCapabilitiesHandler;
import org.deegree.services.controller.csw.capabilities.GetCapabilities202KVPAdapter;
import org.deegree.services.controller.csw.capabilities.GetCapabilitiesVersionXMLAdapter;
import org.deegree.services.controller.csw.configuration.PublishedInformation;
import org.deegree.services.controller.csw.describerecord.DescribeRecord;
import org.deegree.services.controller.csw.describerecord.DescribeRecordKVPAdapter;
import org.deegree.services.controller.csw.describerecord.DescribeRecordXMLAdapter;
import org.deegree.services.controller.csw.exporthandling.DescribeRecordHandler;
import org.deegree.services.controller.csw.exporthandling.GetRecordsHandler;
import org.deegree.services.controller.csw.exporthandling.TransactionHandler;
import org.deegree.services.controller.csw.getrecords.GetRecords;
import org.deegree.services.controller.csw.getrecords.GetRecordsKVPAdapter;
import org.deegree.services.controller.csw.getrecords.GetRecordsXMLAdapter;
import org.deegree.services.controller.csw.transaction.Transaction;
import org.deegree.services.controller.csw.transaction.TransactionKVPAdapter;
import org.deegree.services.controller.csw.transaction.TransactionXMLAdapter;
import org.deegree.services.controller.exception.ControllerInitException;
import org.deegree.services.controller.ows.OWSException;
import org.deegree.services.controller.ows.OWSException110XMLAdapter;
import org.deegree.services.controller.utils.HttpRequestWrapper;
import org.deegree.services.controller.utils.HttpResponseWrapper;
import org.deegree.services.csw.CSWService;
import org.deegree.services.csw.configuration.ServiceConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CSWController extends AbstractOGCServiceController {

    private static final Logger LOG = LoggerFactory.getLogger( CSWController.class );

    private static int UPDATE_SEQUENCE = -1;

    private ServiceIdentificationType identification;

    private ServiceProviderType provider;

    private CSWService service;
    
    private DescribeRecordHandler describeRecordHandler;
    
    private GetRecordsHandler getRecordsHandler;
    
    private TransactionHandler transHandler;
    
   

    private HashMap<String, HashMap> optionalOperations = new HashMap<String, HashMap>();

    // private DCPType dcpType;

    // List<String> operations;

    private static final ImplementationMetadata<CSWRequestType> IMPLEMENTATION_METADATA = new ImplementationMetadata<CSWRequestType>() {
        {
            supportedVersions = new Version[] { VERSION_202 };
            handledNamespaces = new String[] { CSW_202_NS };
            handledRequests = CSWRequestType.class;
            supportedConfigVersions = new Version[] { Version.parseVersion( "0.3.0" ) };
        }
    };

    @Override
    public void init( XMLAdapter controllerConf, DeegreeServicesMetadata serviceMetadata )
                            throws ControllerInitException {
        init( serviceMetadata, IMPLEMENTATION_METADATA, controllerConf );

        LOG.info( "Initializing CSW controller." );
        
        

        // check config version
        String configVersion = controllerConf.getRootElement().getAttributeValue( new QName( "configVersion" ) );
        checkConfigVersion( controllerConf.getSystemId(), configVersion );

        NamespaceContext nsContext = new NamespaceContext();
        nsContext.addNamespace( CSWConstants.CSW_PREFIX, "http://www.deegree.org/services/csw" );

        ServiceConfiguration sc = null;
        PublishedInformation pi = null;

        try {
            XPath xpath = new XPath( "csw:ServiceConfiguration", nsContext );

            //Unmarshaller u = JAXBContext.newInstance( "org.deegree.services.csw.configuration" ).createUnmarshaller();

            final String additionalClasspath = "org.deegree.services.controller.csw.configuration:org.deegree.services.csw.configuration";
            Unmarshaller u = getUnmarshaller( additionalClasspath, null );

            OMElement scElement = controllerConf.getRequiredElement( controllerConf.getRootElement(), xpath );

            // turn the application schema location into an absolute URL
            sc = (ServiceConfiguration) u.unmarshal( scElement.getXMLStreamReaderWithoutCaching() );
            //URL resolvedSchemaLocation = controllerConf.resolve( sc.getRecordstore() );
//            sc.setApplicationSchemaLocation( resolvedSchemaLocation.toExternalForm() );
            
            u = JAXBContext.newInstance( "org.deegree.services.controller.csw.configuration" ).createUnmarshaller();
            xpath = new XPath( "csw:PublishedInformation", nsContext );
            OMElement piElement = controllerConf.getRequiredElement( controllerConf.getRootElement(), xpath );
            pi = (PublishedInformation) u.unmarshal( piElement.getXMLStreamReaderWithoutCaching() );
            syncWithMainController( pi );
            setOptionalOperations(pi);
            
            service = new CSWService(sc);
            
            
            
        } catch ( XMLParsingException e ) {
            throw new ControllerInitException( "TODO", e );
        } catch ( JAXBException e ) {
            throw new ControllerInitException( "TODO", e );
        } catch ( RecordStoreException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } 
        validateAndSetOfferedVersions( pi.getAcceptVersions().getVersion() );
        describeRecordHandler = new DescribeRecordHandler(service);
        getRecordsHandler = new GetRecordsHandler(service, serviceMetadata); 
        transHandler = new TransactionHandler(service, serviceMetadata);

    }


    @Override
    protected void doKVP( Map<String, String> normalizedKVPParams, HttpRequestWrapper request,
                          HttpResponseWrapper response, List<FileItem> multiParts )
                            throws ServletException, IOException {

        try {
            String rootElement = KVPUtils.getRequired( normalizedKVPParams, "REQUEST" );
            CSWRequestType requestType = getRequestType( rootElement );
            
            Version requestVersion = getVersion( normalizedKVPParams.get( "VERSION" ) );
            
            String serviceAttr = KVPUtils.getRequired( normalizedKVPParams, "SERVICE" );
            if ( !"CSW".equals( serviceAttr ) ) {
                throw new OWSException( "Wrong service attribute: '" + serviceAttr + "' -- must be 'CSW'.",
                                        OWSException.INVALID_PARAMETER_VALUE, "service" );
            }
            if ( requestType != CSWRequestType.GetCapabilities ) {
                checkVersion( requestVersion );
            }

            switch ( requestType ) {

            case GetCapabilities:
                GetCapabilities getCapabilities = GetCapabilities202KVPAdapter.parse(requestVersion, normalizedKVPParams );
                doGetCapabilities( getCapabilities, request, response );
                break;
            case DescribeRecord:
                DescribeRecord descRec = DescribeRecordKVPAdapter.parse(normalizedKVPParams);
                //describeRecordResponse = new DescribeRecordResponseXMLAdapter(service);
                describeRecordHandler.doDescribeRecord(descRec, response);
                break;
            case GetRecords:
                GetRecords getRec = GetRecordsKVPAdapter.parse( normalizedKVPParams );
                getRecordsHandler.doGetRecords( getRec, response );
                break;
            case GetRecordById:
                
                break;
                
            case Transaction:
                Transaction trans = TransactionKVPAdapter.parse( normalizedKVPParams );
                transHandler.doTransaction( trans, response );
                break;
            }
        } catch ( OWSException e ) {
            sendServiceException( e, response );
        } catch ( XMLStreamException e ) {
            e.printStackTrace();
        } catch ( SQLException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    

    @Override
    protected void doXML( XMLAdapter requestDoc, HttpRequestWrapper request, HttpResponseWrapper response,
                          List<FileItem> multiParts )
                            throws ServletException, IOException {
        response.setContentType( "text/xml" );

        try {
            String rootElement = requestDoc.getRootElement().getLocalName();
            CSWRequestType requestType = getRequestType( rootElement );

            // check if requested version is supported and offered (except for GetCapabilities)
            Version requestVersion = getVersion( requestDoc.getRootElement().getAttributeValue( new QName( "version" ) ) );
            if ( requestType != CSWRequestType.GetCapabilities ) {
                checkVersion( requestVersion );
            }

            switch ( requestType ) {

            case GetCapabilities:
                GetCapabilitiesVersionXMLAdapter getCapabilitiesAdapter = new GetCapabilitiesVersionXMLAdapter();
                getCapabilitiesAdapter.setRootElement( requestDoc.getRootElement() );
                GetCapabilities cswRequest = getCapabilitiesAdapter.parse( requestVersion );
                doGetCapabilities( cswRequest, request, response );
                break;
            case DescribeRecord:
                DescribeRecordXMLAdapter describeRecordAdapter = new DescribeRecordXMLAdapter();
                describeRecordAdapter.setRootElement( requestDoc.getRootElement() );
                DescribeRecord cswDRRequest = describeRecordAdapter.parse( requestVersion );
                describeRecordHandler.doDescribeRecord( cswDRRequest, response );
                break;
            case GetRecords:
                GetRecordsXMLAdapter getRecordsAdapter = new GetRecordsXMLAdapter();
                getRecordsAdapter.setRootElement( requestDoc.getRootElement() );
                GetRecords cswGRRequest = getRecordsAdapter.parse( requestVersion );
                getRecordsHandler.doGetRecords( cswGRRequest, response );
                break;
            case Transaction:
                TransactionXMLAdapter transAdapter = new TransactionXMLAdapter();
                transAdapter.setRootElement( requestDoc.getRootElement() );
                Transaction cswTRequest = transAdapter.parse( requestVersion );
                transHandler.doTransaction( cswTRequest, response );
                break;
            }

        } catch ( OWSException e ) {
            sendServiceException( e, response );
        } catch ( MissingParameterException e ) {
            sendServiceException( new OWSException( e ), response );
        } catch ( InvalidParameterValueException e ) {
            sendServiceException( new OWSException( e ), response );
        } catch ( Exception e ) {
            sendServiceException( new OWSException( e.getMessage(), OWSException.NO_APPLICABLE_CODE ), response );
        }

    }

    @Override
    public void destroy() {
        // TODO Auto-generated method stub

    }

    /**
     * Method for mapping the request operation to the implemented 
     * operations located in {@link CSWConstants}
     * 
     * @param requestName
     * @return CSWRequestType
     * @throws OWSException
     */
    private CSWRequestType getRequestType( String requestName )
                            throws OWSException {
        CSWRequestType requestType = null;
        try {

            requestType = IMPLEMENTATION_METADATA.getRequestTypeByName( requestName );
        } catch ( IllegalArgumentException e ) {
            throw new OWSException( e.getMessage(), OWSException.OPERATION_NOT_SUPPORTED );
        }
        return requestType;
    }

    private void doGetCapabilities( GetCapabilities request, HttpRequestWrapper requestWrapper,
                                    HttpResponseWrapper response )
                            throws XMLStreamException, IOException, OWSException {

        checkOrCreateDCPGetURL( requestWrapper );
        checkOrCreateDCPPostURL( requestWrapper );
        Set<Sections> sections = getSections( request );
        // Set<String> operations = getOperations(request);
        Version negotiatedVersion = negotiateVersion( request );
        response.setContentType( "text/xml; charset=UTF-8" );

        XMLStreamWriter xmlWriter = getXMLResponseWriter( response, null );
        GetCapabilitiesHandler.export( xmlWriter, mainControllerConf, sections, identification, optionalOperations, negotiatedVersion );
        xmlWriter.flush();

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
    
    
    
    

    

    private void sendServiceException( OWSException ex, HttpResponseWrapper response )
                            throws ServletException {

        // TODO correct status code?
        sendException( "application/vnd.ogc.se_xml", "UTF-8", null, 300, new OWSException110XMLAdapter(), ex, response );
    }

    

    private Set<Sections> getSections( GetCapabilities capabilitiesReq ) {
        Set<String> sections = capabilitiesReq.getSections();
        Set<Sections> result = new HashSet<Sections>();
        if ( !( sections.isEmpty() || sections.contains( "/" ) ) ) {
            final int length = "/CSW_Capabilities/".length();
            for ( String section : sections ) {
                if ( section.startsWith( "/CSW_Capabilities/" ) ) {
                    section = section.substring( length );
                }
                try {
                    result.add( Sections.valueOf( section ) );
                } catch ( IllegalArgumentException ex ) {
                    // unknown section name
                    // the spec does not say what to do, so we ignore it
                }
            }
        }
        return result;
    }

    /**
     * sets the identification to the main controller or it will be synchronized with the maincontroller.
     * 
     * @param publishedInformation
     */
    private void syncWithMainController( PublishedInformation publishedInformation ) {
        if ( identification == null ) {
            if ( publishedInformation == null || publishedInformation.getServiceIdentification() == null ) {
                // LOG.info(
                // "Using global service identification because no WCS specific service identification was defined." );
                identification = mainControllerConf.getServiceIdentification();
            } else {
                identification = synchronizeServiceIdentificationWithMainController( publishedInformation.getServiceIdentification() );
            }
        }
        if ( provider == null ) {
            if ( publishedInformation == null || publishedInformation.getServiceProvider() == null ) {
                // LOG.info( "Using gloval serviceProvider because no WCS specific service provider was defined." );
                provider = mainControllerConf.getServiceProvider();
            } else {
                provider = synchronizeServiceProviderWithMainControllerConf( publishedInformation.getServiceProvider() );
            }

        }

       

    }
    

    /**
     * sets the optionalOperations from the xml-file, 
     * 
     * @param publishedInformation
     */
    private void setOptionalOperations( PublishedInformation publishedInformation ){
        if ( optionalOperations.isEmpty() ) {

            /*HashMap<String, List<String>> mapParam;
            HashMap<String, List<String>> mapConstr;
            HashMap<String, HashMap> paramConstr;

            AllowedOperations.GetCapabilities capa = publishedInformation.getAllowedOperations().getGetCapabilities();
            AllowedOperations.DescribeRecord descRec = publishedInformation.getAllowedOperations().getDescribeRecord();
            AllowedOperations.GetRecords getRec = publishedInformation.getAllowedOperations().getGetRecords();
            
           
            if ( !( capa == null ) ) {

                paramConstr = new HashMap<String, HashMap>();
                mapParam = new HashMap<String, List<String>>();
                mapConstr = new HashMap<String, List<String>>();
                
                //the sections-parameter should be hardcoded because of spec
                List<String> listValue = new LinkedList<String>();
                
                listValue.add( "ServiceIdentification" );
                listValue.add( "ServiceProvider" );
                listValue.add( "OperationsMetadata" );
                listValue.add( "Filter_Capabilities" );
                
                mapParam.put( "sections", listValue );
                for ( ParamConstrType params : capa.getParameter() ) {

                    makeList( params, mapParam );

                }

                for ( ParamConstrType constr : capa.getConstraint() ) {

                    generateValueList( constr, mapConstr );

                }

                paramConstr.put( "Parameter", mapParam );
                paramConstr.put( "Constraint", mapConstr );

                optionalOperations.put( CSWRequestType.GetCapabilities.name(), paramConstr );

            }else{
               optionalOperations = null;
               
            }

            if ( !( descRec == null ) ) {

                paramConstr = new HashMap<String, HashMap>();
                mapParam = new HashMap<String, List<String>>();
                mapConstr = new HashMap<String, List<String>>();

                for ( ParamConstrType params : descRec.getParameter() ) {
                    
                    generateValueList( params, mapParam );

                }

                for ( ParamConstrType constr : descRec.getConstraint() ) {

                    generateValueList( constr, mapConstr );

                }

                paramConstr.put( "Parameter", mapParam );
                paramConstr.put( "Constraint", mapConstr );

                optionalOperations.put( CSWRequestType.DescribeRecord.name(), paramConstr );
            }else{
                optionalOperations = null;
                
             }

            if ( !( getRec == null ) ) {

                paramConstr = new HashMap<String, HashMap>();
                mapParam = new HashMap<String, List<String>>();
                mapConstr = new HashMap<String, List<String>>();

                for ( ParamConstrType params : getRec.getParameter() ) {

                    generateValueList( params, mapParam );

                }

                for ( ParamConstrType constr : getRec.getConstraint() ) {

                    generateValueList( constr, mapConstr );

                }

                paramConstr.put( "Parameter", mapParam );
                paramConstr.put( "Constraint", mapConstr );

                optionalOperations.put( CSWRequestType.GetRecords.name(), paramConstr );
            }else{
                optionalOperations = null;
                
             }*/

        }
        
    }

    

    /**
     * sets the identification to the main controller or it will be synchronized with the maincontroller. sets the
     * provider to the provider of the configured main controller or it will be synchronized with it's values.
     * 
     * @param serviceConfiguration
     * @throws ControllerInitException
     */
//    private ServiceConfiguration parseServiceConfiguration( XMLAdapter controllerConf, NamespaceContext nsContext )
//                            throws ControllerInitException {
//        ServiceConfiguration SerConf = null;
//        try {
//            XPath xpath = new XPath( "csw:ServiceConfiguration", nsContext );
//            Unmarshaller u = JAXBContext.newInstance( "org.deegree.services.csw.configuration" ).createUnmarshaller();
//            OMElement scElement = controllerConf.getRequiredElement( controllerConf.getRootElement(), xpath );
//            SerConf = (ServiceConfiguration) u.unmarshal( scElement.getXMLStreamReaderWithoutCaching() );
//        } catch ( XMLParsingException e ) {
//            throw new ControllerInitException( "TODO", e );
//        } catch ( JAXBException e ) {
//            throw new ControllerInitException( "TODO", e );
//        }
//        return SerConf;
//    }

    /**
     * sets the identification to the main controller or it will be synchronized with the maincontroller. sets the
     * provider to the provider of the configured main controller or it will be synchronized with it's values.
     * 
     * @param publishedInformation
     * @throws ControllerInitException
     */
    private PublishedInformation parsePublishedInformation( XMLAdapter controllerConf, NamespaceContext nsContext )
                            throws ControllerInitException {

        PublishedInformation pubInf = null;
        try {
            XPath xpath = new XPath( "csw:PublishedInformation", nsContext );
            Unmarshaller u = JAXBContext.newInstance( "org.deegree.services.controller.csw.configuration" ).createUnmarshaller();
            OMElement piElement = controllerConf.getRequiredElement( controllerConf.getRootElement(), xpath );
            pubInf = (PublishedInformation) u.unmarshal( piElement.getXMLStreamReaderWithoutCaching() );
        } catch ( XMLParsingException e ) {
            throw new ControllerInitException( "TODO", e );
        } catch ( JAXBException e ) {
            throw new ControllerInitException( "TODO", e );
        }
        return pubInf;
    }

    private final static boolean isEmpty( String value ) {
        return value == null || "".equals( value );
    }

    private Version getVersion( String versionString )
                            throws OWSException {
        Version version = null;
        if ( versionString != null ) {
            try {
                version = Version.parseVersion( versionString );
            } catch ( InvalidParameterValueException e ) {
                throw new OWSException( e );
            }
        }
        return version;
    }
    
    private static XMLStreamWriter getXMLStreamWriter( Writer writer )
    throws XMLStreamException {
XMLOutputFactory factory = XMLOutputFactory.newInstance();
factory.setProperty( XMLOutputFactory.IS_REPAIRING_NAMESPACES, Boolean.TRUE );
return new FormattingXMLStreamWriter( factory.createXMLStreamWriter( writer ) );
}

}
