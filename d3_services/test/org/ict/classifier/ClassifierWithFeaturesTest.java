package org.ict.classifier;

import Jama.Matrix;
import junit.framework.TestCase;
import org.ict.classifier.util.NormalDistribution;
import org.junit.Test;

/**
 * User: mityok
 * Date: 14.02.2010
 * Time: 23:11:47
 */
public class ClassifierWithFeaturesTest extends TestCase {

  @Test
  public void testClassify() throws Exception {
    NormalDistribution nd0 = new NormalDistribution(new double[] {0}, new double[] {1});
    NormalDistribution nd1 = new NormalDistribution(new double[] {5}, new double[] {1});
    Clazz.Builder cb0 = new Clazz.Builder(0);
    Clazz.Builder cb1 = new Clazz.Builder(1);

    for (int i = 0; i < 100; i ++) {
      cb0.addPoint(Point.create(new double[] {nd0.next()[0], 0}));
      cb1.addPoint(Point.create(new double[] {nd1.next()[0], 0}));
    }

    ClassificationTask.Builder tb = new ClassificationTask.Builder();
    tb.addClass(cb0.createClass());
    tb.addClass(cb1.createClass());

    ClassificationTask task = tb.createTask();

    ParzenClassifier classifier = ParzenClassifier.createClassifier(task);

    Matrix features = LandgrebeFeatures.extractFeatures(classifier);

    ClassifierWithFeatures cwf = new ClassifierWithFeatures(classifier, features);

    int err = 0;
    for (Clazz c : task.getClasses()) {
      for (Point p : c.getPoints()) {
        err += cwf.classify(p) != c.getId() ? 1 : 0;
      }
    }

    assertTrue("Errors = " + err + " acceptable = 10", err < 10);
  }
}
