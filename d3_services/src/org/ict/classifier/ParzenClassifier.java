package org.ict.classifier;

import Jama.Matrix;

import java.util.Random;

/**
 * NOT Thread safe
 *
 *
 * User: mityok
 * Date: 24.01.2010
 * Time: 21:01:14
 */
class ParzenClassifier extends BayesClassifier {

  private static final double DEFAULT_H = 10.0;

  private final double h;

  public static ParzenClassifier createClassifier(ClassificationTask task) {
    return createClassifier(task, 10);
  }
  
  public static ParzenClassifier createClassifier(ClassificationTask task, int attempts) {

    double optimalH, h;
    h = optimalH = DEFAULT_H;
    int minErr = -1 ;
    
    Random r = new Random();
    for ( int i = 0 ; i < attempts ; i++ ) {
      h = Math.max(h + 10. * r.nextDouble() - 5., 5.);

      int errorsCount = new ParzenClassifier(task, h).getSlidingExamError();

      if (minErr == -1 ||
        errorsCount < minErr ||
        (errorsCount == minErr && optimalH < h)) {

        minErr = errorsCount;
        optimalH = h;
      } else {
        h = optimalH;
      }
    }

    return new ParzenClassifier(task, optimalH);
  }
  
  private ParzenClassifier(ClassificationTask task, double h) {
    super(task);
    this.h = h;
  }

  @Override
  public double getProbability(Point point, Clazz clazz) {
    return getParzenDensity(point, clazz);
  }

  @Override
  public double getEpsilon() {
    return h / 50.0;
  }

  @Override
  public Classifier recountInNewFeatures(Matrix features) {
    return createClassifier(task.recountInNewFeatures(features));
  }

  private static double parzenCore(double x) {
    return Math.exp( -0.5 * ( x * x ))/( Math.sqrt( 2.0 * Math.PI ));
  }

  private double getParzenDensity(Point p, Clazz c) {
    double counter = 0.0d;
    boolean pIsOwn = false;
    final int dim = p.getDim();

    for (Point ownPoint : c.getPoints()) {

      // Attention! Point to be classified is not participate in density sum.
      // (critical for sliding exam correctness)

      if (p.equals(ownPoint)) {
        pIsOwn = true;
        continue;
      }
      double s = 1.0;

      for (int j = 0; j < dim; j++)
        s = s * parzenCore((p.get(j) - ownPoint.get(j))/h);
      counter = counter + s * ownPoint.getWeight();
    }
    counter /= ( (double) (pIsOwn ? c.getWeight() - p.getWeight() : c.getWeight()) * Math.pow(h, dim));
    return counter;
  }



}
