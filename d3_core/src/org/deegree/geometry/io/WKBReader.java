//$HeadURL: http://wald.intevation.org/svn/deegree/deegree3/core/trunk/src/org/deegree/geometry/io/WKBReader.java $
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
package org.deegree.geometry.io;

import java.io.IOException;
import java.io.InputStream;

import org.deegree.geometry.Geometry;
import org.deegree.geometry.standard.AbstractDefaultGeometry;
import org.deegree.geometry.standard.primitive.DefaultPoint;

import com.vividsolutions.jts.io.InputStreamInStream;
import com.vividsolutions.jts.io.ParseException;

/**
 * Reads {@link Geometry} objects encoded as Well-Known Binary (WKB).
 * 
 * TODO re-implement without delegating to JTS
 * TODO add support for non-SFS geometries (e.g. non-linear curves)
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: mschneider $
 * 
 * @version $Revision: 21403 $, $Date: 2009-12-11 22:02:50 +0600 (Птн, 11 Дек 2009) $
 */
public class WKBReader {

    // TODO remove the need for this object
    private static AbstractDefaultGeometry defaultGeom = new DefaultPoint( null, null, null, new double[] { 0.0, 0.0 } );

    public static Geometry read( byte[] wkb )
                            throws ParseException {
        // com.vividsolutions.jts.io.WKBReader() is not thread safe
        return defaultGeom.createFromJTS( new com.vividsolutions.jts.io.WKBReader().read( wkb ) );
    }

    public static Geometry read( InputStream is )
                            throws IOException, ParseException {
        // com.vividsolutions.jts.io.WKBReader() is not thread safe        
        return defaultGeom.createFromJTS( new com.vividsolutions.jts.io.WKBReader().read( new InputStreamInStream( is ) ) );
    }
}