//$HeadURL: http://wald.intevation.org/svn/deegree/deegree3/core/trunk/test/org/deegree/crs/configuration/gml/GMLCRSProviderTest.java $
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

package org.deegree.crs.configuration.gml;

import java.io.File;

import junit.framework.TestCase;

import org.deegree.crs.CRSCodeType;
import org.deegree.crs.components.Axis;
import org.deegree.crs.components.Ellipsoid;
import org.deegree.crs.components.GeodeticDatum;
import org.deegree.crs.components.Unit;
import org.deegree.crs.configuration.CRSConfiguration;
import org.deegree.crs.configuration.CRSProvider;
import org.deegree.crs.coordinatesystems.CoordinateSystem;
import org.deegree.crs.coordinatesystems.GeographicCRS;
import org.deegree.crs.coordinatesystems.ProjectedCRS;
import org.deegree.crs.projections.Projection;
import org.deegree.crs.projections.cylindric.TransverseMercator;
import org.deegree.crs.transformations.helmert.Helmert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <code>GMLCRSProviderTest</code> test the loading of a projected crs as well as the loading of the default
 * configuration.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * 
 * @author last edited by: $Author: rbezema $
 * 
 * @version $Revision: 18791 $, $Date: 2009-07-29 18:50:10 +0700 (Срд, 29 Июл 2009) $
 * 
 */
public class GMLCRSProviderTest extends TestCase {

    private static final String CONFIG_FILE = "/org/deegree/crs/configuration/gml/gmlDictionary.xml";

    // "${user.home}/workspace/adv_registry2/docs/specification/AdV-Registry-P2_130-v02_dictionary_2.xml";

    private static Logger LOG = LoggerFactory.getLogger( GMLCRSProviderTest.class );

    private GMLCRSProvider getProvider() {
        File f = new File( this.getClass().getResource( CONFIG_FILE ).getPath() );
        if ( !f.exists() ) {
            LOG.error( "No configuration file found, nothing to test. " );
            throw new NullPointerException( "The file was not found. " );
        }
        final String old_value = CRSConfiguration.setDefaultFileProperty( GMLCRSProviderTest.class.getResource(
                                                                                                                CONFIG_FILE ).getPath() );
        CRSProvider provider = CRSConfiguration.getCRSConfiguration( "org.deegree.crs.configuration.gml.GMLCRSProvider" ).getProvider();
        assertNotNull( provider );
        if ( !( provider instanceof GMLCRSProvider ) ) {
            throw new NullPointerException( "The provider was not loaded. " );
        }
        CRSConfiguration.setDefaultFileProperty( old_value );
        return (GMLCRSProvider) provider;

    }

    /**
     * Tries to load the configuration
     */
    @Test
    public void testLoadingConfiguration() {
        try {
            GMLCRSProvider gProvider = getProvider();
            assertFalse( gProvider.canExport() );
        } catch ( NullPointerException e ) {
            // nottin.
        }
    }

    /**
     * Tries to create a crs by id.
     */
    @Test
    public void testCRSByID() {
        try {
            GMLCRSProvider gProvider = getProvider();
            // for (String id : gProvider.getCRSByID(""))) {
            // System.out.println ("id: " + id);
            // }
            // try loading the gaus krueger zone 3.
            // System.out.println( CRSCodeType.valueOf( "urn:ogc:def:crs:EPSG::31467" ) );
            CoordinateSystem testCRS = gProvider.getCRSByCode( CRSCodeType.valueOf( "urn:ogc:def:crs:EPSG::31467" ) );
            testCRS_31467( testCRS );
            testCRS = gProvider.getCRSByCode( CRSCodeType.valueOf( "SOME_DUMMY_CODE" ) );
            assertTrue( testCRS == null );
        } catch ( NullPointerException e ) {
            // nottin.
        }
    }

