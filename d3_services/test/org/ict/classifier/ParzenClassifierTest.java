package org.ict.classifier;

import Jama.Matrix;
import junit.framework.TestCase;
import org.ict.classifier.io.AsciiTaskReader;
import org.ict.classifier.util.NormalDistribution;
import org.junit.Test;

import javax.swing.*;
import java.awt.*;
import java.io.Reader;
import java.io.StringReader;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * User: mityok
 * Date: 09.02.2010
 * Time: 1:08:39
 */
public class ParzenClassifierTest extends TestCase {

  // Значения расстояния Джефриса-Матусита для ошибок в 5% , 10% , 15%
  private static final double r5 = 10.89;
  private static final double r10 = 6.55;
  private static final double r15 = 4.33;

  // Допустимые значения ошибок для опеделения состоятельности классификатора.
  private static final double errorLimit5 = 0.055;
  private static final double errorLimit10 = 0.11;
  private static final double errorLimit15 = 0.16;

  // Число повторений проверки для исключения влияния некорректных выборок
  private static final int repeats = 10;

  // Объем обучающих выборок в точках на каждую размерность
  private static final int trainPointsPerDimension = 100;

    // Объем контрольной выборки в  точках на каждую размерность
  private static final int controlPointsPerDimension = 1000;


  private static final int DIM = 3;

  private static final double SIGMA = 10.;

  // Задача из двух нормально распределенных классах
  @Test
  public void test_rawParzenClassify() throws Exception {

    double [] mu = new double[DIM];
    double [] sigma = new double[DIM];

    for (int i = 0; i < DIM; i ++) {
      mu[i] = 0;
      sigma[i] = SIGMA;
    }


    List<Double> errors = new ArrayList<Double>(repeats);

    for (int r = 0; r < repeats; r++) {
      long start = System.nanoTime();

      ClassificationTask.Builder tb = new ClassificationTask.Builder();
      Clazz.Builder cb0 = new Clazz.Builder(0);
      Clazz.Builder cb1 = new Clazz.Builder(1);

      // Два нормально распределенных класса с ошибкой классификации 5% 
      mu[0] = 0.0;
      NormalDistribution n0 = new NormalDistribution(mu, sigma);
      mu[0] = Math.sqrt(r5) * SIGMA;
      NormalDistribution n1 = new NormalDistribution(mu, sigma);

      for (int i = 0; i < DIM * trainPointsPerDimension; i++ ) {
        cb0.addPoint(Point.create(n0.next()));
        cb1.addPoint(Point.create(n1.next()));
      }
      tb.addClass(cb0.createClass());
      tb.addClass(cb1.createClass());

      //ParzenClassifier classifier = ParzenClassifier.createClassifier(tb.createTask());
      //ParzenClassifier classifier = ParzenClassifier.createClassifier(HyperCubeOptimizer.optimizeTask(tb.createTask()));
      Classifier classifier = Classifiers.createTreeClassifier(tb.createTask());

      int errCount = 0;
      int [] byClassDistribution = new int[] {0, 0};
      for (int i = 0; i < DIM * controlPointsPerDimension; i++ ) {
        int res = classifier.classify(Point.create(n0.next()));
        byClassDistribution[res] ++;
        if (0 != res) { errCount ++; }
        res = classifier.classify(Point.create(n1.next()));
        byClassDistribution[res] ++;
        if (1 != res) { errCount ++; }
      }
      double e = (double) errCount / (2.0 * DIM * controlPointsPerDimension);
      System.out.println(" results = " + Arrays.toString(byClassDistribution) +
                         " time = " + TimeUnit.MILLISECONDS.convert(System.nanoTime() - start, TimeUnit.NANOSECONDS) +
                         " error = " + ( 100.0 * e ) + "%" );
      errors.add(e);
    }

    double averageError = 0;
    for (Double e : errors) {
      averageError += e;
    }
    averageError /= errors.size();

    System.out.println(" expected error 5% ; acceptable error " + ( 100.0 * errorLimit5 ) + "% ; actual error = " + ( 100.0 * averageError ) + "%");
    assertTrue("Unacceptable classification error:  expected 5% ; acceptable " + ( 100.0 * errorLimit5 ) + "% ; actual " + ( 100.0 * averageError ) + "%",
                averageError < errorLimit5);
  }


  public static void main(String [] argv) {

    JFrame frame = new JFrame("Display image");
    Panel panel = new Panel() {
      public void paint(Graphics g) {

        double [] mu = new double[DIM];
        double [] sigma = new double[DIM];

        for (int i = 0; i < DIM; i ++) {
          mu[i] = 0;
          sigma[i] = SIGMA;
        }

        mu[0] = 0.0;
        NormalDistribution n0 = new NormalDistribution(mu, sigma);
        mu[0] = Math.sqrt(r5) * SIGMA;
        NormalDistribution n1 = new NormalDistribution(mu, sigma);

        for (int i = 0 ; i < 2000; i++) {
          double [] point = n0.next();
          g.drawLine(250 + (int)Math.round(point[0]), 250 + (int)Math.round(point[1]),
                     250 + (int)Math.round(point[0]), 250 + (int)Math.round(point[1]));
          point = n1.next();
          g.drawLine(250 + (int)Math.round(point[0]), 250 + (int)Math.round(point[1]),
                     250 + (int)Math.round(point[0]), 250 + (int)Math.round(point[1]));
        }
      }
    };
    frame.getContentPane().add(panel);
    frame.setSize(500, 500);
    frame.setVisible(true);
  }
}
