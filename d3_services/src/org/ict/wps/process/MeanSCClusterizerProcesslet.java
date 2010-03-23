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
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;

import org.apache.commons.codec.binary.Base64InputStream;
import org.apache.commons.codec.binary.Base64OutputStream;

/**
 * User: Yorik
 * Date: 22.02.2010
 * Time: 17:30:21
 */
public class MeanSCClusterizerProcesslet implements Processlet {

  /**
   *  Raster image to be classified
   */
  private static final String INPUT_RASTER = "InputRaster";
  /**
   *  Classified image with fake colors
   */
  private static final String OUTPUT_RASTER = "OutputRaster";

  private static final Logger LOG = LoggerFactory.getLogger( MeanSCClusterizerProcesslet.class );

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
    LOG.debug( "MeanSCClusterizerProcesslet#destroy() called" );
  }

  @Override
  public void init() {
    LOG.debug( "MeanSCClusterizerProcesslet#init() called" );
  }


  /**
   *  Main method.
   *  Process logic should be implemented here
   *
   */
  private OutputBundle doProcess(InputBundle inputBundle, ProcessletExecutionInfo info) {

    LOG.trace("create MeanSC clusterizer");
    LOG.trace(inputBundle.raster.getData().toString());
    Clusterizer MeanSC = Clusterizers.createMeanSCClusterizer(inputBundle.task);

    LOG.trace("prepare input image");
    Raster data = inputBundle.raster.getData();

    ArrayList<Point> points = new ArrayList<Point>(data.getWidth() * data.getHeight());
    double [] pixel = new double[data.getNumBands()];

    for (int x = 0; x < data.getWidth(); x ++) {
//      LOG.trace("image line " + x + " of " + data.getWidth() + "\n");
      for (int y = 0; y < data.getHeight(); y ++) {
        double[] p = data.getPixel(data.getMinX() + x, data.getMinY() + y, pixel);
        points.add(Point.create(p, 1, x, y));
      }
    }

    LOG.trace("start image processing");
    if(!MeanSC.canProcess(inputBundle.task))
      throw new IllegalArgumentException("unexpected parameters for MeanSC algorithm");

    LinkedList<Cluster> clusters = MeanSC.apply(points, inputBundle.task, LOG);

    LOG.trace(String.valueOf(clusters.size()) + " clusters found:");
    int i = 0;
    for (Cluster cl : clusters) {
      LOG.trace("\\ cluster " + String.valueOf(i) + " = " + (cl == null ? "null" : String.valueOf(cl.getWeight())));
      i++;
    }
    BufferedImage image = new BufferedImage(data.getWidth(), data.getHeight(), BufferedImage.TYPE_INT_RGB);

    for (Cluster cl : clusters) {
      Color color = Cluster.getDefaultColor(clusters.indexOf(cl));
      for (Point p : cl.getPoints())
        image.setRGB(p.getXPos(), p.getYPos(), color.getRGB());
    }

    LOG.trace("finish image processing");

    return new OutputBundle(inputBundle.raster);
  }

  private InputBundle parseInputs(ProcessletInputs inputs) throws ProcessletException {
    try {
      ClusteringTask task = new ClusteringTask(3);
      { //  getting clusterization task
        LiteralInput cellSize = (LiteralInput) inputs.getParameter(MeanSCClusterizer.CELL_SIZE);
        LOG.debug("Input : " + MeanSCClusterizer.CELL_SIZE + " = " + cellSize.getValue());
        task.addParameter(new ClusteringParameter(MeanSCClusterizer.CELL_SIZE, Double.parseDouble(cellSize.getValue())));

        LiteralInput nMin = (LiteralInput) inputs.getParameter(MeanSCClusterizer.N_MIN);
        LOG.debug("Input : " + MeanSCClusterizer.N_MIN + " = " + nMin.getValue());
        task.addParameter(new ClusteringParameter(MeanSCClusterizer.N_MIN, Double.parseDouble(nMin.getValue())));

        LiteralInput T = (LiteralInput) inputs.getParameter(MeanSCClusterizer.THRESHOLD);
        LOG.debug("Input : " + MeanSCClusterizer.THRESHOLD + " = " + T.getValue());
        task.addParameter(new ClusteringParameter(MeanSCClusterizer.THRESHOLD, Double.parseDouble(T.getValue())));
      }

      BufferedImage raster;
      // getting input raster
      ComplexInput inputRaster = (ComplexInput) inputs.getParameter(INPUT_RASTER);
      LOG.debug( "- inputRaster.mimeType : " + inputRaster.getMimeType());
      LOG.debug( "- inputRaster.encoding : " + inputRaster.getEncoding());
      LOG.debug( "- inputRaster.schema   : " + inputRaster.getSchema());

//      RasterIOOptions opts = new RasterIOOptions(RasterGeoReference.OriginLocation.OUTER);
//      opts.add(RasterIOOptions.READ_WLD_FILE, null);

//      MimeType mimeType = new MimeType(inputRaster.getMimeType());
//      opts.add(RasterIOOptions.OPT_FORMAT, mimeType.getSubType());

      InputStream in;
      if ("base64".equals(inputRaster.getEncoding())) {
        in = new Base64InputStream(inputRaster.getValueAsBinaryStream());
      } else {
        in = inputRaster.getValueAsBinaryStream();
      }

      raster = ImageIO.read(in);
      
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
      ImageIO.write(outputBundle.image,  "png", new Base64OutputStream(outputRaster.getBinaryOutputStream()) );

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
