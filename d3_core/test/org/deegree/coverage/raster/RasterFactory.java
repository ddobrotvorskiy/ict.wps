//$HeadURL: http://wald.intevation.org/svn/deegree/deegree3/core/trunk/test/org/deegree/coverage/raster/RasterFactory.java $
/*----------------    FILE HEADER  ------------------------------------------
 This file is part of deegree.
 Copyright (C) 2001-2009 by:
 Department of Geography, University of Bonn
 http://www.giub.uni-bonn.de/deegree/
 lat/lon GmbH
 http://www.lat-lon.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.
 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 Lesser General Public License for more details.
 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 Contact:

 Andreas Poth
 lat/lon GmbH
 Aennchenstr. 19
 53177 Bonn
 Germany
 E-Mail: poth@lat-lon.de

 Prof. Dr. Klaus Greve
 Department of Geography
 University of Bonn
 Meckenheimer Allee 166
 53115 Bonn
 Germany
 E-Mail: greve@giub.uni-bonn.de
 ---------------------------------------------------------------------------*/

package org.deegree.coverage.raster;

import static java.lang.System.currentTimeMillis;
import static java.lang.System.gc;

import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.media.jai.BorderExtender;
import javax.media.jai.ImageLayout;
import javax.media.jai.JAI;
import javax.media.jai.OperationRegistry;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.ParameterListDescriptor;
import javax.media.jai.RegistryElementDescriptor;
import javax.media.jai.RegistryMode;
import javax.media.jai.RenderedOp;

import junit.framework.Assert;

import org.deegree.commons.utils.FileUtils;
import org.deegree.coverage.raster.data.info.BandType;
import org.deegree.coverage.raster.data.info.DataType;
import org.deegree.coverage.raster.data.info.InterleaveType;
import org.deegree.coverage.raster.data.info.RasterDataInfo;
import org.deegree.coverage.raster.data.nio.PixelInterleavedRasterData;
import org.deegree.coverage.raster.geom.RasterRect;
import org.deegree.coverage.raster.utils.Rasters;
import org.junit.Test;

/**
 * The <code>RasterFactory</code> class TODO add class documentation here.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author: rbezema $
 * @version $Revision: 21789 $, $Date: 2010-01-06 22:26:14 +0600 (Срд, 06 Янв 2010) $
 * 
 */
public class RasterFactory {

    private final static float[] TEST_HEIGHTS = new float[] { -10.912f, -5.8f, 0.001f, 5.5f, 10.1f, 15.3f };

    private static final int TILE_SIZE = 500;

    /**
     * Test the creation of a float buffered image.
     * 
     * @throws IOException
     */
    @Test
    public void testFloatRaster()
                            throws IOException {
        int width = 20;
        int height = 20;
        RasterDataInfo rdi = new RasterDataInfo( new BandType[] { BandType.BAND_0 }, DataType.FLOAT,
                                                 InterleaveType.PIXEL );
        PixelInterleavedRasterData pird = new PixelInterleavedRasterData( new RasterRect( 0, 0, width, height ), width,
                                                                          height, rdi );

        for ( int y = 0; y < height; y++ ) {
            for ( int x = 0; x < width; x++ ) {
                pird.setFloatSample( x, y, 0, TEST_HEIGHTS[(int) ( Math.random() * TEST_HEIGHTS.length )] );
            }
        }

        BufferedImage img = org.deegree.coverage.raster.utils.RasterFactory.rasterDataToImage( pird );
        Assert.assertEquals( width, img.getWidth() );
        Assert.assertEquals( height, img.getHeight() );

        byte[] result = new byte[DataType.FLOAT.getSize()];
        ByteBuffer converter = ByteBuffer.wrap( result );
        for ( int by = 0; by < height; ++by ) {
            for ( int bx = 0; bx < width; ++bx ) {
                int val = img.getRGB( bx, by );
                converter.putInt( 0, val );
                float realVal = converter.getFloat( 0 );
                float compareVal = pird.getFloatSample( bx, by, 0 );
                Assert.assertEquals( compareVal, realVal );
            }
        }
        // ImageIO.write( img, "tif", new File( "/tmp/out.tif" ) );
    }

