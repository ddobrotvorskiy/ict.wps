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

import org.deegree.coverage.raster.AbstractRaster;
import org.deegree.coverage.raster.geom.RasterGeoReference;
import org.deegree.coverage.raster.io.RasterIOOptions;
import org.deegree.coverage.raster.utils.RasterFactory;
import org.deegree.services.controller.OGCFrontController;
import org.deegree.services.wps.*;
import org.deegree.services.wps.input.ComplexInput;
import org.deegree.services.wps.output.ComplexOutput;
import org.ict.classifier.ClassificationTask;
import org.ict.classifier.Classifier;
import org.ict.classifier.Classifiers;
import org.ict.classifier.Point;
import org.ict.classifier.io.AsciiTaskReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.*;
import java.net.URL;

public class ParzenClassifierProcesslet implements Processlet {

  private static final Logger log = LoggerFactory.getLogger( ParzenClassifierProcesslet.class );

  /**
   *  Raster image to be classified
   */
  private static final String INPUT_RASTER = "InputRaster";
  /**
   *  Training sample in Ascii format
   */
  private static final String INPUT_TRAINING_SAMPLE = "TrainingSample";
  /**
   *  Classified image with fake colors
   */
  private static final String OUTPUT_RASTER = "OutputRaster";


  public void process( ProcessletInputs in, ProcessletOutputs out, ProcessletExecutionInfo info ) throws ProcessletException {

    log.trace( "BEGIN ParzenClassifierProcesslet#execute(), context: " + OGCFrontController.getContext() );

    info.setStartedMessage("Classification started");
    info.setPercentCompleted(0);

    log.trace("parsing inputs");
    InputBundle inputBundle = parseInputs(in);

    log.trace("performing process logic");
    OutputBundle outputBundle = null;
    try {
      outputBundle = doProcess(inputBundle, info);
    } catch (Exception e) {
      log.error("Error while processing request", e);
      throw new ProcessletException(e.getMessage());
    }

    log.trace("sending results");
    sendOutput(outputBundle, out);

    info.setPercentCompleted(100);
    info.setSucceededMessage("Classification succeeded");

    log.trace( "END ParzenClassifierProcesslet#execute()" );
  }

  public void destroy() {
    log.debug( "ParzenClassifierProcesslet#destroy() called" );
  }

  public void init() {
    log.debug( "ParzenClassifierProcesslet#init() called" );
  }


  /**
   *  Main method.
   *  Process logic should be implemented here
   *
   */
  private OutputBundle doProcess(InputBundle inputBundle, ProcessletExecutionInfo info) {

    log.trace("create tree classifier");

    //TODO Return tree classifier after debug
    //Classifier classifier = Classifiers.createTreeClassifier(inputBundle.task);
    Classifier classifier = Classifiers.createClassifier(inputBundle.task);


    log.trace("start pixel processing");
    Raster data = inputBundle.raster.getData();

    BufferedImage image = new BufferedImage(data.getWidth(), data.getHeight(), BufferedImage.TYPE_INT_RGB);

    double [] pixel = new double[data.getNumBands()];

    int totalPoints = data.getWidth() * data.getHeight();
    for (int x = 0; x < data.getWidth(); x ++) {

      info.setPercentCompleted(100 * x * data.getHeight() / totalPoints);

      for (int y = 0; y < data.getHeight(); y ++) {
        double[] p = data.getPixel(data.getMinX() + x, data.getMinY() + y, pixel);
        int classId = classifier.classify(Point.create(p));
        Color color = inputBundle.task.getClassById(classId).getColor();
        image.setRGB(x, y, color.getRGB());
      }
    }
    log.trace("finish pixel processing");

    return new OutputBundle(image);
  }



