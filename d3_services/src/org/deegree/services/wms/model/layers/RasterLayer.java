//$HeadURL: http://wald.intevation.org/svn/deegree/deegree3/services/trunk/src/org/deegree/services/wms/model/layers/RasterLayer.java $
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
package org.deegree.services.wms.model.layers;

import static java.lang.Integer.MAX_VALUE;
import static org.deegree.coverage.rangeset.Interval.Closure.open;
import static org.deegree.coverage.rangeset.RangeSetBuilder.createBandRangeSetFromRaster;
import static org.deegree.coverage.rangeset.ValueType.Void;
import static org.deegree.coverage.raster.interpolation.InterpolationType.BILINEAR;
import static org.deegree.coverage.raster.interpolation.InterpolationType.NEAREST_NEIGHBOR;
import static org.deegree.coverage.raster.interpolation.InterpolationType.NONE;
import static org.deegree.feature.types.property.PrimitiveType.DECIMAL;
import static org.slf4j.LoggerFactory.getLogger;

import java.awt.Graphics2D;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javax.xml.namespace.QName;

import org.deegree.commons.utils.Pair;
import org.deegree.commons.utils.Triple;
import org.deegree.coverage.AbstractCoverage;
import org.deegree.coverage.filter.raster.RasterFilter;
import org.deegree.coverage.rangeset.AxisSubset;
import org.deegree.coverage.rangeset.Interval;
import org.deegree.coverage.rangeset.RangeSet;
import org.deegree.coverage.rangeset.SingleValue;
import org.deegree.coverage.raster.AbstractRaster;
import org.deegree.coverage.raster.MultiResolutionRaster;
import org.deegree.coverage.raster.SimpleRaster;
import org.deegree.coverage.raster.data.RasterData;
import org.deegree.coverage.raster.data.info.BandType;
import org.deegree.coverage.raster.data.info.DataType;
import org.deegree.coverage.raster.interpolation.InterpolationType;
import org.deegree.crs.CRS;
import org.deegree.crs.exceptions.TransformationException;
import org.deegree.crs.exceptions.UnknownCRSException;
import org.deegree.feature.Feature;
import org.deegree.feature.FeatureCollection;
import org.deegree.feature.GenericFeature;
import org.deegree.feature.GenericFeatureCollection;
import org.deegree.feature.GenericProperty;
import org.deegree.feature.Property;
import org.deegree.feature.types.FeatureType;
import org.deegree.feature.types.GenericFeatureType;
import org.deegree.feature.types.property.PropertyType;
import org.deegree.feature.types.property.SimplePropertyType;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.GeometryTransformer;
import org.deegree.protocol.wms.dims.DimensionInterval;
import org.deegree.rendering.r2d.Java2DRasterRenderer;
import org.deegree.rendering.r2d.se.unevaluated.Style;
import org.deegree.rendering.r2d.styling.RasterStyling;
import org.deegree.rendering.r2d.styling.Styling;
import org.deegree.services.controller.wms.ops.GetFeatureInfo;
import org.deegree.services.controller.wms.ops.GetMap;
import org.deegree.services.wcs.WCServiceException;
import org.deegree.services.wcs.coverages.CoverageTransform;
import org.deegree.services.wcs.model.Grid;
import org.deegree.services.wms.WMSException.InvalidDimensionValue;
import org.deegree.services.wms.WMSException.MissingDimensionValue;
import org.deegree.services.wms.configuration.AbstractLayerType;
import org.deegree.services.wms.model.Dimension;
import org.slf4j.Logger;

/**
 * <code>RasterLayer</code>
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author: aionita $
 * 
 * @version $Revision: 21495 $, $Date: 2009-12-16 19:51:38 +0600 (Срд, 16 Дек 2009) $
 */
public class RasterLayer extends Layer {

    private static final Logger LOG = getLogger( RasterLayer.class );

    private AbstractRaster raster;

    private MultiResolutionRaster multiraster;

    private GeometryTransformer trans;

    private GenericFeatureType featureType;

