package org.ict.classifier;

import junit.framework.TestCase;
import org.ict.classifier.io.AsciiTaskReader;
import org.junit.Test;

import java.io.Reader;
import java.io.StringReader;

/**
 * User: mityok
 * Date: 08.02.2010
 * Time: 23:25:39
 */
public class HyperCubeOptimizerTest extends TestCase {

  private static final String taskStr =
                  "     6    8   11\n" +
                  "     6    8   11\n" +
                  "     6    8   11\n" +
                  "     6    8   11\n" +
                  "\n" +
                  "    33  195  104\n" +
                  "    33  195  104\n" +
                  "    33  195  104\n" +
                  "\n" +
                  "   103  141  158\n" +
                  "   103  141  158\n" +
                  "   103  141  158\n" +
                  "   126  165  185\n" +
                  "   126  165  185\n" +
                  "   126  165  185\n";

  @Test
  public void test_optimizeTask() throws Exception {
    ClassificationTask.Builder tb = new ClassificationTask.Builder();
    Clazz.Builder cb = new Clazz.Builder(0);
    cb.addPoint(Point.create(new double[] {6, 8, 11}, 4));
    tb.addClass(cb.createClass());

    cb = new Clazz.Builder(1);
    cb.addPoint(Point.create(new double[] {33, 195, 104}, 3));
    tb.addClass(cb.createClass());

    cb = new Clazz.Builder(2);
    cb.addPoint(Point.create(new double[] {103, 141, 158}, 3));
    cb.addPoint(Point.create(new double[] {126, 165, 185}, 3));
    tb.addClass(cb.createClass());

    ClassificationTask expected = tb.createTask();

    Reader reader = new StringReader(taskStr);
    ClassificationTask actual = HyperCubeOptimizer.optimizeTask(new AsciiTaskReader(reader).readTask());

    assertEquals(expected, actual);
  }
}
