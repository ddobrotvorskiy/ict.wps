//$HeadURL: http://wald.intevation.org/svn/deegree/deegree3/core/trunk/src/org/deegree/coverage/raster/container/MemoryTileContainer.java $
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
package org.deegree.coverage.raster.container;

import java.util.ArrayList;
import java.util.List;

import org.deegree.coverage.raster.AbstractRaster;
import org.deegree.coverage.raster.geom.RasterGeoReference;
import org.deegree.coverage.raster.utils.RasterFactory;
import org.deegree.geometry.Envelope;

/**
 * This TileContainer keeps all tiles (AbstractRaster) in memory.
 * 
 * Use this container for tiles with a few thousand or less tiles. The AbstractRaster should be loaded with a LAZY or
 * CACHED LoadingPolicy (see {@link RasterFactory}).
 * 
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 * @author last edited by: $Author: rbezema $
 * 
 * @version $Revision: 20731 $, $Date: 2009-11-11 21:51:49 +0600 (Срд, 11 Ноя 2009) $
 */
public class MemoryTileContainer implements TileContainer {

    private List<AbstractRaster> tiles = new ArrayList<AbstractRaster>();

    private RasterGeoReference rasterReference;

    private Envelope envelope;

    /**
     * Creates a MemoryTileContainer with no tiles.
     * 
     * @param geoRasterRef
     *            of this tile container
     * @param envelope
     *            of this tile container.
     */
    public MemoryTileContainer( RasterGeoReference geoRasterRef, Envelope envelope ) {
        this.envelope = envelope;
        this.rasterReference = geoRasterRef;
    }

    /**
     * Creates a MemoryTileContainer with given tiles.
     * 
     * @param abstractRasters
     *            one or more tiles
     */
    public MemoryTileContainer( AbstractRaster... abstractRasters ) {
        if ( abstractRasters != null ) {
            for ( AbstractRaster raster : abstractRasters ) {
                if ( raster != null ) {
                    addTile( raster );
                }
            }
        }
    }

    /**
     * Creates a MemoryTileContainer with given tiles.
     * 
     * @param abstractRasters
     *            one or more tiles
     */
    public MemoryTileContainer( List<AbstractRaster> abstractRasters ) {
        if ( abstractRasters != null ) {
            for ( AbstractRaster raster : abstractRasters ) {
                addTile( raster );
            }
        }
    }

    /**
     * Adds a new tile to the container.
     * 
     * @param raster
     *            new tile
     */
    public synchronized void addTile( AbstractRaster raster ) {
        if ( raster != null ) {
            if ( this.envelope == null ) {
                this.envelope = raster.getEnvelope();
            } else {
                this.envelope = this.envelope.merge( raster.getEnvelope() );
            }
            if ( this.rasterReference == null ) {
                this.rasterReference = raster.getRasterReference();
            } else {
                this.rasterReference = RasterGeoReference.merger( this.rasterReference, raster.getRasterReference() );
            }
            tiles.add( raster );
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.model.raster.TileContainer#getTiles(org.deegree.geometry.primitive.Envelope)
     */
    public List<AbstractRaster> getTiles( Envelope env ) {
        List<AbstractRaster> result = new ArrayList<AbstractRaster>();
        for ( AbstractRaster r : tiles ) {
            if ( env.intersects( r.getEnvelope() ) ) {
                result.add( r );
            }
        }
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.model.raster.TileContainer#getEnvelope()
     */
    public Envelope getEnvelope() {
        return envelope;
    }

    public RasterGeoReference getRasterReference() {
        return rasterReference;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        for ( AbstractRaster r : tiles ) {
            result.append( r.toString() );
            result.append( "\n\t" );
        }
        if ( result.length() > 0 ) {
            result.delete( result.length() - 3, result.length() );
        }
        return result.toString();
    }

}
