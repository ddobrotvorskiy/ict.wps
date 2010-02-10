//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/base/trunk/resources/eclipse/files_template.xml $
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

package org.deegree.services.controller.wps;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.xml.bind.JAXBElement;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.deegree.commons.utils.time.DateUtils;
import org.deegree.services.controller.OGCFrontController;
import org.deegree.services.controller.ows.OWSException;
import org.deegree.services.controller.utils.HttpResponseWrapper;
import org.deegree.services.controller.wps.execute.ExecuteRequest;
import org.deegree.services.controller.wps.execute.ExecuteResponse;
import org.deegree.services.controller.wps.execute.ExecuteResponseXMLAdapter;
import org.deegree.services.controller.wps.execute.RawDataOutput;
import org.deegree.services.controller.wps.execute.RequestedOutput;
import org.deegree.services.controller.wps.execute.ResponseDocument;
import org.deegree.services.controller.wps.storage.ResponseDocumentStorage;
import org.deegree.services.controller.wps.storage.StorageLocation;
import org.deegree.services.controller.wps.storage.StorageManager;
import org.deegree.services.wps.Processlet;
import org.deegree.services.wps.ProcessletException;
import org.deegree.services.wps.ProcessletInputs;
import org.deegree.services.wps.ProcessletOutputs;
import org.deegree.services.wps.output.BoundingBoxOutputImpl;
import org.deegree.services.wps.output.ComplexOutputImpl;
import org.deegree.services.wps.output.LiteralOutputImpl;
import org.deegree.services.wps.output.ProcessletOutput;
import org.deegree.services.wps.processdefinition.BoundingBoxOutputDefinition;
import org.deegree.services.wps.processdefinition.ComplexOutputDefinition;
import org.deegree.services.wps.processdefinition.LiteralOutputDefinition;
import org.deegree.services.wps.processdefinition.ProcessDefinition;
import org.deegree.services.wps.processdefinition.ProcessletOutputDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Responsible for handling <code>Execute</code> requests sent to the {@link WPSController}. Also keeps track of the
 * process executions.
 * 
 * @see WPSController
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author <a href="mailto:jason.surratt@gmail.com">Jason R. Surratt</a>
 * @author last edited by: $Author: schneider $
 * 
 * @version $Revision: $, $Date: $
 */
public class ExecutionManager {

    private static final Logger LOG = LoggerFactory.getLogger( ExecutionManager.class );

    private StorageManager storageManager;

    private ExecutorService exec;

    // number of executions to keep track of
    private static int MAX_ENTRIES = 100;

    // A list of all processes that have been run or are currently running.
    private ConcurrentLinkedQueue<ProcessletExecution> processStateList = new ConcurrentLinkedQueue<ProcessletExecution>();

    // key: response document of the process (currently running), value: status object
    private Map<ResponseDocumentStorage, ProcessletExecution> responseDocumentIdToState = new ConcurrentHashMap<ResponseDocumentStorage, ProcessletExecution>();

    /**
     * Creates a new {@link ExecutionManager} for a {@link WPSController}.
     * 
     * @param master
     *            {@link WPSController} that will delegate the processing of execute requests
     * @param storageManager
     *            used for creating storage locations for web-accessible resources (response documents / process
     *            outputs)
     */
    ExecutionManager( WPSController master, StorageManager storageManager ) {
        this.storageManager = storageManager;
        this.exec = Executors.newCachedThreadPool();
    }

    /**
     * This method should be called in lieu of the constructor directly to ensure the ProcessExecution gets added to the
     * processList. See {@link ProcessletExecution} for a definition of the parameters.
     */
    private ProcessletExecution createProcessletExecution( ExecuteRequest request, StorageLocation responseStorage,
                                                           URL serviceInstance, List<RequestedOutput> outputParams,
                                                           ProcessletOutputs outputs ) {
        ProcessletExecution result = new ProcessletExecution( request, responseStorage, serviceInstance, outputParams,
                                                              outputs );
        synchronized ( processStateList ) {
            if ( processStateList.size() == MAX_ENTRIES ) {
                processStateList.poll();
            }
            processStateList.add( result );
        }
        return result;
    }

    /**
     * Returns a collection of information on all processes, including processes that haven't run, are running and have
     * already stopped.
     * 
     * @return Returns a collection of information on all processes, including processes that haven't run, are running
     *         and have already stopped. The returned result should not be modified.
     */
    public Collection<ProcessletExecution> getAllProcesses() {
        return processStateList;
    }

