//$HeadURL: http://wald.intevation.org/svn/deegree/deegree3/core/trunk/src/org/deegree/rendering/r2d/RenderHelper.java $
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

package org.deegree.rendering.r2d;

import static java.awt.geom.AffineTransform.getRotateInstance;
import static java.awt.geom.AffineTransform.getScaleInstance;
import static java.awt.geom.AffineTransform.getTranslateInstance;
import static java.lang.Math.PI;
import static java.lang.Math.max;
import static java.lang.Math.toRadians;
import static org.apache.batik.bridge.BridgeContext.DYNAMIC;
import static org.slf4j.LoggerFactory.getLogger;

import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashSet;

import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.DocumentLoader;
import org.apache.batik.bridge.GVTBuilder;
import org.apache.batik.bridge.UserAgent;
import org.apache.batik.bridge.UserAgentAdapter;
import org.apache.batik.dom.svg.SAXSVGDocumentFactory;
import org.apache.batik.gvt.GVTTreeWalker;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.gvt.RootGraphicsNode;
import org.deegree.rendering.r2d.styling.components.Mark;
import org.deegree.rendering.r2d.styling.components.UOM;
import org.slf4j.Logger;
import org.w3c.dom.svg.SVGDocument;

/**
 * <code>RenderHelper</code>
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author: aschmitz $
 * 
 * @version $Revision: 21378 $, $Date: 2009-12-10 20:26:56 +0600 (Чтв, 10 Дек 2009) $
 */
public class RenderHelper {

    private static final Logger LOG = getLogger( RenderHelper.class );

    /**
     * Example: calculateStarPolygon (5, 2) connects the first, third, fifth, second, fourth and again first edge in
     * that order.
     * 
     * @param edges
     *            the number of edges
     * @param skip
     *            specifies, how to connect the edges
     * @param size
     * @return a general path that draws the star polygon by the rule
     */
    public static Path2D.Double calculateStarPolygon( final int edges, final int skip, final double size ) {
        // calculate from circle around (0, 0)
        // start with the point on the top
        // then, for subsequent points, use the addition theroem
        // after calculating the points, move the circle back from (0, 0) inside the rect

        double[] xs = new double[edges];
        double[] ys = new double[edges];
        double half = size / 2;
        xs[0] = 0;
        ys[0] = -half;
        double ang = PI * 2 / edges;
        double sinbeta = Math.sin( ang );
        double cosbeta = Math.cos( ang );

        for ( int i = 1; i < edges; ++i ) {
            xs[i] = xs[i - 1] * cosbeta - ys[i - 1] * sinbeta;
            ys[i] = xs[i - 1] * sinbeta + ys[i - 1] * cosbeta;
        }

        for ( int i = 0; i < edges; ++i ) {
            xs[i] += half;
            ys[i] += half;
        }

        // build the actual star by drawing lines between the points in the proper order
        HashSet<Integer> visited = new HashSet<Integer>( edges );
        Path2D.Double path = new Path2D.Double();
        for ( int i = 0; i < skip; ++i ) {
            int k = i;
            if ( visited.contains( k ) ) {
                continue;
            }
            path.moveTo( xs[k], ys[k] );
            visited.add( k );
            while ( k < edges ) {
                k += skip;
                if ( k < edges ) {
                    path.lineTo( xs[k], ys[k] );
                    visited.add( k );
                } else {
                    path.lineTo( xs[k - edges], ys[k - edges] );
                    if ( !visited.contains( k - edges ) ) {
                        k -= edges;
                        visited.add( k );
                    }
                }
            }
        }

        path.closePath();
        return path;
    }

