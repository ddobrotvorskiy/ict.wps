package org.ict.classifier.model;

import org.ict.classifier.ClassificationTask;
import org.ict.classifier.Clazz;
import org.ict.classifier.Point;
import org.ict.classifier.util.NormalDistribution;

/**
 * User: mityok
 * Date: 21.02.2010
 * Time: 13:40:08
 */
public class Model6Normals {

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



  private static final int DIM = 3; // >= 3

  private static final double SIGMA = 10.;


  private final ClassificationTask task;
  private final NormalDistribution[] n = new NormalDistribution[6];

  public Model6Normals() {
    double [] mu = new double[DIM];
    double [] sigma = new double[DIM];

    for (int i = 0; i < DIM; i ++) {
      mu[i] = 0;
      sigma[i] = SIGMA;
    }

    double deltaMu = Math.sqrt(r5) * SIGMA;

    for (int k = 0; k < 3 ; k++) {
      mu[k] = SIGMA * 20;
      n[2 * k] = new NormalDistribution(mu, sigma);
      mu[k] = SIGMA * 20 + deltaMu;
      n[2 * k + 1] = new NormalDistribution(mu, sigma);
      mu[k] = 0;
    }


    Clazz.Builder [] b = new Clazz.Builder [6];
    for (int k = 0; k < 6; k++) {
      b[k] = new Clazz.Builder(k);
    }

    for (int i = 0; i < DIM * trainPointsPerDimension; i++ ) {
      for (int k = 0; k < 6; k++) {
        b[k].addPoint(Point.create(n[k].next()));
      }
    }

    ClassificationTask.Builder tb = new ClassificationTask.Builder();
    for (int k = 0; k < 6; k++) {
      tb.addClass(b[k].createClass());
    }

    this.task = tb.createTask();
  }


  public ClassificationTask getTask() {
    return task;
  }

  public Point generatePoint(int classId) {
    if (classId < 0 || classId >= n.length)
      throw new IllegalArgumentException("No such class");

    return Point.create(n[classId].next());
  }

  public static double getAcceptableError() {
    return errorLimit5;
  }

  public static double getExpectedError() {
    return 0.05;
  }
}