package org.ict.classifier;

import Jama.EigenvalueDecomposition;
import Jama.Matrix;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * User: mityok
 * Date: 27.01.2010
 * Time: 1:27:34
 */
class LandgrebeFeatures {

  private LandgrebeFeatures() {
    throw new IllegalStateException("Not instantiable");
  }

  private static final double eigenvaluesRelationThreshold = 1000.0;

  public static Matrix extractFeatures(BayesClassifier classifier) {

    Map<Clazz, List<Point>> correctPoints = new HashMap<Clazz, List<Point>>();
    int commonWeight = 0;
    System.out.println("Finding correctly classified points");
    ClassificationTask task = classifier.getTask();

    for (Clazz c : task.getClasses()) {
      commonWeight += c.getWeight();
      List<Point> points = new LinkedList<Point>();
      for (Point p : c.getPoints()) {
        if (c.getId() == classifier.classify(p)) {
          points.add(p);
        }
      }
      if (points.isEmpty())
        throw new ClassifierException("Correctly classified points not found for class " + c.getId() + " " + c.getLegend());
      correctPoints.put(c, points);
    }

//
//    for (Iterator i = subClasses.iterator(); i.hasNext();) {
//      final AbstractClass subClass = (AbstractClass) i.next();
//      final List list  = (List) correctPoints.get(subClass);
//      System.out.println("Class " + subClass.getName() + "(id = " + subClass.getId()+ ") : " + list.size() +" / " + subClass.points.size());
//    }

//    System.out.println("Calculating EDBFM");
    int dim = task.getDimension();
//    System.out.println("dim = " + dim);
    Matrix EDBFM = new Matrix(dim, dim);
    double [][] A = EDBFM.getArray();

    for (Clazz c1 : task.getClasses()) {
      List<Point> points1 = correctPoints.get(c1);
      double q1 = c1.getWeight() / ((double) commonWeight);
      for (Clazz c2 : task.getClasses()) {
        if (c1 == c2)
          continue;

//        System.out.println("Processing class pair "+ c1.getLegend() + "(id = " + c1.getId()+ ")  &  " + c2.getLegend() + " (id = " + c2.getId()+ ")");
        List<Point> points2 = correctPoints.get(c2);
        double q2 = c2.getWeight() / ((double) commonWeight);

        for (Point x1 : points1) {
          Point x2 = x1.nearest(points2);

          //  п. 3 (найти точку границы)
          Point s = getBorderPoint(c1, x1, c2, x2, classifier);

          //  п. 4 (оценить вектор нормали)
          double[] n = getNormalVector(c1, c2, s, classifier);

          //  п. 5 (оценить матрицу признаков)
          for (int k = 0; k < dim; k++) {
            for (int l = 0; l < dim; l++) {
              A[k][l] += n[k] * n[l] * q1 * q2;
            }
          }
        }
      }
    }

    System.out.println("Finding eigenvector decomposition");

    // 6. упорядочиваем собственные векторы по собственным значениям
    EigenvalueDecomposition eig = EDBFM.eig();
    double [] eigenValues = eig.getRealEigenvalues();
    double [][] V = eig.getV().getArray();
    double [][] F = new double[dim] [dim];

//    Matrix features1 = new Matrix(V, dim, dim);
//    System.out.println("Features before sorting for class " + getClassInfo("", false));
//    features1.print(features1.getColumnDimension(), features1.getRowDimension());
//    System.out.println("EigenValues:");
//    for (int j = 0; j<dim; j++)
//      System.out.print("  " + eigenValues[j]);
//    System.out.println();


    double maxValue = -10;
    int newDim = 0;

    for (int i = 0; i < dim; i++) {
      int indmax = 0;
      for (int j = 0; j < dim; j++) {
        if (eigenValues[j] > eigenValues[indmax]) {
          indmax = j;
        }
      }
      if (maxValue < -1) { // == -10
        maxValue = eigenValues[indmax];
      }
      if (maxValue/eigenValues[indmax] < eigenvaluesRelationThreshold ) {
        for (int k = 0; k < dim; k++) {
          F[k][i] = V[k][indmax];
        }
        eigenValues[indmax] = Double.MIN_VALUE;
        newDim ++;
      } else
        break;
    }

    Matrix features = new Matrix(F, dim, newDim);
    //System.out.println("Features extracted in " + "???" + " ms " + newDim + " features needed,  threshold = " + eigenvaluesRelationThreshold);
    System.out.println("Features:");
    features.print(features.getColumnDimension(), features.getRowDimension());
    System.out.println("EigenValues:");
    for (int j = 0; j<dim; j++)
      System.out.print("  " + eigenValues[j]);
    System.out.println();


    return features;
  }

  private static Point getBorderPoint(Clazz c1, Point p1, Clazz c2, Point p2, BayesClassifier classifier) {
    double epsilon = classifier.getEpsilon();

    Point s = Point.median(p1, p2);

    while (Point.distance(p1, p2) > epsilon) {
      if (classifier.getProbability(s, c1) > classifier.getProbability(s, c2)) {
        p1 = s;
      } else {
        p2 = s;
      }
      s = Point.median(p1, p2);
    }

    return s;
  }

  // TODO rewrite this shit !!!
  private static double [] getNormalVector(Clazz c1, Clazz c2, Point s, BayesClassifier classifier) {
    double [] x = s.getArray();

    int dim = s.getDim();
    double eps = classifier.getEpsilon();

    double [] n = new double[dim];
    double df0 = decisionFunction(c1, c2, s, classifier);
    for (int i = 0; i < dim; i++) {
      x[i] += eps;
      n[i] = (decisionFunction(c1, c2, Point.create(x), classifier) - df0) / eps;
      x[i] -= eps;
    }

    return n;
  }

  private static double decisionFunction(Clazz c1, Clazz c2, Point p, BayesClassifier classifier) {
    return (-1.0) * Math.log(classifier.getProbability(p, c1) / classifier.getProbability(p, c2));
  }
}