    /**
     * @param mark
     * @param size
     * @param center
     * @param rotation
     * @return a shape representing the mark
     */
    public static Shape getShapeFromMark( Mark mark, double size, boolean center, double rotation ) {
        if ( mark.shape != null ) {
            Rectangle2D box = mark.shape.getBounds2D();
            double cur = max( box.getWidth(), box.getHeight() );
            double fac = size / cur;
            AffineTransform trans = getScaleInstance( fac, fac );
            if ( center ) {
                trans.translate( -box.getWidth() / 2, -box.getHeight() / 2 );
                trans.translate( -box.getMinX(), -box.getMinY() );
            } else {
                trans.translate( -box.getMinX(), -box.getMinY() );
            }
            trans.rotate( toRadians( rotation ) );
            return trans.createTransformedShape( mark.shape );
        }

        GeneralPath shape = new GeneralPath();

        if ( mark.font != null ) {
            FontRenderContext frc = new FontRenderContext( null, false, true );
            GlyphVector vec = mark.font.deriveFont( (float) size ).createGlyphVector( frc, new int[] { mark.markIndex } );
            Rectangle2D bounds = vec.getVisualBounds();
            shape.append( vec.getOutline( -(float) bounds.getX(), -(float) bounds.getY() ), false );
        } else {
            switch ( mark.wellKnown ) {
            case CIRCLE:
                shape.append( new Ellipse2D.Double( 0, 0, size, size ), false );
                break;
            case CROSS: {
                double half = size / 2;
                shape.append( new Line2D.Double( half, 0, half, size ), false );
                shape.append( new Line2D.Double( 0, half, size, half ), false );
                break;
            }
            case SQUARE:
                shape.append( new Rectangle2D.Double( 0, 0, size, size ), false );
                break;
            case STAR: {
                shape.append( calculateStarPolygon( 5, 2, size ), false );
                break;
            }
            case TRIANGLE:
                Path2D.Double path = new Path2D.Double();
                path.moveTo( size / 2, 0 );
                path.lineTo( 0, size );
                path.lineTo( size, size );
                path.closePath();
                shape.append( path, false );
                break;
            case X:
                shape.append( new Line2D.Double( 0, 0, size, size ), false );
                shape.append( new Line2D.Double( size, 0, 0, size ), false );
                break;
            }
        }
        return getRotateInstance( toRadians( rotation ) ).createTransformedShape( shape );
    }

    /**
     * @param mark
     * @param size
     * @param uom
     * @param renderer
     * @param x
     * @param y
     * @param rotation
     */
    public static void renderMark( Mark mark, int size, UOM uom, Java2DRenderer renderer, double x, double y,
                                   double rotation ) {
        if ( size == 0 ) {
            LOG.debug( "Not rendering a symbol because the size is zero." );
            return;
        }
        if ( mark.fill == null && mark.stroke == null ) {
            LOG.debug( "Not rendering a symbol because no fill/stroke is available/configured." );
            return;
        }

        Shape shape = getShapeFromMark( mark, size - 1, false, rotation );
        Rectangle2D box = shape.getBounds2D();
        shape = getTranslateInstance( x - box.getWidth() / 2, y - box.getHeight() / 2 ).createTransformedShape( shape );

        if ( mark.fill != null ) {
            renderer.applyFill( mark.fill, uom );
            renderer.graphics.fill( shape );
        }
        if ( mark.stroke != null ) {
            renderer.applyStroke( mark.stroke, uom, shape, 0, null );
        }
    }

    /**
     * @param url
     * @param size
     * @param rotation
     * @return a shape object from the given svg
     */
    public static Shape getShapeFromSvg( String url, double size, double rotation ) {
        try {
            Shape shape = getShapeFromSvg( new URL( url ).openStream(), url );
            if ( shape != null ) {
                AffineTransform at = getScaleInstance( size, size );
                at.rotate( toRadians( rotation ) );
                return at.createTransformedShape( shape );
            }
        } catch ( IOException e ) {
            LOG.warn( "The svg image at '{}' could not be read: {}", url, e.getLocalizedMessage() );
            LOG.debug( "Stack trace", e );
        }

        return null;
    }

    /**
     * @param in
     * @param url
     * @return a shape object
     */
    public static Shape getShapeFromSvg( InputStream in, String url ) {
        try {
            SAXSVGDocumentFactory fac = new SAXSVGDocumentFactory( "org.apache.xerces.parsers.SAXParser" );
            SVGDocument doc = fac.createSVGDocument( url, in );
            GVTBuilder builder = new GVTBuilder();
            UserAgent userAgent = new UserAgentAdapter();
            DocumentLoader loader = new DocumentLoader( userAgent );
            BridgeContext ctx = new BridgeContext( userAgent, loader );
            ctx.setDynamicState( DYNAMIC );
            RootGraphicsNode root = builder.build( ctx, doc ).getRoot();

            AffineTransform t = new AffineTransform();
            Rectangle2D rect = root.getBounds();
            double max = max( rect.getWidth(), rect.getHeight() );
            t.scale( 1 / max, 1 / max );
            t.translate( -rect.getX(), -rect.getY() );

            root.setTransform( t );

            GVTTreeWalker walker = new GVTTreeWalker( root );
            GraphicsNode node = root;
            // should not include root's shape in the path as it doesn't always work properly
            GeneralPath shape = new GeneralPath();
            while ( ( node = walker.nextGraphicsNode() ) != null ) {
                AffineTransform t2 = (AffineTransform) t.clone();
                t2.concatenate( node.getTransform() );
                node.setTransform( t2 );
                shape.append( node.getOutline(), false );
            }

            return root.getOutline();
        } catch ( IOException e ) {
            LOG.warn( "The svg image at '{}' could not be read: {}", url, e.getLocalizedMessage() );
            LOG.debug( "Stack trace", e );
        }

        return null;
    }

}
