package org.ict.wps.process;

import org.deegree.coverage.raster.AbstractRaster;
import org.deegree.coverage.raster.data.RasterData;
import org.deegree.coverage.raster.geom.RasterGeoReference;
import org.deegree.coverage.raster.io.RasterIOOptions;
import org.deegree.coverage.raster.utils.RasterFactory;
import org.deegree.services.controller.OGCFrontController;
import org.deegree.services.wps.*;
import org.deegree.services.wps.input.ComplexInput;
import org.deegree.services.wps.output.ComplexOutput;

import org.ict.classifier.ClassificationTask;/*
import org.ict.classifier.Classifier;
import org.ict.classifier.Classifiers;
import org.ict.classifier.Point;*/
import org.ict.classifier.semisupervised.SSClassifier;
import org.ict.classifier.io.AsciiTaskReader;
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

public class SSClassifierProcesslet implements Processlet {

  private static final Logger LOG = LoggerFactory.getLogger( SSClassifierProcesslet.class );

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

    LOG.trace( "BEGIN SSClassifierProcesslet#execute(), context: " + OGCFrontController.getContext() );

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

    LOG.trace( "END SSClassifierProcesslet#execute()" );
  }

  public void destroy() {
  }

  public void init() {
  }


  /**
   *  Main method.
   *  Process logic should be implemented here
   *
   */
  private OutputBundle doProcess(InputBundle inputBundle, ProcessletExecutionInfo info) {
    //Classifiers.createTreeClassifier(inputBundle.task);

    LOG.trace("start pixel processing");
//    Raster data = inputBundle.raster.getData();

    SSClassifier classifier = new SSClassifier( inputBundle.task, inputBundle.image.getData() );
    classifier.build();
    classifier.classify();    
    BufferedImage im = classifier.toImage();
    return new OutputBundle(im);
  }



  private InputBundle parseInputs(ProcessletInputs inputs) throws ProcessletException {
    try {
      ClassificationTask task;
      { //  getting classification task
        ComplexInput trainingSample = (ComplexInput) inputs.getParameter(INPUT_TRAINING_SAMPLE);
        LOG.debug( "- trainingSample: " + trainingSample);
        LOG.debug( "- trainingSample.mimeType : " + trainingSample.getMimeType());
        LOG.debug( "- trainingSample.encoding : " + trainingSample.getEncoding());
        LOG.debug( "- trainingSample.schema   : " + trainingSample.getSchema());

        AsciiTaskReader taskReader = new AsciiTaskReader(new BufferedReader(
                new InputStreamReader(trainingSample.getValueAsBinaryStream())));

        task = taskReader.readTask();
      }

      BufferedImage raster;
      {
        ComplexInput inputRaster = (ComplexInput) inputs.getParameter(INPUT_RASTER);
        LOG.debug( "- inputRaster: " + inputRaster);
        LOG.debug( "- inputRaster.mimeType : " + inputRaster.getMimeType());
        LOG.debug( "- inputRaster.encoding : " + inputRaster.getEncoding());
        LOG.debug( "- inputRaster.schema   : " + inputRaster.getSchema());
        raster = ImageIO.read(inputRaster.getValueAsBinaryStream());
      }
      return new InputBundle(task, raster);
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
    final ClassificationTask task;
    final BufferedImage image;

    private InputBundle(ClassificationTask task, BufferedImage image) {
      assert task != null;
      assert image != null;
      this.task = task;
      this.image = image;
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