    /**
     * Returns a collection of information on all running processes.
     * 
     * @return Returns a collection of information on all running processes. The returned result should not be modified.
     */
    public Collection<ProcessletExecution> getRunningProcesses() {
        return responseDocumentIdToState.values();
    }

    /**
     * Handles {@link ExecuteRequest} requests that shall return a single "raw" output param (with no encapsulating
     * document) directly in the HTTP response body.
     * <p>
     * RawDataOutput always implies that the {@link Processlet} is executed synchronously, i.e. in the calling thread.
     * </p>
     * 
     * @param request
     *            request to be executed
     * @param response
     *            provides access to the HTTP response
     * @param process
     *            process to be invoked
     * @throws IOException
     * @throws ProcessletException
     * @throws XMLStreamException
     */
    void handleRawDataOutput( ExecuteRequest request, HttpResponseWrapper response, Processlet process )
                            throws IOException, ProcessletException, XMLStreamException {

        ProcessDefinition processDef = request.getProcessDefinition();
        ProcessletInputs inputs = request.getDataInputs();
        RequestedOutput output = ( (RawDataOutput) request.getResponseForm() ).getRequestedOutput();
        LOG.debug( "RawDataOutput, parameter: " + output.getOutputType().getIdentifier().getValue() + ", mimeType: "
                   + output.getMimeType() );

        String mimeType = output.getMimeType();
        String encoding = output.getEncoding();
        String schema = output.getSchemaURL();

        // use the HTTP output stream as a RawStreamOutput param
        if (mimeType != null) {
            response.setContentType( mimeType );
        }
        ProcessletOutputDefinition outputType = output.getOutputType();
        ProcessletOutput outputParam = null;
        if ( outputType instanceof ComplexOutputDefinition ) {
            outputParam = new ComplexOutputImpl( (ComplexOutputDefinition) outputType, response.getOutputStream(),
                                                 true, mimeType, schema, encoding );
        }
        ProcessletOutputs outputParams = new ProcessletOutputs( processDef, Collections.singletonList( outputParam ) );

        ProcessletExecution state = createProcessletExecution( request, null, null, null, outputParams );
        executeProcess( process, inputs, outputParams, state );
    }

