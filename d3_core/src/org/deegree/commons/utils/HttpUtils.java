//$HeadURL: http://wald.intevation.org/svn/deegree/deegree3/core/trunk/src/org/deegree/commons/utils/HttpUtils.java $
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

package org.deegree.commons.utils;

import static javax.imageio.ImageIO.read;
import static org.deegree.commons.utils.ArrayUtils.join;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.Map;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.stax.XMLStreamReaderWrapper;

/**
 * <code>HttpUtils</code>
 *
 * Example use from rhino:
 *
 * <code>
 * var u = org.deegree.commons.utils.HttpUtils
 * u.retrieve(u.UTF8STRING, "http://demo.deegree.org/deegree-wms/services?request=capabilities&service=WMS")
 * </code>
 *
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author: mschneider $
 *
 * @version $Revision: 18171 $, $Date: 2009-06-17 21:00:07 +0700 (Срд, 17 Июн 2009) $
 */
public class HttpUtils {

    static final XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();

    /**
     * <code>Worker</code> is used to specify how to return the stream from the remote location.
     *
     * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
     * @author last edited by: $Author: mschneider $
     *
     * @version $Revision: 18171 $, $Date: 2009-06-17 21:00:07 +0700 (Срд, 17 Июн 2009) $
     * @param <T>
     */
    public interface Worker<T> {
        /**
         * @param in
         * @return some object created from the input stream
         * @throws IOException
         */
        T work( InputStream in )
                                throws IOException;
    }

    /**
     * Directly returns the stream.
     */
    public static final Worker<InputStream> STREAM = new Worker<InputStream>() {
        public InputStream work( InputStream in ) {
            return in;
        }
    };

    /**
     * Returns streaming XMLAdapter.
     */
    public static final Worker<XMLAdapter> XML = new Worker<XMLAdapter>() {
        public XMLAdapter work( InputStream in ) {
            return new XMLAdapter( in );
        }
    };

    /**
     * Returns streaming XMLAdapter.
     */
    public static final Worker<XMLStreamReaderWrapper> XML_STREAM = new Worker<XMLStreamReaderWrapper>() {
        public XMLStreamReaderWrapper work( InputStream in ) throws IOException {
            try {
                return new XMLStreamReaderWrapper(xmlInputFactory.createXMLStreamReader(in ), "Post response" );
            } catch ( XMLStreamException e ) {
                throw new IOException ("Error creating XMLStreamReader for POST response: " + e.getMessage());
            }
        }
    };

    /**
     * Returns a decoded String.
     */
    public static final Worker<String> UTF8STRING = getStringWorker( "UTF-8" );

    /**
     * Returns a BufferedImage.
     */
    public static final Worker<BufferedImage> IMAGE = new Worker<BufferedImage>() {
        public BufferedImage work( InputStream in )
                                throws IOException {
            return read( in );
        }
    };

    /**
     * @param encoding
     * @return a string producer for a specific encoding
     */
    public static Worker<String> getStringWorker( final String encoding ) {
        return new Worker<String>() {
            public String work( InputStream in )
                                    throws IOException {
                BufferedReader bin = new BufferedReader( new InputStreamReader( in, encoding ) );
                StringBuilder b = new StringBuilder();
                String str;
                while ( ( str = bin.readLine() ) != null ) {
                    b.append( str ).append( "\n" );
                }
                bin.close();
                return b.toString();
            }
        };
    }

    /**
     * @param <T>
     * @param worker
     * @param url
     * @return some object from the url
     * @throws IOException
     */
    public static <T> T retrieve( Worker<T> worker, URL url )
                            throws IOException {
        return worker.work( url.openStream() );
    }

    /**
     * @param <T>
     * @param worker
     * @param url
     * @return some object from the url
     * @throws MalformedURLException
     * @throws IOException
     */
    public static <T> T retrieve( Worker<T> worker, String url )
                            throws MalformedURLException, IOException {
        return retrieve( worker, new URL( url ) );
    }

    /**
     * @param <T>
     * @param worker
     * @param url
     * @param map
     * @return some object from the url
     * @throws IOException
     * @throws MalformedURLException
     */
    public static <T> T retrieve( Worker<T> worker, String url, Map<String, String> map )
                            throws MalformedURLException, IOException {
        if ( !url.endsWith( "?" ) && !url.endsWith( "&" ) ) {
            url += url.indexOf( "?" ) == -1 ? "?" : "&";
        }
        LinkedList<String> list = new LinkedList<String>();
        for ( String k : map.keySet() ) {
            list.add( k + "=" + map.get( k ) );
        }
        url += join( "&", list );
        return retrieve( worker, url );
    }

    /**
     * Performs an HTTP-Get request and provides typed access to the response.
     *
     * @param <T>
     * @param worker
     * @param url
     * @param postBody
     * @param headers
     * @return some object from the url
     * @throws HttpException
     * @throws IOException
     */
    public static <T> T post( Worker<T> worker, String url, InputStream postBody, Map<String,String> headers)
                            throws HttpException, IOException {
        // TODO no proxies used
        HttpClient client = new HttpClient();
        PostMethod post = new PostMethod( url );
        post.setRequestEntity( new InputStreamRequestEntity(postBody) );
        for ( String key : headers.keySet() ) {
            post.setRequestHeader( key, headers.get( key ));
        }
        client.executeMethod( post );
        return worker.work( post.getResponseBodyAsStream() );
    }

    /**
     * Performs an HTTP-Get request and provides typed access to the response.
     *
     * @param <T>
     * @param worker
     * @param url
     * @param headers
     * @return some object from the url
     * @throws HttpException
     * @throws IOException
     */
    public static <T> T get( Worker<T> worker, String url, Map<String,String> headers)
                            throws HttpException, IOException {

        // TODO no proxies used
        HttpClient client = new HttpClient();
        GetMethod get = new GetMethod( url );
        for ( String key : headers.keySet() ) {
            get.setRequestHeader( key, headers.get( key ));
        }
        client.executeMethod( get );
        return worker.work( get.getResponseBodyAsStream() );
    }
}
