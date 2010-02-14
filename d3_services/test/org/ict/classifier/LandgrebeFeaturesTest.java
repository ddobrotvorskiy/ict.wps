package org.ict.classifier;

import Jama.Matrix;
import junit.framework.TestCase;
import org.ict.classifier.util.NormalDistribution;
import org.junit.Test;

/**
 * User: mityok
 * Date: 14.02.2010
 * Time: 22:58:48
 */
public class LandgrebeFeaturesTest extends TestCase {


  /**
   * Create two classes in 2- dimensional space with one redundant feature
   *
   * @throws Exception
   */
  @Test
  public void testExtractFeatures() throws Exception {

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

    assertTrue("Features count = " + features.getColumnDimension(), features.getColumnDimension() == 1);
  }
}