    /**
     * Handles {@link ExecuteRequest} requests that shall return their output encapsulated in a response document.
     * <p>
     * The {@link Processlet} may be executed synchronously (storeExecuteResponse=false) or asynchronously
     * (storeExecuteResponse=true).
     * </p>
     * 
     * @param request
     *            request to be executed
     * @param response
     *            provides access to the HTTP response
     * @param process
     *            process to be invoked
     * @throws OWSException
     * @throws ProcessletException
     * @throws IOException
     * @throws XMLStreamException
     */
    void handleResponseDocumentOutput( ExecuteRequest request, HttpResponseWrapper response, Processlet process )
                            throws OWSException, ProcessletException, XMLStreamException, IOException {

        LOG.debug( "ResponseDocument" );

        URL serviceInstance = new URL( OGCFrontController.getHttpGetURL()
                                       + "service=WPS&request=GetCapabilities&version=1.0.0" );

        ProcessDefinition processDef = request.getProcessDefinition();
        ProcessletInputs inputs = request.getDataInputs();
        ResponseDocument outputFormat = (ResponseDocument) request.getResponseForm();

        // generate output objects for storing the process' outputs
        List<RequestedOutput> outputParams = null;
        if ( outputFormat != null && outputFormat.getOutputDefinitions().size() != 0 ) {
            // use output parameters from request
            outputParams = outputFormat.getOutputDefinitions();
        } else {
            // use all output parameters from the process definition
            LOG.debug( "No output parameters specified, using all from process definition." );
            outputParams = new ArrayList<RequestedOutput>();
            List<JAXBElement<? extends ProcessletOutputDefinition>> outputParameters = processDef.getOutputParameters().getProcessOutput();
            for ( JAXBElement<? extends ProcessletOutputDefinition> element : outputParameters ) {
                ProcessletOutputDefinition outputType = element.getValue();
                LOG.debug( "- " + outputType.getIdentifier().getValue() );
                String mimeType = null;
                String encoding = null;
                String schema = null;
                if ( outputType instanceof ComplexOutputDefinition ) {
                    mimeType = ( (ComplexOutputDefinition) outputType ).getDefaultFormat().getMimeType();
                    encoding = ( (ComplexOutputDefinition) outputType ).getDefaultFormat().getEncoding();
                    schema = ( (ComplexOutputDefinition) outputType ).getDefaultFormat().getSchema();
                }
                String uom = null;
                if ( outputType instanceof LiteralOutputDefinition ) {
                    LiteralOutputDefinition literalOutput = (LiteralOutputDefinition) outputType;
                    uom = literalOutput.getDefaultUOM() != null ? literalOutput.getDefaultUOM().getValue() : null;
                }
                outputParams.add( new RequestedOutput( outputType, false, mimeType, encoding, schema, uom, null, null ) );
            }
        }

        List<ProcessletOutput> out = new ArrayList<ProcessletOutput>( outputParams.size() );
        for ( RequestedOutput outputDef : outputParams ) {
            out.add( createOutputParameter( outputDef ) );
        }

        ProcessletOutputs outputs = new ProcessletOutputs( processDef, out );
        ResponseDocumentStorage responseStorage = null;
        ProcessletExecution state = null;

        if ( outputFormat != null && outputFormat.getStoreExecuteResponse() ) {
            // response will be stored as a web-accessible resource, only a dummy response document is directly
            // returned in the HTTP response stream (-> asynchronous process execution)
            LOG.debug( "Store response document as web-accessible resource (asynchronous execution)" );

            // allocate a storage location for the final response document
            responseStorage = storageManager.allocateResponseDocumentStorage();

            state = createProcessletExecution( request, responseStorage, serviceInstance, outputParams, outputs );

            // submit the process for asynchronous execution
            ProcessWorker worker = new ProcessWorker( process, outputs, state, outputParams, responseStorage, request );
            exec.execute( worker );
        } else {
            // response is directly returned in the HTTP response stream (-> synchronous process execution)
            LOG.debug( "Return response document in response stream (synchronous execution)" );

            if ( outputFormat != null && outputFormat.getStatus() ) {
                String msg = "Parameter 'status' can only be true if 'store' is true.";
                throw new OWSException( msg, OWSException.INVALID_PARAMETER_VALUE, "status" );
            }

            state = createProcessletExecution( request, responseStorage, serviceInstance, outputParams, outputs );
            executeProcess( process, inputs, outputs, state );
        }

        // write ExecuteResponse document
        response.setContentType( "text/xml; charset=UTF-8" );

        ExecuteResponse executeResponse = new ExecuteResponse( responseStorage, serviceInstance, state, outputParams,
                                                               outputs, request );
        try {
            XMLStreamWriter writer = response.getXMLWriter();
            ExecuteResponseXMLAdapter.export100( writer, executeResponse );
            writer.flush();
        } catch ( Exception e ) {
            String msg = "Generating ExecuteResponse document failed: " + e.getMessage();
            LOG.error( msg, e );
            throw new OWSException( msg, OWSException.NO_APPLICABLE_CODE );
        }
    }

    void sendResponseDocument( HttpResponseWrapper response, ResponseDocumentStorage location ) {

        ProcessletExecution status = responseDocumentIdToState.get( location );

        LOG.debug( "Checking if a process corresponding to output '" + location.getId()
                   + "' is known (=still running)." );
        if ( status == null ) {
            LOG.debug( "No. Trying to return the stored response document." );
            if ( location.getFile().exists() ) {
                location.sendResource( response );
            } else {
                try {
                    response.sendError( 404, "No stored (or pending) WPS response document with id '"
                                             + location.getId() + "' found." );
                } catch ( IOException e1 ) {
                    e1.printStackTrace();
                }
            }
        } else {
            LOG.debug( "Yes. Process is still running, so generating a preliminary result document for the client dynamically." );
            response.setContentType( "text/xml; charset=UTF-8" );
            try {
                XMLStreamWriter writer = response.getXMLWriter();
                ExecuteResponseXMLAdapter.export100( writer, status.createExecuteResponse() );
                writer.flush();
            } catch ( Exception e ) {
                String msg = "Generating ExecuteResponse document failed: " + e.getMessage();
                LOG.error( msg, e );
            }
        }
    }

    /**
     * Returns the {@link ProcessletExecution} for a given response document location.
     * 
     * @param location
     *            storage location of a response document
     * @return the {@link ProcessletExecution} of the corresponding process if it is still running, null otherwise
     */
    ProcessletExecution getPendingExecutionState( StorageLocation location ) {
        return responseDocumentIdToState.get( location );
    }

