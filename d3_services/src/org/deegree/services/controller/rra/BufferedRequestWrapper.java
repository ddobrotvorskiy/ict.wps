//$HeadURL: http://wald.intevation.org/svn/deegree/deegree3/services/trunk/src/org/deegree/services/controller/rra/BufferedRequestWrapper.java $
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
package org.deegree.services.controller.rra;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

/**
 * <code>BufferedResponseWrapper</code>
 *
 * This is a custom {@link HttpServletRequestWrapper}. The {@link ServletInputStream} that is returned by this wrapper
 * supports the {@link InputStream#reset()} method.
 *
 *
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 * @author last edited by: $Author: mschneider $
 *
 * @version $Revision: 18171 $, $Date: 2009-06-17 21:00:07 +0700 (Срд, 17 Июн 2009) $
 *
 */
public class BufferedRequestWrapper extends HttpServletRequestWrapper {

    private BufferedServletInputStream buffer;

    /**
     * @param request
     * @throws IOException
     */
    public BufferedRequestWrapper( HttpServletRequest request ) throws IOException {
        super( request );
        this.buffer = new BufferedServletInputStream( request.getInputStream() );
    }

    @Override
    public ServletInputStream getInputStream()
                            throws IOException {
        return buffer;
    }

    @Override
    public BufferedReader getReader()
                            throws IOException {
        // TODO: only allow a call to either getInputStream _or_ getReader, see servlet api contract.
        String encoding = getCharacterEncoding();
        return new BufferedReader( new InputStreamReader( buffer, encoding ) );
    }
}