    /**
     * Test the creation of a float buffered image.
     * 
     * @throws IOException
     * @throws InterruptedException
     */
    // @Test
    public synchronized void testTiledImage()
                            throws IOException, InterruptedException {

        long t = System.currentTimeMillis();

        RenderedOp jpg = getJAIImage( "test_tiled_image.jpg" );
        RenderedOp tiff = getJAIImage( "test_tiled_image_lzw.tif" );
        RenderedOp tiffNone = getJAIImage( "test_tiled_image.tif" );

        System.out.println( "jpg:  " + jpg.getWidth() + ", " + jpg.getHeight() );
        System.out.println( "tiff:  " + tiff.getWidth() + ", " + tiff.getHeight() );
        System.out.println( "tiffNone:  " + tiffNone.getWidth() + ", " + tiffNone.getHeight() );

        // synchronized ( tiff ) {
        // tiff.wait( 10000 );
        // tiff.notifyAll();
        // }

        long ret = currentTimeMillis();
        saveSubset( jpg, "jpg", 0, 0, 250, 260 );
        System.out.println( "jpg subset 1: " + ( currentTimeMillis() - ret ) + "millis" );
        // jpg.dispose();
        //
        ret = currentTimeMillis();
        saveSubset( jpg, "jpg", 2098, 2000, 1050, 1008 );
        System.out.println( "jpg subset 2: " + ( currentTimeMillis() - ret ) + "millis" );
        jpg.dispose();

        gc();
        gc();
        gc();
        gc();
        gc();
        gc();
        synchronized ( tiff ) {
            System.out.println( "done getting jpg." );
            // tiff.wait( 50000 );
            tiff.notifyAll();
        }

        ret = currentTimeMillis();
        saveSubset( tiff, "jai_tif_1", 0, 0, 250, 260 );
        System.out.println( "compressed tiff subset 1: " + ( currentTimeMillis() - ret ) + "millis" );

        ret = currentTimeMillis();
        saveSubset( tiff, "jai_tif_2", 4998, 4900, 10500, 13080 );
        System.out.println( "compressed tiff subset 2: " + ( currentTimeMillis() - ret ) + "millis" );

        gc();
        gc();
        gc();
        gc();
        gc();
        gc();
        synchronized ( tiff ) {
            System.out.println( "done getting compressed." );
            tiff.wait( 5000 );
            tiff.notifyAll();
        }
        ret = currentTimeMillis();
        saveSubset( tiffNone, "jai_tif_none_1", 0, 0, 250, 260 );
        System.out.println( "tiff subset 1: " + ( currentTimeMillis() - ret ) + "millis" );
        ret = currentTimeMillis();
        saveSubset( tiffNone, "jai_tif_none_2", 4998, 4900, 10500, 13080 );
        System.out.println( "tiff subset 2: " + ( currentTimeMillis() - ret ) + "millis" );

        System.out.println( "jai total: " + ( currentTimeMillis() - t ) + "millis" );
        t = System.currentTimeMillis();
    }

    private void saveSubset( RenderedOp image, String name, int x, int y, int w, int h )
                            throws IOException {
        System.out.println( "getting subset: " + name );
        WritableRaster jpgRaster = image.getColorModel().createCompatibleWritableRaster( w, h ).createWritableTranslatedChild(
                                                                                                                               x,
                                                                                                                               y );
        image.copyExtendedData( jpgRaster, BorderExtender.createInstance( BorderExtender.BORDER_ZERO ) );
        // BufferedImage img = new BufferedImage( image.getColorModel(), jpgRaster.getWritableParent(), false, null );
        // ImageIO.write( img, "png", File.createTempFile( name, ".png" ) );
        System.out.println( "wrote subset: " + name );
    }