  private InputBundle parseInputs(ProcessletInputs inputs) throws ProcessletException {
    try {
      ClassificationTask task;
      { //  getting classification task
        ComplexInput trainingSample = (ComplexInput) inputs.getParameter(INPUT_TRAINING_SAMPLE);
        log.debug( "- trainingSample: " + trainingSample);
        log.debug( "- trainingSample.mimeType : " + trainingSample.getMimeType());
        log.debug( "- trainingSample.encoding : " + trainingSample.getEncoding());
        log.debug( "- trainingSample.schema   : " + trainingSample.getSchema());

        AsciiTaskReader taskReader = new AsciiTaskReader(new BufferedReader(
                new InputStreamReader(trainingSample.getValueAsBinaryStream())));

        task = taskReader.readTask();
      }

      BufferedImage raster;
      //AbstractRaster raster;
      { // getting input raster
        ComplexInput inputRaster = (ComplexInput) inputs.getParameter(INPUT_RASTER);
        log.debug( "- inputRaster: " + inputRaster);
        log.debug( "- inputRaster.mimeType : " + inputRaster.getMimeType());
        log.debug( "- inputRaster.encoding : " + inputRaster.getEncoding());
        log.debug( "- inputRaster.schema   : " + inputRaster.getSchema());

//        RasterIOOptions opts = new RasterIOOptions(RasterGeoReference.OriginLocation.OUTER);
//        opts.add(RasterIOOptions.READ_WLD_FILE, null);
//
//        MimeType mimeType = new MimeType(inputRaster.getMimeType());
//        opts.add(RasterIOOptions.OPT_FORMAT, mimeType.getSubType());

        raster = ImageIO.read(inputRaster.getValueAsBinaryStream());
        //raster = RasterFactory.loadRasterFromStream(inputRaster.getValueAsBinaryStream(), opts);
      }

      return new InputBundle(task, raster);

//    } catch (MimeTypeParseException e) {
//      log.error(e.getMessage(), e);
//      throw new ProcessletException(e.getMessage());
    } catch (IOException e) {
      log.error("Raster load failed with i/o exception", e);
      throw new ProcessletException("Raster load failed with i/o exception");
    }
  }

  private void sendOutput(OutputBundle outputBundle, ProcessletOutputs out) throws ProcessletException {
    try {
      ComplexOutput outputRaster = (ComplexOutput) out.getParameter(OUTPUT_RASTER);
      log.debug( "Setting output raster (requested=" + outputRaster.isRequested() + ")" );
      log.debug( "- outputRaster: " + outputRaster);
      log.debug( "- outputRaster.mimeType : " + outputRaster.getRequestedMimeType());
      log.debug( "- outputRaster.schema   : " + outputRaster.getRequestedSchema());

      // for debug
//      log.debug("storing result in file");
//      ImageIO.write(outputBundle.image,  "png", new File("/tmp/wps/result.png") );

      log.debug("sending result to output stream");
      ImageIO.write(outputBundle.image,  "png", outputRaster.getBinaryOutputStream() );

    } catch (IOException e) {
      log.error("Result raster transfer failed with i/o exception", e);
      throw new ProcessletException("Result raster transfer failed with i/o exception");
    }
  }


  /**
   *  Composite class for process inputs in usable format
   *
   */
  private static class InputBundle {
    final ClassificationTask task;
    final BufferedImage raster;

    private InputBundle(ClassificationTask task, BufferedImage raster) {
      assert task != null;
      assert raster != null;
      this.task = task;
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
        log.debug("raster == " + (raster == null ? "null" : raster));
      } catch (IOException e) {
        log.error("Raster load failed with i/o exception", e);
        throw new ProcessletException("Raster load failed with i/o exception");
      }

      try {
        RasterIOOptions options = new RasterIOOptions();
        options.add( RasterIOOptions.OPT_FORMAT, "png" );
        log.debug("storing result in file");
        RasterFactory.saveRasterToFile(raster.copy(), new File("/tmp/wps/result.png"), options);

        log.debug("sending result to output stream");
        RasterFactory.saveRasterToStream(raster.copy(), new FileOutputStream("/tmp/wps/result-stream.png"), options);
      } catch (IOException e) {
        log.error("Result raster transfer failed with i/o exception", e);
        throw new ProcessletException("Result raster transfer failed with i/o exception");
      }


    } catch (ProcessletException e) {
      log.error("Shit happened", e);
    }
  }
}