    private void testCRS_31467( CoordinateSystem testCRS ) {
        assertNotNull( testCRS );
        assertTrue( testCRS instanceof ProjectedCRS );
        ProjectedCRS realCRS = (ProjectedCRS) testCRS;
        assertNotNull( realCRS.getProjection() );
        Projection projection = realCRS.getProjection();
        assertTrue( projection instanceof TransverseMercator );
        // do stuff with projection
        TransverseMercator proj = (TransverseMercator) projection;
        assertEquals( 0.0, proj.getProjectionLatitude() );
        assertEquals( Math.toRadians( 9.0 ), proj.getProjectionLongitude() );
        assertEquals( 1.0, proj.getScale() );
        assertEquals( 3500000.0, proj.getFalseEasting() );
        assertEquals( 0.0, proj.getFalseNorthing() );
        assertTrue( proj.getHemisphere() );

        // test the datum.
        GeodeticDatum datum = realCRS.getGeodeticDatum();
        assertNotNull( datum );
        // assertEquals( "EPSG:6314", datum.getIdentifier() );
        assertEquals( "urn:ogc:def:datum:EPSG::6314", datum.getCode().getOriginal() );
        // assertEquals( PrimeMeridian.GREENWICH, datum.getPrimeMeridian() );
        // assertEquals( "urn:adv:meridian:Greenwich", datum.getPrimeMeridian().getIdentifier() );

        // test the ellips
        Ellipsoid ellips = datum.getEllipsoid();
        assertNotNull( ellips );
        // assertEquals( "EPSG:7004", ellips.getIdentifier() );
        assertEquals( "urn:ogc:def:ellipsoid:EPSG::7004", ellips.getCode().getOriginal() );
        assertEquals( Unit.METRE, ellips.getUnits() );
        assertEquals( 6377397.155, ellips.getSemiMajorAxis() );
        assertEquals( 299.1528128, ellips.getInverseFlattening() );

        // test towgs84 params
        Helmert toWGS = datum.getWGS84Conversion();
        assertNotNull( toWGS );
        assertTrue( toWGS.hasValues() );
        assertEquals( "urn:ogc:def:coordinateOperation:EPSG::1777", toWGS.getCode().getOriginal() );
        assertEquals( 598.1, toWGS.dx );
        assertEquals( 73.7, toWGS.dy );
        assertEquals( 418.2, toWGS.dz );
        assertEquals( 0.202, Unit.RADIAN.convert( toWGS.ex, Unit.ARC_SEC ) );
        assertEquals( 0.045, Unit.RADIAN.convert( toWGS.ey, Unit.ARC_SEC ) );
        assertEquals( -2.455, Unit.RADIAN.convert( toWGS.ez, Unit.ARC_SEC ) );
        assertEquals( 6.7, toWGS.ppm );

        // test the geographic
        GeographicCRS geographic = realCRS.getGeographicCRS();
        assertNotNull( geographic );
        assertEquals( "urn:ogc:def:crs:EPSG::4314", geographic.getCode().getOriginal() );
        // assertEquals( "EPSG:4314", geographic.getIdentifier() );
        Axis[] ax = geographic.getAxis();
        assertEquals( 2, ax.length );
        assertEquals( Axis.AO_EAST, ax[1].getOrientation() );
        assertEquals( Unit.DEGREE, ax[1].getUnits() );
        assertEquals( Axis.AO_NORTH, ax[0].getOrientation() );
        assertEquals( Unit.DEGREE, ax[0].getUnits() );
    }

    /**
     * Test a cache
     */
    public void testCache() {
        GMLCRSProvider gProvider = getProvider();

        CoordinateSystem testCRS = gProvider.getCRSByCode( CRSCodeType.valueOf( "urn:ogc:def:crs:EPSG::31467" ) );
        testCRS_31467( testCRS );

        testCRS = gProvider.getCRSByCode( CRSCodeType.valueOf( "urn:ogc:def:crs:EPSG::31467" ) );
        testCRS_31467( testCRS );
    }
}
