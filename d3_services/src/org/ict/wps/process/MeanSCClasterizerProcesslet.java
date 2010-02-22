package org.ict.wps.process;

import org.deegree.services.controller.OGCFrontController;
import org.deegree.services.wps.*;
import org.deegree.services.wps.input.ComplexInput;
import org.deegree.services.wps.input.LiteralInput;
import org.deegree.services.wps.output.ComplexOutput;
import org.ict.clusterizer.*;
import org.ict.clusterizer.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * User: Yorik
 * Date: 22.02.2010
 * Time: 17:30:21
 */
public class MeanSCClasterizerProcesslet implements Processlet {

  /**
   *  Raster image to be classified
   */
  private static final String INPUT_RASTER = "InputRaster";
  /**
   *  Classified image with fake colors
   */
  private static final String OUTPUT_RASTER = "OutputRaster";

  private static final Logger LOG = LoggerFactory.getLogger( ParzenClassifierProcesslet.class );

  @Override
  public void process( ProcessletInputs in, ProcessletOutputs out, ProcessletExecutionInfo info ) throws ProcessletException {

    LOG.trace( "BEGIN MeanSCClassterizerProcesslet#execute(), context: " + OGCFrontController.getContext() );

    info.setStartedMessage("Clasterization started");
    info.setPercentCompleted(0);

    LOG.trace("parsing inputs");
    InputBundle inputBundle = parseInputs(in);

    LOG.trace("performing process logic");
    OutputBundle outputBundle = doProcess(inputBundle, info);

    LOG.trace("sending results");
    sendOutput(outputBundle, out);

    info.setPercentCompleted(100);
    info.setSucceededMessage("Classterization succeeded");

    LOG.trace( "END MeanSCClassterizerProcesslet#execute()" );
  }

  @Override
  public void destroy() {
    LOG.debug( "MeanSCClasterizerProcesslet#destroy() called" );
  }

  @Override
  public void init() {
    LOG.debug( "MeanSCClasterizerProcesslet#init() called" );
  }


  /**
   *  Main method.
   *  Process logic should be implemented here
   *
   */
  private OutputBundle doProcess(InputBundle inputBundle, ProcessletExecutionInfo info) {

    LOG.trace("create tree classifier");
    Clusterizer MeanSC = Clusterizers.createMeanSCClusterizer(inputBundle.task);

    LOG.trace("prepare input image");
    Raster data = inputBundle.raster.getData();

    ArrayList<Point> points = new ArrayList<Point>(data.getWidth() * data.getHeight());
    double [] pixel = new double[data.getNumBands()];

    for (int x = 0; x < data.getWidth(); x ++) {
      for (int y = 0; y < data.getHeight(); y ++) {
        double[] p = data.getPixel(data.getMinX() + x, data.getMinY() + y, pixel);
        points.add(Point.create(p, 1, x, y));
      }
    }

    LOG.trace("start image processing");
    if(!MeanSC.canProcess(inputBundle.task))
      throw new IllegalArgumentException("unexpected parameters for MeanSC algorithm");

    ArrayList<Cluster> clusters = MeanSC.apply(points, inputBundle.task);

    BufferedImage image = new BufferedImage(data.getWidth(), data.getHeight(), BufferedImage.TYPE_INT_RGB);

    for (Cluster cl : clusters) {
      Color color = Cluster.getDefaultColor(cl.getId());
      for (Point p : cl.getPoints())
        image.setRGB(p.getXPos(), p.getYPos(), color.getRGB());
    }

    LOG.trace("finish image processing");

    return new OutputBundle(image);
  }

  private InputBundle parseInputs(ProcessletInputs inputs) throws ProcessletException {
    try {
      ClusteringTask task = new ClusteringTask(3);
      { //  getting clusterization task
        LiteralInput cellSize = (LiteralInput) inputs.getParameter(MeanSCClusterizer.CELL_SIZE);
        LOG.debug("Input : " + MeanSCClusterizer.CELL_SIZE + " = " + cellSize.getValue());
        task.addParameter(new ClusteringParameter(MeanSCClusterizer.CELL_SIZE, Double.valueOf(cellSize.getValue())));

        LiteralInput nMin = (LiteralInput) inputs.getParameter(MeanSCClusterizer.N_MIN);
        LOG.debug("Input : " + MeanSCClusterizer.N_MIN + " = " + nMin.getValue());
        task.addParameter(new ClusteringParameter(MeanSCClusterizer.N_MIN, Double.valueOf(nMin.getValue())));

        LiteralInput T = (LiteralInput) inputs.getParameter(MeanSCClusterizer.THRESHOLD);
        LOG.debug("Input : " + MeanSCClusterizer.THRESHOLD + " = " + T.getValue());
        task.addParameter(new ClusteringParameter(MeanSCClusterizer.THRESHOLD, Double.valueOf(T.getValue())));
      }

      BufferedImage raster;
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

        raster = ImageIO.read(inputRaster.getValueAsBinaryStream());
        //raster = RasterFactory.loadRasterFromStream(inputRaster.getValueAsBinaryStream(), opts);
      }

      return new InputBundle(task, raster);

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
    final ClusteringTask task;
    final BufferedImage raster;

    private InputBundle(ClusteringTask task, BufferedImage raster) {
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
}