    /**
     * @param lay
     * @param parent
     * @param raster
     */
    public RasterLayer( AbstractLayerType lay, Layer parent, AbstractCoverage raster ) {
        super( lay, parent );
        this.raster = (AbstractRaster) ( raster instanceof AbstractRaster ? raster : null );
        this.multiraster = (MultiResolutionRaster) ( raster instanceof MultiResolutionRaster ? raster : null );

        CRS crs = raster == null ? multiraster.getCoordinateSystem() : raster.getCoordinateSystem();
        try {
            trans = new GeometryTransformer( crs.getWrappedCRS() );
        } catch ( IllegalArgumentException e ) {
            LOG.warn( "CRS {} of raster data source is not known.", crs );
        } catch ( UnknownCRSException e ) {
            LOG.warn( "CRS {} of raster data source is not known.", crs );
        }

        List<PropertyType> pts = new LinkedList<PropertyType>();
        pts.add( new SimplePropertyType( new QName( "value" ), 0, -1, DECIMAL, false, null ) );
        featureType = new GenericFeatureType( new QName( "data" ), pts, false );
    }

    @Override
    public FeatureType getFeatureType() {
        return featureType;
    }

    @Override
    public Pair<FeatureCollection, LinkedList<String>> getFeatures( GetFeatureInfo fi, Style style )
                            throws MissingDimensionValue, InvalidDimensionValue {
        try {
            Envelope bbox = (Envelope) trans.transform( fi.getClickBox( 1 ) );
            AbstractRaster raster = this.raster;
            if ( raster == null ) {
                raster = multiraster.getRaster( bbox.getSpan0() );
            }
            SimpleRaster res = CoverageTransform.transform( raster, fi.getClickBox( 1 ),
                                                            Grid.fromSize( 2, 2, MAX_VALUE, bbox ),
                                                            InterpolationType.NEAREST_NEIGHBOR.toString() ).getAsSimpleRaster();
            RasterData data = res.getRasterData();
            GenericFeatureCollection col = new GenericFeatureCollection();
            List<Property<?>> props = new LinkedList<Property<?>>();
            DataType dataType = data.getDataType();
            switch ( dataType ) {
            case SHORT:
            case USHORT:
                props.add( new GenericProperty<BigDecimal>(
                                                            (PropertyType<BigDecimal>) featureType.getPropertyDeclarations().get(
                                                                                                                                  0 ),
                                                            new BigDecimal( data.getShortSample( 0, 0, 0 ) ) ) );
                break;
            case BYTE:
            case DOUBLE:
            case INT:
            case UNDEFINED:
                LOG.warn( "The raster is of type '{}', this is handled as float currently.", dataType );
            case FLOAT:
                props.add( new GenericProperty<Float>(
                                                       (PropertyType<Float>) featureType.getPropertyDeclarations().get(
                                                                                                                        0 ),
                                                       data.getFloatSample( 0, 0, 0 ) ) );
                break;
            }

            Feature f = new GenericFeature( featureType, null, props, null );
            col.add( f );
            return new Pair<FeatureCollection, LinkedList<String>>( col, new LinkedList<String>() );
        } catch ( IllegalArgumentException e ) {
            LOG.warn( "Could not transform bbox of request to raster CRS." );
            LOG.debug( "Stack trace:", e );
        } catch ( TransformationException e ) {
            LOG.warn( "Could not transform bbox of request to raster CRS." );
            LOG.debug( "Stack trace:", e );
        } catch ( UnknownCRSException e ) {
            LOG.warn( "Could not transform bbox of request to raster CRS." );
            LOG.debug( "Stack trace:", e );
        } catch ( WCServiceException e ) {
            LOG.debug( "No raster data was found for this bbox." );
            LOG.trace( "Stack trace:", e );
        }
        return new Pair<FeatureCollection, LinkedList<String>>( null, new LinkedList<String>() );
    }

    @Override
    public Envelope getBbox() {
        return raster == null ? multiraster.getEnvelope() : raster.getEnvelope();
    }

