package org.ict.clusterizer;

import junit.framework.TestCase;
import org.apache.commons.codec.binary.Base64InputStream;
import org.junit.Test;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;

/**
 * Test class for debugging of MeanSC algorythm
 *
 * User: Yorik
 * Date: 16.03.2010
 * Time: 11:22:15
 */
public class MeanSCClusterizerTest extends TestCase {

  @Test
  public void test_apply_model() throws Exception {
    // 2D model data (200 points)
    ArrayList<Point> points = new ArrayList<Point>(200);
    points.add(Point.create(new double[]{88.0, 89.0}, 1, 0, 0));
    points.add(Point.create(new double[]{195.0, 141.0}, 1, 0, 0));
    points.add(Point.create(new double[]{183.0, 78.0}, 1, 0, 0));
    points.add(Point.create(new double[]{177.0, 214.0}, 1, 0, 0));
    points.add(Point.create(new double[]{112.0, 110.0}, 1, 0, 0));
    points.add(Point.create(new double[]{220.0, 160.0}, 1, 0, 0));
    points.add(Point.create(new double[]{221.0, 141.0}, 1, 0, 0));
    points.add(Point.create(new double[]{216.0, 130.0}, 1, 0, 0));
    points.add(Point.create(new double[]{210.0, 151.0}, 1, 0, 0));
    points.add(Point.create(new double[]{185.0, 215.0}, 1, 0, 0));
    points.add(Point.create(new double[]{191.0, 90.0}, 1, 0, 0));
    points.add(Point.create(new double[]{207.0, 137.0}, 1, 0, 0));
    points.add(Point.create(new double[]{195.0, 209.0}, 1, 0, 0));
    points.add(Point.create(new double[]{179.0, 207.0}, 1, 0, 0));
    points.add(Point.create(new double[]{193.0, 93.0}, 1, 0, 0));
    points.add(Point.create(new double[]{139.0, 98.0}, 1, 0, 0));
    points.add(Point.create(new double[]{220.0, 159.0}, 1, 0, 0));
    points.add(Point.create(new double[]{186.0, 201.0}, 1, 0, 0));
    points.add(Point.create(new double[]{145.0, 76.0}, 1, 0, 0));
    points.add(Point.create(new double[]{207.0, 153.0}, 1, 0, 0));
    points.add(Point.create(new double[]{227.0, 141.0}, 1, 0, 0));
    points.add(Point.create(new double[]{227.0, 128.0}, 1, 0, 0));
    points.add(Point.create(new double[]{119.0, 96.0}, 1, 0, 0));
    points.add(Point.create(new double[]{173.0, 86.0}, 1, 0, 0));
    points.add(Point.create(new double[]{199.0, 132.0}, 1, 0, 0));
    points.add(Point.create(new double[]{214.0, 92.0}, 1, 0, 0));
    points.add(Point.create(new double[]{225.0, 158.0}, 1, 0, 0));
    points.add(Point.create(new double[]{214.0, 150.0}, 1, 0, 0));
    points.add(Point.create(new double[]{118.0, 91.0}, 1, 0, 0));
    points.add(Point.create(new double[]{174.0, 84.0}, 1, 0, 0));
    points.add(Point.create(new double[]{191.0, 109.0}, 1, 0, 0));
    points.add(Point.create(new double[]{217.0, 149.0}, 1, 0, 0));
    points.add(Point.create(new double[]{202.0, 143.0}, 1, 0, 0));
    points.add(Point.create(new double[]{171.0, 75.0}, 1, 0, 0));
    points.add(Point.create(new double[]{124.0, 72.0}, 1, 0, 0));
    points.add(Point.create(new double[]{179.0, 202.0}, 1, 0, 0));
    points.add(Point.create(new double[]{209.0, 190.0}, 1, 0, 0));
    points.add(Point.create(new double[]{177.0, 96.0}, 1, 0, 0));
    points.add(Point.create(new double[]{219.0, 159.0}, 1, 0, 0));
    points.add(Point.create(new double[]{167.0, 211.0}, 1, 0, 0));
    points.add(Point.create(new double[]{107.0, 89.0}, 1, 0, 0));
    points.add(Point.create(new double[]{108.0, 90.0}, 1, 0, 0));
    points.add(Point.create(new double[]{205.0, 195.0}, 1, 0, 0));
    points.add(Point.create(new double[]{150.0, 96.0}, 1, 0, 0));
    points.add(Point.create(new double[]{105.0, 130.0}, 1, 0, 0));
    points.add(Point.create(new double[]{190.0, 164.0}, 1, 0, 0));
    points.add(Point.create(new double[]{83.0, 115.0}, 1, 0, 0));
    points.add(Point.create(new double[]{190.0, 198.0}, 1, 0, 0));
    points.add(Point.create(new double[]{205.0, 119.0}, 1, 0, 0));
    points.add(Point.create(new double[]{215.0, 149.0}, 1, 0, 0));
    points.add(Point.create(new double[]{206.0, 198.0}, 1, 0, 0));
    points.add(Point.create(new double[]{157.0, 103.0}, 1, 0, 0));
    points.add(Point.create(new double[]{193.0, 102.0}, 1, 0, 0));
    points.add(Point.create(new double[]{169.0, 93.0}, 1, 0, 0));
    points.add(Point.create(new double[]{131.0, 96.0}, 1, 0, 0));
    points.add(Point.create(new double[]{212.0, 163.0}, 1, 0, 0));
    points.add(Point.create(new double[]{230.0, 168.0}, 1, 0, 0));
    points.add(Point.create(new double[]{199.0, 176.0}, 1, 0, 0));
    points.add(Point.create(new double[]{199.0, 105.0}, 1, 0, 0));
    points.add(Point.create(new double[]{141.0, 84.0}, 1, 0, 0));
    points.add(Point.create(new double[]{84.0, 116.0}, 1, 0, 0));
    points.add(Point.create(new double[]{211.0, 144.0}, 1, 0, 0));
    points.add(Point.create(new double[]{246.0, 175.0}, 1, 0, 0));
    points.add(Point.create(new double[]{134.0, 84.0}, 1, 0, 0));
    points.add(Point.create(new double[]{142.0, 82.0}, 1, 0, 0));
    points.add(Point.create(new double[]{150.0, 82.0}, 1, 0, 0));
    points.add(Point.create(new double[]{197.0, 176.0}, 1, 0, 0));
    points.add(Point.create(new double[]{230.0, 170.0}, 1, 0, 0));
    points.add(Point.create(new double[]{153.0, 86.0}, 1, 0, 0));
    points.add(Point.create(new double[]{182.0, 93.0}, 1, 0, 0));
    points.add(Point.create(new double[]{158.0, 90.0}, 1, 0, 0));
    points.add(Point.create(new double[]{142.0, 87.0}, 1, 0, 0));
    points.add(Point.create(new double[]{122.0, 69.0}, 1, 0, 0));
    points.add(Point.create(new double[]{157.0, 84.0}, 1, 0, 0));
    points.add(Point.create(new double[]{137.0, 89.0}, 1, 0, 0));
    points.add(Point.create(new double[]{93.0, 104.0}, 1, 0, 0));
    points.add(Point.create(new double[]{113.0, 106.0}, 1, 0, 0));
    points.add(Point.create(new double[]{204.0, 159.0}, 1, 0, 0));
    points.add(Point.create(new double[]{194.0, 185.0}, 1, 0, 0));
    points.add(Point.create(new double[]{176.0, 195.0}, 1, 0, 0));
    points.add(Point.create(new double[]{193.0, 108.0}, 1, 0, 0));
    points.add(Point.create(new double[]{194.0, 196.0}, 1, 0, 0));
    points.add(Point.create(new double[]{90.0, 129.0}, 1, 0, 0));
    points.add(Point.create(new double[]{85.0, 120.0}, 1, 0, 0));
    points.add(Point.create(new double[]{222.0, 122.0}, 1, 0, 0));
    points.add(Point.create(new double[]{183.0, 202.0}, 1, 0, 0));
    points.add(Point.create(new double[]{163.0, 98.0}, 1, 0, 0));
    points.add(Point.create(new double[]{127.0, 66.0}, 1, 0, 0));
    points.add(Point.create(new double[]{207.0, 170.0}, 1, 0, 0));
    points.add(Point.create(new double[]{102.0, 93.0}, 1, 0, 0));
    points.add(Point.create(new double[]{144.0, 84.0}, 1, 0, 0));
    points.add(Point.create(new double[]{211.0, 169.0}, 1, 0, 0));
    points.add(Point.create(new double[]{163.0, 84.0}, 1, 0, 0));
    points.add(Point.create(new double[]{202.0, 190.0}, 1, 0, 0));
    points.add(Point.create(new double[]{187.0, 189.0}, 1, 0, 0));
    points.add(Point.create(new double[]{97.0, 118.0}, 1, 0, 0));
    points.add(Point.create(new double[]{193.0, 103.0}, 1, 0, 0));
    points.add(Point.create(new double[]{139.0, 95.0}, 1, 0, 0));
    points.add(Point.create(new double[]{196.0, 189.0}, 1, 0, 0));
    points.add(Point.create(new double[]{35.0, 111.0}, 1, 0, 0));
    points.add(Point.create(new double[]{56.0, 153.0}, 1, 0, 0));
    points.add(Point.create(new double[]{72.0, 48.0}, 1, 0, 0));
    points.add(Point.create(new double[]{42.0, 137.0}, 1, 0, 0));
    points.add(Point.create(new double[]{30.0, 86.0}, 1, 0, 0));
    points.add(Point.create(new double[]{47.0, 70.0}, 1, 0, 0));
    points.add(Point.create(new double[]{32.0, 72.0}, 1, 0, 0));
    points.add(Point.create(new double[]{30.0, 125.0}, 1, 0, 0));
    points.add(Point.create(new double[]{86.0, 159.0}, 1, 0, 0));
    points.add(Point.create(new double[]{158.0, 138.0}, 1, 0, 0));
    points.add(Point.create(new double[]{146.0, 129.0}, 1, 0, 0));
    points.add(Point.create(new double[]{14.0, 56.0}, 1, 0, 0));
    points.add(Point.create(new double[]{94.0, 170.0}, 1, 0, 0));
    points.add(Point.create(new double[]{39.0, 141.0}, 1, 0, 0));
    points.add(Point.create(new double[]{118.0, 159.0}, 1, 0, 0));
    points.add(Point.create(new double[]{128.0, 172.0}, 1, 0, 0));
    points.add(Point.create(new double[]{89.0, 169.0}, 1, 0, 0));
    points.add(Point.create(new double[]{149.0, 122.0}, 1, 0, 0));
    points.add(Point.create(new double[]{131.0, 175.0}, 1, 0, 0));
    points.add(Point.create(new double[]{50.0, 152.0}, 1, 0, 0));
    points.add(Point.create(new double[]{158.0, 146.0}, 1, 0, 0));
    points.add(Point.create(new double[]{30.0, 145.0}, 1, 0, 0));
    points.add(Point.create(new double[]{88.0, 155.0}, 1, 0, 0));
    points.add(Point.create(new double[]{37.0, 125.0}, 1, 0, 0));
    points.add(Point.create(new double[]{149.0, 179.0}, 1, 0, 0));
    points.add(Point.create(new double[]{27.0, 99.0}, 1, 0, 0));
    points.add(Point.create(new double[]{44.0, 99.0}, 1, 0, 0));
    points.add(Point.create(new double[]{88.0, 174.0}, 1, 0, 0));
    points.add(Point.create(new double[]{132.0, 135.0}, 1, 0, 0));
    points.add(Point.create(new double[]{78.0, 31.0}, 1, 0, 0));
    points.add(Point.create(new double[]{32.0, 83.0}, 1, 0, 0));
    points.add(Point.create(new double[]{83.0, 159.0}, 1, 0, 0));
    points.add(Point.create(new double[]{59.0, 44.0}, 1, 0, 0));
    points.add(Point.create(new double[]{45.0, 149.0}, 1, 0, 0));
    points.add(Point.create(new double[]{41.0, 114.0}, 1, 0, 0));
    points.add(Point.create(new double[]{53.0, 125.0}, 1, 0, 0));
    points.add(Point.create(new double[]{31.0, 107.0}, 1, 0, 0));
    points.add(Point.create(new double[]{78.0, 167.0}, 1, 0, 0));
    points.add(Point.create(new double[]{128.0, 143.0}, 1, 0, 0));
    points.add(Point.create(new double[]{69.0, 144.0}, 1, 0, 0));
    points.add(Point.create(new double[]{135.0, 150.0}, 1, 0, 0));
    points.add(Point.create(new double[]{154.0, 142.0}, 1, 0, 0));
    points.add(Point.create(new double[]{131.0, 161.0}, 1, 0, 0));
    points.add(Point.create(new double[]{166.0, 149.0}, 1, 0, 0));
    points.add(Point.create(new double[]{145.0, 141.0}, 1, 0, 0));
    points.add(Point.create(new double[]{50.0, 140.0}, 1, 0, 0));
    points.add(Point.create(new double[]{37.0, 116.0}, 1, 0, 0));
    points.add(Point.create(new double[]{26.0, 107.0}, 1, 0, 0));
    points.add(Point.create(new double[]{53.0, 153.0}, 1, 0, 0));
    points.add(Point.create(new double[]{42.0, 121.0}, 1, 0, 0));
    points.add(Point.create(new double[]{33.0, 116.0}, 1, 0, 0));
    points.add(Point.create(new double[]{53.0, 100.0}, 1, 0, 0));
    points.add(Point.create(new double[]{142.0, 129.0}, 1, 0, 0));
    points.add(Point.create(new double[]{48.0, 44.0}, 1, 0, 0));
    points.add(Point.create(new double[]{35.0, 143.0}, 1, 0, 0));
    points.add(Point.create(new double[]{67.0, 160.0}, 1, 0, 0));
    points.add(Point.create(new double[]{76.0, 147.0}, 1, 0, 0));
    points.add(Point.create(new double[]{38.0, 162.0}, 1, 0, 0));
    points.add(Point.create(new double[]{75.0, 159.0}, 1, 0, 0));
    points.add(Point.create(new double[]{29.0, 73.0}, 1, 0, 0));
    points.add(Point.create(new double[]{80.0, 152.0}, 1, 0, 0));
    points.add(Point.create(new double[]{161.0, 139.0}, 1, 0, 0));
    points.add(Point.create(new double[]{67.0, 168.0}, 1, 0, 0));
    points.add(Point.create(new double[]{52.0, 49.0}, 1, 0, 0));
    points.add(Point.create(new double[]{99.0, 164.0}, 1, 0, 0));
    points.add(Point.create(new double[]{44.0, 110.0}, 1, 0, 0));
    points.add(Point.create(new double[]{89.0, 159.0}, 1, 0, 0));
    points.add(Point.create(new double[]{127.0, 162.0}, 1, 0, 0));
    points.add(Point.create(new double[]{109.0, 162.0}, 1, 0, 0));
    points.add(Point.create(new double[]{153.0, 143.0}, 1, 0, 0));
    points.add(Point.create(new double[]{65.0, 157.0}, 1, 0, 0));
    points.add(Point.create(new double[]{46.0, 160.0}, 1, 0, 0));
    points.add(Point.create(new double[]{37.0, 108.0}, 1, 0, 0));
    points.add(Point.create(new double[]{132.0, 150.0}, 1, 0, 0));
    points.add(Point.create(new double[]{58.0, 48.0}, 1, 0, 0));
    points.add(Point.create(new double[]{75.0, 155.0}, 1, 0, 0));
    points.add(Point.create(new double[]{82.0, 144.0}, 1, 0, 0));
    points.add(Point.create(new double[]{44.0, 58.0}, 1, 0, 0));
    points.add(Point.create(new double[]{160.0, 124.0}, 1, 0, 0));
    points.add(Point.create(new double[]{148.0, 156.0}, 1, 0, 0));
    points.add(Point.create(new double[]{115.0, 151.0}, 1, 0, 0));
    points.add(Point.create(new double[]{72.0, 137.0}, 1, 0, 0));
    points.add(Point.create(new double[]{113.0, 177.0}, 1, 0, 0));
    points.add(Point.create(new double[]{145.0, 124.0}, 1, 0, 0));
    points.add(Point.create(new double[]{30.0, 109.0}, 1, 0, 0));
    points.add(Point.create(new double[]{34.0, 69.0}, 1, 0, 0));
    points.add(Point.create(new double[]{156.0, 128.0}, 1, 0, 0));
    points.add(Point.create(new double[]{114.0, 148.0}, 1, 0, 0));
    points.add(Point.create(new double[]{49.0, 55.0}, 1, 0, 0));
    points.add(Point.create(new double[]{26.0, 118.0}, 1, 0, 0));
    points.add(Point.create(new double[]{39.0, 96.0}, 1, 0, 0));
    points.add(Point.create(new double[]{29.0, 128.0}, 1, 0, 0));
    points.add(Point.create(new double[]{73.0, 167.0}, 1, 0, 0));
    points.add(Point.create(new double[]{37.0, 77.0}, 1, 0, 0));
    points.add(Point.create(new double[]{53.0, 157.0}, 1, 0, 0));
    points.add(Point.create(new double[]{44.0, 125.0}, 1, 0, 0));
    points.add(Point.create(new double[]{34.0, 116.0}, 1, 0, 0));
    points.add(Point.create(new double[]{144.0, 170.0}, 1, 0, 0));
    points.add(Point.create(new double[]{43.0, 58.0}, 1, 0, 0));
    points.add(Point.create(new double[]{23.0, 70.0}, 1, 0, 0));
    points.add(Point.create(new double[]{72.0, 123.0}, 1, 0, 0));

    ClusteringTask task = new ClusteringTask(3);
    task.addParameter(new ClusteringParameter(MeanSCClusterizer.CELL_SIZE, 25.0d));
    task.addParameter(new ClusteringParameter(MeanSCClusterizer.N_MIN, 3.0d));
    task.addParameter(new ClusteringParameter(MeanSCClusterizer.THRESHOLD, 1.5d));

    MeanSCClusterizer meanSC = new MeanSCClusterizer();
    meanSC.apply(points, task);
  }

  @Test
  public void test_apply_image() throws Exception {
    BufferedImage raster = ImageIO.read(new FileInputStream(new File("H:/Work/WPS/ict.wps/request/academ_beach/academ_beach.png")));
    Raster data = raster.getData();

    ArrayList<Point> points = new ArrayList<Point>(data.getWidth() * data.getHeight());
    double [] pixel = new double[data.getNumBands()];

    for (int x = 0; x < data.getWidth(); x ++) {
      for (int y = 0; y < data.getHeight(); y ++) {
        double[] p = data.getPixel(data.getMinX() + x, data.getMinY() + y, pixel);
        points.add(Point.create(p, 1, x, y));
      }
    }

    ClusteringTask task = new ClusteringTask(3);
    task.addParameter(new ClusteringParameter(MeanSCClusterizer.CELL_SIZE, 25.0d));
    task.addParameter(new ClusteringParameter(MeanSCClusterizer.N_MIN, 3.0d));
    task.addParameter(new ClusteringParameter(MeanSCClusterizer.THRESHOLD, 1.5d));

    MeanSCClusterizer meanSC = new MeanSCClusterizer();
    meanSC.apply(points, task);
  }
}
