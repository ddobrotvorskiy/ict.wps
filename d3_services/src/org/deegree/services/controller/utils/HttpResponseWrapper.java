//$HeadURL: http://wald.intevation.org/svn/deegree/deegree3/services/trunk/src/org/deegree/services/controller/utils/HttpResponseWrapper.java $
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

package org.deegree.services.controller.utils;

import static org.slf4j.LoggerFactory.getLogger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.deegree.commons.xml.CommonNamespaces;
import org.deegree.commons.xml.schema.SchemaValidator;
import org.deegree.commons.xml.stax.FormattingXMLStreamWriter;
import org.slf4j.Logger;

/**
 * This is a custom {@link HttpServletResponseWrapper}. It will buffer all data internally and will only send the result
 * after {@link #flushBuffer()} is called.
 * <p>
 * This allows two things:
 * <ul>
 * <li>The header can be changed after the response is generated.</li>
 * <li>The whole response and the header can be discarded.</li>
 * </ul>
 * </p>
 * With the first the service is able to set the Content-length. The second allows to discard the generated response and
 * start the response from scratch. This is useful if an exception occurred and a ExceptionReport should be returned and
 * not the partial original result.
 * <p>
 * This wrapper allows the change between {@link #getWriter()} and {@link #getOutputStream()} after {@link #reset()} was
 * called. This is unlike the original servlet API that throws an {@link IllegalStateException} when getWriter is called
 * after getOutputStream, or vice versa.
 * </p>
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: mschneider $
 * 
 * @version $Revision: 20096 $, $Date: 2009-10-13 00:54:15 +0700 (Втр, 13 Окт 2009) $
 */
public class HttpResponseWrapper extends HttpServletResponseWrapper {

    private static final Logger LOG = getLogger( HttpResponseWrapper.class );

    private final ByteArrayOutputStream buffer;

    /**
     * The servlet api only allows a call to either getWriter or getOutputStream. This enum will protocol the current
     * state.
     */
    private enum ReturnType {
        NOT_DEFINED_YET, PRINT_WRITER, OUTPUT_STREAM
    }

    private ReturnType returnType = ReturnType.NOT_DEFINED_YET;

    private PrintWriter printWriter;

    private ServletOutputStream outputStream;

    private XMLStreamWriter xmlWriter;

    /**
     * @param response
     */
    public HttpResponseWrapper( HttpServletResponse response ) {
        super( response );
        buffer = new ByteArrayOutputStream();
        outputStream = new BufferedServletOutputStream( buffer );
    }

    @Override
    public PrintWriter getWriter()
                            throws IOException {
        if ( returnType == ReturnType.NOT_DEFINED_YET ) {
            String encoding = getCharacterEncoding();
            if ( encoding == null || "".equals( encoding ) ) {
                encoding = Charset.defaultCharset().name();
            }
            OutputStreamWriter writer = new OutputStreamWriter( outputStream, encoding );
            printWriter = new PrintWriter( writer );
            returnType = ReturnType.PRINT_WRITER;
        }
        if ( returnType == ReturnType.OUTPUT_STREAM ) {
            throw new IllegalStateException( "getOutputStream() has already been called for this response" );
        }
        return printWriter;
    }

    @Override
    public ServletOutputStream getOutputStream()
                            throws IOException {
        if ( returnType == ReturnType.NOT_DEFINED_YET ) {
            returnType = ReturnType.OUTPUT_STREAM;
        }
        if ( returnType == ReturnType.PRINT_WRITER ) {
            throw new IllegalStateException( "getWriter() has already been called for this response" );
        }
        return outputStream;
    }

    /**
     * Returns an {@link XMLStreamWriter} for writing a response with XML content.
     * <p>
     * NOTE: This method may be called more than once -- the first call will create an {@link XMLStreamWriter} object
     * and subsequent calls return the same object. This provides a very convenient way to produce plain XML responses
     * and SOAP wrapped response bodies with the same code.
     * </p>
     * 
     * @return {@link XMLStreamWriter} for writing the response
     * @throws IOException
     * @throws XMLStreamException
     */
    public XMLStreamWriter getXMLWriter()
                            throws IOException, XMLStreamException {
        if ( xmlWriter == null ) {
            XMLOutputFactory factory = XMLOutputFactory.newInstance();
            factory.setProperty( "javax.xml.stream.isRepairingNamespaces", Boolean.TRUE );
            String encoding = "UTF-8";
            xmlWriter = new FormattingXMLStreamWriter( factory.createXMLStreamWriter( getOutputStream(), encoding ) );
        }
        return xmlWriter;
    }

    /**
     * Performs a schema-based validation of the response (only if the written output was XML).
     */
    public void validate() {

        boolean isXML = xmlWriter != null || ( getContentType() != null && getContentType().contains( "text/xml" ) );

        if ( isXML && buffer != null ) {

            long begin = System.currentTimeMillis();
            List<String> messages = null;
            String output = buffer.toString();

            LOG.debug( "Output: " + output );

            XMLStreamReader reader;
            try {
                reader = XMLInputFactory.newInstance().createXMLStreamReader( new StringReader( output ) );
                reader.nextTag();
                QName firstElement = reader.getName();
                if ( new QName( CommonNamespaces.XSNS, "schema" ).equals( firstElement ) ) {
                    LOG.info( "Validating generated XML output (schema document)." );
                    messages = SchemaValidator.validateSchema( new StringReader( output ) );
                } else {
                    LOG.info( "Validating generated XML output (instance document)." );
                    messages = SchemaValidator.validate( new StringReader( output ) );
                }
            } catch ( Exception e ) {
                messages = Collections.singletonList( e.getLocalizedMessage() );
            }

            long elapsed = System.currentTimeMillis() - begin;
            if ( messages.size() == 0 ) {
                LOG.info( "Output is valid. Validation took " + elapsed + " ms." );
            } else {
                LOG.error( "Output is ***invalid***: " + messages.size() + " error(s)/warning(s). Validation took "
                           + elapsed + " ms." );
                for ( String msg : messages ) {
                    LOG.error( "***" + msg );
                }
            }
        }
    }

    @Override
    public void flushBuffer()
                            throws IOException {
        buffer.flush();
        buffer.writeTo( super.getOutputStream() );
        buffer.reset();
        if ( xmlWriter != null ) {
            try {
                xmlWriter.flush();
            } catch ( XMLStreamException e ) {
                LOG.debug( e.getLocalizedMessage(), e );
                throw new IOException( e );
            }
        }
        super.flushBuffer();
    }

    @Override
    public void reset() {
        if ( !isCommitted() ) {
            buffer.reset();
            super.reset();
            returnType = ReturnType.NOT_DEFINED_YET;
        } else {
            super.reset(); // throws IllegalStateException
        }
    }

    @Override
    public int getBufferSize() {
        return buffer.size();
    }

    /**
     * This is a ServletOutputStream that uses our internal ByteArrayOutputStream to buffer all data.
     */
    private static class BufferedServletOutputStream extends ServletOutputStream {

        private final ByteArrayOutputStream buffer;

        public BufferedServletOutputStream( ByteArrayOutputStream buffer ) {
            this.buffer = buffer;
        }

        @Override
        public void write( int b )
                                throws IOException {
            buffer.write( b );
        }
    }
}
