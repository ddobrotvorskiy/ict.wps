package org.ict.classifier.model;

import org.ict.classifier.ClassificationTask;
import org.ict.classifier.Clazz;
import org.ict.classifier.Point;
import org.ict.classifier.util.NormalDistribution;

import javax.swing.*;
import java.awt.*;

/**
 * User: mityok
 * Date: 21.02.2010
 * Time: 13:42:16
 */
public class Model2Normals {

// Значения расстояния Джефриса-Матусита для ошибок в 5% , 10% , 15%
  private static final double r5 = 10.89;
  private static final double r10 = 6.55;
  private static final double r15 = 4.33;

  // Допустимые значения ошибок для опеделения состоятельности классификатора.
  private static final double errorLimit5 = 0.055;
  private static final double errorLimit10 = 0.11;
  private static final double errorLimit15 = 0.16;

  // Объем обучающих выборок в точках на каждую размерность
  private static final int trainPointsPerDimension = 100;


  private static final int DIM = 3;


  private static final double SIGMA = 10.;


  private final ClassificationTask task;
  private final NormalDistribution n[] = new NormalDistribution[2];

  public Model2Normals() {
    double [] mu = new double[DIM];
    double [] sigma = new double[DIM];

    for (int i = 0; i < DIM; i ++) {
      mu[i] = 0;
      sigma[i] = SIGMA;
    }

    ClassificationTask.Builder tb = new ClassificationTask.Builder();
    Clazz.Builder cb0 = new Clazz.Builder(0);
    Clazz.Builder cb1 = new Clazz.Builder(1);

    // Два нормально распределенных класса с ошибкой классификации 5%
    mu[0] = 0.0;
    n[0] = new NormalDistribution(mu, sigma);
    mu[0] = Math.sqrt(r5) * SIGMA;
    n[1] = new NormalDistribution(mu, sigma);

    for (int i = 0; i < DIM * trainPointsPerDimension; i++ ) {
      cb0.addPoint(Point.create(n[0].next()));
      cb1.addPoint(Point.create(n[1].next()));
    }
    tb.addClass(cb0.createClass());
    tb.addClass(cb1.createClass());

    task = tb.createTask();
  }

  public ClassificationTask getTask() {
    return task;
  }

  public Point generatePoint(int classId) {
    if (classId < 0 || classId > 1)
      throw new IllegalArgumentException("No such class");

    return Point.create(n[classId].next());
  }

  public static double getAcceptableError() {
    return errorLimit5;
  }

  public static double getExpectedError() {
    return 0.05;
  }

  public static void main(String [] argv) {

    JFrame frame = new JFrame("Display image");
    Panel panel = new Panel() {
      public void paint(Graphics g) {

        Model2Normals model = new Model2Normals();

        for (int i = 0 ; i < 2000; i++) {
          for (int classId = 0 ; classId < 2 ; classId ++) {
            Point point = model.generatePoint(i);
            g.drawLine(250 + (int)Math.round(point.get(0)), 250 + (int)Math.round(point.get(1)),
                    250 + (int)Math.round(point.get(0)), 250 + (int)Math.round(point.get(1)));
          }
        }
      }
    };
    frame.getContentPane().add(panel);
    frame.setSize(500, 500);
    frame.setVisible(true);
  }
}


