//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/base/trunk/resources/eclipse/files_template.xml $
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

package org.ict.wps.process;

import org.apache.commons.codec.binary.Base64InputStream;
import org.deegree.coverage.raster.AbstractRaster;
import org.deegree.coverage.raster.data.RasterData;
import org.deegree.coverage.raster.geom.RasterGeoReference;
import org.deegree.coverage.raster.io.RasterIOOptions;
import org.deegree.coverage.raster.utils.RasterFactory;
import org.deegree.services.controller.OGCFrontController;
import org.deegree.services.wps.*;
import org.deegree.services.wps.input.ComplexInput;
import org.deegree.services.wps.output.ComplexOutput;
/*
import org.ict.classifier.ClassificationTask;
import org.ict.classifier.Classifier;
import org.ict.classifier.Classifiers;
import org.ict.classifier.Point;
import org.ict.classifier.io.AsciiTaskReader;
*/
import org.ict.classifier.cca.CCAClusterizator;
import org.ict.classifier.cca.DataPoint;
import org.ict.classifier.cca.Data;
import org.ict.classifier.cca.DataPointReal;
import org.ict.classifier.cca.Parameters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.*;
import java.net.URL;

public class CCAProcesslet implements Processlet {

  private static final Logger LOG = LoggerFactory.getLogger( CCAProcesslet.class );

  /**
   *  Raster image to be classified
   */
  private static final String INPUT_RASTER = "InputRaster";
  /**
   *  Classified image with fake colors
   */
  private static final String OUTPUT_RASTER = "OutputRaster";



  public void process( ProcessletInputs in, ProcessletOutputs out, ProcessletExecutionInfo info ) throws ProcessletException {

    LOG.trace( "BEGIN CCAProcesslet#execute(), context: " + OGCFrontController.getContext() );

    info.setStartedMessage("Classification started");
    info.setPercentCompleted(0);

    LOG.trace("parsing inputs");
    InputBundle inputBundle = parseInputs(in);

    LOG.trace("performing process logic");
    OutputBundle outputBundle = doProcess(inputBundle, info);

    LOG.trace("sending results");
    sendOutput(outputBundle, out);

    info.setPercentCompleted(100);
    info.setSucceededMessage("Classification succeeded");

    LOG.trace( "END CCAProcesslet#execute()" );
  }

  public void destroy() {
    LOG.debug( "CCAProcesslet#destroy() called" );
  }

  public void init() {
    LOG.debug( "CCAProcesslet#init() called" );
  }


  /**
   *  Main method.
   *  Process logic should be implemented here
   *
   */
  private OutputBundle doProcess(InputBundle inputBundle, ProcessletExecutionInfo info) {

    LOG.trace("start pixel processing");
    Raster dat = inputBundle.raster.getData();

    BufferedImage image = new BufferedImage(dat.getWidth(), dat.getHeight(), BufferedImage.TYPE_INT_RGB);

    double [] pixel = new double[dat.getNumBands()];

    int totalPoints = dat.getWidth() * dat.getHeight();    
    
    Data date = new Data(totalPoints , dat.getNumBands());
    int j=0;
    for (int x = 0; x < dat.getWidth(); x ++) {
      for (int y = 0; y < dat.getHeight(); y ++) {    	  
        double[] p = dat.getPixel(dat.getMinX() + x, dat.getMinY() + y, pixel);    
		DataPoint point = new DataPoint(dat.getNumBands());		
		for(int i=0; i<dat.getNumBands(); i++)
			point.coordinates[i] = (int)p[i];		
		date.pointsArray[j] = point;		
		j++;        
      }
    }
    
	Parameters param = new Parameters();
    param.cellsize = 32;                  
    param.backgroundpercent = 0.1;      
    param.densityThreshold = 0.9;        
    param.emptyThreshold = 0;         
    param.forCCA = true;
    
	CCAClusterizator cca = new CCAClusterizator();
	cca.clustering(date, param);
	
	int[] clFon = new int[dat.getNumBands()];
	for (j=0; j<clFon.length; j++)
		clFon[j]=0;
	int[][] cvet = new int[cca.kolCluster][dat.getNumBands()];
	for (int i=0; i<cvet.length; i++)
		for (j=0; j<cvet[i].length; j++)
			cvet[i][j]=0;
	int nPixel;
	j=0;
	for(int x=0; x<dat.getWidth(); x++){
		for(int y=0; y<dat.getHeight(); y++){
			if (date.pointsArray[j].classNumber < 0)
				image.setRGB(x, y, 0);
			else
			{
				int nClass = date.pointsArray[j].classNumber;
				if ( Ravno(cvet[nClass], clFon) )
				{
					double[] p = dat.getPixel(dat.getMinX() + x, dat.getMinY() + y, pixel);
					for(int i=0; i<dat.getNumBands(); i++)
						cvet[nClass][i] = (int)p[i];	
					
					nPixel = (cvet[nClass][0] << 16) | (cvet[nClass][1] << 8) | cvet[nClass][2];
					image.setRGB(x, y, nPixel);
				}
				else
				{
					nPixel = (cvet[nClass][0] << 16) | (cvet[nClass][1] << 8) | cvet[nClass][2];
					image.setRGB(x, y, nPixel);
				}
			}				
			j++;
		}
	}	

    LOG.trace("finish pixel processing");

    return new OutputBundle(image);
  }
  
  
  private static boolean Ravno(int[] cvet, int[] clFon)
  {
	  for (int i=0; i<clFon.length; i++)
		  if (clFon[i] != cvet[i]) return false;
	  return true;
  }