    private RenderedOp getJAIImage( String file )
                            throws IOException {

        // jpg: 15000, 15000
        // tiff: 20000, 20000
        // tiffNone: 20000, 20000
        // getting subset: jpg
        // wrote subset: jpg
        // jpg subset 1: 13005millis
        // getting subset: jpg
        // wrote subset: jpg
        // jpg subset 2: 13248millis
        // done getting jpg.
        // getting subset: jai_tif_1
        // wrote subset: jai_tif_1
        // compressed tiff subset 1: 249millis
        // getting subset: jai_tif_2
        // wrote subset: jai_tif_2
        // compressed tiff subset 2: 6115millis
        // done getting compressed.
        // getting subset: jai_tif_none_1
        // wrote subset: jai_tif_none_1
        // tiff subset 1: 94millis
        // getting subset: jai_tif_none_2
        // wrote subset: jai_tif_none_2
        // tiff subset 2: 4532millis
        // jai total: 44543millis

        File f = new File( RasterFactory.class.getResource( file ).getFile() );
        ImageInputStream iis = ImageIO.createImageInputStream( f );

        Iterator<ImageReader> iter = ImageIO.getImageReadersByFormatName( FileUtils.getFileExtension( f ) );
        ImageReader reader = null;
        if ( iter.hasNext() ) {
            // use the first.
            reader = iter.next();
            reader.setInput( iis );
        }

        RenderingHints hints = null;
        // if ( reader.isImageTiled( 0 ) ) {
        int width = reader.getWidth( 0 );
        int height = reader.getHeight( 0 );
        int numberOfTiles = Rasters.calcApproxTiles( width, height, TILE_SIZE );
        int tileSize = Rasters.calcTileSize( width, numberOfTiles );

        ImageLayout layout = new ImageLayout();
        layout.setTileWidth( TILE_SIZE );
        layout.setTileHeight( TILE_SIZE );
        hints = new RenderingHints( JAI.KEY_IMAGE_LAYOUT, layout );
        // }
        reader.dispose();
        iis.close();
        ParameterBlockJAI pbj = new ParameterBlockJAI( "ImageRead" );
        iis = ImageIO.createImageInputStream( f );
        pbj.setParameter( "Input", iis );

        RenderedOp result = JAI.create( "ImageRead", pbj, hints );

        return result;
    }

    private void enableTilingForReader( ImageReader imageReader, ImageInputStream iis ) {
        ParameterBlockJAI pbj = new ParameterBlockJAI( "ImageRead" );
        pbj.setParameter( "Input", iis );

        RenderedOp result = JAI.create( "ImageRead", pbj, null );

        int width = result.getWidth();
        int height = result.getHeight();
        int numberOfTiles = Rasters.calcApproxTiles( width, height, TILE_SIZE );
        int tileSize = Rasters.calcTileSize( width, numberOfTiles );

        ImageLayout layout = new ImageLayout();
        layout.setTileWidth( tileSize );
        layout.setTileHeight( tileSize );
        result.setRenderingHint( JAI.KEY_IMAGE_LAYOUT, layout );
    }

    private void outputJAIParams() {
        JAI jai = JAI.getDefaultInstance();
        OperationRegistry reg = jai.getOperationRegistry();
        RenderingHints renderingHints = jai.getRenderingHints();
        System.out.println( renderingHints.values() );

        String[] modeNames = RegistryMode.getModeNames();
        for ( String modeName : modeNames ) {
            System.out.println( "**** " + modeName + " ****" );
            String[] descriptorNames = reg.getDescriptorNames( modeName );
            for ( String dn : descriptorNames ) {
                System.out.println( "- " + dn );
                RegistryElementDescriptor descriptor = reg.getDescriptor( modeName, dn );
                String[] supportedModes = descriptor.getSupportedModes();
                for ( String sm : supportedModes ) {
                    System.out.println( " - " + sm );
                    ParameterListDescriptor parameterListDescriptor = descriptor.getParameterListDescriptor( sm );
                    if ( parameterListDescriptor != null ) {
                        String[] parameterNames = parameterListDescriptor.getParamNames();
                        if ( parameterNames != null ) {
                            for ( String parameterName : parameterNames ) {
                                System.out.println( "  - " + parameterName );
                            }
                        }
                    } else {
                        System.out.println( "  - no parameters" );
                    }
                }
            }
        }

    }
}
