package org.ict.classifier;

import junit.framework.TestCase;
import org.ict.classifier.model.Model6Normals;
import org.ict.util.Timer;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * User: mityok
 * Date: 09.02.2010
 * Time: 1:08:39
 */
public class TreeClassifierTest extends TestCase {

  private static Logger log = LoggerFactory.getLogger(TreeClassifier.class);


  // Число повторений проверки для исключения влияния некорректных выборок
  private static final int repeats = 5;

  // Объем контрольной выборки в  точках на каждую размерность
  private static final int controlPointsPerDimension = 1000;

  // Задача из шести двух нормально распределенных классов, объединенных по парам с ошибкой 5%
  @Test
  public void test_treeClassify() throws Exception {

    List<Double> errors = new ArrayList<Double>(repeats);

    for (int r = 0; r < repeats; r++) {
      long start = System.nanoTime();

      Model6Normals model = new Model6Normals();
      ClassificationTask task = model.getTask();

      Timer timer = new Timer();
      timer.setState("building tree classifier");
      TreeClassifier treeClassifier = new TreeClassifier(task);
      timer.setState("classification");

      int errCount = 0;
      int [] byClassDistribution = new int[6];
      int res;
      int DIM = task.getDimension();
      for (int i = 0; i < DIM * controlPointsPerDimension; i++ ) {
        for (int k = 0; k < 6; k++) {
          res = treeClassifier.classify(model.generatePoint(k));
          if (res != k) errCount ++;
          byClassDistribution[res] ++;
        }
      }

      timer.stop();
      log.trace(timer.getReport());

      double e = (double) errCount / (6.0 * DIM * controlPointsPerDimension);
      log.trace(" results = " + Arrays.toString(byClassDistribution) +
                         " time = " + TimeUnit.MILLISECONDS.convert(System.nanoTime() - start, TimeUnit.NANOSECONDS) +
                         " error = " + ( 100.0 * e ) + "%" );
      errors.add(e);
    }

    double averageError = 0;
    for (Double e : errors) {
      averageError += e;
    }
    averageError /= errors.size();

    double errorLimit = Model6Normals.getAcceptableError();

    log.trace(" expected error " + (100.0 * Model6Normals.getExpectedError()) + "% ; acceptable error " + ( 100.0 * errorLimit ) + "% ; actual error = " + ( 100.0 * averageError ) + "%");
    assertTrue("Unacceptable classification error:  expected 5% ; acceptable " + ( 100.0 * errorLimit ) + "% ; actual " + ( 100.0 * averageError ) + "%",
                averageError < errorLimit);
  }

}