    @Override
    public LinkedList<String> paintMap( Graphics2D g, GetMap gm, Style style )
                            throws MissingDimensionValue, InvalidDimensionValue {

        LinkedList<String> warnings = new LinkedList<String>();

        try {
            Java2DRasterRenderer renderer = new Java2DRasterRenderer( g );

            Envelope bbox = gm.getBoundingBox();
            AbstractRaster raster = this.raster;
            if ( raster == null ) {
                raster = multiraster.getRaster( ( (Envelope) trans.transform( bbox ) ).getSpan0() / gm.getWidth() );
            }

            Pair<RangeSet, LinkedList<String>> p = getDimensionFilter( gm.getDimensions() );
            if ( p != null && p.first != null ) {
                RangeSet cbr = createBandRangeSetFromRaster( null, null, raster );
                raster = new RasterFilter( raster ).apply( cbr, p.first );
                warnings.addAll( p.second );
            }

            InterpolationType interpol = NONE;
            switch ( gm.getInterpolation().get( this ) ) {
            case BICUBIC:
                LOG.warn( "Raster API does not support bicubic interpolation, using bilinear instead." );
            case BILINEAR:
                interpol = BILINEAR;
                break;
            case NEARESTNEIGHBOR:
            case NEARESTNEIGHBOUR:
                interpol = NEAREST_NEIGHBOR;
                break;
            }

            raster = CoverageTransform.transform( raster, bbox, Grid.fromSize( gm.getWidth(), gm.getHeight(),
                                                                               MAX_VALUE, bbox ), interpol.toString() );

            LinkedList<Triple<Styling, Geometry, String>> list = style == null ? null : style.evaluate( null );
            if ( list != null && list.size() > 0 ) {
                for ( Triple<Styling, Geometry, String> t : list ) {
                    renderer.render( (RasterStyling) t.first, raster );
                }
            } else {
                renderer.render( null, raster );
            }
        } catch ( IllegalArgumentException e ) {
            LOG.debug( "Stack trace:", e );
            LOG.warn( "Could not transform bbox of request to raster CRS." );
        } catch ( TransformationException e ) {
            LOG.debug( "Stack trace:", e );
            LOG.warn( "Could not transform bbox of request to raster CRS." );
        } catch ( UnknownCRSException e ) {
            LOG.debug( "Stack trace:", e );
            LOG.warn( "Could not transform bbox of request to raster CRS." );
        } catch ( WCServiceException e ) {
            LOG.debug( "No raster data was found for this bbox." );
            LOG.trace( "Stack trace:", e );
        }
        return warnings;
    }

    /**
     * @param dims
     * @return a filter or null, if no dimensions have been requested
     * @throws MissingDimensionValue
     * @throws InvalidDimensionValue
     */
    private Pair<RangeSet, LinkedList<String>> getDimensionFilter( HashMap<String, List<?>> dims )
                            throws MissingDimensionValue, InvalidDimensionValue {

        LinkedList<String> warnings = new LinkedList<String>();
        LinkedList<AxisSubset> ops = new LinkedList<AxisSubset>();

        // TODO TIME, how to do this with raster API?

        for ( String name : dimensions.keySet() ) {
            List<SingleValue<?>> singleValues = new LinkedList<SingleValue<?>>();
            List<Interval<?, ?>> intervals = new LinkedList<Interval<?, ?>>();

            Dimension<Object> dim = dimensions.get( name );

            List<?> vals = dims.get( name );

            if ( dims.get( name ) == null ) {
                vals = dim.getDefaultValue();

                if ( vals == null ) {
                    throw new MissingDimensionValue( name );
                }
                String units = dim.getUnits();
                if ( name.equals( "elevation" ) ) {
                    warnings.add( "99 Default value used: elevation=" + vals.get( 0 ) + " "
                                  + ( units == null ? "m" : units ) );
                } else {
                    warnings.add( "99 Default value used: DIM_" + name + "=" + vals.get( 0 ) + " " + units );
                }
            }

            for ( Object o : vals ) {

                if ( !dim.getNearestValue() && !dim.isValid( o ) ) {
                    throw new InvalidDimensionValue( name, o.toString() );
                }

                if ( o instanceof DimensionInterval<?, ?, ?> ) {
                    DimensionInterval<?, ?, ?> iv = (DimensionInterval<?, ?, ?>) o;
                    final String min = iv.min.toString();
                    final String max = iv.max.toString();
                    intervals.add( new Interval<String, String>( new SingleValue<String>( Void, min ),
                                                                 new SingleValue<String>( Void, max ), open, null,
                                                                 false, null ) );
                } else {
                    if ( dim.getNearestValue() ) {
                        Object nearest = dim.getNearestValue( o );
                        if ( !nearest.equals( o ) ) {
                            o = nearest;
                            if ( "elevation".equals( name ) ) {
                                warnings.add( "99 Nearest value used: elevation=" + o + " " + dim.getUnits() );
                            } else {
                                warnings.add( "99 Nearest value used: DIM_" + name + "=" + o + " " + dim.getUnits() );
                            }
                        }
                    }
                    singleValues.add( new SingleValue<String>( Void, o.toString() ) );
                }
            }

            if ( singleValues.size() > 1 && ( !dim.getMultipleValues() ) ) {
                throw new InvalidDimensionValue( "elevation", vals.toString() );
            }

            ops.add( new AxisSubset( BandType.BAND_0.name(), null, intervals, singleValues ) );
        }

        if ( ops.isEmpty() ) {
            return null;
        }
        return new Pair<RangeSet, LinkedList<String>>( new RangeSet( ops ), warnings );
    }

}
