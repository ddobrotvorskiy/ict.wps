package org.ict.classifier.util;

import javax.swing.*;
import java.awt.*;

/**
 * User: mityok
 * Date: 09.02.2010
 * Time: 20:44:37
 */
public class NormalDistributionTest extends Panel {

  public void paint(Graphics g) {

    NormalDistribution nd = new NormalDistribution(new double[] {250, 250}, new double[] {50, 50});

    for (int i = 0 ; i < 2000; i++) {
      double [] point = nd.next();
      g.drawLine((int)Math.round(point[0]), (int)Math.round(point[1]), (int)Math.round(point[0]), (int)Math.round(point[1]));
    }
  }

  public static void main(String [] argv) {

    JFrame frame = new JFrame("Display image");
    Panel panel = new NormalDistributionTest();
    frame.getContentPane().add(panel);
    frame.setSize(500, 500);
    frame.setVisible(true);
  }
}
