package org.ict.classifier;

import junit.framework.TestCase;
import org.ict.classifier.model.FixedModel2Normals;
import org.ict.classifier.model.Model;
import org.ict.classifier.model.Model2Normals;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * User: mityok
 * Date: 09.02.2010
 * Time: 1:08:39
 */
public class ParzenClassifierTest extends TestCase {

  private static Logger log = LoggerFactory.getLogger(ParzenClassifierTest.class);

  // Число повторений проверки для исключения влияния некорректных выборок
  private static final int repeats = 10;

    // Объем контрольной выборки в  точках на каждую размерность
  private static final int controlPointsPerDimension = 1000;


  // Задача из двух нормально распределенных классах
  @Test
  public void test_rawParzenClassify() throws Exception {
    List<Double> errors = new ArrayList<Double>(repeats);

    for (int r = 0; r < repeats; r++) {
      long start = System.nanoTime();

      Model2Normals model = new Model2Normals();
      ClassificationTask task = model.getTask();

      //ParzenClassifier classifier = ParzenClassifier.createClassifier(tb.createTask());
      //ParzenClassifier classifier = ParzenClassifier.createClassifier(HyperCubeOptimizer.optimizeTask(tb.createTask()));
      Classifier classifier = Classifiers.createClassifier(task);

      int errCount = 0;
      int [] byClassDistribution = new int[task.getClasses().size()];
      int res;
      for (int i = 0; i < model.getTask().getDimension() * controlPointsPerDimension; i++ ) {
        for (int classId = 0 ; classId < task.getClasses().size() ; classId ++) {
          res = classifier.classify(model.generatePoint(classId));
          byClassDistribution[res] ++;
          if (classId != res) { errCount ++; }
        }
      }
      double e = (double) errCount / (1.0 * task.getDimension() * task.getClasses().size() * controlPointsPerDimension);
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

    double errorLimit = Model2Normals.getAcceptableError();

    log.trace(" expected error " + (100.0 * Model2Normals.getExpectedError()) + "% ; acceptable error " + ( 100.0 * errorLimit ) + "% ; actual error = " + ( 100.0 * averageError ) + "%");
    assertTrue("Unacceptable classification error:  expected 5% ; acceptable " + ( 100.0 * errorLimit ) + "% ; actual " + ( 100.0 * averageError ) + "%",
                averageError < errorLimit);
  }

// Задача из двух нормально распределенных классах
  @Test
  public void test_rawParzenClassify2() throws Exception {

    Model model = FixedModel2Normals.newInstance();

    ClassificationTask task = model.getTask();
    Classifier classifier = Classifiers.createClassifier(task);

    {
      StringBuilder sb = new StringBuilder();

      for (Clazz c : classifier.getTask().getClasses()) {
        sb.append(c.getId()).append(" : ")
          .append(c.getLegend())
          .append(" (").append(c.getPoints().size()).append(")");

        sb.append("; ");
      }
      sb.append(" se = ").append(classifier.getSlidingExamError());

      sb.append("\n");
      log.trace(sb.toString());
    }

    long start = System.nanoTime();

    int errCount = 0;
    Map<Integer, Long> byClassDistribution = new HashMap<Integer, Long>(task.getClasses().size());
    int res;
    int pointsCount = 0;
    for (Model.ControlPoint cp : model) {
      res = classifier.classify(cp.p);
      Long cnt = byClassDistribution.get(res);
      byClassDistribution.put(res, cnt == null ? 1 : (cnt + 1));
      if (cp.c.getId() != res) {
        errCount ++;
      }
      pointsCount ++;
    }

    double e = (double) errCount / (1.0 * pointsCount);
    log.trace(" results = " + byClassDistribution +
            " time = " + TimeUnit.MILLISECONDS.convert(System.nanoTime() - start, TimeUnit.NANOSECONDS) +
            " error = " + ( 100.0 * e ) + "%" );

    String msg = " expected error " + (100.0 * model.getExpectedError() ) + "% ;" +
                 " acceptable error " + (100.0 * model.getAcceptableError()) + "% ;" +
                 " actual error " + ( 100.0 * e ) + "%";
    log.trace(msg);
    assertTrue(msg, e < model.getAcceptableError());
  }
}