  private InputBundle parseInputs(ProcessletInputs inputs) throws ProcessletException {
    try {

      BufferedImage raster;
      //AbstractRaster raster;
      { // getting input raster
        ComplexInput inputRaster = (ComplexInput) inputs.getParameter(INPUT_RASTER);
        LOG.debug( "- inputRaster: " + inputRaster);
        LOG.debug( "- inputRaster.mimeType : " + inputRaster.getMimeType());
        LOG.debug( "- inputRaster.encoding : " + inputRaster.getEncoding());
        LOG.debug( "- inputRaster.schema   : " + inputRaster.getSchema());

//        RasterIOOptions opts = new RasterIOOptions(RasterGeoReference.OriginLocation.OUTER);
//        opts.add(RasterIOOptions.READ_WLD_FILE, null);
//
//        MimeType mimeType = new MimeType(inputRaster.getMimeType());
//        opts.add(RasterIOOptions.OPT_FORMAT, mimeType.getSubType());

        InputStream in;
        if ("base64".equals(inputRaster.getEncoding())) {
          in = new Base64InputStream(inputRaster.getValueAsBinaryStream());
        } else {
          in = inputRaster.getValueAsBinaryStream();
        }

        raster = ImageIO.read(in);
//        raster = ImageIO.read(inputRaster.getValueAsBinaryStream());
        //raster = RasterFactory.loadRasterFromStream(inputRaster.getValueAsBinaryStream(), opts);
      }

      return new InputBundle(raster);

//    } catch (MimeTypeParseException e) {
//      LOG.error(e.getMessage(), e);
//      throw new ProcessletException(e.getMessage());
    } catch (IOException e) {
      LOG.error("Raster load failed with i/o exception", e);
      throw new ProcessletException("Raster load failed with i/o exception");
    }
  }

  private void sendOutput(OutputBundle outputBundle, ProcessletOutputs out) throws ProcessletException {
    try {
      ComplexOutput outputRaster = (ComplexOutput) out.getParameter(OUTPUT_RASTER);
      LOG.debug( "Setting output raster (requested=" + outputRaster.isRequested() + ")" );
      LOG.debug( "- outputRaster: " + outputRaster);
      LOG.debug( "- outputRaster.mimeType : " + outputRaster.getRequestedMimeType());
      LOG.debug( "- outputRaster.schema   : " + outputRaster.getRequestedSchema());

      // for debug
//      LOG.debug("storing result in file");
//      ImageIO.write(outputBundle.image,  "png", new File("/tmp/wps/result.png") );

      LOG.debug("sending result to output stream");
      ImageIO.write(outputBundle.image,  "png", outputRaster.getBinaryOutputStream() );

    } catch (IOException e) {
      LOG.error("Result raster transfer failed with i/o exception", e);
      throw new ProcessletException("Result raster transfer failed with i/o exception");
    }
  }


  /**
   *  Composite class for process inputs in usable format
   *
   */
  private static class InputBundle {
    final BufferedImage raster;

    private InputBundle(BufferedImage raster) {
      assert raster != null;
      this.raster = raster;
    }
  }

  /**
   *  Composite class for process outputs in usable format
   *
   */
  private static class OutputBundle {
    final BufferedImage image;

    private OutputBundle(BufferedImage image) {
      assert image != null;
      this.image = image;
    }
  }

  public static void main(String[] args) {


    try {
      AbstractRaster raster = null;
      try {
        URL url = new URL("file:///tmp/wps/codase-small.jpg");

        RasterIOOptions opts = new RasterIOOptions(RasterGeoReference.OriginLocation.OUTER);
        opts.add(RasterIOOptions.READ_WLD_FILE, null);
        opts.add(RasterIOOptions.OPT_FORMAT, "jpg" );
        raster = RasterFactory.loadRasterFromStream(new BufferedInputStream(url.openStream()), opts);
        LOG.debug("raster == " + (raster == null ? "null" : raster));
      } catch (IOException e) {
        LOG.error("Raster load failed with i/o exception", e);
        throw new ProcessletException("Raster load failed with i/o exception");
      }

      try {
        RasterIOOptions options = new RasterIOOptions();
        options.add( RasterIOOptions.OPT_FORMAT, "png" );
        LOG.debug("storing result in file");
        RasterFactory.saveRasterToFile(raster.copy(), new File("/tmp/wps/result.png"), options);

        LOG.debug("sending result to output stream");
        RasterFactory.saveRasterToStream(raster.copy(), new FileOutputStream("/tmp/wps/result-stream.png"), options);
      } catch (IOException e) {
        LOG.error("Result raster transfer failed with i/o exception", e);
        throw new ProcessletException("Result raster transfer failed with i/o exception");
      }


    } catch (ProcessletException e) {
      LOG.error("Shit happened", e);
    }
  }
}