    /**
     * Creates a {@link ProcessletOutput} parameter object for storing a requested process output value.
     * 
     * @param outputDef
     * @return object for storing the corresponding process output value
     */
    private ProcessletOutput createOutputParameter( RequestedOutput outputDef )
                            throws OWSException {

        ProcessletOutput processOutput = null;

        ProcessletOutputDefinition outputType = outputDef.getOutputType();
        if ( outputType instanceof BoundingBoxOutputDefinition ) {
            processOutput = new BoundingBoxOutputImpl( (BoundingBoxOutputDefinition) outputType, true );
        } else if ( outputType instanceof LiteralOutputDefinition ) {
            processOutput = new LiteralOutputImpl( (LiteralOutputDefinition) outputType, outputDef.getUom(), true );
        } else if ( outputType instanceof ComplexOutputDefinition ) {
            try {
                String requestedMimeType = outputDef.getMimeType();
                String requestedEncoding = outputDef.getEncoding();
                String requestedSchema = outputDef.getSchemaURL();
                processOutput = new ComplexOutputImpl( (ComplexOutputDefinition) outputType,
                                                       storageManager.allocateOutputStorage( outputDef.getMimeType() ),
                                                       true, requestedMimeType, requestedEncoding, requestedSchema );
            } catch ( Exception e ) {
                String msg = "Unable to create sink for complex output parameter: " + e.getMessage();
                LOG.error( msg, e );
                throw new OWSException( msg, OWSException.NO_APPLICABLE_CODE );
            }
        }
        return processOutput;
    }

    private void executeProcess( Processlet process, ProcessletInputs inputs, ProcessletOutputs outputs,
                                 ProcessletExecution state ) {
        try {
            // process execution is about to start right now
            state.setStarted();

            // invoke process code
            process.process( inputs, outputs, state );

            // close all open output sinks (for complex outputs)
            for ( ProcessletOutput output : outputs.getParameters() ) {
                if ( output instanceof ComplexOutputImpl ) {
                    ( (ComplexOutputImpl) output ).close();
                }
            }

            // process execution has finished successfully
            state.setSucceeded( "Process execution finished@"
                                + DateUtils.formatISO8601Date( new Date( System.currentTimeMillis() ) ) );
        } catch ( ProcessletException e ) {
            String msg = "Process execution failed: " + e.getMessage();
            LOG.debug( msg, e );
            state.setFailed( new OWSException( msg, OWSException.NO_APPLICABLE_CODE ) );
        } catch ( Exception e ) {
            String msg = "Process execution failed: " + e.getMessage();
            LOG.debug( msg, e );
            state.setFailed( new OWSException( msg, OWSException.NO_APPLICABLE_CODE ) );
        }
    }

    /**
     * Runnable wrapper for executing processes asynchronously.
     * <p>
     * Used if storing of the execute response document is requested.
     * </p>
     */
    private class ProcessWorker implements Runnable {

        private Processlet process;

        private ProcessletOutputs outputs;

        private ProcessletExecution state;

        private ResponseDocumentStorage responseStorage;

        private List<RequestedOutput> outputParams;

        private ExecuteRequest request;

        ProcessWorker( Processlet process, ProcessletOutputs outputs, ProcessletExecution state,
                       List<RequestedOutput> outputParams, ResponseDocumentStorage responseStorage,
                       ExecuteRequest request ) {
            this.process = process;
            this.outputs = outputs;
            this.state = state;
            this.outputParams = outputParams;
            this.responseStorage = responseStorage;
            this.request = request;
        }

        /**
         * This method is only called when the processlet is executed asynchronously, i.e. when the response document
         * should be stored as a web-accessible resource.
         */
        @Override
        public void run() {

            // register the storage location of the response document from the lookup map
            responseDocumentIdToState.put( responseStorage, state );

            executeProcess( process, request.getDataInputs(), outputs, state );

            LOG.debug( "Storing final response document at " + responseStorage );

            // write final ExecuteResponse document
            try {
                URL serviceInstance = new URL( OGCFrontController.getHttpGetURL()
                                               + "service=WPS&request=GetCapabilities&version=1.0.0" );

                ExecuteResponse executeResponse = new ExecuteResponse( responseStorage, serviceInstance, state,
                                                                       outputParams, outputs, request );

                XMLOutputFactory factory = XMLOutputFactory.newInstance();
                factory.setProperty( "javax.xml.stream.isRepairingNamespaces", Boolean.TRUE );
                XMLStreamWriter writer = factory.createXMLStreamWriter( new OutputStreamWriter(
                                                                                                responseStorage.getOutputStream(),
                                                                                                "UTF-8" ) );
                ExecuteResponseXMLAdapter.export100( writer, executeResponse );
                writer.flush();
            } catch ( Exception e ) {
                String msg = "Generating ExecuteResponse document failed: " + e.getMessage();
                LOG.error( msg, e );
            }

            // deregister the storage location of the response document
            responseDocumentIdToState.remove( responseStorage );
        }
    }
}
