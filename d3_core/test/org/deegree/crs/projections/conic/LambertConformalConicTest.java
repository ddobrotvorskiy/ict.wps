//$HeadURL: http://wald.intevation.org/svn/deegree/deegree3/core/trunk/test/org/deegree/crs/projections/conic/LambertConformalConicTest.java $
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

package org.deegree.crs.projections.conic;

import javax.vecmath.Point2d;

import org.deegree.crs.components.Unit;
import org.deegree.crs.exceptions.ProjectionException;
import org.deegree.crs.projections.ProjectionTest;
import org.deegree.crs.projections.conic.LambertConformalConic;
import org.junit.Test;

/**
 * <code>StereographicAlternativeTest</code> test the lambert conformal conic projection
 *
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 *
 * @author last edited by: $Author: mschneider $
 *
 * @version $Revision: 18171 $, $Date: 2009-06-17 21:00:07 +0700 (Срд, 17 Июн 2009) $
 *
 */
public class LambertConformalConicTest extends ProjectionTest {

    private static final LambertConformalConic projection_26985 = new LambertConformalConic(
                                                                                             Math.toRadians( 39.45 ),
                                                                                             Math.toRadians( 38.3 ),
                                                                                             geographic_4258,
                                                                                             0,
                                                                                             400000.0,
                                                                                             new Point2d(
                                                                                                          Math.toRadians( -77 ),
                                                                                                          Math.toRadians( 37.66666666666665 ) ),
                                                                                             Unit.METRE );

    /**
     * reference point created with proj4 command : <code>
     * proj -f "%.8f" +proj=lcc +ellps=GRS80 +lon_0=-77 +lat_0=37.66666666665 +k=1 +x_0=400000 +y_0=0 +lat_1=39.45 +lat_2=38.3
     * 6.610765 53.235916
     * 5402441.35292079        4213918.86230420
     * </code>
     *
     * @throws ProjectionException
     */
    @Test
    public void testAccuracy()
                            throws ProjectionException {

        Point2d sourcePoint = new Point2d( Math.toRadians( 6.610765 ), Math.toRadians( 53.235916 ) );
        Point2d targetPoint = new Point2d( 5402441.35292079, 4213918.86230420 );

        doForwardAndInverse( projection_26985, sourcePoint, targetPoint );
    }

    /**
     * tests the consistency of the {@link LambertConformalConic} projection.
     */
    @Test
    public void testConsistency() {
        consistencyTest( projection_26985, 0, 400000, new Point2d( Math.toRadians( -77 ),
                                                                   Math.toRadians( 37.66666666666665 ) ), Unit.METRE,
                         1, true, false, "lambertConformalConic" );
        assertEquals( Math.toRadians( 39.45 ), projection_26985.getFirstParallelLatitude() );
        assertEquals( Math.toRadians( 38.3 ), projection_26985.getSecondParallelLatitude() );
    }

}
